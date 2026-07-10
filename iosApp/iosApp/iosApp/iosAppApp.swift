import SwiftUI
import composeApp

@main
struct iOSApp: App {
    init() {
        KoinInitKt.doInitKoin()
        MainViewControllerKt.doInitCoil()
    }

    var body: some Scene {
        WindowGroup {
            ZStack {
                Color(red: 14/255, green: 9/255, blue: 8/255)
                    .ignoresSafeArea()
                ContentView()
                    .ignoresSafeArea()
                    .onOpenURL { url in
                        AuthCallbackHandlerKt.handleAuthCallback(url: url.absoluteString)
                    }
            }
        }
    }
}
