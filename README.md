# Overview

This Project uses Google's Mediapipe gesture recognizer and its respective example Android app to create a way for you to control media with your hand.
The apps that work with this and have been tested are Spotify, YouTube Music (with a premium subscription), and Soundcloud. Any other music app has been untested.
In this current state, we are using the default Mediapipe gestures, and each is connected to music controls.
They are as follows...

| **Open Palm** | **Closed fist** |
|---|---|
| ![Open_Palm](https://github.com/user-attachments/assets/d377c8a4-d126-4e67-939b-de20c0cec7dd) | ![Fist](https://github.com/user-attachments/assets/182448ab-a959-4325-a9aa-55a95a7437ac) |
| *Play music* | *Pause music* |

| **Pointing up**| **Peace sign or victory** |
|---|---|
| ![pointing_up](https://github.com/user-attachments/assets/c5b0a74d-8ce4-4d12-9f8b-f603e059ce12) | ![Victory](https://github.com/user-attachments/assets/3b3540c3-3dc9-471f-af7a-b27e9884fb1b) |
| *Previous song* | *Next Song* |


| **Thumbs Up**| **Thumbs Down** |
|---|---|
| ![Thumb_Up](https://github.com/user-attachments/assets/0d2bc6b3-4df1-4313-8b38-bdaf9d8d2121) | ![Thumb_Down](https://github.com/user-attachments/assets/ef2266cd-4415-421a-a7ca-207cefa90ec7) |
| *Volume Up* | *Volume Down* |


| **I Love You**|
|---|
| ![I_Love_You](https://github.com/user-attachments/assets/43bfd083-1b48-40c7-9cff-908e3d88a16a) |
| *Select Next Application* | 



# How to Use / Permissions

In order to use the app and for the Mediapipe model to interact with the Android device, it needs the "Notification read, reply & control" permission which is listed as a restricted permission. 
The app should direct you to the settings for you to turn it on, but the option may be greyed out. If this is the case then you will need to follow your phones directions
on how to turn on restricted permissions for our application. 

It should be noted that this application only uses notifications to obtain the session tokens for the various media apps on your phone. 
When the session is obtained, the token is provided to a media controller and the rest of the notification data is discarded. If you are still skeptical, you may review the code found in
"MyMediaListener.kt" to see exactly what our application is doing with your notifications.

