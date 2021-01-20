package com.benjaminfaal.ps4remotepkginstaller.authentication.impl;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.net.URI;

@Order(-4)
@Component
@ConditionalOnWebApplication
public class LoginWithPSNManually extends PSNLoginAuthenticationMethod {

    @Override
    public boolean isSupported() {
        return Desktop.isDesktopSupported();
    }

    @Override
    public String toString() {
        return super.toString() + " manually";
    }

    @Override
    protected boolean authenticateWithPSN() throws Exception {
        Desktop desktop = Desktop.getDesktop();
        JOptionPane.showMessageDialog(null, "Login with your PSN account and copy and paste the redirect URL from the addressbar.");
        desktop.browse(URI.create(getAuthorizationUrl()));

        String redirectUri = getRedirectUri();
        String redirectUriWithCode = JOptionPane.showInputDialog(null, "Copy and paste the redirect URL from the addressbar.", redirectUri);

        if (redirectUriWithCode != null && isValidRedirectUriWithCode(redirectUriWithCode)) {
            desktop.browse(URI.create(getLoginUrl(redirectUriWithCode)));
            return true;
        }
        return false;
    }

}
