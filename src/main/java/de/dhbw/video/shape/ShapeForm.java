package de.dhbw.video.shape;

public enum ShapeForm {
    TRIANGLE,
    SQUARE,
    RECT,
    PENTAGON,
    CIRCLE;

    public int toInt(){
        switch (this){
            case TRIANGLE -> {return 0;}
            case RECT -> {return 1;}
            case SQUARE -> {return 2;}
            case PENTAGON -> {return 3;}
            case CIRCLE -> {return 4;}
            default -> {return 0;}
        }
    }
}
