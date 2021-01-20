package com.benjaminfaal.ps4remotepkginstaller.service.impl;

import com.benjaminfaal.ps4remotepkginstaller.event.AuthenticatingStatusEvent;
import com.benjaminfaal.ps4remotepkginstaller.service.AuthenticationService;
import com.benjaminfaal.ps4remotecontrol.ddp.model.Console;
import com.benjaminfaal.ps4remotecontrol.util.UserCredentialUtil;
import com.benjaminfaal.ps4remotepkginstaller.Settings;
import com.benjaminfaal.ps4remotepkginstaller.authentication.AuthenticationMethod;
import com.benjaminfaal.ps4remotepkginstaller.authentication.event.AuthenticatedEvent;
import com.benjaminfaal.ps4remotepkginstaller.authentication.event.DeAuthenticatedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.swing.*;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    @Autowired
    private Settings settings;

    @Autowired
    private AuthenticationMethod[] authenticationMethods;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public boolean isAuthenticated(Console console) {
        return settings.getProperty(getKey(console), "").length() == UserCredentialUtil.HASH_LENGTH;
    }

    @Override
    public String authenticate(Console console) {
        final String key = getKey(console);
        if (isAuthenticated(console)) {
            return settings.getProperty(key);
        }

        String message = "Select a method to authenticate with " + console.getUserFriendlyName();
        AuthenticationMethod authenticationMethod = (AuthenticationMethod) JOptionPane.showInputDialog(null, message, "Authenticate", JOptionPane.QUESTION_MESSAGE, null, authenticationMethods, null);
        if (authenticationMethod == null) {
            throw new IllegalStateException("Authentication cancelled");
        }

        if (!authenticationMethod.isSupported()) {
            String unsupportedWarning = "This authentication method is not supported in the current configuration or OS and may not work correctly, do you want to continue?";
            if (JOptionPane.showConfirmDialog(null, unsupportedWarning, "Unsupported authentication method", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
                throw new IllegalStateException("Authentication cancelled due to unsupported authentication method");
            }
        }

        try {
            applicationEventPublisher.publishEvent(new AuthenticatingStatusEvent(true));
            String credential = authenticationMethod.authenticate();
            if (StringUtils.hasText(credential)) {
                if (credential.length() != UserCredentialUtil.HASH_LENGTH) {
                    throw new IllegalArgumentException("Invalid User credential: " + credential + " must be " + UserCredentialUtil.HASH_LENGTH + " characters");
                }
                settings.setProperty(key, credential);
                applicationEventPublisher.publishEvent(new AuthenticatedEvent(console));
                return credential;
            }
            throw new IllegalStateException(authenticationMethod + " failed");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error authenticating with: " + console.getUserFriendlyName() + System.lineSeparator() + e.getMessage(), "Error authenticating", JOptionPane.ERROR_MESSAGE);
            throw new IllegalStateException("Error authenticating with: " + console.getUserFriendlyName() + System.lineSeparator() + e.getMessage(), e);
        } finally {
            applicationEventPublisher.publishEvent(new AuthenticatingStatusEvent(false));
        }
    }

    @Override
    public void deAuthenticate(Console console) {
        settings.remove(getKey(console));
        applicationEventPublisher.publishEvent(new DeAuthenticatedEvent(console));
    }

    private String getKey(Console console){
        return "authentication-" + console.get("host-id");
    }

}
