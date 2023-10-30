package de.dhbw.communication;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class EventQueues {
    public static final BlockingQueue<UIMessage> toUI = new ArrayBlockingQueue<>(20);
    public static final BlockingQueue<Setting> toController = new ArrayBlockingQueue<>(20);
    public static final BlockingQueue<MidiMessage> toMidi = new ArrayBlockingQueue<>(20);
}
