package com.mydashboardapp.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "locations",
    indices = [Index("parentLocationId")],
    foreignKeys = [
        ForeignKey(
            entity = Location::class,
            parentColumns = ["id"],
            childColumns = ["parentLocationId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Location(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String?,
    val type: String?, // e.g., "Room", "Shelf", "Box", "Drawer", "Building"
    val parentLocationId: Long?, // for hierarchical locations (Room -> Shelf -> Box)
    val address: String?, // physical address for buildings/rooms
    val coordinates: String?, // lat,lng for precise location
    val qrCode: String?, // QR code for easy identification
    val color: String?, // hex color for UI identification
    val icon: String?, // icon identifier
    val capacity: String?, // storage capacity description
    val temperature: String?, // storage temperature requirements
    val humidity: String?, // humidity requirements
    val accessInstructions: String?, // how to access this location
    val securityLevel: String?, // e.g., "Public", "Private", "Restricted"
    val notes: String?,
    val imageUrl: String?, // photo of the location
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
