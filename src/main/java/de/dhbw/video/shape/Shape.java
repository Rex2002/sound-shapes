package de.dhbw.video.shape;

import lombok.Getter;
import lombok.Setter;
import org.opencv.core.MatOfPoint;
import org.opencv.imgproc.Moments;

@Getter
public class Shape {
    MatOfPoint contour;
    ShapeForm form;
    @Setter
    public int[] pos;
    @Setter
    ShapeType type;
    public Shape(MatOfPoint contour, ShapeForm form, int[] pos){
        this.contour = contour;
        this.form = form;
        this.pos = pos;
        this.type = ShapeType.NONE;
    }

    public static int[] calcPositionFromMoments(Moments m){
        return new int[]{
                (int)(m.m10 / m.m00),
                (int)(m.m01 / m.m00)
        };
    }

}
