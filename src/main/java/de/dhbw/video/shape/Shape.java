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
    @Setter
    ShapeColor color;
    public Shape(MatOfPoint contour, ShapeForm form, int[] pos, ShapeColor color){
        this.contour = contour;
        this.form = form;
        this.pos = pos;
        this.type = ShapeType.NONE;
        this.color = color;
    }

    public static int[] calcPositionFromMoments(Moments m){
        return new int[]{
                (int)(m.m10 / m.m00),
                (int)(m.m01 / m.m00)
        };
    }

    public int toInt(){
        return form.toInt() * 3 + color.toInt();
    }

}
