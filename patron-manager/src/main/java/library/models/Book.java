package library.models;

public class Book {
    // Fields
    private String bookID;
    private String title;
    private String author;
    private String isbn; 
    private int publicationYear;
    private String categoryID;
    private String categoryName; // Included for TableView display (joining)

    // --- CONSTRUCTOR 1: For creating NEW books (Used by BookController) ---
    // Takes 6 arguments (excluding CategoryName, which is not stored in the book table)
    public Book(String bookID, String title, String author, String isbn, int publicationYear, String categoryID) {
        this.bookID = bookID;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.publicationYear = publicationYear;
        this.categoryID = categoryID;
    }
    
    // --- CONSTRUCTOR 2: For reading FULL records (Used by BookDAO.readAllBooks) ---
    // Takes 7 arguments (includes CategoryName from the JOIN)
    public Book(String bookID, String title, String author, String isbn, int publicationYear, String categoryID, String categoryName) {
        this.bookID = bookID;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.publicationYear = publicationYear;
        this.categoryID = categoryID;
        this.categoryName = categoryName; 
    }


    // ----------------------
    // --- Getters (Read) ---
    // ----------------------
    public String getBookID() {
        return bookID;
    }
    public String getTitle() {
        return title;
    }
    public String getAuthor() {
        return author;
    }
    public String getIsbn() { // Matches PropertyValueFactory("isbn")
        return isbn;
    }
    public int getPublicationYear() {
        return publicationYear;
    }
    public String getCategoryID() {
        return categoryID;
    }
    public String getCategoryName() { // Used for TableView display
        return categoryName;
    }

    // ----------------------
    // --- Setters (Write) --
    // ----------------------
    public void setBookID(String bookID) {
        this.bookID = bookID;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public void setAuthor(String author) {
        this.author = author;
    }
    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }
    public void setPublicationYear(int publicationYear) {
        this.publicationYear = publicationYear;
    }
    public void setCategoryID(String categoryID) {
        this.categoryID = categoryID;
    }
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}