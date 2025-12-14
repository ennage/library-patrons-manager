module application {
    requires java.sql;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.base;
    
    opens application to javafx.fxml;
    opens handlers to javafx.fxml;
    opens library.models to javafx.base;
    
    exports application;
}
