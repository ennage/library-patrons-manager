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
    // 1. FXML COMPONENT INJECTIONS
    // -------------------------------------------
    @FXML private TableView<Book> bookTable;
    @FXML private TableColumn<Book, String> bookIDColumn;
    @FXML private TableColumn<Book, String> titleColumn;
    @FXML private TableColumn<Book, String> authorColumn;
    @FXML private TableColumn<Book, String> isbnColumn;
    @FXML private TableColumn<Book, Integer> publicationYearColumn;
    @FXML private TableColumn<Book, String> categoryNameColumn;
    
    @FXML private TextField titleField;
    @FXML private TextField authorField;
    @FXML private TextField isbnField;
    @FXML private TextField publicationYearField;
    @FXML private ComboBox<Category> categoryComboBox;
    @FXML private Button saveBookButton;
    @FXML private Button deleteBookButton; // Used for disable/enable state

    // -------------------------------------------
    // 2. DATA LAYER INSTANCE AND STATE
    // -------------------------------------------
    private final BookDAO bookDAO = new BookDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private ObservableList<Book> bookList;
    private ObservableList<Category> categoryList;
    private Book selectedBook; 

    // -------------------------------------------
    // 3. INITIALIZATION METHOD
    // -------------------------------------------
    @FXML
    public void initialize() {
        // Configure Table Columns
        bookIDColumn.setCellValueFactory(new PropertyValueFactory<>("bookID"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        isbnColumn.setCellValueFactory(new PropertyValueFactory<>("isbn")); // Assumes getIsbn()
        publicationYearColumn.setCellValueFactory(new PropertyValueFactory<>("publicationYear"));
        categoryNameColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName")); 

        // Add Selection Listener for Details AND Button State
        bookTable.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                showBookDetails(newValue);
                deleteBookButton.setDisable(newValue == null);
            });

        // Set initial state
        deleteBookButton.setDisable(true); 

        loadCategories();
        loadBooks();
    }
    
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
    
    private void showBookDetails(Book book) {
        if (book != null) {
            selectedBook = book;
            titleField.setText(book.getTitle());
            authorField.setText(book.getAuthor());
            isbnField.setText(book.getIsbn());
            publicationYearField.setText(String.valueOf(book.getPublicationYear()));
            
            // Select the Category object
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
        saveBookButton.setText(selectedBook != null ? "Update Book" : "Save New Book");
    }

    // -------------------------------------------
    // 4. EVENT HANDLERS (CRUD Actions)
    // -------------------------------------------
    
    @FXML
    private void handleSaveBook() {
        // 1. Validation
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
            pubYear = Integer.parseInt(publicationYearField.getText());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Publication Year must be a valid number.");
            return;
        }

        try {
            if (selectedBook != null) {
                // --- A. UPDATE EXISTING BOOK ---
                selectedBook.setTitle(title);
                selectedBook.setAuthor(author);
                selectedBook.setIsbn(isbn);
                selectedBook.setPublicationYear(pubYear);
                selectedBook.setCategoryID(selectedCategory.getCategoryID());
                selectedBook.setCategoryName(selectedCategory.getCategoryName());
                
                bookDAO.updateBook(selectedBook);
                bookTable.refresh();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Book updated successfully.");

            } else {
                // --- B. CREATE NEW BOOK (selectedBook is null) ---
                Book newBook = new Book(
                    "", // ID placeholder
                    title, 
                    author, 
                    isbn, 
                    pubYear,
                    selectedCategory.getCategoryID() 
                );
                
                Book savedBook = bookDAO.createBook(newBook); 
                // Set the display name for the UI refresh
                savedBook.setCategoryName(selectedCategory.getCategoryName());
                
                bookList.add(savedBook); 
                showAlert(Alert.AlertType.INFORMATION, "Success", "New Book created successfully.");
            }
            
            // FIX: Ensure clear is called for a guaranteed state reset after success
            handleClearFields(); 

        } catch (SQLException e) {
            if (e.getSQLState() != null && e.getSQLState().startsWith("23")) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "The ISBN is already in use.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to save book. Check logs.");
            }
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteBook() {
        // ... (Deletion logic is assumed correct) ...
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
            if (e.getSQLState() != null && e.getSQLState().startsWith("23")) { 
                showAlert(Alert.AlertType.ERROR, "Deletion Error", "Cannot delete book. It is referenced in a transaction record.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to delete book. Check logs.");
            }
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleClearFields() {
        titleField.clear();
        authorField.clear();
        isbnField.clear();
        publicationYearField.clear(); 
        categoryComboBox.getSelectionModel().clearSelection();
        
        bookTable.getSelectionModel().clearSelection();
        selectedBook = null; 
        saveBookButton.setText("Save New Book"); // Explicitly reset button text
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}