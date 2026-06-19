# Shortcut Magic ✨

**Made by Gemini with two prompts**

Shortcut Magic is a powerful yet simple Android utility that allows you to create custom shortcuts on your home screen for almost anything. Whether it's a deep-linked URL, a local file, or a direct launcher for another app, Shortcut Magic puts it one tap away.

## 🚀 Features

-   **App Shortcuts**: Create direct launchers for any installed application.
-   **Web Shortcuts**: One-tap access to your favorite websites with automatic protocol handling.
-   **File Shortcuts**: Quick access to documents, images, or any file on your local storage.
-   **Custom Icons**: Pick any image from your gallery to use as a shortcut icon.
-   **History Management**: Keep track of all shortcuts you've created and clear them easily when needed.
-   **Material 3 Design**: A clean, modern interface with a beautiful "Magic Wand" aesthetic.

## 🛠️ Built With

-   **Kotlin**: 100% modern Android development.
-   **Navigation Component**: Seamless fragment transitions.
-   **View Binding**: Safe and efficient UI interactions.
-   **Gson**: Efficient local storage of shortcut metadata.
-   **ShortcutManager API**: Native Android integration for "Pin to Home Screen" functionality.

## 📸 Screenshots

| Dashboard | Create Shortcut | App Picker |
| :--- | :--- | :--- |
| ![Dashboard](https://via.placeholder.com/200x400?text=Dashboard) | ![Create](https://via.placeholder.com/200x400?text=Create+Form) | ![Picker](https://via.placeholder.com/200x400?text=App+Picker) |

*(Note: Replace placeholders with actual screenshots for a truly "cool" readme!)*

## 📦 Installation

1.  Clone the repository:
    ```bash
    git clone https://github.com/albertincx/ai-shortcut-magic-android.git
    ```
2.  Open in **Android Studio**.
3.  Build and Run on your device (API 28+).

## 🤖 CI/CD & Play Store Deployment

This project is equipped with a GitHub Actions workflow for automated deployment. To use it:

1.  **Prepare a Service Account**: In Google Play Console, create a Service Account with "Release Manager" permissions and download the JSON key.
2.  **Generate a Signing Key**: Use `keytool` to create a `.jks` file.
3.  **Set up GitHub Secrets**: Go to your repository **Settings > Secrets and variables > Actions** and add:
    -   `SIGNING_KEY_BASE64`: Your `.jks` file encoded in base64 (`base64 -i your_key.jks`).
    -   `ALIAS`: Your key alias.
    -   `KEY_STORE_PASSWORD`: Your keystore password.
    -   `KEY_PASSWORD`: Your key password.
    -   `SERVICE_ACCOUNT_JSON`: The plain text content of your Google Service Account JSON.
4.  **Deploy**: Push a tag starting with `v` (e.g., `git tag v1.0.0 && git push origin v1.0.0`) to trigger an automatic build and upload to the **Internal Sharing** track.

## 🤝 Contributing

Feedback and contributions are welcome! Feel free to check the [Issues](https://github.com/albertincx/ai-shortcut-magic-android/issues) page.

---
*Created as a demonstration of rapid Android prototyping.*
