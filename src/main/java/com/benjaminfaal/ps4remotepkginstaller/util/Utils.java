package com.benjaminfaal.ps4remotepkginstaller.util;

import lombok.experimental.UtilityClass;

import java.nio.file.Path;
import java.nio.file.Paths;

@UtilityClass
public class Utils {

    public Path getWorkingDirectory() {
        return Paths.get(System.getProperty("user.home")).resolve("ps4-remote-pkg-installer");
    }

}
