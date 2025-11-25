# Relinx App Permissions Documentation

## Current Permissions (v1.3.0)

### Required Permissions

1. **INTERNET**
   - **Purpose**: Essential for app functionality
   - **Usage**: 
     - Loading website favicons
     - AdMob advertisement serving
     - Checking link validity (optional)
   - **User Impact**: Required for core app features
   - **Privacy**: No personal data transmitted

### No Storage Permissions Required (v1.3.0+)
- **Export Function**: Uses app's internal storage (no permissions needed)
- **Data Storage**: All data stored in app's private directory
- **No External Access**: App doesn't access external storage or media files

## Removed Permissions (Fixed in v1.3.0)

### Previously Declared (All Removed)
- ❌ `WRITE_EXTERNAL_STORAGE` - Removed, using internal storage for export
- ❌ `READ_EXTERNAL_STORAGE` - Not needed for link management
- ❌ `READ_MEDIA_IMAGES` - App doesn't access photos
- ❌ `READ_MEDIA_VIDEO` - App doesn't access videos  
- ❌ `READ_MEDIA_AUDIO` - App doesn't access audio files

## Privacy Benefits

### Minimal Permission Model
- **Link Management**: All data stored locally, no external access needed
- **No Photo Access**: App doesn't request or use photo/video permissions
- **No Location**: No location permissions requested
- **No Contacts**: No contact access required
- **No Camera/Microphone**: No media capture permissions

### Data Handling
- **Internal Storage Only**: All user data and exports stored in app's private directory
- **No External Access**: No external storage or media access
- **No Cloud Sync**: No external servers accessed for user data
- **Anonymous Ads**: AdMob uses anonymous advertising identifiers only
- **Export Control**: User controls when/how data is exported (saves to app's internal directory)

## Play Store Compliance

### Permission Justification
✅ **INTERNET**: Required for favicon loading and advertisements

### Google Play Requirements Met
- ✅ No sensitive permissions without justification
- ✅ No photo/video/media access requests
- ✅ No storage permissions required
- ✅ Minimal permission footprint (INTERNET only)
- ✅ Clear functionality mapping

## Technical Implementation

### Export Functionality (Updated v1.3.0)
- **All Android Versions**: Uses app's internal storage directory
- **No Permissions**: No storage permissions required on any Android version
- **User Access**: Users can access exported files through app sharing mechanisms
- **Privacy**: Files stored privately within app's allocated space

### AdMob Integration
- **Anonymous**: No personal data collection
- **Standard**: Uses only INTERNET permission
- **Compliant**: Follows Google AdMob policies

## Status: ✅ FULLY COMPLIANT

Relinx v1.3.0 uses only INTERNET permission, with no access to storage, photos, videos, or any sensitive user data.

**Updated**: November 9, 2025
**Version**: 1.3.0