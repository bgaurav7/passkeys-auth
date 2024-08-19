# Passkeys with Android's CredentialManager API (Server Integration)

This project demonstrates the integration of the WebAuthn Passkey API with an Android application using the Credential Manager API. It allows users to choose between passkeys and traditional passwords for login.

## Project Structure:

- Server (passkeys-server): A Node.js server that handles authentication requests and manages passkey challenges.
- Android App (CredentialManager): An Android application that utilizes the Credential Manager API for passkey creation, storage, and retrieval for authentication with the server.

## Server Setup:

- Installation: Run npm install in the server directory to install dependencies.
- Running the Server: Start the server using node index.js.

## Android App Setup:

- Server Domain Update: Modify the server domain within ApiClient.kt in the Android project. Ensure your server runs on HTTPS.
- App Certificate and Package Update: Update the app certificate fingerprint and package name in your server's .well-known/assetlinks.json file.
- Run the Application: Build and run the app on your Android device.

## Additional Resources:

Server with Passkeys and Password Integration: https://github.com/bgaurav7/passkeys-auth-server
Detailed Android Passkey Usage: https://github.com/bgaurav7/passkeys-auth-android

### Note: This project provides a basic example for educational purposes. For production use, prioritize robust security practices and adapt the implementation to your specific requirements.