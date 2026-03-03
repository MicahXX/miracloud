module org.miracloud.frontend {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.miracloud.frontend to javafx.fxml;
    exports org.miracloud.frontend;
}