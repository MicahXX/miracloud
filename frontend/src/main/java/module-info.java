module org.miracloud.frontend {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires java.net.http;
    requires com.gluonhq.attach.util;
    requires java.prefs;
    requires com.fasterxml.jackson.databind;

    opens org.miracloud.frontend to javafx.fxml;
    exports org.miracloud.frontend;
    exports org.miracloud.frontend.views;
    opens org.miracloud.frontend.views to javafx.fxml;
}