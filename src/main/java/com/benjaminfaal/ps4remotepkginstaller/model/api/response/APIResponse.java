package com.benjaminfaal.ps4remotepkginstaller.model.api.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public abstract class APIResponse {

    private Status status;

    private Integer error;

    private String errorCode;

    public boolean isSuccess() {
        return status == Status.success;
    }

    private enum Status {
        success,
        fail
    }
}
