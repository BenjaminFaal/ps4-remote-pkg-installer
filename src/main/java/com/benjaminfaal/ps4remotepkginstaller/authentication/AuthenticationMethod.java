package com.benjaminfaal.ps4remotepkginstaller.authentication;

public abstract class AuthenticationMethod {

    public abstract boolean isSupported();

    public abstract String authenticate() throws Exception;

    @Override
    public abstract String toString();

}
