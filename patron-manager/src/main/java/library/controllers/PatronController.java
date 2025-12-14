package library.controllers;

import java.sql.SQLException;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import library.models.Patron;
import library.utilities.PatronDAO;

public class PatronController {

    // -------------------------------------------
    // 1. FXML COMPONENT INJECTIONS
    // -------------------------------------------
    @FXML private TableView<Patron> patronTable;
    @FXML private TableColumn<Patron, String> patronIDColumn;
    @FXML private TableColumn<Patron, String> firstNameColumn;
    @FXML private TableColumn<Patron, String> lastNameColumn;
    @FXML private TableColumn<Patron, String> emailColumn;
    @FXML private TableColumn<Patron, String> phoneColumn;
    @FXML private TableColumn<Patron, String> addressColumn;
    
    @FXML private TextField patronIDField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField addressField;
    @FXML private Button savePatronButton;
    @FXML private Button deletePatronButton;

    // -------------------------------------------
    // 2. DATA LAYER INSTANCE AND STATE
    // -------------------------------------------
    private final PatronDAO patronDAO = new PatronDAO();
    private ObservableList<Patron> patronList;
    private Patron selectedPatron; 

    // -------------------------------------------
    // 3. INITIALIZATION METHOD
    // -------------------------------------------
    @FXML
    public void initialize() {
        // Configure Table Columns (All PropertyValueFactory names match the Patron model's getters)
        patronIDColumn.setCellValueFactory(new PropertyValueFactory<>("patronID"));
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone")); // FIX: Using "phone"
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        
        // Add Selection Listener for Details AND Button State
        patronTable.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                showPatronDetails(newValue);
                deletePatronButton.setDisable(newValue == null);
            });

        // Set initial state
        deletePatronButton.setDisable(true); 

        loadPatrons();
    }
    
    private void loadPatrons() {
        try {
            List<Patron> patrons = patronDAO.readAllPatrons();
            patronList = FXCollections.observableArrayList(patrons);
            patronTable.setItems(patronList);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load patrons.");
            e.printStackTrace();
        }
    }
    
    private void showPatronDetails(Patron patron) {
        if (patron != null) {
            selectedPatron = patron;
            patronIDField.setText(patron.getPatronID());
            firstNameField.setText(patron.getFirstName());
            lastNameField.setText(patron.getLastName());
            emailField.setText(patron.getEmail());
            phoneField.setText(patron.getPhone()); // FIX: Using getPhone()
            addressField.setText(patron.getAddress());
        } else {
            handleClearFields();
            selectedPatron = null;
        }
        savePatronButton.setText(selectedPatron != null ? "Update Patron" : "Save New Patron");
    }

    // -------------------------------------------
    // 4. EVENT HANDLERS (CRUD Actions)
    // -------------------------------------------
    
    @FXML
    private void handleSavePatron() {
        // 1. Validation
        if (firstNameField.getText().trim().isEmpty() || lastNameField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "First and Last Name cannot be empty.");
            return;
        }
        
        // 2. Get Data
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();
        String address = addressField.getText();

        try {
            if (selectedPatron != null) {
                // --- A. UPDATE EXISTING PATRON ---
                selectedPatron.setFirstName(firstName);
                selectedPatron.setLastName(lastName);
                selectedPatron.setEmail(email);
                selectedPatron.setPhone(phone); // FIX: Using setPhone()
                selectedPatron.setAddress(address);
                
                patronDAO.updatePatron(selectedPatron);
                patronTable.refresh();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Patron updated successfully.");

            } else {
                // --- B. CREATE NEW PATRON (selectedPatron is null) ---
                Patron newPatron = new Patron(
                    "", // ID placeholder
                    firstName, 
                    lastName, 
                    email, 
                    phone, // FIX: Matches constructor
                    address
                );
                
                Patron savedPatron = patronDAO.createPatron(newPatron); 
                patronList.add(savedPatron); 
                showAlert(Alert.AlertType.INFORMATION, "Success", "New Patron created successfully.");
            }
            
            // FIX: Ensure clear is called for a guaranteed state reset after success
            handleClearFields(); 

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to save patron. Check logs.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeletePatron() {
        Patron patronToDelete = patronTable.getSelectionModel().getSelectedItem();

        if (patronToDelete == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a patron to delete.");
            return;
        }
        
        try {
            patronDAO.deletePatron(patronToDelete.getPatronID());
            patronList.remove(patronToDelete);
            handleClearFields();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Patron deleted successfully.");
        } catch (SQLException e) {
            if (e.getSQLState() != null && e.getSQLState().startsWith("23")) { 
                showAlert(Alert.AlertType.ERROR, "Deletion Error", "Cannot delete patron. There are transactions linked to this patron.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to delete patron. Check logs.");
            }
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleClearFields() {
        patronIDField.clear();
        firstNameField.clear();
        lastNameField.clear();
        emailField.clear();
        phoneField.clear();
        addressField.clear();
        
        patronTable.getSelectionModel().clearSelection();
        selectedPatron = null; 
        savePatronButton.setText("Save New Patron"); 
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}