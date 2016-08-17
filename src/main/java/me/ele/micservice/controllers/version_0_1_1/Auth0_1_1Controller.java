package me.ele.micservice.controllers.version_0_1_1;

import me.ele.micservice.controllers.AuthController;

/**
 * Created by alvin on 2015/10/16.
 */
public class Auth0_1_1Controller extends AuthController{
    public void index() {
        renderText("version: 0.1.1");
    }
}
