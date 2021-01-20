package com.benjaminfaal.ps4remotepkginstaller;

import com.benjaminfaal.ps4remotepkginstaller.util.Utils;
import lombok.Getter;
import lombok.experimental.Delegate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.DefaultPropertiesPersister;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

@Component
public class Settings {

    private static final File PROPERTIES_FILE = Utils.getWorkingDirectory().resolve("settings.properties").toFile();

    @Value("${project.name}")
    private String projectName;

    @Getter
    @Delegate
    private final Properties properties = new Properties();

    @PostConstruct
    public void load() throws IOException {
        load(properties);
    }

    @PreDestroy
    public void save() throws IOException {
        new DefaultPropertiesPersister().store(properties, new FileWriter(PROPERTIES_FILE), projectName);
    }

    public static void load(Properties properties) throws IOException {
        if (PROPERTIES_FILE.exists()) {
            new DefaultPropertiesPersister().load(properties, new FileReader(PROPERTIES_FILE));
        }
    }

}
