# Icon Placeholder Files

This file lists all the icon files you need to create. Each file should be created as a placeholder with the correct dimensions.

## Android App Icons

### Mipmap Icons
```
icons/android-app/mipmap-mdpi/
├── ic_launcher.png (48x48)
├── ic_launcher_round.png (48x48)
└── ic_launcher_foreground.png (108x108)

icons/android-app/mipmap-hdpi/
├── ic_launcher.png (72x72)
├── ic_launcher_round.png (72x72)
└── ic_launcher_foreground.png (162x162)

icons/android-app/mipmap-xhdpi/
├── ic_launcher.png (96x96)
├── ic_launcher_round.png (96x96)
└── ic_launcher_foreground.png (216x216)

icons/android-app/mipmap-xxhdpi/
├── ic_launcher.png (144x144)
├── ic_launcher_round.png (144x144)
└── ic_launcher_foreground.png (324x324)

icons/android-app/mipmap-xxxhdpi/
├── ic_launcher.png (192x192)
├── ic_launcher_round.png (192x192)
└── ic_launcher_foreground.png (432x432)
```

## Google Play Store Assets

```
icons/google-play-store/
├── play-store-icon.png (512x512)
├── feature-graphic.png (1024x500)
└── screenshots/
    ├── screenshot-phone-1.png (1080x1920)
    ├── screenshot-phone-2.png (1080x1920)
    ├── screenshot-phone-3.png (1080x1920)
    ├── screenshot-tablet-7in.png (1200x1920)
    └── screenshot-tablet-10in.png (1920x1200)
```

## Total Files to Create: 23

### Android App Icons: 15 files
- 5 ic_launcher.png files (different sizes)
- 5 ic_launcher_round.png files (different sizes)
- 5 ic_launcher_foreground.png files (different sizes)

### Google Play Store: 8 files
- 1 play-store-icon.png
- 1 feature-graphic.png
- 5 screenshot files (3 phone + 2 tablet)

## Quick Start Guide

1. **Start with the largest size:** Create a 512x512 base icon design
2. **Use an icon generator:** Tools like Android Asset Studio can generate all sizes from one base image
3. **Create the feature graphic:** Design a 1024x500 banner for the Play Store
4. **Take screenshots:** Capture your app in action on different device sizes
5. **Test everything:** Make sure icons look good at all sizes

## Recommended Workflow

1. Design your base icon concept (512x512)
2. Generate all Android app icon sizes
3. Create the Play Store icon (512x512, no transparency)
4. Design the feature graphic (1024x500)
5. Take app screenshots
6. Test all assets at different sizes
7. Replace the placeholder files with your final designs 