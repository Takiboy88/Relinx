# Google Play Console - Mapping File Upload Guide

## 🎯 How to Upload Deobfuscation File (Mapping File)

The warning "Aucun fichier de désobscurcissement n'est associé à cet App Bundle" appears because the mapping file needs to be uploaded **separately** to Google Play Console.

### 📁 Files to Upload

1. **AAB Bundle**: `app-release.aab` (8.0 MB)
2. **Mapping File**: `mapping.txt` (33 MB)

### 📋 Step-by-Step Upload Process

#### Step 1: Upload AAB Bundle
1. Go to Google Play Console
2. Navigate to your app → Production → Create new release
3. Upload `app-release.aab` file

#### Step 2: Upload Mapping File
1. **After uploading AAB**, scroll down to find "Deobfuscation files" section
2. Click **"Upload deobfuscation file"** 
3. Select the `mapping.txt` file from: 
   ```
   /Users/takitareeq/AndroidStudioProjects/LinkBox/app/build/outputs/mapping/release/mapping.txt
   ```
4. Upload the file (33 MB)

#### Step 3: Verify Upload
- ✅ AAB bundle should show "Signed"
- ✅ Mapping file should show "Uploaded" 
- ✅ No more warnings about missing deobfuscation file

### 🔧 Alternative Method: Automatic Upload

If you want automatic upload in future releases, add this to `app/build.gradle.kts`:

```kotlin
android {
    // ... existing config
    
    buildTypes {
        release {
            // ... existing config
            
            // Enable automatic mapping file upload (requires Play Console API)
            // playConsole {
            //     uploadMappingFile = true
            // }
        }
    }
}
```

### 📊 File Locations (v1.2.0)

| File Type | Location | Size | Purpose |
|-----------|----------|------|---------|
| **AAB Bundle** | `app/build/outputs/bundle/release/app-release.aab` | 8.0 MB | App installation |
| **Mapping File** | `app/build/outputs/mapping/release/mapping.txt` | 33 MB | Crash deobfuscation |
| **Configuration** | `app/build/outputs/mapping/release/configuration.txt` | 47 KB | ProGuard config used |
| **Resources** | `app/build/outputs/mapping/release/resources.txt` | 1.5 MB | Resource mapping |

### 🎯 Benefits After Upload

Once both files are uploaded:
- ✅ **No more warnings** about missing deobfuscation files
- ✅ **Readable crash reports** with original class/method names
- ✅ **Better debugging** for ANR (Application Not Responding) issues
- ✅ **Professional deployment** with full crash analysis support

### 📱 Play Console UI Guide

1. **Release Dashboard** → **Production** → **Create new release**
2. **Upload** → Drag `app-release.aab` 
3. **Deobfuscation files** section appears → **Upload deobfuscation file**
4. **Select** `mapping.txt` → **Upload**
5. **Review** → Both files should show as successfully uploaded
6. **Save** → **Review release** → **Start rollout to production**

### ⚠️ Important Notes

- **Keep mapping file**: Store `mapping.txt` safely for future crash analysis
- **Version specific**: Each app version needs its own mapping file
- **File size**: 33 MB mapping file is normal for obfuscated apps
- **Required**: Mapping file is essential for meaningful crash reports

## ✅ Status: Ready for Upload

Your Relinx v1.2.0 is ready with:
- ✅ Signed AAB bundle (8.0 MB)
- ✅ Mapping file generated (33 MB) 
- ✅ Clean permissions (no photo/video access)
- ✅ R8/ProGuard optimization enabled
- ✅ API 35 compliance

**Upload both files to eliminate the deobfuscation warning!**

---
**Generated**: November 9, 2025  
**Version**: 1.2.0  
**Build**: 3