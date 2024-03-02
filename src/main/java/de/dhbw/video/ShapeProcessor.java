package de.dhbw.video;

import de.dhbw.communication.EventQueues;
import de.dhbw.communication.Setting;
import de.dhbw.communication.SettingType;
import de.dhbw.video.shape.Shape;
import de.dhbw.video.shape.ShapeForm;
import de.dhbw.video.shape.ShapeType;
import lombok.Getter;
import lombok.Setter;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.util.List;

import static de.dhbw.Statics.*;

public class ShapeProcessor {

    private List<Shape> shapes;
    // consists of x_pos, y_pos, width, height, is_rect(0/1)
    @Getter
    private final int[] playFieldInfo;
    private final Mat[] playFieldBoundaries;
    private int frameHeight;
    private int frameWidth;
    @Getter
    private Mat frame;
    @Getter
    private boolean[][] soundMatrix;
    @Setter
    private double lastVelocity = 0, lastTempo = 0;
    @Setter
    private boolean enableControlMarker = true;
    private final int[] cmRegocCount = new int[2];
    @Setter
    private int beatsPerBar = DEFAULT_TIME_ENUMERATOR * 2;
    public ShapeProcessor(){
        playFieldBoundaries = new Mat[4];
        for(int i = 0; i < 4; i++) {
            playFieldBoundaries[i] = new Mat(2,1, CvType.CV_32SC1);
        }
        playFieldInfo = new int[5];
    }

    public void processShapes(List<Shape> shapes, Mat frame){
        this.shapes = shapes;
        this.frame = frame;
        frameWidth = frame.width();
        frameHeight = frame.height();
        detectPlayField();
        if(playFieldInfo[4] == 1){
            generateSoundMatrix();
        }
        if(enableControlMarker) detectControlMarkers();
    }


    private void detectPlayField(){
        playFieldInfo[4] = 1;
        Shape[] squares = shapes.stream().filter(shape -> shape.getForm() == ShapeForm.SQUARE).toArray(Shape[]::new);
        if(squares.length < 4){
            playFieldInfo[4] = 0;
            return;
        }
        Shape[] corners = new Shape[]{squares[0], squares[1], squares[2], squares[3]};
        // calculates the (Euclidean) distance of each square to each corner and checks if the respective square is the one closest to any corner.
        // if the latter is the case, the found square is considered the new closest one for further comparisons
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
            if(Math.sqrt( square.pos[0] * square.pos[0] + (frameHeight - square.pos[1]) * (frameHeight - square.pos[1])) > Math.sqrt(corners[1].pos[0] * corners[1].pos[0] + (frameHeight - corners[1].pos[1]) * (frameHeight - corners[1].pos[1]))){
                corners[1] = square;
            }
            // bottom left
            if(Math.sqrt((frameWidth - square.pos[0]) * (frameWidth - square.pos[0]) + square.pos[1] * square.pos[1]) > Math.sqrt((frameWidth - corners[3].pos[0]) * (frameWidth - corners[3].pos[0]) + corners[3].pos[1] * corners[3].pos[1])){
                corners[3] = square;
            }
        }
        for(int i = 0; i < 4; i++){
            playFieldBoundaries[i].put(0,0, corners[i].pos[0] - corners[(i+1) % 4].pos[0]);
            playFieldBoundaries[i].put(1,0, corners[i].pos[1] - corners[(i+1) % 4].pos[1]);
            corners[i].setType(ShapeType.FIELD_MARKER);
        }
        playFieldInfo[0] = (corners[0].pos[0] + corners[3].pos[0]) / 2;
        playFieldInfo[1] = (corners[0].pos[1] + corners[1].pos[1]) / 2;
        playFieldInfo[2] = (corners[1].pos[0] + corners[2].pos[0]) / 2 - playFieldInfo[0];
        playFieldInfo[3] = (corners[2].pos[1] + corners[3].pos[1]) / 2 - playFieldInfo[1];
        playFieldInfo[4] = checkRectangular() ? 1 : 0;
        for(Shape s : shapes) {
            if (s.pos[0] > playFieldInfo[0] && s.pos[0] < playFieldInfo[0] + playFieldInfo[2]
                    && s.pos[1] > playFieldInfo[1] && s.pos[1] < playFieldInfo[1] + playFieldInfo[3] && s.getType() != ShapeType.FIELD_MARKER) {
                s.setType(ShapeType.SOUND_MARKER);
            }
        }
    }

    public void detectControlMarkers(){
        List<Shape> rect = shapes.stream().filter(shape -> shape.getForm() == ShapeForm.RECT && shape.getType() == ShapeType.NONE).toList();
        List<Shape> triangles = shapes.stream().filter(shape -> shape.getForm() == ShapeForm.TRIANGLE && shape.getType() == ShapeType.NONE).toList();

        if(rect.size() == 1 && ( rect.getFirst().pos[0] < playFieldInfo[0] || rect.getFirst().pos[0] > playFieldInfo[0] + playFieldInfo[2])){
            rect.getFirst().setType(ShapeType.CONTROL_MARKER);
            double nextVelocity = (double) rect.getFirst().pos[1]/480;
            if(Math.abs(nextVelocity - lastVelocity) > 0.05){
                cmRegocCount[0]++;
                if(cmRegocCount[0] == 15) {
                    System.out.println("Changing velocity with cm: " + rect.size() + ", setting to " + nextVelocity);
                    lastVelocity = nextVelocity;
                    EventQueues.toController.offer(new Setting<>(SettingType.CM_VELOCITY, lastVelocity));
                    cmRegocCount[0] = 0;
                }
            }
            else{
                cmRegocCount[0] = 0;
            }
        }
        if(triangles.size() == 1 && (triangles.getFirst().pos[0] < playFieldInfo[0] || triangles.getFirst().pos[0] > playFieldInfo[0] + playFieldInfo[2])){
            triangles.getFirst().setType(ShapeType.CONTROL_MARKER);
            double nextTempo = (double) triangles.getFirst().pos[1]/480;
            if(Math.abs(nextTempo - lastTempo) > 0.05){
                cmRegocCount[1]++;
                if(cmRegocCount[1] == 15) {
                    System.out.println("Changing tempo with cm: " + triangles.size() + ", setting to " + nextTempo);
                    lastTempo = nextTempo;
                    EventQueues.toController.offer(new Setting<>(SettingType.CM_TEMPO, lastTempo));
                    cmRegocCount[1] = 0;
                }
            }
            else{
                cmRegocCount[1] = 0;
            }
        }

    }

    /**
     * This method must not be called when the playField is not valid!
     */
    private void generateSoundMatrix() {
        soundMatrix = new boolean[beatsPerBar * NO_BARS][NO_NOTES];
        int barOffset, beatNo;
        for( Shape s : shapes.stream().filter(s -> s.getType() == ShapeType.SOUND_MARKER).toList() ) {
            barOffset = s.pos[1] - playFieldInfo[1] > playFieldInfo[3]/2 ? NO_BARS/2 : 0;
            beatNo = (int) (((s.pos[0] - playFieldInfo[0])/ (double) playFieldInfo[2]) * beatsPerBar);
            soundMatrix[barOffset * beatsPerBar + beatNo][s.toInt()] = true;
        }
    }

    private boolean checkRectangular() {
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

}
