package de.dhbw;

import de.dhbw.communication.EventQueues;
import de.dhbw.communication.UIMessage;
import de.dhbw.ui.App;
import de.dhbw.video.VideoInput;
import javafx.application.Application;
import nu.pattern.OpenCV;
import org.opencv.core.Mat;

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
}