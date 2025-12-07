# 📱 Configuration KMPNotifier

## 📦 Fichiers générés

- `org.society.appname.fcm.NotificationListener` : Composable pour écouter les notifications

## 🔧 Fichiers modifiés

- ✅ Application.kt
- ✅ MainActivity.kt  
- ✅ iOSApp.swift
- ✅ *NavHost.kt (NotificationListener)
- ✅ MainViewModel.kt (saveToken)
- ✅ build.gradle.kts
- ✅ libs.versions.toml

## 📝 Configuration Firebase

### Android
Ajoutez `google-services.json` dans `composeApp/`

### iOS
1. Ajoutez `GoogleService-Info.plist` dans `iosApp/iosApp/`
2. Configurez Push Notifications + Background Modes dans Xcode

## 📚 Documentation

- [KMPNotifier GitHub](https://github.com/mirzemehdi/KMPNotifier)
- [Firebase Cloud Messaging](https://firebase.google.com/docs/cloud-messaging)