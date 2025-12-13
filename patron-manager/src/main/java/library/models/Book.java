package library.models;

public class Book {
    // Essentials for data access and JavaFX TableView
    // Fields correspond to the columns in the 'books' table
    private String bookID;
    private String title;
    private String author;
    private String isbn;
    private int publicationYear;
    private String categoryID;

    // Constructor
    // A convenient constructor to create a Patron object when reading from the database
    public Book(String bookID, String title, String author, String isbn, int publicationYear, String categoryID) {
        this.bookID = bookID;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.publicationYear = publicationYear;
        this.categoryID = categoryID;
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
    public String getISBN() {
        return isbn;
    }
    public int getPublicationYear() {
        return publicationYear;
    }
    public String getCategoryId() {
        return categoryID;
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
    public void setISBN(String isbn) {
        this.isbn = isbn;
    }
    public void setPublicationYear(int publicationYear) {
        this.publicationYear = publicationYear;
    }
    public void setCategoryId(String categoryId) {
        this.categoryID = categoryId;
    }
}

