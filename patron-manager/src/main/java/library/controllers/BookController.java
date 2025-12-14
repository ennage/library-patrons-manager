package library.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import library.models.Book;
import library.models.Category;
import library.utilities.BookDAO;
import library.utilities.CategoryDAO;

import java.sql.SQLException;
import java.util.List;

public class BookController {

    // -------------------------------------------
    // 1. FXML COMPONENT INJECTIONS (The VIEW)
    // -------------------------------------------
    // fx:id must match the elements in your BookManagerView.fxml
    @FXML private TableView<Book> bookTable;
    @FXML private TableColumn<Book, String> bookIDColumn;
    @FXML private TableColumn<Book, String> titleColumn;
    @FXML private TableColumn<Book, String> authorColumn;
    @FXML private TableColumn<Book, Integer> publicationYearColumn; // NEW: Added publication year column
    @FXML private TableColumn<Book, String> categoryNameColumn;
    
    @FXML private TextField titleField;
    @FXML private TextField authorField;
    @FXML private TextField isbnField;
    @FXML private TextField publicationYearField; // NEW: Added publication year input field
    @FXML private ComboBox<Category> categoryComboBox; 
    // Assuming you have Save, Update, and Delete buttons linked via onAction attributes

    // -------------------------------------------
    // 2. DATA LAYER INSTANCE AND STATE
    // -------------------------------------------
    private BookDAO bookDAO = new BookDAO();
    private CategoryDAO categoryDAO = new CategoryDAO();
    private ObservableList<Book> bookList;
    private ObservableList<Category> categoryList;
    private Book selectedBook; 

    // -------------------------------------------
    // 3. INITIALIZATION METHOD
    // -------------------------------------------
    @FXML
    public void initialize() {
        // --- Configure Table Columns (Property names match the Book model getters) ---
        bookIDColumn.setCellValueFactory(new PropertyValueFactory<>("bookID"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        publicationYearColumn.setCellValueFactory(new PropertyValueFactory<>("publicationYear")); // NEW
        categoryNameColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName")); // Requires getCategoryName() in Book model

        // --- Add Listener for Table Selection ---
        bookTable.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> showBookDetails(newValue));

        // --- Load Data on startup ---
        loadCategories(); // Must load categories first for the ComboBox
        loadBooks();
    }
    
    /**
     * Retrieves all categories and populates the ComboBox.
     */
    private void loadCategories() {
        try {
            List<Category> categories = categoryDAO.readAllCategories();
            categoryList = FXCollections.observableArrayList(categories);
            categoryComboBox.setItems(categoryList);
            
            // Set the converter to display Category Name in the ComboBox
            categoryComboBox.setConverter(new javafx.util.StringConverter<Category>() {
                @Override
                public String toString(Category category) {
                    return category != null ? category.getCategoryName() : "";
                }
                @Override
                public Category fromString(String string) {
                    return null;
                }
            });
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load categories for selection.");
            e.printStackTrace();
        }
    }
    
    /**
     * Retrieves all book records from the DAO and populates the TableView.
     * NOTE: This assumes BookDAO.readAllBooks() performs a JOIN to fetch CategoryName.
     */
    private void loadBooks() {
        try {
            List<Book> books = bookDAO.readAllBooks();
            bookList = FXCollections.observableArrayList(books);
            bookTable.setItems(bookList);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load books.");
            e.printStackTrace();
        }
    }
    
    /**
     * Populates the input fields when a book is selected in the table.
     */
    private void showBookDetails(Book book) {
        if (book != null) {
            selectedBook = book;
            titleField.setText(book.getTitle());
            authorField.setText(book.getAuthor());
            isbnField.setText(book.getISBN());
            publicationYearField.setText(String.valueOf(book.getPublicationYear())); // NEW
            
            // Find and select the corresponding Category object in the ComboBox
            categoryComboBox.getSelectionModel().select(
                categoryList.stream()
                            .filter(c -> c.getCategoryID().equals(book.getCategoryID()))
                            .findFirst()
                            .orElse(null)
            );
            
        } else {
            handleClearFields();
            selectedBook = null;
        }
    }

    // -------------------------------------------
    // 4. EVENT HANDLERS (CRUD Actions)
    // -------------------------------------------
    
    /**
     * Handles both CREATE and UPDATE actions (linked to a 'Save' button).
     */
    @FXML
    private void handleSaveBook() {
        // 1. Basic Validation
        if (titleField.getText().trim().isEmpty() || authorField.getText().trim().isEmpty() || categoryComboBox.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Title, Author, and Category cannot be empty.");
            return;
        }
        
        // 2. Get Data and Convert Types
        String title = titleField.getText();
        String author = authorField.getText();
        String isbn = isbnField.getText();
        Category selectedCategory = categoryComboBox.getValue();
        int pubYear;
        
        try {
            pubYear = Integer.parseInt(publicationYearField.getText()); // Parse Publication Year
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Publication Year must be a valid number.");
            return;
        }

        try {
            if (selectedBook != null) {
                // --- A. UPDATE EXISTING BOOK ---
                selectedBook.setTitle(title);
                selectedBook.setAuthor(author);
                selectedBook.setISBN(isbn);
                selectedBook.setPublicationYear(pubYear); // NEW
                selectedBook.setCategoryID(selectedCategory.getCategoryID());
                
                // Manually update the name for immediate UI refresh
                selectedBook.setCategoryName(selectedCategory.getCategoryName());
                
                bookDAO.updateBook(selectedBook);
                bookTable.refresh();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Book updated successfully.");

            } else {
                // --- B. CREATE NEW BOOK ---
                Book newBook = new Book(
                    "", // ID placeholder, DAO will generate
                    title, 
                    author, 
                    isbn, 
                    pubYear, // NEW
                    selectedCategory.getCategoryID() 
                );
                
                Book savedBook = bookDAO.createBook(newBook); 
                
                // CRITICAL FIX: Manually set the category name on the returned object 
                // for the TableView display
                savedBook.setCategoryName(selectedCategory.getCategoryName());
                
                bookList.add(savedBook); 
                showAlert(Alert.AlertType.INFORMATION, "Success", "New Book created successfully.");
            }
            handleClearFields(); 

        } catch (SQLException e) {
            // Handle unique constraints (ISBN) or other DB errors
            if (e.getSQLState() != null && e.getSQLState().startsWith("23")) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "The ISBN is already in use.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to save book. Check logs.");
            }
            e.printStackTrace();
        }
    }

    /**
     * Deletes the currently selected book.
     */
    @FXML
    private void handleDeleteBook() {
        Book bookToDelete = bookTable.getSelectionModel().getSelectedItem();

        if (bookToDelete == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a book to delete.");
            return;
        }
        
        try {
            bookDAO.deleteBook(bookToDelete.getBookID());
            bookList.remove(bookToDelete);
            handleClearFields();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Book deleted successfully.");

        } catch (SQLException e) {
            // Check for Foreign Key Constraint violation (if the book is currently borrowed/in a transaction)
            if (e.getSQLState() != null && e.getSQLState().startsWith("23")) { 
                showAlert(Alert.AlertType.ERROR, "Deletion Error", 
                            "Cannot delete book. There are currently transactions linked to this book.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to delete book. Check logs.");
            }
            e.printStackTrace();
        }
    }
    
    /**
     * Clears all input fields and resets the controller state.
     */
    @FXML
    private void handleClearFields() {
        titleField.clear();
        authorField.clear();
        isbnField.clear();
        publicationYearField.clear(); 
        categoryComboBox.getSelectionModel().clearSelection();
        
        bookTable.getSelectionModel().clearSelection();
        selectedBook = null; 
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