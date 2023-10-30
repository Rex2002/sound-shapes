package de.dhbw.communication;

import lombok.Data;
import org.opencv.core.Mat;

import java.util.List;

@Data
public class UIMessage {
    private Mat frame;
    private List<Object> shapes;
    private Setting setting;

    public UIMessage(Mat frame) {
        this.frame = frame;
    }

    public UIMessage(List<Object> shapes) {
        this.shapes = shapes;
    }

    public UIMessage(Setting setting) {
        this.setting = setting;
    }
}
