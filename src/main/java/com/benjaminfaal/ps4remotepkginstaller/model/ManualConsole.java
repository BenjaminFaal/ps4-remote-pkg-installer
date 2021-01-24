package com.benjaminfaal.ps4remotepkginstaller.model;

import com.benjaminfaal.ps4remotecontrol.ddp.model.Console;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ManualConsole extends Console {

    @JsonIgnore
    @Override
    public String getUserFriendlyName() {
        return super.getUserFriendlyName() + " (Manual)";
    }

    @JsonIgnore
    @Override
    public boolean isEmpty() {
        return super.isEmpty();
    }
}
