package library.utilities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import configuration.DBConnector;
import library.models.Patron;

public class PatronDAO {
    private static final String PATRON_PREFIX = "PT-";

    // --- ID GENERATION LOGIC ---
    public String generateNextPatroId(@SuppressWarnings("exports") Connection conn) throws SQLException {
        // SQL: Find the highest PatronID that starts with the defined prefix
        // ORDER BY DESC and LIMIT 1 ensures we get the latest one quickly.
        String sql = "SELECT PatronID FROM patrons WHERE PatronID LIKE ? ORDER BY PatronID DESC LIMIT 1";
        
        int nextSequence = 1;
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Use the prefix + wildcard (%) to search, e.g., "PT-%"
            pstmt.setString(1, PATRON_PREFIX + "%");
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String lastId = rs.getString("PatronID"); // e.g., "PT-0008"
                    
                    // Extract the number part (4 digits)
                    String numberPart = lastId.substring(PATRON_PREFIX.length());
                    
                    // Convert to integer and increment
                    nextSequence = Integer.parseInt(numberPart) + 1;
                }
            }
        }
        
        // Format the new sequence number with zero-padding (0001, 0010, 0100, 1000)
        DecimalFormat df = new DecimalFormat("0000"); // 4 sequential digits
        String newSequence = df.format(nextSequence); 

        // Return the new full ID
        return PATRON_PREFIX + newSequence; // e.g., "PT-0009"
    }
    // --------------------------------------
    //  ---------- CRUD OPERATIONS ----------
    // --------------------------------------

    //  ---------- CREATE ----------
    public Patron createPatron(Patron patron) throws SQLException {
    // SQL: use ? as placeholders for safe parameter injection
    String sql = "INSERT INTO patrons (PatronID, FirstName, LastName, Address, Email, PhoneNumber) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
    Connection link = null;
    PreparedStatement state = null;
    ResultSet generatedKeys;

    try {
        link = DBConnector.getConnection();
        state = link.prepareStatement(sql);
        
        //  map Patron object fields to the PreparedStatement parameters
        state.setString(1, patron.getPatronID());
        state.setString(2, patron.getFirstName());
        state.setString(3, patron.getLastName());
        state.setString(4, patron.getAddress());
        state.setString(5, patron.getEmail());
        state.setString(6, patron.getPhoneNumber());
        state.executeUpdate();

        generatedKeys = state.getGeneratedKeys();
        if (generatedKeys.next()) {
            // Assuming PatronID is the first generated key (index 1) and is a String
            String generatedId = generatedKeys.getString(1);
            // CRUCIAL: Set the actual ID back onto the Patron object
            patron.setPatronID(generatedId); 
        }
        return patron;
        
    } catch (SQLException e) {
        System.err.println("Error creating patron: " + e.getMessage());
        throw e;
        // Let the Controller handle the error (e.g., display an alert)
    } finally {
        // prevent memory leaks
        if (state != null) state.close();
        if (link != null) link.close();
    }
}

    //  ---------- READ ----------
    public List<Patron> readAllPatrons() throws SQLException {
        //  initialize list to store the results
        List<Patron> patrons = new ArrayList<>();
        
        //  SQL query to select all columns from the 'patrons' table
        String sql = "SELECT * FROM patrons";
        
        Connection link = null;
        PreparedStatement state = null;
        ResultSet resultSet = null;
        try {
            link = DBConnector.getConnection();
            state = link.prepareStatement(sql);
            resultSet = state.executeQuery();
            
            //  loop through the result set, create Patron objects, and add them to the list
            while (resultSet.next()) {
                String patronId = resultSet.getString("PatronID");
                String firstName = resultSet.getString("FirstName");
                String lastName = resultSet.getString("LastName");
                String address = resultSet.getString("Address");
                String email = resultSet.getString("Email");
                // Retrieve the phone number as a String from the database
                String phoneNumber = resultSet.getString("PhoneNumber"); 
                Patron patron = new Patron(patronId, firstName, lastName, address, email, phoneNumber);
                patrons.add(patron);
            }
        } catch (SQLException e) {
            // Log the error for debugging purposes
            System.err.println("Error reading all patrons: " + e.getMessage());
            throw e;
            // ^^^^ Re-throw the exception for the calling Controller to handle
        } finally {
            //  prevent memory leaks
            if (resultSet != null) resultSet.close();
            if (state != null) state.close();
            if (link != null) link.close();
        }
        return patrons;
    }
    

    //  ---------- UPDATE ----------
    public void updatePatron(Patron patron) throws SQLException {
        // SQL: List all columns that can be changed, use PatronID in the WHERE clause
        String sql = "UPDATE patrons SET FirstName = ?, LastName = ?, Address = ?, Email = ?, PhoneNumber = ? " +
                    "WHERE PatronID = ?";
        
        Connection link = null;
        PreparedStatement state = null;
        try {
            link = DBConnector.getConnection();
            state = link.prepareStatement(sql);
            
            //  map updated fields to the parameters (in order)
            state.setString(1, patron.getFirstName());
            state.setString(2, patron.getLastName());
            state.setString(3, patron.getAddress());
            state.setString(4, patron.getEmail());
            state.setString(5, patron.getPhoneNumber());
            //  map ID to the WHERE clause (this MUST be the last parameter)
            state.setString(6, patron.getPatronID());
            state.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error updating patron: " + e.getMessage());
            throw e;
        } finally {
            if (state != null) state.close();
            if (link != null) link.close();
        }
    }

    //  ---------- DELETE ----------
    public void deletePatron(String patronId) throws SQLException {
        String sql = "DELETE FROM patrons WHERE PatronID = ?";

        Connection link = null;
        PreparedStatement state = null;
        try {
            link = DBConnector.getConnection();
            state = link.prepareStatement(sql);
            
            state.setString(1, patronId);
            int rowsAffected = state.executeUpdate();
            
            if (rowsAffected == 0) {
                System.out.println("No patron found with ID: " + patronId);
            }
        } catch (SQLException e) {
            // Log the error for debugging purposes
            System.err.println("Error deleting patron: " + e.getMessage());

            // You can check for FK constraint errors here if needed:
            if (e.getSQLState().startsWith("23")) { 
                System.err.println("Cannot delete patron. They have outstanding transactions.");
            }
            
            // Always re-throw the exception so the Controller can handle the UI Alert
            throw e; 
        } finally {
            if (state != null) state.close();
            if (link != null) link.close();
        }
    }
}