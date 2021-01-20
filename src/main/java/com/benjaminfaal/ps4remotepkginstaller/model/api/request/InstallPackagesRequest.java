package com.benjaminfaal.ps4remotepkginstaller.model.api.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class InstallPackagesRequest extends InstallRequest {

    private String[] packages;

    private String[] localFiles;

    private final String type = "direct";

}
