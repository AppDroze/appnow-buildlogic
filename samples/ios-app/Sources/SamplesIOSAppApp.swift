import SwiftUI
import ShowcaseLib

@main
struct SamplesIOSAppApp: App {
    init() {
        // Initialize shared KMP library (Koin DI)
        SharedApi.shared.initialize()
    }
    var body: some Scene {
        WindowGroup { ContentView() }
    }
}

