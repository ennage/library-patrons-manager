package application;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane; 
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Load main layout
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/MainApplication.fxml"));
            
            // FIX: The loaded root node must be cast to BorderPane
            BorderPane root = loader.load(); 
            
            Scene scene = new Scene(root, 1000, 700);

            String cssPath = getClass().getResource("/elements/style.css").toExternalForm();
            scene.getStylesheets().add(cssPath);

            primaryStage.setScene(scene);
            primaryStage.setTitle("Library Management System"); 
            primaryStage.show();
        } catch (IOException e) {
            System.err.println("Failed to load FXML: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}