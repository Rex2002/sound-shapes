package de.dhbw.music;

import de.dhbw.video.shape.Shape;
import de.dhbw.video.shape.ShapeForm;
import de.dhbw.video.shape.ShapeType;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.util.List;

import static de.dhbw.statics.*;

public class ShapeProcessor {

    List<Shape> shapes;
    // consists of x_pos, y_pos, width, height, is_rect(0/1)
    int[] playfieldInfo;
    Mat[] playFieldBoundaries;
    int frameHeight;
    int frameWidth;
    boolean[][] soundMatrix;
    public ShapeProcessor(){
        playFieldBoundaries = new Mat[4];
        for(int i = 0; i < 4; i++) {
            playFieldBoundaries[i] = new Mat(2,1, CvType.CV_32SC1);
        }
        playfieldInfo = new int[5];
    }

    //TODO check if making the width and height of  the input-video globally available makes sense
    public void processShapes(List<Shape> shapes, int width, int height){
        this.shapes = shapes;
        frameWidth = width;
        frameHeight = height;
        detectPlayfield();
        if(playfieldInfo[4] == 1){
            generateSoundMatrix();
        }
        // TODO > treat control markers
    }

    public boolean[][] getSoundMatrix(){
        return soundMatrix;
    }

    private void detectPlayfield(){
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
                if(Math.sqrt( square.pos[0] * square.pos[0] + (frameHeight - square.pos[1]) * (frameHeight - square.pos[1])) > Math.sqrt(corners[1].pos[0] * corners[1].pos[0] + (frameHeight - corners[1].pos[1]) * (frameHeight - corners[1].pos[1]))){
                    corners[1] = square;
                }
                // bottom left
                if(Math.sqrt((frameWidth - square.pos[0]) * (frameWidth - square.pos[0]) + square.pos[1] * square.pos[1]) > Math.sqrt((frameWidth - corners[3].pos[0]) * (frameWidth - corners[3].pos[0]) + corners[3].pos[1] * corners[3].pos[1])){
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

    /**
     * This method must not be called when the playfield is not valid!!!
     */
    private void generateSoundMatrix(){
        soundMatrix = new boolean[NO_BEATS][NO_INSTR];
        int barOffset, beatNo;
        for(Shape s : shapes.stream().filter(s -> s.getType() == ShapeType.SOUND_MARKER).toList()){
            barOffset = s.pos[1] - playfieldInfo[1] > playfieldInfo[3]/2 ? NO_BARS/2 : 0;
            beatNo = (int) (((s.pos[0] - playfieldInfo[0])/ (double) playfieldInfo[2]) * NO_BEATS);
            soundMatrix[barOffset * NO_BEATS/NO_BARS + beatNo][s.getForm().toInt()] = true;
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
}
