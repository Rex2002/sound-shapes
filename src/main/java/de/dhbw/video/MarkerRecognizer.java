package de.dhbw.video;

import de.dhbw.video.shape.Shape;
import de.dhbw.video.shape.ShapeForm;
import de.dhbw.video.shape.ShapeType;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class MarkerRecognizer {
    private static final int EXPECTED_SHAPE_NO = 30;
    Mat frame, gray, blurred, bin, hierarchy;
    List<MatOfPoint> contours;
    List<Shape> shapes;

    // consists of x_pos, y_pos, width, height, is_rect(0/1)
    int[] playfieldInfo;
    int height, width;
    Mat[] playFieldBoundaries;
    public MarkerRecognizer(){
        playFieldBoundaries = new Mat[4];
        for(int i = 0; i < 4; i++) {
            playFieldBoundaries[i] = new Mat(2,1, CvType.CV_32SC1);
        }
        playfieldInfo = new int[5];
    }

    public void setFrame(Mat frame){
        this.frame = frame;
        width = frame.width();
        height = frame.height();
        // TODO check how many contours usually are found and adapt initial capacity
        contours = new ArrayList<>(EXPECTED_SHAPE_NO);
    }

    public void detectShapes(){
        findContours();
        if(contours == null || contours.isEmpty()){
            System.err.println("No contours detected");
            // TODO message to UI to display
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
            contour2f = new MatOfPoint2f(contour);
            perimeter = Imgproc.arcLength(contour2f, true);
            Imgproc.approxPolyDP(contour2f, contourTarget2f, 0.04 * perimeter, true);
            int edgeNo = contourTarget2f.toList().size();
            if(edgeNo == 3){
                form = ShapeForm.TRIANGLE;
            }
            else if(edgeNo == 4){
                rrec = Imgproc.minAreaRect(contourTarget2f);
                areaRatio = rrec.size.width / rrec.size.height;
                if(areaRatio >= 0.5 && areaRatio <= 1.5) form = ShapeForm.SQUARE;
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
    }

    public void classify(){
        playfieldInfo[4] = 1;
        Shape[] squares = shapes.stream().filter(shape -> shape.getForm() == ShapeForm.SQUARE).toArray(Shape[]::new);
        if(squares.length < 4){
            playfieldInfo[4] = 0;
            System.err.println("Not enough squares for playfield");
        }
        else {
            Shape[] corners = new Shape[]{squares[0], squares[1], squares[2], squares[3]};
            for (Shape square : squares) {
                // top left
                if(Math.sqrt(square.pos[1] * square.pos[1] + square.pos[0] * square.pos[0]) < Math.sqrt(corners[0].pos[0] * corners[0].pos[0] + corners[0].pos[1] * corners[0].pos[1])){
                    corners[0] = square;
                }
                // bottom right
                if(Math.sqrt(square.pos[1] * square.pos[1] + square.pos[0] * square.pos[0]) > Math.sqrt(corners[2].pos[0] * corners[2].pos[0] + corners[2].pos[1] * corners[2].pos[1])){
                    corners[2] = square;
                }
                // top right
                if(Math.sqrt( square.pos[0] * square.pos[0] + (height - square.pos[1]) * (height - square.pos[1])) > Math.sqrt(corners[1].pos[0] * corners[1].pos[0] + (height - corners[1].pos[1]) * (height - corners[1].pos[1]))){
                    corners[1] = square;
                }
                // bottom left
                if(Math.sqrt((width - square.pos[0]) * (width - square.pos[0]) + square.pos[1] * square.pos[1]) > Math.sqrt((width - corners[3].pos[0]) * (width - corners[3].pos[0]) + corners[3].pos[1] * corners[3].pos[1])){
                    corners[3] = square;
                }
            }
            for(Shape c : corners) c.setType(ShapeType.FIELD_MARKER);
            for(int i = 0; i < 4; i++){
                playFieldBoundaries[i].put(0,0, corners[i].pos[0] - corners[(i+1) % 4].pos[0]);
                playFieldBoundaries[i].put(1,0, corners[i].pos[1] - corners[(i+1) % 4].pos[1]);
            }
            playfieldInfo[0] = (corners[0].pos[0] + corners[3].pos[0]) / 2;
            playfieldInfo[1] = (corners[0].pos[1] + corners[1].pos[1]) / 2;
            playfieldInfo[2] = (corners[1].pos[0] + corners[2].pos[0]) / 2;
            playfieldInfo[3] = (corners[2].pos[1] + corners[3].pos[1]) / 2;
            playfieldInfo[4] = checkRectangularity() ? 1 : 0;
            for(Shape s : shapes){
                if(s.pos[0] > playfieldInfo[0] && s.pos[0] < playfieldInfo[0] + playfieldInfo[2]
                && s.pos[1] > playfieldInfo[1] && s.pos[1] < playfieldInfo[1] + playfieldInfo[3]){
                    s.setType(ShapeType.SOUND_MARKER);
                }
            }
        }
    }

    private boolean checkRectangularity(){
        double angle;
        for(int i = 0; i < 4; i++){
            angle = Math.acos(
                            playFieldBoundaries[i].dot(playFieldBoundaries[(i+1)%4])
                            / (Core.norm(playFieldBoundaries[i]) * Core.norm(playFieldBoundaries[(i+1)%4])))
                    / Math.PI * 180f;
            if(angle <= 75 || angle >= 105){
                return false;
            }
        }
        return true;
    }

    private void findContours(){
        Imgproc.cvtColor(frame, gray, Imgproc.COLOR_BGR2GRAY);
        // TODO find out what these values actually mean and potentially adapt
        Imgproc.GaussianBlur(gray, blurred, new Size(11,11), 4/6f);
        Imgproc.threshold(blurred, bin, 100, 255, Imgproc.THRESH_BINARY_INV);
        // TODO find out what RETR_TREE and CHAIN_APPROX_SIMPLE mean
        Imgproc.findContours(bin, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
    }


}
