package de.dhbw.video;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

public class VideoInput {
    VideoCapture cap;
    public VideoInput(int deviceNo){
        cap = new VideoCapture(deviceNo);
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
        if(cap != null && cap.isOpened()) {
            cap.read(frame);
        }
        Imgproc.resize(frame, frame, new Size(640,640 * (double) frame.height() / frame.width() ));
    }

    public void setInputDevice(int deviceNo) {
        cap = new VideoCapture(deviceNo);
        checkOpen();
    }
}
