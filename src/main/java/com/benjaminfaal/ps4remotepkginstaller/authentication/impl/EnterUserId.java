package com.benjaminfaal.ps4remotepkginstaller.authentication.impl;

import com.benjaminfaal.ps4remotecontrol.util.UserCredentialUtil;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(-1)
@Component
public class EnterUserId extends EnterStringAuthenticationMethod {

    @Override
    public int getLength() {
        return UserCredentialUtil.USER_ID_LENGTH;
    }

    @Override
    public String getExample() {
        return "9019256736915509894";
    }

    @Override
    public String getType() {
        return "PSN user ID";
    }

    @Override
    public String convert(String input) {
        return UserCredentialUtil.convertUserIdToCredentialHash(input);
    }

}
