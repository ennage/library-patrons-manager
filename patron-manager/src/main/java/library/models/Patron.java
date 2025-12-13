package library.models;

public class Patron {
    // Essentials for data access and JavaFX TableView
    // Fields correspond to the columns in the 'patrons' table
    private String patronID;
    private String firstName;
    private String lastName;
    private String address;
    private String email;
    private String phoneNumber; 

    // Constructor
    // A convenient constructor to create a Patron object when reading from the database
    public Patron(String patronID, String firstName, String lastName, String address, String email, String phoneNumber) {
        this.patronID = patronID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    // ----------------------
    // --- Getters (Read) ---
    // ----------------------
    public String getPatronID() {
        return patronID;
    }
    public String getFirstName() {
        return firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }
        public String getEmail() {
        return email;
    }
    public String getAddress() {
        return address;
    }

    // ----------------------
    // --- Setters (Write) --
    // ----------------------
    public void setPatronID(String patronId) {
        this.patronID = patronId;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public void setAddress(String address) {
        this.address = address;
    }
}