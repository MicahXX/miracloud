package org.miracloud.frontend.controller;

import org.miracloud.frontend.views.loginView;

public class signupController {

    public void handleSignup(String email, String username, String password){
        System.out.println("email: " + email + "\nusername: " + username + "\npassword: " + password);

        // todo: check if checkbox is checked
        // todo: hash password here
        // todo: send it as json to backend
    }

    public void toLogin() {
        new loginView().show();
    }
}
