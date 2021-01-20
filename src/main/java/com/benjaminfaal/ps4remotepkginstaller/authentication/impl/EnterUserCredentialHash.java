package com.benjaminfaal.ps4remotepkginstaller.authentication.impl;

import com.benjaminfaal.ps4remotecontrol.util.UserCredentialUtil;
import org.springframework.stereotype.Component;

@Component
public class EnterUserCredentialHash extends EnterStringAuthenticationMethod {

    @Override
    public String getType() {
        return "PSN user credential hash";
    }

    @Override
    public String getExample() {
        return "143f2dccb2a0d91b5473563cd77a937a18d6b14fe58060a78f95c62da0b488c8";
    }

    @Override
    public int getLength() {
        return UserCredentialUtil.HASH_LENGTH;
    }

}
