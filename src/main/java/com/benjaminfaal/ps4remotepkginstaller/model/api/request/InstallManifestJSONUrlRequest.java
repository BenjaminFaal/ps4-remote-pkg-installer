package com.benjaminfaal.ps4remotepkginstaller.model.api.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class InstallManifestJSONUrlRequest extends InstallRequest {

    private String url;

    private final String type = "ref_pkg_url";

}
