package org.miracloud.frontend.controller;

import javafx.stage.Stage;
import org.miracloud.frontend.AppState;
import org.miracloud.frontend.views.signupView;

public class loginController {
    public void toSignup() {
        AppState.navigateTo("signup");
    }
}