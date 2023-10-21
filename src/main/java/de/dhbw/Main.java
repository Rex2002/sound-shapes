package de.dhbw;

import de.dhbw.communication.EventQueues;
import de.dhbw.communication.Message;
import de.dhbw.communication.MsgType;
import de.dhbw.ui.App;
import javafx.application.Application;
import nu.pattern.OpenCV;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        Thread uiThread = new Thread( () -> Application.launch( App.class, args) );
        uiThread.start();

        mockVideoInput();

        System.out.print("Hello World");
    }

    private static void mockVideoInput() {
        OpenCV.loadLocally();

        VideoCapture capture = new VideoCapture();
        boolean open = capture.open(0);

        if ( !open ) {
            System.err.println("Encountered Error while attempting to open camera connection.");
            System.exit(1);
        }
        System.out.println("Successfully opened camera!");

        Runnable frameGrabber = () -> {
            Mat frame = new Mat();
            capture.read( frame );
            Message msg = new Message( MsgType.FRAME, frame );
            try {
                EventQueues.toUI.offer( msg );
            } catch (Exception e) {
                System.out.println("Catch");
                e.printStackTrace();
            }
        };

        ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor();
        timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);

    }
}