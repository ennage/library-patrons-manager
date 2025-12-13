package library.models;

public class Transaction {
    // Essentials for data access and JavaFX TableView
    // Fields correspond to the 'transactions' table columns
    private String transactionID;
    private String bookID;
    private String patronID;
    private String dateBorrowed;
    private String dueDate;
    private String dateReturned;

    // --- Constructor ---
    // A convenient constructor to create a Patron object when reading from the database
    public Transaction(String transactionID, String bookID, String patronID, String dateBorrowed, String dueDate, String dateReturned) {
        this.transactionID = transactionID;
        this.bookID = bookID;
        this.patronID = patronID;
        this.dateBorrowed = dateBorrowed;
        this.dueDate = dueDate;
        this.dateReturned = dateReturned;
    }

    // ----------------------
    // --- Getters (Read) ---
    // ----------------------
    public String getTransactionID() {
        return transactionID;
    }
    public String getBookID() {
        return bookID;
    }
    public String getPatronID() {
        return patronID;
    }
    public String getDateBorrowed() {
        return dateBorrowed;
    }
    public String getDueDate() {
        return dueDate;
    }
    public String getDateReturned() {
        return dateReturned;
    }

    // ----------------------
    // --- Setters (Write) --
    // ----------------------
    public void setTransactionId(String transactionID) {
        this.transactionID = transactionID;
    }
    public void setBookId(String bookID) {
        this.bookID = bookID;
    }
    public void setPatronId(String patronID) {
        this.patronID = patronID;
    }
    public void setDateBorrowed(String dateBorrowed) {
        this.dateBorrowed = dateBorrowed;
    }
    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }
    public void setDateReturned(String dateReturned) {
        this.dateReturned = dateReturned;
    }
}