# Relinx Signing Configuration

## Keystore Details
- **Keystore File**: `relinx-release-key.keystore`
- **Location**: Project root directory
- **Alias**: `relinx`
- **Algorithm**: RSA 2048-bit
- **Validity**: 10,000 days (~27 years)
- **Store Password**: `relinx2025`
- **Key Password**: `relinx2025`

## Certificate Information
- **CN**: Relinx
- **OU**: Development
- **O**: Relinx
- **L**: Unknown
- **S**: Unknown
- **C**: Unknown

## Signed Release Bundle
- **File**: `app/build/outputs/bundle/release/app-release.aab`
- **Size**: ~8.0 MB (R8/ProGuard optimized)
- **Version**: 1.3.0
- **Target SDK**: API 35 (Android 15)
- **Obfuscation**: ✅ R8/ProGuard enabled
- **Mapping File**: `app/build/outputs/mapping/release/mapping.txt`
- **Signed**: ✅ Yes
- **Ready for Play Store**: ✅ Yes

## Security Notes
⚠️ **IMPORTANT**: Keep the keystore file and passwords secure!
- Store the keystore file in a safe location
- Never commit the keystore to version control
- Keep a backup of the keystore file
- Use these same signing credentials for all future app updates

## Verification
The AAB bundle has been signed with the release keystore and is ready for Google Play Console upload.

**Build Command Used**:
```bash
./gradlew clean bundleRelease
```

**Generated on**: November 9, 2025