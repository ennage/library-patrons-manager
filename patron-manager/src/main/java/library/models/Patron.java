package library.models;

public class Patron {
    
    // Fields correspond to the columns in the 'patrons' table
    private String patronID;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address; 

    // Constructor: Order synchronized for consistency (ID, First, Last, Email, Phone, Address)
    public Patron(String patronID, String firstName, String lastName, String email, String phone, String address) {
        this.patronID = patronID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.address = address;
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
    public String getEmail() {
        return email;
    }
    public String getPhone() { // FIX: Getter name matches "phone" PropertyValueFactory
        return phone;
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
    public void setEmail(String email) {
        this.email = email;
    }
    public void setPhone(String phone) { // FIX: Setter name matches convention
        this.phone = phone;
    }
    public void setAddress(String address) {
        this.address = address;
    }
}