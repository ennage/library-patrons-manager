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
    private String generateNextPatronId(Connection conn) throws SQLException {
        String sql = "SELECT PatronID FROM patrons WHERE PatronID LIKE ? ORDER BY PatronID DESC LIMIT 1";
        int nextSequence = 1;
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, PATRON_PREFIX + "%"); 
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String lastId = rs.getString("PatronID");
                    String numberPart = lastId.substring(PATRON_PREFIX.length());
                    nextSequence = Integer.parseInt(numberPart) + 1;
                }
            }
        }
        
        DecimalFormat df = new DecimalFormat("0000"); 
        String newSequence = df.format(nextSequence); 

        return PATRON_PREFIX + newSequence;
    }
    
    //  ---------- CREATE (Fixes "wont save") ----------
    public Patron createPatron(Patron patron) throws SQLException { 
        // SQL order: (1:ID, 2:First, 3:Last, 4:Email, 5:Phone, 6:Address)
        String sql = "INSERT INTO patrons (PatronID, FirstName, LastName, Email, PhoneNumber, Address) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection link = DBConnector.getConnection()) {
            
            String newId = generateNextPatronId(link);
            patron.setPatronID(newId);
            
            try (PreparedStatement state = link.prepareStatement(sql)) {
                
                // Mapped to SQL order
                state.setString(1, patron.getPatronID());
                state.setString(2, patron.getFirstName());
                state.setString(3, patron.getLastName());
                state.setString(4, patron.getEmail());
                state.setString(5, patron.getPhone()); // FIX: Using getPhone()
                state.setString(6, patron.getAddress());
                state.executeUpdate();
            }
            
            return patron;
            
        } catch (SQLException e) {
            System.err.println("Error creating patron: " + e.getMessage());
            throw e;
        }
    }

    //  ---------- READ ALL ----------
    public List<Patron> readAllPatrons() throws SQLException {
        List<Patron> patrons = new ArrayList<>();
        // FIX: Ensure SQL selects the column name used for phone number (e.g., PhoneNumber)
        String sql = "SELECT PatronID, FirstName, LastName, Email, PhoneNumber, Address FROM patrons ORDER BY PatronID";

        try (Connection link = DBConnector.getConnection();
            PreparedStatement state = link.prepareStatement(sql);
            ResultSet resultSet = state.executeQuery()) {

            while (resultSet.next()) {
                // Constructor Call Order: (ID, First, Last, Email, Phone, Address)
                Patron patron = new Patron(
                    resultSet.getString("PatronID"),
                    resultSet.getString("FirstName"),
                    resultSet.getString("LastName"),
                    resultSet.getString("Email"),
                    resultSet.getString("PhoneNumber"), // FIX: Using DB column name to retrieve value
                    resultSet.getString("Address")
                );
                patrons.add(patron);
            }
        } catch (SQLException e) {
            System.err.println("Error reading all patrons: " + e.getMessage());
            throw e; 
        } 
        
        return patrons;
    }

    //  ---------- UPDATE ----------
    public void updatePatron(Patron patron) throws SQLException {
        String sql = "UPDATE patrons SET FirstName = ?, LastName = ?, Email = ?, PhoneNumber = ?, Address = ? " +
                    "WHERE PatronID = ?";
        
        try (Connection link = DBConnector.getConnection();
            PreparedStatement state = link.prepareStatement(sql)) {
            
            state.setString(1, patron.getFirstName());
            state.setString(2, patron.getLastName());
            state.setString(3, patron.getEmail());
            state.setString(4, patron.getPhone()); // FIX: Using getPhone()
            state.setString(5, patron.getAddress());
            state.setString(6, patron.getPatronID());
            state.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error updating patron: " + e.getMessage());
            throw e;
        }
    }

    //  ---------- DELETE (Fixes "cant be deleted") ----------
    public void deletePatron(String patronId) throws SQLException {
        String sql = "DELETE FROM patrons WHERE PatronID = ?";

        try (Connection link = DBConnector.getConnection();
            PreparedStatement state = link.prepareStatement(sql)) {
            
            state.setString(1, patronId);
            state.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error deleting patron: " + e.getMessage());
            throw e; 
        }
    }
}