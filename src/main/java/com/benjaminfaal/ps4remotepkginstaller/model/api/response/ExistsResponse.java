package com.benjaminfaal.ps4remotepkginstaller.model.api.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ExistsResponse extends APIResponse {

    private boolean exists;

}
