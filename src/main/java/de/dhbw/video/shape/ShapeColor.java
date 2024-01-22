package de.dhbw.video.shape;

public enum ShapeColor {
    RED,
    GREEN,
    BLUE,
    UNDEFINED;

    @Override
    public String toString() {
        switch(this){
            case RED -> {return "RED";}
            case GREEN -> {return "GREEN";}
            case BLUE -> {return "BLUE";}
            default -> {return "UNDEFINED";}
        }
    }
}