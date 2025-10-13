// swift-tools-version:5.9
import PackageDescription

let package = Package(
    name: "ShowcaseLibBinary",
    platforms: [
        .iOS(.v13)
    ],
    products: [
        .library(name: "ShowcaseLibBinary", targets: ["ShowcaseLib"])
    ],
    targets: [
        .binaryTarget(
            name: "ShowcaseLib",
            path: "../showcase-lib/build/XCFrameworks/release/ShowcaseLib.xcframework"
        )
    ]
)

