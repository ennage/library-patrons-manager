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
import javafx.scene.control.cell.PropertyValueFactory; // Assuming your DAO is in library.utilities
import library.models.Patron;
import library.utilities.PatronDAO;

public class PatronController {
    
    // -------------------------------------------
    // 1. FXML COMPONENT INJECTIONS (The VIEW)
    // -------------------------------------------
    // The fx:id from PatronManagerView.fxml must match these variable names
    @FXML private TableView<Patron> patronTable;
    
    // Columns (used for mapping data to the table)
    @FXML private TableColumn<Patron, String> patronIDColumn;
    @FXML private TableColumn<Patron, String> firstNameColumn;
    @FXML private TableColumn<Patron, String> lastNameColumn;
    @FXML private TableColumn<Patron, String> emailColumn;
    @FXML private TableColumn<Patron, String> phoneColumn;
    @FXML private TableColumn<Patron, String> addressColumn;

    // Input Fields (used for Create/Update operations)
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField addressField;
    @FXML private Button savePatronButton;
    @FXML private Button deletePatronButton;

    // -------------------------------------------
    // 2. DATA LAYER INSTANCE (The MODEL)
    // -------------------------------------------
    private PatronDAO patronDAO = new PatronDAO();
    private ObservableList<Patron> patronList; // Holds data for the TableView
    private Patron selectedPatron; // Tracks the patron selected in the table for editing/deletion


    // -------------------------------------------
    // 3. INITIALIZATION METHOD (Runs once on load)
    // -------------------------------------------
    @FXML
    public void initialize() {
        // --- Configure Table Columns ---
        patronIDColumn.setCellValueFactory(new PropertyValueFactory<>("patronID")); 
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));

        // --- Add Selection Listener for Details AND Button State ---
    patronTable.getSelectionModel().selectedItemProperty().addListener(
        (observable, oldValue, newValue) -> {
            showPatronDetails(newValue);
            
            // FIX: Disable button if no row is selected
            deletePatronButton.setDisable(newValue == null); 
        });
        deletePatronButton.setDisable(true);
        loadPatrons();
    }

    private void showPatronDetails(Patron patron) {
        if (patron != null) {
            selectedPatron = patron; // Track the selected item
            firstNameField.setText(patron.getFirstName());
            lastNameField.setText(patron.getLastName());
            emailField.setText(patron.getEmail());
            phoneField.setText(patron.getPhoneNumber());
            addressField.setText(patron.getAddress());
        } else {
            handleClearFields(); // Clear fields if nothing is selected (or selection is cleared)
            selectedPatron = null;
        }
        savePatronButton.setText(selectedPatron != null ? "Update Patron" : "Save New Patron");
    }
    
    /**
     * Calls the DAO to retrieve all patron records and populates the TableView.
     */
    private void loadPatrons() {
        try {
            // 1. Call the DAO method (Read operation)
            List<Patron> patrons = patronDAO.readAllPatrons();
            
            // 2. Convert List to JavaFX ObservableList
            patronList = FXCollections.observableArrayList(patrons);
            
            // 3. Set the data source for the TableView
            patronTable.setItems(patronList);
            
        } catch (SQLException e) {
            // In a real application, you would show a JavaFX Alert here
            System.err.println("Error: Failed to load patrons from database.");
            e.printStackTrace();
        }
    }

    
    // -------------------------------------------
    // 4. EVENT HANDLERS (CRUD & UI Actions)
    // -------------------------------------------
    
    // --- CREATE Action (Linked to Save button onAction) ---
    @FXML
    private void handleSavePatron() {
        // 1. Validate input (basic check)
        if (firstNameField.getText().trim().isEmpty() || lastNameField.getText().trim().isEmpty()) {
            System.err.println("Validation Error: First Name and Last Name cannot be empty.");
            showAlert(Alert.AlertType.WARNING, "Validation Error", "First Name and Last Name cannot be empty.");
            return; 
        }

        try {
            if (selectedPatron != null) {
                // --- A. UPDATE EXISTING PATRON ---
                selectedPatron.setFirstName(firstNameField.getText());
                selectedPatron.setLastName(lastNameField.getText());
                selectedPatron.setEmail(emailField.getText());
                selectedPatron.setPhoneNumber(phoneField.getText());
                selectedPatron.setAddress(addressField.getText());

                patronDAO.updatePatron(selectedPatron);
                patronTable.refresh();
                // Refresh TableView item (necessary for immediate UI update)

            } else {
                // --- B. CREATE NEW PATRON ---
                // Note: Assuming Patron constructor/setter handles auto-generated ID from DAO 
                // OR you pass a placeholder ID and the DAO returns the generated ID.
                // For simplicity, we assume the DAO method handles ID generation.
                Patron newPatron = new Patron(
                    "", // Placeholder for ID if needed, otherwise rely on DAO to generate
                    firstNameField.getText(),
                    lastNameField.getText(),
                    emailField.getText(),
                    phoneField.getText(),
                    addressField.getText()
                );

                // Call DAO to save and get the Patron object back with its generated ID
                Patron savedPatron = patronDAO.createPatron(newPatron); 
                
                // Add the new patron to the ObservableList to update the table UI
                patronList.add(savedPatron); 
            }
            handleClearFields(); 
            // Clear fields after successful operation
        } catch (SQLException e) {
            System.err.println("Error: Failed to save/update patron.");
            e.printStackTrace();
        }
    }

    // --- DELETE Action (Linked to Delete button onAction) ---
    @FXML
    private void handleDeletePatron() {
        Patron patronToDelete = patronTable.getSelectionModel().getSelectedItem();

        if (patronToDelete == null) {
            // Validation check for selection (different from input field validation)
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a patron to delete.");
            return;
        }
        
        // You could add a confirmation alert here (e.g., "Are you sure?")
        
        try {
            // 1. Call DAO to delete from the database
            patronDAO.deletePatron(patronToDelete.getPatronID());
            
            // 2. Remove from the ObservableList to update the table UI
            patronList.remove(patronToDelete);
            
            // 3. Clear fields
            handleClearFields();
            
        } catch (SQLException e) {
            // Check for specific foreign key error and display a custom message
            if (e.getSQLState().startsWith("23")) { 
                showAlert(Alert.AlertType.ERROR, "Deletion Error", 
                        "Cannot delete patron. They have outstanding transactions or linked data.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to delete patron. Check logs for details.");
            }
            e.printStackTrace(); // Log the full error
        }
    }


    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // --- CLEAR Fields Action (Linked to Clear button onAction) ---
    @FXML
    private void handleClearFields() {
        firstNameField.clear();
        lastNameField.clear();
        emailField.clear();
        phoneField.clear();
        addressField.clear();
        // Crucial: Deselect the table item and reset state
        patronTable.getSelectionModel().clearSelection();
        selectedPatron = null; 
    }
    
    // Other handlers (handleEditSelection, handleDeleteSelection, etc.) go here...
}