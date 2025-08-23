# Icon Requirements for Listen App

## 📱 Android App Icons

### Mipmap Icons (App Launcher Icons)
These are the icons that appear on the home screen and app drawer.

| Density | Directory | Size (px) | Filename |
|---------|-----------|-----------|----------|
| mdpi | `mipmap-mdpi/` | 48x48 | `ic_launcher.png` |
| hdpi | `mipmap-hdpi/` | 72x72 | `ic_launcher.png` |
| xhdpi | `mipmap-xhdpi/` | 96x96 | `ic_launcher.png` |
| xxhdpi | `mipmap-xxhdpi/` | 144x144 | `ic_launcher.png` |
| xxxhdpi | `mipmap-xxxhdpi/` | 192x192 | `ic_launcher.png` |

### Adaptive Icons (Android 8.0+)
For modern Android devices, you need adaptive icons with foreground and background layers.

| Density | Directory | Size (px) | Filename |
|---------|-----------|-----------|----------|
| mdpi | `mipmap-mdpi/` | 108x108 | `ic_launcher_foreground.png` |
| hdpi | `mipmap-hdpi/` | 162x162 | `ic_launcher_foreground.png` |
| xhdpi | `mipmap-xhdpi/` | 216x216 | `ic_launcher_foreground.png` |
| xxhdpi | `mipmap-xxhdpi/` | 324x324 | `ic_launcher_foreground.png` |
| xxxhdpi | `mipmap-xxxhdpi/` | 432x432 | `ic_launcher_foreground.png` |

### Round Icons
Some launchers prefer round icons.

| Density | Directory | Size (px) | Filename |
|---------|-----------|-----------|----------|
| mdpi | `mipmap-mdpi/` | 48x48 | `ic_launcher_round.png` |
| hdpi | `mipmap-hdpi/` | 72x72 | `ic_launcher_round.png` |
| xhdpi | `mipmap-xhdpi/` | 96x96 | `ic_launcher_round.png` |
| xxhdpi | `mipmap-xxhdpi/` | 144x144 | `ic_launcher_round.png` |
| xxxhdpi | `mipmap-xxxhdpi/` | 192x192 | `ic_launcher_round.png` |

## 🎮 Google Play Store Icons

### Feature Graphic
- **Size:** 1024x500 pixels
- **Format:** PNG or JPG
- **Filename:** `feature-graphic.png`
- **Purpose:** Banner displayed at the top of your app's Play Store page

### App Icon (Play Store)
- **Size:** 512x512 pixels
- **Format:** PNG (32-bit with alpha channel)
- **Filename:** `play-store-icon.png`
- **Purpose:** Icon displayed in Play Store search results and app page

### Screenshots (Optional but Recommended)
- **Phone screenshots:** 1080x1920 pixels (9:16 ratio)
- **7-inch tablet:** 1200x1920 pixels (5:8 ratio)
- **10-inch tablet:** 1920x1200 pixels (8:5 ratio)
- **Format:** PNG or JPG
- **Filename:** `screenshot-phone-1.png`, `screenshot-tablet-1.png`, etc.

## 🎨 Design Guidelines

### App Icon Design
- **Safe Zone:** Keep important elements within the center 66% of the icon
- **Background:** Use solid colors or simple gradients
- **Foreground:** Clear, recognizable symbol (microphone for Listen app)
- **Style:** Material Design principles recommended
- **Transparency:** Avoid transparency in app icons

### Play Store Icon
- **No transparency:** Must have a solid background
- **High contrast:** Should be visible on light and dark backgrounds
- **Scalable:** Should look good at small sizes
- **Brand consistency:** Should match your app's design language

### Feature Graphic
- **Text:** Include app name and tagline
- **Visual hierarchy:** Most important information should be visible at small sizes
- **Brand colors:** Use your app's color scheme
- **Call to action:** Consider including a brief value proposition

## 📁 Directory Structure

```
icons/
├── ICON_REQUIREMENTS.md
├── android-app/
│   ├── mipmap-mdpi/
│   │   ├── ic_launcher.png (48x48)
│   │   ├── ic_launcher_round.png (48x48)
│   │   └── ic_launcher_foreground.png (108x108)
│   ├── mipmap-hdpi/
│   │   ├── ic_launcher.png (72x72)
│   │   ├── ic_launcher_round.png (72x72)
│   │   └── ic_launcher_foreground.png (162x162)
│   ├── mipmap-xhdpi/
│   │   ├── ic_launcher.png (96x96)
│   │   ├── ic_launcher_round.png (96x96)
│   │   └── ic_launcher_foreground.png (216x216)
│   ├── mipmap-xxhdpi/
│   │   ├── ic_launcher.png (144x144)
│   │   ├── ic_launcher_round.png (144x144)
│   │   └── ic_launcher_foreground.png (324x324)
│   └── mipmap-xxxhdpi/
│       ├── ic_launcher.png (192x192)
│       ├── ic_launcher_round.png (192x192)
│       └── ic_launcher_foreground.png (432x432)
└── google-play-store/
    ├── play-store-icon.png (512x512)
    ├── feature-graphic.png (1024x500)
    └── screenshots/
        ├── screenshot-phone-1.png (1080x1920)
        ├── screenshot-phone-2.png (1080x1920)
        ├── screenshot-tablet-7in.png (1200x1920)
        └── screenshot-tablet-10in.png (1920x1200)
```

## 🛠️ Tools for Icon Creation

### Recommended Software
- **Adobe Photoshop:** Professional image editing
- **GIMP:** Free alternative to Photoshop
- **Figma:** Great for vector-based design
- **Sketch:** Mac-only design tool
- **Inkscape:** Free vector graphics editor

### Online Tools
- **Android Asset Studio:** Generate Android icons from a single image
- **App Icon Generator:** Create all sizes from one high-res image
- **Canva:** Easy-to-use design tool with templates

## 📋 Checklist

### Android App Icons
- [ ] Create base icon design (512x512 recommended)
- [ ] Generate all mipmap sizes
- [ ] Create round versions
- [ ] Create adaptive icon foreground layers
- [ ] Test on different device densities

### Google Play Store
- [ ] Create 512x512 Play Store icon
- [ ] Design 1024x500 feature graphic
- [ ] Create app screenshots (phone and tablet)
- [ ] Test icons at different sizes
- [ ] Ensure no transparency in Play Store assets

### Quality Assurance
- [ ] Icons are clear and recognizable at small sizes
- [ ] No pixelation or blurriness
- [ ] Consistent branding across all assets
- [ ] Proper contrast and visibility
- [ ] Follow Material Design guidelines

## 🎯 Current App Theme

Based on your current app configuration:
- **Primary Color:** Green (#3DDC84)
- **Icon Symbol:** Microphone
- **Style:** Material Design
- **Background:** Solid green with white microphone

Consider updating this to better reflect your app's unique identity and branding. 