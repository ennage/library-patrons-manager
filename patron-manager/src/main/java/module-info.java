module application {
    // 1. MODULE REQUIREMENTS
    requires java.sql;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.base;       // Needed for data binding
    requires javafx.graphics;   // Needed for the Application class
    
    // 2. EXPORTS (Public access to packages)
    exports application;        // Export the main application package to be launched
    exports configuration;
    exports library.controllers;
    exports library.models;
    exports library.utilities;

    // 3. OPENS (Access via reflection, necessary for FXMLLoader and Data Binding)
    opens application to javafx.fxml; 
    opens configuration to javafx.fxml;
    opens library.controllers to javafx.fxml; 
    opens library.models to javafx.base; // Critical for TableView/ComboBox data binding
    opens library.utilities to javafx.base; 
    
}