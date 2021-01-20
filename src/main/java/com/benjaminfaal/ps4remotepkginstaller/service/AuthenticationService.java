package com.benjaminfaal.ps4remotepkginstaller.service;

import com.benjaminfaal.ps4remotecontrol.ddp.model.Console;

public interface AuthenticationService {

    boolean isAuthenticated(Console console);

    String authenticate(Console console);

    void deAuthenticate(Console console);

}
