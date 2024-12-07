
# Clipboard Sync App for Android

This Android application synchronizes the clipboard between your Android device and a server running the [ClipboardSync Server](https://github.com/AdamJou/ClipboardSyncServer). It supports both text and images.

## Features

- Real-time synchronization of text and images between Android and the server.
- Runs as a background service.
- Supports WebSocket communication.

## Requirements

### Prerequisites

1. **Server**: Ensure you have the [ClipboardSync Server](https://github.com/AdamJou/ClipboardSyncServer) running on your machine. Follow the server's README.md for setup instructions.
2. **Android Studio**: Install Android Studio on your development machine.
3. **Android Device**: The app requires a device or emulator running Android 6.0 (API level 23) or higher.

### Configuration

To use the app, you need to configure the server's IP address and port.

1. Create a `local.properties` file in the root of the project if it doesn't exist.
2. Add the following line to the `local.properties` file:

    ```properties
    server.url=ws://<your-server-ip>:8080
    ```

   Replace `<your-server-ip>` with the IP address of your machine running the server.

## Setup

1. Clone the repository:

    ```bash
    git clone https://github.com/AdamJou/ClipboardSyncApp.git
    ```

2. Open the project in Android Studio.

3. Sync the project with Gradle files.

4. Build and run the application on your device or emulator.

## Usage

1. Ensure the [ClipboardSync Server](https://github.com/AdamJou/ClipboardSyncServer) is running on your machine.
2. Install and launch the app on your Android device.
3. The app will automatically connect to the server and synchronize clipboard content.




### Configuration

- **`local.properties`**: File for specifying the server URL.
- **`build.gradle.kts`**: Project's build configuration.

## License

This project is licensed under the MIT License.
