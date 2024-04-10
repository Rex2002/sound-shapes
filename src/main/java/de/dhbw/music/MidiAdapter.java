package de.dhbw.music;

import de.dhbw.communication.EventQueues;
import de.dhbw.communication.MidiBatchMessage;
import de.dhbw.Statics;
import lombok.Setter;

import static de.dhbw.Statics.*;

public class MidiAdapter {
    int lastInterval = -1;
    @Setter
    int velocity = DEFAULT_VELOCITY;
    boolean playing = true;
    @Setter
    boolean metronomeActive = false;
    MidiBatchMessage midiBatchMessage = new MidiBatchMessage();
    @Setter
    int channel = 9;
    private int beatsPerBar = DEFAULT_TIME_ENUMERATOR * 2;
    private boolean timeDoubled = true;

    public void setMute(boolean muted) {
        this.playing = !muted;
    }
    public void tickMidi(int posInBeat, boolean[][] soundMatrix) {
        int factor = timeDoubled ? 2 : 1;
        if(posInBeat != lastInterval && playing){
            midiBatchMessage.clearMessages();
            lastInterval = posInBeat;
            if(metronomeActive && posInBeat % factor == 0) {
                if(posInBeat == 0 || posInBeat == beatsPerBar) {
                    midiBatchMessage.addMidiMessage(Statics.METRONOME_UP_SOUND, velocity, -1, 9);
                }
                else {
                    midiBatchMessage.addMidiMessage(Statics.METRONOME_SOUND, velocity, -1, 9);
                }
            }
            if(soundMatrix != null) {
                for (int note = 0; note < soundMatrix[posInBeat].length; note++) {
                    if (soundMatrix[posInBeat][note]) {
                        midiBatchMessage.addMidiMessage(int2Note(note), velocity, -1, channel);
                    }
                }
            }
            EventQueues.toMidi.offer(midiBatchMessage);
        }
    }

    public void setTimeInfo(int beatsPerBar, boolean doubled) {
        this.beatsPerBar = beatsPerBar;
        this.timeDoubled = doubled;
    }

    // TODO rework sound numbers, since it just repeats thrice at the moment
    public int int2Note(int i){
        if(channel == 10) return i  + 60;
        return switch (i) {
            case 0 -> 42;
            case 1 -> 36;
            case 2 -> 38;
            case 3 -> 49;
            case 4 -> 51;
            case 5 -> 42;
            case 6 -> 36;
            case 7 -> 38;
            case 8 -> 49;
            case 9 -> 51;
            case 10 -> 42;
            case 11 -> 36;
            case 12 -> 38;
            case 13 -> 49;
            case 14 -> 51;
            default -> 56;
        };
    }
}
