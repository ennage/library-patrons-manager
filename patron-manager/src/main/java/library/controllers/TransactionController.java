package library.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import library.models.*;
import library.utilities.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import configuration.GlobalEventManager;

public class TransactionController {

    // -------------------------------------------
    // 1. FXML COMPONENT INJECTIONS (The VIEW)
    // -------------------------------------------
    @FXML private ComboBox<Patron> patronComboBox;
    @FXML private ComboBox<Book> bookComboBox;
    @FXML private Button borrowButton;
    @FXML private Button returnButton;

    @FXML private TableView<Transaction> transactionTable;
    @FXML private TableColumn<Transaction, String> transactionIDColumn;
    @FXML private TableColumn<Transaction, String> patronNameColumn;
    @FXML private TableColumn<Transaction, String> bookTitleColumn;
    @FXML private TableColumn<Transaction, LocalDate> borrowDateColumn;
    @FXML private TableColumn<Transaction, LocalDate> dueDateColumn;
    
    // -------------------------------------------
    // 2. DATA LAYER INSTANCE AND STATE
    // -------------------------------------------
    private PatronDAO patronDAO = new PatronDAO();
    private BookDAO bookDAO = new BookDAO();
    private TransactionDAO transactionDAO = new TransactionDAO();
    
    private ObservableList<Transaction> outstandingLoansList;
    
    // -------------------------------------------
    // 3. INITIALIZATION METHOD
    // -------------------------------------------
    @FXML
    public void initialize() {
        // --- Configure Table Columns ---
        transactionIDColumn.setCellValueFactory(new PropertyValueFactory<>("transactionID"));
        patronNameColumn.setCellValueFactory(new PropertyValueFactory<>("patronName")); // Requires getPatronName() in Transaction model
        bookTitleColumn.setCellValueFactory(new PropertyValueFactory<>("bookTitle")); // Requires getBookTitle() in Transaction model
        borrowDateColumn.setCellValueFactory(new PropertyValueFactory<>("dateBorrowed"));
        dueDateColumn.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        
        // --- Load Initial Data ---
        loadPatronsAndBooks();
        loadOutstandingLoans();
        
        // --- Selection Listener for Return Button ---
        transactionTable.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                // Enable return button only if a loan is selected
                returnButton.setDisable(newValue == null);
            });
        // --- Global Refresh Listener ---
        GlobalEventManager.getInstance().getRefreshSignal().addListener((obs, oldVal, newVal) -> {
            // Reloads the list of currently loaned books
            loadOutstandingLoans(); 
            // Reloads patrons for the ComboBox
            loadPatronsAndBooks();                     
        });
    }

    /**
     * Loads Patrons and Books and populates their respective ComboBoxes.
     */
    private void loadPatronsAndBooks() {
        try {
            // Load Patrons
            List<Patron> patrons = patronDAO.readAllPatrons();
            patronComboBox.setItems(FXCollections.observableArrayList(patrons));
            patronComboBox.setConverter(new javafx.util.StringConverter<Patron>() {
                @Override public String toString(Patron p) { return p != null ? p.getFirstName() + " " + p.getLastName() : ""; }
                @Override public Patron fromString(String string) { return null; }
            });

            // Load Books
            List<Book> books = bookDAO.readAllBooks();
            bookComboBox.setItems(FXCollections.observableArrayList(books));
            bookComboBox.setConverter(new javafx.util.StringConverter<Book>() {
                @Override public String toString(Book b) { return b != null ? b.getTitle() + " (" + b.getAuthor() + ")" : ""; }
                @Override public Book fromString(String string) { return null; }
            });
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load Patrons or Books for selection.");
            e.printStackTrace();
        }
    }

    /**
     * Loads transactions that do not have a return date (i.e., currently borrowed).
     */
    private void loadOutstandingLoans() {
        try {
            List<Transaction> loans = transactionDAO.readOutstandingLoans(); // Assuming this method exists
            outstandingLoansList = FXCollections.observableArrayList(loans);
            transactionTable.setItems(outstandingLoansList);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load outstanding loans.");
            e.printStackTrace();
        }
    }
    
    // -------------------------------------------
    // 4. EVENT HANDLERS (Borrow/Return Actions)
    // -------------------------------------------
    
    /**
     * Handles the book borrowing process.
     */
    @FXML
    private void handleBorrowBook() {
        Patron selectedPatron = patronComboBox.getValue();
        Book selectedBook = bookComboBox.getValue();
        
        if (selectedPatron == null || selectedBook == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Error", "Please select both a Patron and a Book.");
            return;
        }

        try {
            // 1. Availability Check (Binary: check for outstanding loan for this book ID)
            if (transactionDAO.isBookCurrentlyBorrowed(selectedBook.getBookID())) { // Assuming this method exists
                showAlert(Alert.AlertType.ERROR, "Borrow Error", "This book is currently unavailable (already on loan).");
                return;
            }

            // 2. Perform Borrowing via DAO
            // Assuming business rule: All loans are for 14 days (or another fixed period)
            Transaction newLoan = transactionDAO.borrowBook(
                selectedPatron.getPatronID(), 
                selectedBook.getBookID(), 
                LocalDate.now().plusDays(14) // Due date calculation
            );
            
            // 3. Update UI
            // The DAO returns the full Transaction object (with IDs, dates, etc.)
            
            // Since the Transaction model likely only has PatronID/BookID, we manually set the names 
            // for the TableView display before adding to the list.
            newLoan.setPatronName(selectedPatron.getFirstName() + " " + selectedPatron.getLastName());
            newLoan.setBookTitle(selectedBook.getTitle());

            outstandingLoansList.add(newLoan);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Book successfully borrowed!");
            
            // Clear selections
            patronComboBox.getSelectionModel().clearSelection();
            bookComboBox.getSelectionModel().clearSelection();
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to record loan.");
            e.printStackTrace();
        }
    }

    /**
     * Handles the book return process.
     */
    @FXML
    private void handleReturnBook() {
        Transaction loanToReturn = transactionTable.getSelectionModel().getSelectedItem();
        
        if (loanToReturn == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Error", "Please select a transaction to return.");
            return;
        }

        try {
            // 1. Perform Return via DAO
            transactionDAO.returnBook(loanToReturn.getTransactionID()); // Assuming this updates returnDate in DB
            
            // 2. Update UI
            outstandingLoansList.remove(loanToReturn);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Book successfully returned!");
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to record return.");
            e.printStackTrace();
        }
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