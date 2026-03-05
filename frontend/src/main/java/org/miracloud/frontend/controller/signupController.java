package org.miracloud.frontend.controller;

import org.miracloud.frontend.views.loginView;

public class signupController {

    public String handleSignup(String email, String username, String password, Boolean isChecked){
        System.out.println("email: " + email + "\nusername: " + username + "\npassword: " + password + "\nisChecked: " + isChecked);

        String returnString = "";

        // check if inputs are correct
        if(!email.isEmpty()){
            if(!email.contains("@") && !email.contains(".")){
                returnString += "please use a valid email";
            }
        }
        else{
            returnString += "email needs to be filled out \n";
        }

        if (username.isEmpty()){
            returnString += "username needs to be filled out \n";
        }
        if(password.isEmpty()){
            returnString += "password needs to be filled out \n";
        }
        if(!isChecked){
            returnString += "PLEASE ACCEPT OUR PRIVACY POLICY AND COOKIES";
        }

        if(returnString.isEmpty()){
            // todo: hash password here
            // todo: send it as json to backend
            returnString += "signed up";
        }

        return returnString;
    }

    public void toLogin() {
        new loginView().show();
    }
}
