# app-ads.txt Implementation Guide for Relinx

## What is app-ads.txt?

The `app-ads.txt` file is an industry standard that helps ensure ad inventory authenticity in mobile apps. It allows app developers to publicly declare which ad networks are authorized to sell their inventory, helping to prevent counterfeit inventory and unauthorized ad sales.

## File Location

**Current Location**: `/Users/takitareeq/AndroidStudioProjects/LinkBox/app-ads.txt`

## Implementation Steps

### 1. Upload to Developer Website
You need to upload the `app-ads.txt` file to your developer website at:
```
https://yourdomain.com/app-ads.txt
```

### 2. File Content
```
google.com, pub-5941977662892783, DIRECT, f08c47fec0942fa0
```

This line indicates:
- **google.com**: The advertising system domain (Google AdMob)
- **pub-5941977662892783**: Your AdMob publisher ID
- **DIRECT**: The relationship type (direct relationship with Google)
- **f08c47fec0942fa0**: Google's seller account ID (standard for all Google ads.txt entries)

### 3. Google Play Console Configuration
1. Go to Google Play Console
2. Navigate to your app's "Monetization" section
3. Add your developer website URL where the app-ads.txt is hosted
4. Google will verify the file automatically

### 4. AdMob Integration
In your AdMob account:
1. Go to "Apps" section
2. Find your Relinx app
3. Verify that the app-ads.txt status shows as "Verified"

## Benefits

- **Fraud Prevention**: Prevents unauthorized ad sales
- **Revenue Protection**: Ensures all ad revenue goes to you
- **Transparency**: Creates public record of authorized ad sellers
- **Industry Compliance**: Meets IAB standards for digital advertising

## Verification

After uploading, you can verify the file is working by visiting:
```
https://yourdomain.com/app-ads.txt
```

The file should be accessible and display the content properly.

## Important Notes

- The file must be accessible via HTTPS
- Content-Type should be "text/plain"
- File must be in the root domain directory
- Updates may take 24-48 hours to propagate

## AdMob Configuration Summary

Your Relinx app is configured with:
- **Publisher ID**: ca-app-pub-5941977662892783
- **Banner Ad Units**:
  - Main Screen: ca-app-pub-5941977662892783/1425740261
  - Links Screen: ca-app-pub-5941977662892783/1824910101
- **App ID**: ca-app-pub-5941977662892783~1234567890 (update with actual app ID from AdMob)

## Next Steps

1. Upload `app-ads.txt` to your website
2. Update the app ID in AndroidManifest.xml with the actual app ID from AdMob console
3. Verify the file in AdMob console
4. Monitor ad performance after Play Store deployment