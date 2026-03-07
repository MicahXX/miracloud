module org.miracloud.frontend {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires java.net.http;


    opens org.miracloud.frontend to javafx.fxml;
    exports org.miracloud.frontend;
    exports org.miracloud.frontend.views;
    opens org.miracloud.frontend.views to javafx.fxml;
}