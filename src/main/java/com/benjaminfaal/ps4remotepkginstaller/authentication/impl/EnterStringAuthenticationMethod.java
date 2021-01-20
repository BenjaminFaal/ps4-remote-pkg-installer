package com.benjaminfaal.ps4remotepkginstaller.authentication.impl;

import com.benjaminfaal.ps4remotepkginstaller.authentication.AuthenticationMethod;

import javax.swing.*;

public abstract class EnterStringAuthenticationMethod extends AuthenticationMethod {

    public abstract int getLength();

    public abstract String getType();

    public abstract String getExample();

    public String convert(String input) {
        return input;
    }

    @Override
    public boolean isSupported() {
        return true;
    }

    @Override
    public final String authenticate() throws Exception {
        String input = JOptionPane.showInputDialog("Enter " + getType() + " for example: " + getExample());
        if (input != null) {
            if (input.length() != getLength()) {
                throw new IllegalArgumentException(getType() + " must be " + getLength() + " characters long for example: " + getExample());
            }
            return convert(input);
        }
        return null;
    }

    @Override
    public final String toString() {
        return "Enter " + getType();
    }
}
