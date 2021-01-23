package com.benjaminfaal.ps4remotepkginstaller;

import com.jogamp.common.util.VersionNumber;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.util.Map;

@CommonsLog
@EnableAsync
@Component
public class Updater {

    private static final String LATEST_RELEASE_URL = "https://api.github.com/repos/benjaminfaal/ps4-remote-pkg-installer/releases/latest";

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${project.version}")
    private String projectVersion;

    @Async
    void checkForUpdate() {
        try {
            Map<String, Object> latestRelease = restTemplate.getForObject(LATEST_RELEASE_URL, Map.class);
            String latestTagName = (String) latestRelease.get("tag_name");
            String changelog = (String) latestRelease.get("body");
            VersionNumber latestVersion = new VersionNumber(latestTagName);
            VersionNumber currentVersion = new VersionNumber(projectVersion);

            if (currentVersion.compareTo(latestVersion) < 0) {
                String message =
                        "There is a new and better version available: " + latestVersion + System.lineSeparator() + System.lineSeparator() +
                                "Changelog:" +
                                System.lineSeparator() +
                                changelog;
                JOptionPane.showMessageDialog(null, message, "Update available", JOptionPane.INFORMATION_MESSAGE);
                Desktop.getDesktop().browse(URI.create((String) latestRelease.get("html_url")));
            }
        } catch (Exception e) {
            log.error("Error checking for update: ", e);
        }
    }

}
