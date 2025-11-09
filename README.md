# Relinx - Smart Link Manager

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-blue.svg)](https://kotlinlang.org)
[![Material Design](https://img.shields.io/badge/Design-Material%203-purple.svg)](https://material.io)

A modern, intuitive Android application for saving, organizing, and managing your web links with categories, tags, and smart features.

## 🚀 Features

### Core Functionality
- **Smart Link Organization**: Save links in customizable categories
- **Tagging System**: Add tags to links for better organization
- **Powerful Search**: Search across all your saved links and categories
- **Drag & Drop**: Reorder categories and links with intuitive gestures

### User Experience
- **Material Design 3**: Modern, beautiful interface
- **Dark/Light Mode**: Automatic and manual theme switching
- **Custom Icons**: 15+ built-in category icons to choose from
- **Favicon Loading**: Automatic website icon fetching with caching

### Data Management
- **Export/Import**: Backup and restore your data in JSON format
- **Local Storage**: All data stored securely on your device using Room Database
- **No Cloud Required**: Complete privacy with offline functionality

### Productivity Features
- **Quick Actions**: Long-press menus for fast link management
- **Link Actions**: Open in browser, copy to clipboard, or share links
- **Pre-configured Categories**: Work, Personal, and Entertainment ready to use

## 📱 Screenshots

*Screenshots coming soon*

## 🏗️ Technical Architecture

### Technology Stack
- **Language**: Kotlin
- **Architecture**: MVVM (Model-View-ViewModel)
- **Database**: Room (SQLite)
- **UI Framework**: Material Design 3
- **Async Operations**: Kotlin Coroutines
- **Dependency Injection**: Manual DI pattern
- **Advertisement**: Google AdMob integration

### Key Components
- **Room Database**: Local data persistence with automatic migrations
- **RecyclerView Adapters**: Efficient list rendering with view holders
- **Constraint Layouts**: Responsive UI design
- **ItemTouchHelper**: Drag and drop functionality
- **Shared Preferences**: App settings and user preferences

## 🛠️ Installation & Setup

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK 24+ (Android 7.0)
- Kotlin 1.9+

### Building from Source
1. **Clone the repository**
   ```bash
   git clone https://github.com/Takiboy88/Relinx.git
   cd Relinx
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an existing project"
   - Navigate to the cloned directory

3. **Configure AdMob (Optional)**
   - Replace test ad unit IDs in the code with your own
   - Update `app/build.gradle.kts` with your AdMob app ID

4. **Build the project**
   ```bash
   ./gradlew assembleDebug
   ```

### Release Build
```bash
./gradlew bundleRelease
```

## 📖 Usage Guide

### Getting Started
1. **Add Categories**: Tap the "+" button to create your first category
2. **Choose Icons**: Select from 15+ built-in icons for your categories
3. **Add Links**: Open a category and add your first link
4. **Organize**: Use drag & drop to reorder items as needed

### Advanced Features
- **Search**: Use the search bar to find links across all categories
- **Export Data**: Menu > Export to backup your links
- **Import Data**: Menu > Import to restore from a backup
- **Theme Toggle**: Switch between light and dark modes

## 🔒 Privacy & Security

- **Local-First**: All data stored on your device
- **No Account Required**: No sign-up or personal information needed
- **No Cloud Sync**: Your data stays private and secure
- **AdMob Privacy**: Only anonymous advertising data collected

See our [Privacy Policy](PRIVACY_POLICY.md) and [Terms of Service](TERMS_OF_SERVICE.md) for complete details.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

### Development Guidelines
- Follow Kotlin coding conventions
- Maintain Material Design principles
- Add tests for new features
- Update documentation as needed

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/Takiboy88/Relinx/issues)
- **Discussions**: [GitHub Discussions](https://github.com/Takiboy88/Relinx/discussions)
- **Email**: [Your Email Address]

## 🗺️ Roadmap

### Upcoming Features
- [ ] Widget support for quick link access
- [ ] Link preview generation
- [ ] Advanced filtering options
- [ ] Backup to cloud storage (optional)
- [ ] Link validation and health checking

## 📊 Statistics

- **App Size**: ~8.7 MB
- **Minimum Android**: API 24 (Android 7.0)
- **Target Android**: API 34 (Android 14)
- **Dependencies**: Minimal external dependencies for security and performance

## 🙏 Acknowledgments

- **Material Design**: Google's design system
- **Room Database**: Local database solution
- **AdMob**: Monetization platform
- **Android Jetpack**: Modern Android development

---

**Made with ❤️ for the Android community**

*Relinx - Your links, organized your way.*