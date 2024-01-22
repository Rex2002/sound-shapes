package de.dhbw.video;

import de.dhbw.video.shape.Shape;
import de.dhbw.video.shape.ShapeColor;
import de.dhbw.video.shape.ShapeForm;
import lombok.Getter;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class MarkerRecognizer {
    private static final int EXPECTED_SHAPE_NO = 30;
    @Getter
    Mat frame, gray = new Mat(), blurred=new Mat(), bin = new Mat(), hierarchy = new Mat();
    List<MatOfPoint> contours = new ArrayList<>(EXPECTED_SHAPE_NO);
    @Getter
    List<Shape> shapes;
    int height, width;
    public MarkerRecognizer(){
    }

    public void setFrame(Mat frame){
        this.frame = frame;
        width = frame.width();
        height = frame.height();
        // TODO check how many contours usually are found and adapt initial capacity
        contours.clear();
    }

    public void detectShapes(){
        findContours();
        if(contours == null || contours.isEmpty()){
            System.err.println("No contours detected");
            // TODO message to UI to display
            return;
        }

        MatOfPoint2f contour2f; MatOfPoint2f contourTarget2f = new MatOfPoint2f();
        RotatedRect rrec;
        double perimeter, area, areaRatio;
        ShapeForm form;
        shapes = new ArrayList<>(EXPECTED_SHAPE_NO);
        for(MatOfPoint contour : contours){
            area = Imgproc.contourArea(contour);
            if(area < 80){
                continue;
            }
            contour2f = new MatOfPoint2f(contour.toArray());
            perimeter = Imgproc.arcLength(contour2f, true);
            Imgproc.approxPolyDP(contour2f, contourTarget2f, 0.04 * perimeter, true);
            int edgeNo = contourTarget2f.toList().size();
            if(edgeNo == 3){
                form = ShapeForm.TRIANGLE;
            }
            else if(edgeNo == 4){
                rrec = Imgproc.minAreaRect(contourTarget2f);
                areaRatio = rrec.size.width / rrec.size.height;
                if(areaRatio >= 0.8 && areaRatio <= 1.2) form = ShapeForm.SQUARE;
                else form = ShapeForm.RECT;
            }
            else if(edgeNo == 5){
                form = ShapeForm.PENTAGON;
            }
            else{
                form = ShapeForm.CIRCLE;
            }
            shapes.add(new Shape(contour, form, Shape.calcPositionFromMoments(Imgproc.moments(contour))));
        }
        detectColours();
    }

    private void detectColours(){
        Mat mask = new Mat(frame.size(), CvType.CV_8UC1, Scalar.all(0));

        int idxMax = 0;
        double prevMax = 0;
        int c = 0;
        for(Shape s:shapes){
            Imgproc.drawContours(mask, List.of(s.getContour()), -1, new Scalar(255), -1);
            for(double val : Core.mean(frame, mask).val){
                if(val > prevMax){
                    idxMax = c;
                    prevMax = val;
                }
                c++;
            }
            switch (idxMax){
                case 0: s.setColor(ShapeColor.BLUE);
                case 1: s.setColor(ShapeColor.GREEN);
                case 2: s.setColor(ShapeColor.RED);
                default:s.setColor(ShapeColor.UNDEFINED);
            }
        }
    }

    private void findContours(){
        Imgproc.cvtColor(frame, gray, Imgproc.COLOR_BGR2GRAY);
        // TODO find out what these values actually mean and potentially adapt
        Imgproc.GaussianBlur(gray, blurred, new Size(11,11), 4/6f);
        Imgproc.threshold(blurred, bin, 65, 255, Imgproc.THRESH_BINARY_INV);
        // TODO find out what RETR_TREE and CHAIN_APPROX_SIMPLE mean
        Imgproc.findContours(bin, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
    }
}
