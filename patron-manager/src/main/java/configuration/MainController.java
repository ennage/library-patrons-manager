package configuration;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Tab;

public class MainController {

    // 1. FXML COMPONENT INJECTIONS (These fx:id's must match the Tab definitions in MainApplication.fxml)
    @FXML private Tab transactionTab;
    @FXML private Tab patronTab;
    @FXML private Tab bookTab;
    @FXML private Tab categoryTab;

    // 2. INITIALIZATION METHOD (Runs automatically after FXML is loaded)
    @FXML
    public void initialize() {
        // Since the FXML is using <fx:include>, this method is now empty.
        // If we were loading dynamically, the code would go here.
    }

    // 3. MENU BAR ACTION HANDLER (Handles the 'Exit' menu item action.)
    @FXML
    private void handleExit() {
        Platform.exit(); // Shuts down the JavaFX application
    }
    
    // 4. HELPER METHOD (Displays a JavaFX error alert to the user.)
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Handler for the global refresh button (from MainApplication.fxml).
     */
    @FXML
    private void handleGlobalRefresh() {
        GlobalEventManager.getInstance().triggerRefresh();
    }
}