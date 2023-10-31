package de.dhbw;

import de.dhbw.communication.EventQueues;
import de.dhbw.communication.UIMessage;
import de.dhbw.music.MidiAdapter;
import de.dhbw.music.MidiOutputDevice;
import de.dhbw.ui.App;
import de.dhbw.video.MarkerRecognizer;
import de.dhbw.video.ShapeProcessor;
import de.dhbw.video.VideoInput;
import javafx.application.Application;
import nu.pattern.OpenCV;
import org.opencv.core.Mat;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    static boolean running = true;
    public static void main(String[] args) {
        OpenCV.loadLocally();
        Thread uiThread = new Thread( () -> Application.launch( App.class, args) );
        uiThread.start();

        runController();
        //mockVideoInput();

        System.out.print("Hello World");
    }

    private static void mockVideoInput() {
        OpenCV.loadLocally();
        VideoInput videoInput = new VideoInput(0);
        Mat frame = new Mat();
        Runnable frameGrabber = () -> {
            videoInput.grabImage(frame);
            UIMessage msg = new UIMessage( frame );
            EventQueues.toUI.offer( msg );
        };

        ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor();
        timer.scheduleAtFixedRate(frameGrabber, 0, 100, TimeUnit.MILLISECONDS);
    }

    public static void runController(){
        // time_zero is not set to zero to optionally allow sync wit ext. clocks later on by simply manipulating time_zero
        long time_zero = System.currentTimeMillis();
        long time = time_zero;
        Settings settings = new Settings(120);
        VideoInput videoIn = new VideoInput(0);
        MarkerRecognizer markerRecognizer = new MarkerRecognizer();
        ShapeProcessor shapeProcessor = new ShapeProcessor();
        MidiAdapter midiAdapter = new MidiAdapter();
        MidiOutputDevice midiOutputDevice = new MidiOutputDevice();
        midiOutputDevice.setMidiDevice(0);
        midiOutputDevice.start();
        Clock clock = new Clock(time_zero);
        clock.setTempo(settings.tempo);
        Mat frame = new Mat();
        int counter = 0;

        while (running){
            if(!EventQueues.toController.isEmpty()){
                // take event and process. Probably set settings accordingly or close application
                //
            }
            clock.tick(System.currentTimeMillis());
            videoIn.grabImage(frame);
            EventQueues.toUI.offer(new UIMessage(frame));
            markerRecognizer.setFrame(frame);
            markerRecognizer.detectShapes();
            shapeProcessor.processShapes(markerRecognizer.getShapes(), frame.width(), frame.height());
            midiAdapter.tickMidi(clock.currentBeat, shapeProcessor.getSoundMatrix(), settings);
            // TODO add sending shapes to UI for display

            if(counter % 10 == 0) {
                if (counter % 100 == 0) {
                    System.out.println("MB used=" + (Runtime.getRuntime().totalMemory() -
                            Runtime.getRuntime().freeMemory()) / (1000 * 1000) + "M");
                    counter = 0;
                    System.out.println("fps: " + 100f / (System.currentTimeMillis() - time) * 1000);
                    time = System.currentTimeMillis();
                    System.gc();
                }
            }
            counter++;
        }
        videoIn.releaseCap();
        midiOutputDevice.release();

    }
}