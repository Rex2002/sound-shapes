package de.dhbw.communication;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Setting<T> {
    private SettingType type;
    private T value;
}
