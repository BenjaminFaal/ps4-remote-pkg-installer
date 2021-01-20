package com.benjaminfaal.ps4remotepkginstaller;

import com.benjaminfaal.ps4remotepkginstaller.ui.MainUI;
import com.benjaminfaal.ps4remotepkginstaller.ui.SetupUI;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;

import javax.swing.*;
import java.util.Properties;

@CommonsLog
@SpringBootApplication
public class PS4RemotePKGInstallerApplication {

    public static void main(String[] args) {
        FlatLaf.install(new FlatLightLaf());

        SetupUI setupUI = new SetupUI();
        setupUI.pack();
        setupUI.setResizable(false);
        setupUI.setLocationRelativeTo(null);
        setupUI.setVisible(true);
        if (setupUI.isCanceled()) {
            System.exit(0);
            return;
        }

        Properties properties = setupUI.getProperties();
        new SpringApplicationBuilder(PS4RemotePKGInstallerApplication.class)
                .headless(false)
                .properties(properties)
                .listeners(event -> {
                    if (event instanceof ApplicationFailedEvent) {
                        Throwable exception = ((ApplicationFailedEvent) event).getException();
                        String message = "Error starting " + ((ApplicationFailedEvent) event).getApplicationContext().getEnvironment().getProperty("project.name");
                        log.error(message, exception);
                        JOptionPane.showMessageDialog(null, message + System.lineSeparator() + exception.getMessage(), message, JOptionPane.ERROR_MESSAGE);
                    } else if (event instanceof ApplicationStartedEvent) {
                        Settings settings = ((ApplicationStartedEvent) event).getApplicationContext().getBean(Settings.class);
                        settings.putAll(properties);

                        MainUI mainUI = ((ApplicationStartedEvent) event).getApplicationContext().getBean(MainUI.class);
                        SwingUtilities.invokeLater(() -> {
                            mainUI.init();
                            mainUI.setSize(600, 480);
                            mainUI.setLocationRelativeTo(null);
                            mainUI.setVisible(true);
                        });
                    }
                })
                .run(args);
    }

}
