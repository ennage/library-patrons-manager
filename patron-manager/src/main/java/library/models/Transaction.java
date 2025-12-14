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
    // --- UI FIELDS ---
    private String patronName;
    private String bookTitle;

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
    // --- UI Display ---
    public String getPatronName() {
        return patronName;
    }
    public String getBookTitle() {
        return bookTitle;
    }


    // ----------------------
    // --- Setters (Write) --
    // ----------------------
    public void setTransactionID(String transactionID) { // Corrected method name to match field case
        this.transactionID = transactionID;
    }
    public void setBookID(String bookID) { // Corrected method name to match field case
        this.bookID = bookID;
    }
    public void setPatronID(String patronID) { // Corrected method name to match field case
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
    // --- UI Display ---
    public void setPatronName(String patronName) {
        this.patronName = patronName;
    }
    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }
}