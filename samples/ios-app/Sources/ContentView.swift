import SwiftUI
import ShowcaseLib

struct ContentView: View {
    var body: some View {
        Text(SharedApi.shared.getMessage())
            .padding()
    }
}

