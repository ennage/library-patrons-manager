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
import library.models.Category;
import library.utilities.CategoryDAO;

public class CategoryController {
    
    // -------------------------------------------
    // 1. FXML COMPONENT INJECTIONS (The VIEW)
    // -------------------------------------------
    @FXML private TableView<Category> categoryTable;
    @FXML private TableColumn<Category, String> categoryIDColumn;
    @FXML private TableColumn<Category, String> nameColumn;
    @FXML private TextField categoryIDField;
    @FXML private TextField nameField;
    @FXML private Button saveCategoryButton;
    @FXML private Button deleteCategoryButton;

    // -------------------------------------------
    // 2. DATA LAYER INSTANCE AND STATE
    // -------------------------------------------
    private CategoryDAO categoryDAO = new CategoryDAO();
    private ObservableList<Category> categoryList;
    private Category selectedCategory; // Tracks the category selected in the table

    // -------------------------------------------
    // 3. INITIALIZATION METHOD
    // -------------------------------------------
    @FXML
    public void initialize() {
        // --- Configure Table Columns ---
        // Maps the column to the fields in the Category model (categoryId and categoryName)
        categoryIDColumn.setCellValueFactory(new PropertyValueFactory<>("categoryID"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        
        // --- Add Listener for Table Selection ---
        categoryTable.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                showCategoryDetails(newValue);
                
                // FIX: Disable the Delete button if no row is selected
                deleteCategoryButton.setDisable(newValue == null); 
            });

        // FIX: Set initial state—Delete button must be disabled when the app first starts.
        deleteCategoryButton.setDisable(true); 

        // --- Load Data on startup ---
        loadCategories();
    }
    
    /**
     * Retrieves all category records from the DAO and populates the TableView.
     */
    private void loadCategories() {
        try {
            List<Category> categories = categoryDAO.readAllCategories();
            categoryList = FXCollections.observableArrayList(categories);
            categoryTable.setItems(categoryList);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load categories.");
            e.printStackTrace();
        }
    }
    
    /**
     * Populates the input field when a category is selected in the table.
     */
    private void showCategoryDetails(Category category) {
        if (category != null) {
            selectedCategory = category;
            categoryIDField.setText(category.getCategoryID());
            nameField.setText(category.getCategoryName());
            // Disable ID field during UPDATE mode to prevent key changes
            categoryIDField.setDisable(true); 

        } else {
            handleClearFields();
            selectedCategory = null;
            // Enable ID field during CREATE mode
            categoryIDField.setDisable(false); 
        }
        // Update the button text based on selection
        saveCategoryButton.setText(selectedCategory != null ? "Update Category" : "Save New Category");
    }

    // -------------------------------------------
    // 4. EVENT HANDLERS (CRUD Actions)
    // -------------------------------------------
    
    /**
     * Handles both CREATE (New Category) and UPDATE (Existing Category) actions.
     */
    @FXML
    private void handleSaveCategory() {
        String inputID = categoryIDField.getText().trim(); // New line to get ID
        String inputName = nameField.getText().trim();
        
        if (inputName.isEmpty() || (selectedCategory == null && inputID.isEmpty())) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Category Name and ID cannot be empty.");
            return;
        }

        try {
            if (selectedCategory != null) {
                // --- A. UPDATE EXISTING CATEGORY ---
                // ID is disabled, only Name is updated
                selectedCategory.setCategoryName(inputName);
                
                categoryDAO.updateCategory(selectedCategory);
                categoryTable.refresh();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Category updated successfully.");

            } else {
                // --- B. CREATE NEW CATEGORY ---
                // This now relies on the user-provided ID
                Category newCategory = new Category(inputID, inputName); // Assuming your model allows this constructor

                // NOTE: Your DAO must now handle the user-provided ID and check for duplicates.
                Category savedCategory = categoryDAO.createCategory(newCategory); // Assuming a new DAO method
                
                // Add the new category to the UI list
                categoryList.add(savedCategory); 
                showAlert(Alert.AlertType.INFORMATION, "Success", "New Category created successfully.");
            }
            handleClearFields(); 

        } catch (SQLException e) {
            String errorMsg = "Failed to save category. Check logs.";
            // Check for Duplicate Entry error (ID or Name)
            if (e.getSQLState().startsWith("23")) {
                errorMsg = "Database Error: The Category ID or Name is already in use.";
            }
            showAlert(Alert.AlertType.ERROR, "Database Error", errorMsg);
            e.printStackTrace();
        }
    }

    /**
     * Deletes the currently selected category from the table and database.
     */
    @FXML
    private void handleDeleteCategory() {
        Category categoryToDelete = categoryTable.getSelectionModel().getSelectedItem();

        if (categoryToDelete == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a category to delete.");
            return;
        }
        
        try {
            categoryDAO.deleteCategory(categoryToDelete.getCategoryID());
            categoryList.remove(categoryToDelete); // Remove from ObservableList
            handleClearFields();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Category deleted successfully.");

        } catch (SQLException e) {
            // Handle Foreign Key Constraint violation (code 23)
            if (e.getSQLState().startsWith("23")) { 
                showAlert(Alert.AlertType.ERROR, "Deletion Error", 
                            "Cannot delete category. Books are currently assigned to this category.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to delete category. Check logs.");
            }
            e.printStackTrace();
        }
    }
    
    /**
     * Clears the input field and resets the controller state.
     */
    @FXML
    private void handleClearFields() {
        categoryIDField.clear();
        nameField.clear();
        categoryTable.getSelectionModel().clearSelection();
        selectedCategory = null; 
        
        categoryIDField.setDisable(false); 
        saveCategoryButton.setText("Save New Category");
    }

    /**
     * Helper method to display a JavaFX Alert to the user.
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}