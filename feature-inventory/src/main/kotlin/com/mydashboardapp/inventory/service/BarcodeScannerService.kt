package com.mydashboardapp.inventory.service

import android.content.Context
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.mydashboardapp.feature.inventory.BuildConfig
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Barcode scanner service using ML Kit.
 * Only available in pro version due to proprietary features.
 */
@Singleton
class BarcodeScannerService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val _scanResult = MutableStateFlow<ScanResult?>(null)
    val scanResult: StateFlow<ScanResult?> = _scanResult.asStateFlow()
    
    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()
    
    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var barcodeScanner: BarcodeScanner? = null
    private var cameraExecutor: ExecutorService? = null
    
    // Check if barcode scanning is available (pro version only)
    val isBarcodeScanningAvailable: Boolean
        get() = BuildConfig.IS_PRO_VERSION
    
    data class ScanResult(
        val rawValue: String,
        val format: Int,
        val displayValue: String,
        val timestamp: Long = System.currentTimeMillis()
    )
    
    enum class ScanMode {
        SINGLE, // Scan one barcode and stop
        CONTINUOUS // Keep scanning until stopped
    }
    
    /**
     * Initialize the barcode scanner
     */
    fun initialize() {
        if (!isBarcodeScanningAvailable) {
            throw IllegalStateException("Barcode scanning is only available in pro version")
        }
        
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_QR_CODE,
                Barcode.FORMAT_UPC_A,
                Barcode.FORMAT_UPC_E,
                Barcode.FORMAT_EAN_8,
                Barcode.FORMAT_EAN_13,
                Barcode.FORMAT_CODE_128,
                Barcode.FORMAT_CODE_39,
                Barcode.FORMAT_CODE_93,
                Barcode.FORMAT_CODABAR,
                Barcode.FORMAT_ITF,
                Barcode.FORMAT_DATA_MATRIX,
                Barcode.FORMAT_PDF417,
                Barcode.FORMAT_AZTEC
            )
            .build()
        
        barcodeScanner = BarcodeScanning.getClient(options)
        cameraExecutor = Executors.newSingleThreadExecutor()
    }
    
    /**
     * Start camera preview for barcode scanning
     */
    suspend fun startCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: androidx.camera.view.PreviewView,
        scanMode: ScanMode = ScanMode.SINGLE
    ) {
        if (!isBarcodeScanningAvailable) {
            return
        }
        
        _isScanning.value = true
        
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        
        cameraProvider = cameraProviderFuture.get()
        
        // Preview
        preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }
        
        // Image analyzer for barcode detection
        imageAnalyzer = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                it.setAnalyzer(cameraExecutor!!) { imageProxy ->
                    processImageProxy(imageProxy, scanMode)
                }
            }
        
        // Select back camera as a default
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        
        try {
            // Unbind use cases before rebinding
            cameraProvider?.unbindAll()
            
            // Bind use cases to camera
            camera = cameraProvider?.bindToLifecycle(
                lifecycleOwner, 
                cameraSelector, 
                preview, 
                imageAnalyzer
            )
            
        } catch (exc: Exception) {
            // Handle camera binding failure
            _isScanning.value = false
        }
    }
    
    /**
     * Stop camera and scanning
     */
    fun stopCamera() {
        cameraProvider?.unbindAll()
        camera = null
        preview = null
        imageAnalyzer = null
        _isScanning.value = false
    }
    
    /**
     * Process camera image for barcode detection
     */
    @OptIn(ExperimentalGetImage::class)
    private fun processImageProxy(imageProxy: ImageProxy, scanMode: ScanMode) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            
            barcodeScanner?.process(image)
                ?.addOnSuccessListener { barcodes ->
                    if (barcodes.isNotEmpty()) {
                        val barcode = barcodes.first()
                        val scanResult = ScanResult(
                            rawValue = barcode.rawValue ?: "",
                            format = barcode.format,
                            displayValue = barcode.displayValue ?: barcode.rawValue ?: ""
                        )
                        
                        _scanResult.value = scanResult
                        
                        // Stop scanning if single mode
                        if (scanMode == ScanMode.SINGLE) {
                            stopCamera()
                        }
                    }
                }
                ?.addOnFailureListener {
                    // Handle scanning failure
                }
                ?.addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
    
    /**
     * Clear scan result
     */
    fun clearScanResult() {
        _scanResult.value = null
    }
    
    /**
     * Toggle camera flash
     */
    fun toggleFlash() {
        camera?.let { camera ->
            if (camera.cameraInfo.hasFlashUnit()) {
                val currentFlashMode = camera.cameraInfo.torchState.value
                camera.cameraControl.enableTorch(currentFlashMode != TorchState.ON)
            }
        }
    }
    
    /**
     * Check if device has flash
     */
    fun hasFlash(): Boolean {
        return camera?.cameraInfo?.hasFlashUnit() ?: false
    }
    
    /**
     * Get supported barcode formats as human-readable strings
     */
    fun getSupportedFormats(): List<String> {
        return listOf(
            "QR Code",
            "UPC-A",
            "UPC-E", 
            "EAN-8",
            "EAN-13",
            "Code 128",
            "Code 39",
            "Code 93",
            "Codabar",
            "ITF",
            "Data Matrix",
            "PDF417",
            "Aztec"
        )
    }
    
    /**
     * Get barcode format name from format code
     */
    fun getFormatName(format: Int): String {
        return when (format) {
            Barcode.FORMAT_QR_CODE -> "QR Code"
            Barcode.FORMAT_UPC_A -> "UPC-A"
            Barcode.FORMAT_UPC_E -> "UPC-E"
            Barcode.FORMAT_EAN_8 -> "EAN-8"
            Barcode.FORMAT_EAN_13 -> "EAN-13"
            Barcode.FORMAT_CODE_128 -> "Code 128"
            Barcode.FORMAT_CODE_39 -> "Code 39"
            Barcode.FORMAT_CODE_93 -> "Code 93"
            Barcode.FORMAT_CODABAR -> "Codabar"
            Barcode.FORMAT_ITF -> "ITF"
            Barcode.FORMAT_DATA_MATRIX -> "Data Matrix"
            Barcode.FORMAT_PDF417 -> "PDF417"
            Barcode.FORMAT_AZTEC -> "Aztec"
            else -> "Unknown"
        }
    }
    
    /**
     * Cleanup resources
     */
    fun cleanup() {
        stopCamera()
        barcodeScanner?.close()
        cameraExecutor?.shutdown()
        cameraProvider = null
        barcodeScanner = null
        cameraExecutor = null
    }
}
