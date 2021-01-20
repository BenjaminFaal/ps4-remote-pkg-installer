package com.benjaminfaal.ps4remotepkginstaller.authentication.impl;

import com.benjaminfaal.jcef.loader.JCefLoader;
import com.benjaminfaal.ps4remotepkginstaller.util.Utils;
import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.CefSettings;
import org.cef.OS;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.handler.CefLoadHandlerAdapter;
import org.cef.network.CefRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

@Order(-5)
@Component
@ConditionalOnWebApplication
public class LoginWithPSN extends PSNLoginAuthenticationMethod {

    private CefApp app;

    @Override
    public boolean isSupported() {
        return OS.isWindows() || OS.isLinux();
    }

    @Override
    protected boolean authenticateWithPSN() throws Exception {
        if (app == null) {
            final Path jcefPath = Utils.getWorkingDirectory().resolve("JCEF");

            if (!Files.exists(jcefPath.resolve(JCefLoader.VERSION).resolve(".installed"))) {
                JOptionPane.showMessageDialog(null, "It will take some time to download (~80MB) and install JCEF, please wait until you see the PSN login page.");
            }

            CefSettings settings = new CefSettings();
            settings.locale = Locale.getDefault().toString();
            settings.windowless_rendering_enabled = false;
            settings.persist_session_cookies = true;
            settings.cache_path = jcefPath.resolve("cache").toString();

            app = JCefLoader.installAndLoad(jcefPath, settings);
        }
        CefClient client = app.createClient();
        CefBrowser browser = client.createBrowser(getAuthorizationUrl(), false, false);

        JDialog dialog = new JDialog((Frame) null, toString(), true);
        dialog.setSize(800, 800);
        dialog.setLocationRelativeTo(null);
        dialog.add(browser.getUIComponent(), BorderLayout.CENTER);

        AtomicBoolean authenticated = new AtomicBoolean();
        client.addLoadHandler(new CefLoadHandlerAdapter() {
            @Override
            public void onLoadStart(CefBrowser browser, CefFrame frame, CefRequest.TransitionType transitionType) {
                String url = frame.getURL();
                if (isValidRedirectUriWithCode(url)) {
                    authenticated.set(true);
                    browser.loadURL(getLoginUrl(url));
                    dialog.setVisible(false);
                }
            }
        });

        dialog.setVisible(true);

        client.dispose();

        return authenticated.get();
    }

}
