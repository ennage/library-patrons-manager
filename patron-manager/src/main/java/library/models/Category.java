package library.models;

public class Category {
    // Essentials for data access and JavaFX TableView
    // Fields correspond to the columns in the 'categories' table
    private String categoryID;
    private String categoryName;

    // Constructor
    // A convenient constructor to create a Patron object when reading from the database
    public Category(String categoryID, String categoryName) {
        this.categoryID = categoryID;
        this.categoryName = categoryName;
    }

    // ----------------------
    // --- Getters (Read) ---
    // ----------------------
    public String getCategoryID() {
        return categoryID;
    }
    public String getCategoryName() {
        return categoryName;
    }

    // ----------------------
    // --- Setters (Write) --
    // ----------------------
    public void setCategoryID(String categoryID) {
        this.categoryID = categoryID;
    }
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}