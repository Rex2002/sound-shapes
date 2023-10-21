package de.dhbw.communication;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class EventQueues {
    public static final BlockingQueue<Message> toUI = new ArrayBlockingQueue<>(10);
    public static final BlockingQueue<Message> toController = new ArrayBlockingQueue<>(10);
    public static final BlockingQueue<Message> toMidi = new ArrayBlockingQueue<>(10);
}
