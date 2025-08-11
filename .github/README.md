# CI/CD and Release Automation

This document describes the GitHub Actions workflows and CI/CD setup for the Best Productivity App.

## 🚀 Workflows Overview

### 1. Build Workflow (`build.yml`)
**Triggers:** Push to `main`, `develop`, `feature/**` branches and pull requests
- Builds APKs and AABs for both `free` and `pro` flavors in `debug` and `release` variants
- Handles release signing via GitHub Secrets
- Uploads build artifacts (APKs, AABs, ProGuard mapping files)
- Provides build summary and status

### 2. Test Workflow (`test.yml`)
**Triggers:** Push to `main`, `develop`, `feature/**` branches and pull requests
- **Unit Tests:** Runs for both flavors with coverage reporting
- **Integration Tests:** Runs on multiple API levels (24, 29, 34)
- **Screenshot Tests:** Visual regression testing
- Publishes test results and generates reports

### 3. Lint Workflow (`lint.yml`)
**Triggers:** Push to `main`, `develop`, `feature/**` branches and pull requests
- **Kotlin Lint:** Detekt and KtLint static analysis
- **Android Lint:** Android-specific code issues
- **Dependency Analysis:** OWASP vulnerability scanning
- **Security Lint:** Secret detection and security analysis
- **Code Quality:** Metrics and complexity analysis

### 4. Release Workflow (`release.yml`)
**Triggers:** 
- Git tags matching `v*.*.*` pattern
- Manual workflow dispatch with version input
- **Build:** Creates signed release APKs and AABs
- **GitHub Release:** Publishes release with assets and notes
- **Play Store:** Deploys to Internal Testing track
- **Notifications:** Slack notifications for deployments

## 🔐 Required GitHub Secrets

### Release Signing
```
KEYSTORE_BASE64          # Base64-encoded release keystore file
KEYSTORE_PASSWORD        # Keystore password
KEY_ALIAS               # Key alias name
KEY_PASSWORD            # Key password
```

### Play Store Deployment
```
GOOGLE_PLAY_API_KEY_JSON # Service account JSON key for Play Store API
```

### Optional Notifications
```
SLACK_WEBHOOK_URL        # Slack webhook for deployment notifications
```

## 📱 Setting Up Release Signing

### 1. Generate Release Keystore
```bash
keytool -genkey -v -keystore release-keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias release-key
```

### 2. Convert Keystore to Base64
```bash
base64 -i release-keystore.jks | pbcopy  # macOS
base64 -i release-keystore.jks           # Linux
```

### 3. Add to GitHub Secrets
- Go to repository Settings → Secrets and variables → Actions
- Add the base64-encoded keystore as `KEYSTORE_BASE64`
- Add keystore password as `KEYSTORE_PASSWORD`
- Add key alias as `KEY_ALIAS`
- Add key password as `KEY_PASSWORD`

## 🏪 Play Store Setup

### 1. Create Service Account
1. Go to Google Cloud Console
2. Create a new service account
3. Download the JSON key file
4. Add JSON content to `GOOGLE_PLAY_API_KEY_JSON` secret

### 2. Grant Permissions
1. Go to Google Play Console
2. Setup → API access
3. Link the service account
4. Grant permissions for releases and app information

### 3. App Registration
Ensure both flavors are registered in Play Console:
- `com.mydashboardapp.free`
- `com.mydashboardapp.pro`

## 🔄 Release Process

### Automated (Tag-based)
1. Create and push a git tag:
```bash
git tag v1.0.0
git push origin v1.0.0
```

2. GitHub Actions will automatically:
   - Build signed release artifacts
   - Create GitHub release
   - Deploy to Play Store Internal Testing

### Manual Release
1. Go to Actions → Release workflow
2. Click "Run workflow"
3. Enter version and release notes
4. Choose deployment options

## 📊 Monitoring and Debugging

### Build Artifacts
- **APKs:** Available for 30 days
- **AABs:** Available for 30 days  
- **Mapping Files:** Available for 90 days (release) / 365 days (tagged releases)

### Test Reports
- Unit test results published as GitHub status checks
- Coverage reports available in workflow artifacts
- Integration test results for each API level

### Security Reports
- OWASP dependency vulnerability scans
- Secret detection results
- ProGuard obfuscation verification
- Security lint findings

## 🛠 Workflow Customization

### Adding New Checks
To add new quality checks, modify the appropriate workflow:
1. `lint.yml` for static analysis
2. `test.yml` for testing
3. `build.yml` for build verification

### Changing Deployment Targets
To modify Play Store deployment:
1. Update `track` in `play-store-release` job
2. Modify `userFraction` for staged rollouts
3. Adjust `inAppUpdatePriority` for update urgency

### Build Variants
To add new flavors or build types:
1. Update `app/build.gradle.kts`
2. Modify matrix strategies in workflows
3. Update artifact naming conventions

## 🏗 Architecture Benefits

### Separation of Concerns
- **Build:** Pure compilation and artifact generation
- **Test:** Quality assurance and validation
- **Lint:** Code quality and security
- **Release:** Distribution and deployment

### Security
- Secrets are never exposed in logs
- Temporary keystore files are cleaned up
- Signed builds use proper Android app signing
- Vulnerability scanning for dependencies

### Scalability
- Matrix builds for multiple configurations
- Parallel execution where possible
- Cached dependencies for faster builds
- Artifact retention policies

## 🚨 Troubleshooting

### Build Failures
1. Check Java/Gradle versions match project requirements
2. Verify all required secrets are set
3. Review dependency resolution issues
4. Check ProGuard rules for obfuscation problems

### Test Failures
1. Review test reports in workflow artifacts
2. Check emulator configuration for integration tests
3. Verify test data and mock configurations
4. Ensure test isolation and cleanup

### Release Issues
1. Verify keystore and signing configuration
2. Check Play Store API permissions
3. Review app registration and package names
4. Validate version codes are incrementing

### Security Alerts
1. Review OWASP dependency reports
2. Address high-severity vulnerabilities
3. Update suppressions file for false positives
4. Monitor secret detection results

## 📈 Future Enhancements

- [ ] Performance testing integration
- [ ] Automated A/B testing setup
- [ ] Multi-environment deployments
- [ ] Crash reporting integration
- [ ] App signing by Google Play
- [ ] Gradle build caching optimization
- [ ] Docker-based build environments
