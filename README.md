# SoundShapes
SoundShapes is a simple and fun out-of-the-box step sequencer.
And we really mean that: The steps are controlled by positioning physical tokens under a camera.
These tokens can just be bits of paper or cardboard and the camera can be your smartphone or external webcam.

The app was developed as our final coding project within the Applied Computer Science bachelor program at [DHBW Mannheim](https://dhbw-mannheim.de/).

<img src="https://file.notion.so/f/f/81470b3b-ef5c-4b58-967d-a834a0478708/76e186fd-4bd4-4634-b2d4-f299a6c8cd91/Model_Video-TopDown.png?id=9296fdcb-807f-4cd6-a801-e8eea133ecbf&table=block&spaceId=81470b3b-ef5c-4b58-967d-a834a0478708&expirationTimestamp=1713196800000&signature=HYbKttQ8axbWXSw30bo2uQhO0JZJaOSFFcP9_TpUEd0&downloadName=Model_Video-TopDown.png" alt="3D-Rendering of SoundShapes Playfield-Setup"/>


## Getting started
1. Clone this repository and set up your virtual environment, you will need JavaFX and Java in version 21.
2. Set up your physical environment: Find a webcam or a way to connect a phone or other camera device to your computer
   and set it up in top-down position over your desk or other surface with enough space for your Playfield.
   SoundShapes works best with a smooth and light background (like a simple white table) and strong consistent lighting.
   1. Smartphones might offer a wireless connection to your computer (like Apple Continuity Camera).
       Wired connections are generally encouraged though as they work much more reliably with OpenCV's video API.
   2. Make sure that the app has permission to access camera input through your operating system.
3. Make your Markers: Cut out the desired amount of markers from coloured paper or cardboard.
   1. You will need a minimum of four squares for CornerMarkers and about four of each for square, triangle, circle and rectangle SoundMarkers.
      You might want to add a few more triangles since they are used for HiHats.
   2. Start with red SoundMarkers and add blue and green ones for additional sounds.
   3. The best size for the markers depends on the size of your video frame.
      Make sure that the diameter of each marker doesn't exceed one eighth of the width of your Playfield (or one twelfth if you plan to use custom time signatures).
      Testing was done with 3x3cm markers on <span style="color: red;">a 30x10cm Playfield</span>.
   4. Use of already coloured paper is encouraged as pencil-coloured markers might not be recognised as reliably. For all three colours darker shades  (i.e. shaedes with greater contrast to the background) tend to work more reliably.
4. Get started:
   1. Connect your camera.
   2. Run the app (adding camera permissions if necessary).
   3. Position four square markers in a rectangle to form your Playfield.
   4. Put down some markers in the Playfield, press play and see what happens!


## Developers
- [Rex2002](https://github.com/Rex2002)
- [MalteRichert](https://github.com/MalteRichert)