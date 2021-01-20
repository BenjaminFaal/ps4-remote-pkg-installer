package com.benjaminfaal.ps4remotepkginstaller.authentication.impl;

import com.benjaminfaal.ps4remotepkginstaller.authentication.AuthenticationMethod;
import com.benjaminfaal.ps4remotecontrol.ddp.PS4DDP;
import com.benjaminfaal.ps4remotecontrol.ddp.model.DDPRequest;
import com.benjaminfaal.ps4remotecontrol.ddp.model.MessageType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.swing.*;

@Order(-3)
@Component
public class LoginWithPS4SecondScreenApp extends AuthenticationMethod {

    @Value("${project.name}")
    private String projectName;

    @Override
    public boolean isSupported() {
        return PS4DDP.isSimulatingSupported();
    }

    @Override
    public String authenticate() throws Exception {
        String message =  "Start the PS4 Second Screen app and connect with \"" + projectName + "\"";
        if (JOptionPane.showConfirmDialog(null, message, "PS4 Second Screen app", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            DDPRequest wakeUpRequest = PS4DDP.simulateStandby(projectName, request -> null, request -> request.getType() == MessageType.WAKEUP);
            if (wakeUpRequest == null) {
                return null;
            }
            return wakeUpRequest.get("user-credential");
        }
        return null;
    }

    @Override
    public String toString() {
        return "Login with PS4 Second Screen app";
    }
}
