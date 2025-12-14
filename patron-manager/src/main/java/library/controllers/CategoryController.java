package library.controllers;

import java.sql.SQLException;
import java.util.List;

import configuration.GlobalEventManager;
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
    
    // FIX HERE: Renamed 'nameColumn' to 'categoryNameColumn' to match FXML fx:id
    @FXML private TableColumn<Category, String> categoryNameColumn; 
    
    @FXML private TextField categoryIDField;
    @FXML private TextField categoryNameField;
    @FXML private Button saveCategoryButton;
    @FXML private Button deleteCategoryButton;

    // -------------------------------------------
    // 2. DATA LAYER INSTANCE AND STATE
    // -------------------------------------------
    private CategoryDAO categoryDAO = new CategoryDAO();
    private ObservableList<Category> categoryList;
    private Category selectedCategory; 

    // -------------------------------------------
    // 3. INITIALIZATION METHOD
    // -------------------------------------------
    @FXML
    public void initialize() {
        // --- Configure Table Columns ---
        categoryIDColumn.setCellValueFactory(new PropertyValueFactory<>("categoryID"));
        
        // FIX HERE: Used the renamed variable 'categoryNameColumn'
        categoryNameColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        
        // --- Add Listener for Table Selection ---
        categoryTable.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                showCategoryDetails(newValue);
                deleteCategoryButton.setDisable(newValue == null); 
            });

        // Set initial state
        deleteCategoryButton.setDisable(true); 
        categoryIDField.setDisable(false);

        // --- Global Refresh Listener ---
        GlobalEventManager.getInstance().getRefreshSignal().addListener((obs, oldVal, newVal) -> {
            loadCategories();
        });
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
            categoryNameField.setText(category.getCategoryName());
            categoryIDField.setDisable(true); 

        } else {
            handleClearFields();
            selectedCategory = null;
            categoryIDField.setDisable(false); 
        }
        saveCategoryButton.setText(selectedCategory != null ? "Update Category" : "Save New Category");
    }

    // -------------------------------------------
    // 4. EVENT HANDLERS (CRUD Actions)
    // -------------------------------------------
    
    @FXML
    private void handleSaveCategory() {
        String inputID = categoryIDField.getText().trim(); 
        String inputName = categoryNameField.getText().trim();
        
        if (inputName.isEmpty() || (selectedCategory == null && inputID.isEmpty())) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Category Name and ID cannot be empty.");
            return;
        }

        try {
            if (selectedCategory != null) {
                // --- A. UPDATE EXISTING CATEGORY ---
                selectedCategory.setCategoryName(inputName);
                
                categoryDAO.updateCategory(selectedCategory);
                categoryTable.refresh();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Category updated successfully.");

            } else {
                // --- B. CREATE NEW CATEGORY ---
                Category newCategory = new Category(inputID, inputName); 

                Category savedCategory = categoryDAO.createCategory(newCategory);
                
                categoryList.add(savedCategory); 
                showAlert(Alert.AlertType.INFORMATION, "Success", "New Category created successfully.");
            }
            handleClearFields(); 

        } catch (SQLException e) {
            String errorMsg = "Failed to save category. Check logs.";
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
            categoryList.remove(categoryToDelete);
            handleClearFields();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Category deleted successfully.");

        } catch (SQLException e) {
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
        categoryNameField.clear();
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