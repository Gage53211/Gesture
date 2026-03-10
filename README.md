This Project used Google's Mediapipe gesture recognizer and the example Android app to create a music controller.
The apps that work with this and have been tested are Spotify, YouTube, and Soundcloud. Any other music app has been untested.
In this current state, we are using the default Mediapipe gestures, and each is connected to music controls.
They are as follows

Open Palm-Play music

![Open_Palm](https://github.com/user-attachments/assets/d377c8a4-d126-4e67-939b-de20c0cec7dd)


Closed fist-Pause music

![Fist](https://github.com/user-attachments/assets/182448ab-a959-4325-a9aa-55a95a7437ac)


Pointing up- Previous song

![pointing_up](https://github.com/user-attachments/assets/c5b0a74d-8ce4-4d12-9f8b-f603e059ce12)


Peace sign or victory- Next Song

![Victory](https://github.com/user-attachments/assets/3b3540c3-3dc9-471f-af7a-b27e9884fb1b)


Thumbs Up - Volume Up

![Thumb_Up](https://github.com/user-attachments/assets/0d2bc6b3-4df1-4313-8b38-bdaf9d8d2121)


Thumbs Down - Volume Down

![Thumb_Down](https://github.com/user-attachments/assets/ef2266cd-4415-421a-a7ca-207cefa90ec7)


ILoveYou - Next Application

![I_Love_You](https://github.com/user-attachments/assets/43bfd083-1b48-40c7-9cff-908e3d88a16a)

How to Use-
In order to use the app and for the Mediapipe model to interact with the Android device, it needs special restricted permissions. 
The app should direct you to the settings for you to turn them on, but you may need to manually allow permissions. After permissions are given, the app takes from the active
media players, so if you were not listening to music, you would need to start up the music player to interact with it.
