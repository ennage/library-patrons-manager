package configuration;

import javafx.application.Platform;
import javafx.fxml.FXML;

public class MainController {

    @FXML
    public void initialize() {
        // No code needed here, as FXML handles tab content loading.
    }
    
    @FXML
    private void handleExit() {
        Platform.exit();
    }
}