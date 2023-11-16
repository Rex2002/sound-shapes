package de.dhbw.communication;

import de.dhbw.video.shape.Shape;
import lombok.Data;
import org.opencv.core.Mat;

import java.util.List;

@Data
public class UIMessage {
    private Mat frame;
    private List<Shape> shapes;
    private int[][] lines;
    private Setting setting;

    public UIMessage(Mat frame) {
        this.frame = frame;
    }

    public UIMessage(List<Shape> shapes) {
        this.shapes = shapes;
    }

    public UIMessage(Setting setting) {
        this.setting = setting;
    }

    public UIMessage(int[][] lines) {
        this.lines = lines;
    }
}
