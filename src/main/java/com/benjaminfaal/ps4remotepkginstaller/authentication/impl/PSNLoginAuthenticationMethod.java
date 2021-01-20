package com.benjaminfaal.ps4remotepkginstaller.authentication.impl;

import com.benjaminfaal.ps4remotepkginstaller.authentication.AuthenticationMethod;
import com.benjaminfaal.ps4remotecontrol.util.UserCredentialUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;

@ConditionalOnWebApplication
public abstract class PSNLoginAuthenticationMethod extends AuthenticationMethod {

    private static final Duration TIMEOUT = Duration.ofMillis(5000);

    @Autowired
    private ServerProperties serverProperties;

    @Autowired
    private OAuth2ClientProperties oAuth2ClientProperties;

    private final Object credentialLock = new Object();

    private String credential;

    @EventListener
    public void handleAuthenticationSuccessEvent(InteractiveAuthenticationSuccessEvent event) {
        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) event.getAuthentication();
        String credential = token.getPrincipal().getAttribute(oAuth2ClientProperties.getProvider().get("psn").getUserNameAttribute());
        if (credential != null && credential.length() == UserCredentialUtil.HASH_LENGTH) {
            this.credential = credential;
            synchronized (credentialLock) {
                credentialLock.notifyAll();
            }
        }
    }

    protected abstract boolean authenticateWithPSN() throws Exception;

    public final String getAuthorizationUrl() {
        return UriComponentsBuilder.newInstance()
                .scheme("http")
                .host(serverProperties.getAddress().getHostAddress())
                .port(serverProperties.getPort())
                .path("/oauth2/authorization/psn")
                .toUriString();
    }

    public final String getLoginUrl(String redirectUriWithCode) {
        return UriComponentsBuilder.fromHttpUrl(redirectUriWithCode)
                .scheme("http")
                .host(serverProperties.getAddress().getHostAddress())
                .port(serverProperties.getPort())
                .replacePath("/login/oauth2/code/psn")
                .toUriString();
    }

    public final String getRedirectUri() {
        return oAuth2ClientProperties.getRegistration().get("psn").getRedirectUri();
    }

    public final boolean isValidRedirectUriWithCode(String redirectUriWithCode) {
        return redirectUriWithCode.contains("code=") && redirectUriWithCode.startsWith(getRedirectUri());
    }

    @Override
    public final String authenticate() throws Exception {
        credential = null;

        if (authenticateWithPSN()) {
            if (credential == null) {
                synchronized (credentialLock) {
                    credentialLock.wait(TIMEOUT.toMillis());
                    if (credential == null) {
                        throw new IllegalStateException("Logging in took longer than " + TIMEOUT.getSeconds() + " seconds");
                    }
                }
            }
            return credential;
        }

        throw new IllegalStateException(toString() + " failed");
    }

    @Override
    public String toString() {
        return oAuth2ClientProperties.getRegistration().get("psn").getClientName();
    }
}
