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
    Mat frame;
    @Getter
    final
    Mat gray = new Mat();
    @Getter
    final
    Mat blurred=new Mat();
    @Getter
    final
    Mat bin = new Mat();
    @Getter
    final Mat hierarchy = new Mat();
    final List<MatOfPoint> contours = new ArrayList<>(EXPECTED_SHAPE_NO);
    @Getter
    List<Shape> shapes = new ArrayList<>(EXPECTED_SHAPE_NO);
    public MarkerRecognizer(){
    }

    public void setFrame(Mat frame){
        this.frame = frame;
        // TODO check how many contours usually are found and adapt initial capacity
        contours.clear();
        //shapes.clear();
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
        double perimeter, area, areaRatio, prevMax;
        int idxMax;
        double[] vals;
        ShapeForm form;
        ShapeColor color;
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
            else if (edgeNo > 5){
                form = ShapeForm.CIRCLE;
            }
            else {
                continue;
            }
            idxMax = 0;
            prevMax = 0;
            Mat mask = new Mat(frame.size(), CvType.CV_8UC1, Scalar.all(0));
            Imgproc.drawContours(mask, List.of(contour), -1, new Scalar(255), -1);
            vals = Core.mean(frame, mask).val;
            mask.release();
            for (int idx = 0; idx < 3; idx++) {
                if(vals[idx] > prevMax){
                    prevMax = vals[idx];
                    idxMax = idx;
                }
            }
            color = switch (idxMax) {
                case 0 -> ShapeColor.BLUE;
                case 1 -> ShapeColor.GREEN;
                case 2 -> ShapeColor.RED;
                default -> ShapeColor.UNDEFINED;
            };
            shapes.add(new Shape(contour, form, Shape.calcPositionFromMoments(Imgproc.moments(contour)), color));
        }
    }

    private void findContours(){
        Imgproc.cvtColor(frame, gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(gray, blurred, new Size(11,11), 0, 0);
        Imgproc.threshold(blurred, bin, 90, 255, Imgproc.THRESH_BINARY_INV);
        Imgproc.findContours(bin, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
    }
}
