package de.dhbw.video;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

public class VideoInput {
    VideoCapture cap;
    public VideoInput(int deviceNo){
        cap = new VideoCapture(deviceNo);
        checkOpen();
    }
    public VideoInput(){
        cap = new VideoCapture();
        checkOpen();
    }

    private void checkOpen(){
        if(!cap.isOpened()){
            System.err.println("Failed to acquire video device");
            System.exit(1);
        }
    }

    public void releaseCap(){
        cap.release();
        cap = null;
    }

    public void grabImage(Mat frame){
        // TODO check if this impacts performance
        if(cap != null && cap.isOpened()) {
            cap.read(frame);
        }
    }
}
