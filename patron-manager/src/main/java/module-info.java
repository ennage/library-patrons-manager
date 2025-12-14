module application {
    requires java.sql;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.base;
    requires javafx.graphics;
    
    opens application to javafx.fxml;
    opens library.controllers to javafx.fxml;
    opens library.models to javafx.base;
    
    exports application;
}
