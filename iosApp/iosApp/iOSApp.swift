import SwiftUI
import ComposeApp
import FirebaseCore

@main
struct iOSApp: App {

    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate

    init () {
        FirebaseApp.configure()

        NotifierManager.shared.initialize(
            configuration: NotificationPlatformConfigurationIos(
                showPushNotification: true,
                askNotificationPermissionOnStart: true,
                notificationSoundName: nil
            )
        )
    }
    var body: some Scene {
        WindowGroup {
            ContentView().ignoresSafeArea(.keyboard)
        }
    }
}
