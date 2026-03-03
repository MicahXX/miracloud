module org.miracloud.frontend {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens org.miracloud.frontend to javafx.fxml;
    exports org.miracloud.frontend;
}