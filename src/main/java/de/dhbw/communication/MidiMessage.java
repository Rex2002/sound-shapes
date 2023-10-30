package de.dhbw.communication;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MidiMessage {
    int note;
    int velocity;
    int offset;
}
