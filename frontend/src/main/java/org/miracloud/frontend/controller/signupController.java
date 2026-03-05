package org.miracloud.frontend.controller;

import org.miracloud.frontend.views.loginView;

public class signupController {

    public String handleSignup(String email, String username, String password, Boolean isChecked){
        System.out.println("email: " + email + "\nusername: " + username + "\npassword: " + password + "\nisChecked: " + isChecked);

        if(!isChecked){
            return "PLEASE ACCEPT OUR PRIVACY POLICY AND COOKIES";
        }

        return "";
        // todo: hash password here
        // todo: send it as json to backend
    }

    public void toLogin() {
        new loginView().show();
    }
}
