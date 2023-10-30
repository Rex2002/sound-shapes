package de.dhbw.video.shape;

import lombok.Getter;
import lombok.Setter;
import org.opencv.core.MatOfPoint;
import org.opencv.imgproc.Moments;

public class Shape {
    MatOfPoint contour;
    @Getter
    ShapeForm form;
    public int[] pos;
    @Getter @Setter
    ShapeType type;
    public Shape(MatOfPoint contour, ShapeForm form, int[] pos){
        this.contour = contour;
        this.form = form;
        this.pos = pos;
    }

    public static int[] calcPositionFromMoments(Moments m){
        return new int[]{
                (int)(m.m10 / m.m00),
                (int)(m.m01 / m.m00)
        };
    }

}
