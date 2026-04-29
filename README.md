# Overview

This Project uses Google's Mediapipe gesture recognizer and its respective example Android app to create a way for you to control media with your hand.
The apps that work with this and have been tested are Spotify, YouTube Music (with a premium subscription), and Soundcloud. Any other music app has been untested.
In this, we are using our own gestures, and each is connected to music controls.
They are as follows...

| **Flat Hand Side** | **Open Hand** |
|---|---|
|![Play](https://github.com/user-attachments/assets/3f11e1e6-d037-4fe3-aeed-756c76d8c6e3)| ![Open_Palm](https://github.com/user-attachments/assets/2d595e45-6159-4f87-8892-5057db2925ed) |
| *Play music* | *Pause music* |



| **One Finger point (with thumb)**| **One finger point (no thumb)** |
|---|---|
| ![nexttrack](https://github.com/user-attachments/assets/ca6d350d-efa2-484a-b096-f682a507d1d1)|  ![prevtrack](https://github.com/user-attachments/assets/dcedeb41-06fa-4010-8de7-d534687d8a4f)|
| *Next Song* | *Previous Song* |


| **Point Up**| **Point Down** |
|---|---|
|![pointing_up](https://github.com/user-attachments/assets/0034449d-c20d-4ca4-bafd-d73449d3aefd)| ![VolumeDown](https://github.com/user-attachments/assets/df43c827-c41a-4b70-98b9-847cc1ea402a)|
| *Volume Up* | *Volume Down* |


| **Two finger point (with thumb)**|  **Two finger point (no thumb)** |
|---|---|
|![pointing_up](https://github.com/user-attachments/assets/a3209a17-ab9c-4a76-a091-13d3bbae2602)| ![VolumeDown](https://github.com/user-attachments/assets/03601929-e1c0-46cc-8d11-a64a6e95f190)|
| *Next App* | *Previous App* |


| **Thumbs Up**|
|---|
|![pointing_up](https://github.com/user-attachments/assets/12f0e1e2-4bee-44e9-8b02-02475f1863f7)| 
| *Like Song / Dislike Song* | 


# How to Use / Permissions (PLEASE READ) 

In order to use the app and for the Mediapipe model to interact with the Android device, it needs the "Notification read, reply & control" permission, which is listed as a restricted permission. 
The app should direct you to the settings for you to turn it on, but the option may be greyed out. If this is the case, then you will need to follow your phone's directions
on how to turn on restricted permissions for our application. 

It should be noted that this application only uses notifications to obtain the session tokens for the various media apps on your phone. 
When the session is obtained, the token is provided to a media controller, and the rest of the notification data is discarded. If you are still skeptical, you may review the code found in
"MyMediaListener.kt" to see exactly what our application is doing with your notifications.

