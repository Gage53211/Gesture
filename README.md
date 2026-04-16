# Overview

This Project uses Google's Mediapipe gesture recognizer and its respective example Android app to create a way for you to control media with your hand.
The apps that work with this and have been tested are Spotify, YouTube Music (with a premium subscription), and Soundcloud. Any other music app has been untested.
In this, we are using our own gestures, and each is connected to music controls.
They are as follows...

| **Flat Hand Side** | **Open Hand** |
|---|---|
|![Play](https://github.com/user-attachments/assets/2e6c2b06-c2f8-4cc3-b6e2-864dca02916a)| ![Open_Palm](https://github.com/user-attachments/assets/d377c8a4-d126-4e67-939b-de20c0cec7dd) |
| *Play music* | *Pause music* |

| **One Finger point (with thumb)**| **One finger point (no thumb)** |
|---|---|
| ![nexttrack](https://github.com/user-attachments/assets/ff60ef60-6982-4afb-8969-9b3d8e18ccbe)|  ![prevtrack](https://github.com/user-attachments/assets/7f8dd320-9cee-4d0e-bdbf-d2837d51d048)|
| *Next Song* | *Previous Song* |


| **Point Up**| **Point Down** |
|---|---|
|![pointing_up](https://github.com/user-attachments/assets/c5b0a74d-8ce4-4d12-9f8b-f603e059ce12)| ![VolumeDown](https://github.com/user-attachments/assets/76b7dade-81e6-4bc1-9065-1e26113a22c7)|
| *Volume Up* | *Volume Down* |


| **Two finger point (with thumb)**|
|---|
| ![nextapp](https://github.com/user-attachments/assets/78ba8d56-8adf-4f1a-90c8-dfe6c01e5343)|
| *Go to next app* | 



# How to Use / Permissions

In order to use the app and for the Mediapipe model to interact with the Android device, it needs the "Notification read, reply & control" permission, which is listed as a restricted permission. 
The app should direct you to the settings for you to turn it on, but the option may be greyed out. If this is the case, then you will need to follow your phone's directions
on how to turn on restricted permissions for our application. 

It should be noted that this application only uses notifications to obtain the session tokens for the various media apps on your phone. 
When the session is obtained, the token is provided to a media controller, and the rest of the notification data is discarded. If you are still skeptical, you may review the code found in
"MyMediaListener.kt" to see exactly what our application is doing with your notifications.

