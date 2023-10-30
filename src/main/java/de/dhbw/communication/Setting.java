package de.dhbw.communication;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Setting {
    private SettingType type;
    private double value; //between 0 and 1,inclusively. Boolean is represented as 0 -> false, 1-> true
}
