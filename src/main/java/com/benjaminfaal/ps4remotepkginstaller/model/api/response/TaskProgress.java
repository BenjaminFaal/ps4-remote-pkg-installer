package com.benjaminfaal.ps4remotepkginstaller.model.api.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class TaskProgress extends APIResponse {

    private Integer id;

    private String bits;

    private String length;

    private String transferred;

    private String lengthTotal;

    private String transferredTotal;

    private Integer numIndex;

    private Integer numTotal;

    private Long restSec;

    private Long restSecTotal;

    private Integer preparingPercent;

    private Integer localCopyPercent;

    @Getter
    @AllArgsConstructor
    public enum Status {

        DOWNLOADING(0x18A, "Downloading"),
        INSTALLING(0x18E, "Installing"),
        INSTALLED(0x193, "Installed"),
        PAUSED(0x1A8, "Paused"),
        STOPPED(0x188, "Stopped");

        private final int bits;

        private final String description;

    }

}
