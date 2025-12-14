package library.utilities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat; // NEW IMPORT for ID generation
import java.time.LocalDate; // NEW IMPORT for date handling
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import configuration.DBConnector;
import library.models.Transaction;

public class TransactionDAO {
    
    private static final String TRANSACTION_PREFIX = "T-";
    private static final DateTimeFormatter SQL_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // ---------------------------------------------
    // --- ID GENERATION LOGIC ---
    // ---------------------------------------------
    /**
     * Generates the next sequential TransactionID (e.g., T-0001).
     */
    private String generateNextTransactionId(Connection conn) throws SQLException {
        String sql = "SELECT TransactionID FROM transactions WHERE TransactionID LIKE ? ORDER BY TransactionID DESC LIMIT 1";
        int nextSequence = 1;
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, TRANSACTION_PREFIX + "%"); 
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String lastId = rs.getString("TransactionID"); 
                    String numberPart = lastId.substring(TRANSACTION_PREFIX.length());
                    nextSequence = Integer.parseInt(numberPart) + 1;
                }
            }
        }
        
        DecimalFormat df = new DecimalFormat("0000"); // 4 sequential digits
        return TRANSACTION_PREFIX + df.format(nextSequence); 
    }

    // ---------------------------------------------
    // --- CREATE/BORROW OPERATION (Modified) ---
    // ---------------------------------------------
    /**
     * Records a new book loan. Returns the full Transaction object with its generated ID.
     * NOTE: Signature adjusted to match the simplified controller call.
     */
    public Transaction borrowBook(String patronID, String bookID, LocalDate dueDate) throws SQLException {
        Connection link = null;
        PreparedStatement state = null;
        
        LocalDate dateBorrowed = LocalDate.now();
        String transactionID;

        // Note: DateReturned is NULL initially for a new loan.
        String sql = "INSERT INTO transactions (TransactionID, BookID, PatronID, DateBorrowed, DueDate, DateReturned) " +
                    "VALUES (?, ?, ?, ?, ?, NULL)"; 

        try {
            link = DBConnector.getConnection();
            
            // 1. Generate ID
            transactionID = generateNextTransactionId(link);
            
            state = link.prepareStatement(sql);
            
            // 2. Map fields to parameters
            state.setString(1, transactionID);
            state.setString(2, bookID);
            state.setString(3, patronID);
            state.setString(4, dateBorrowed.format(SQL_DATE_FORMATTER));
            state.setString(5, dueDate.format(SQL_DATE_FORMATTER));
            state.executeUpdate();
            
            // Return the newly created object (Controller will set PatronName/BookTitle manually)
            return new Transaction(
                transactionID, 
                bookID, 
                patronID, 
                dateBorrowed.format(SQL_DATE_FORMATTER), 
                dueDate.format(SQL_DATE_FORMATTER), 
                null // DateReturned is null
            );
            
        } catch (SQLException e) {
            System.err.println("Error recording book loan: " + e.getMessage());
            throw e;
        } finally {
            if (state != null) state.close();
            if (link != null) link.close();
        }
    }

    // ---------------------------------------------
    // --- NEW: READ OUTSTANDING LOANS ---
    // ---------------------------------------------
    /**
     * Reads all transactions that are currently open (DateReturned IS NULL).
     * Uses JOIN to pull Patron Name and Book Title for the UI.
     */
    public List<Transaction> readOutstandingLoans() throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        
        // FIX: JOIN to get Patron Name and Book Title for the Controller's TableView
        String sql = "SELECT t.*, p.FirstName, p.LastName, b.Title AS BookTitle FROM transactions t " +
                    "JOIN patrons p ON t.PatronID = p.PatronID " +
                    "JOIN books b ON t.BookID = b.BookID " +
                    "WHERE t.DateReturned IS NULL " +
                    "ORDER BY t.DateBorrowed DESC"; 
        
        Connection link = null;
        PreparedStatement state = null;
        ResultSet resultSet = null;
        
        try {
            link = DBConnector.getConnection();
            state = link.prepareStatement(sql);
            resultSet = state.executeQuery();
            
            while (resultSet.next()) {
                String patronName = resultSet.getString("FirstName") + " " + resultSet.getString("LastName");
                String bookTitle = resultSet.getString("BookTitle");
                
                Transaction transaction = new Transaction(
                    resultSet.getString("TransactionID"),
                    resultSet.getString("BookID"),
                    resultSet.getString("PatronID"),
                    resultSet.getString("DateBorrowed"),
                    resultSet.getString("DueDate"),
                    resultSet.getString("DateReturned")
                );
                
                // Manually set the joined fields on the model for the UI
                transaction.setPatronName(patronName); 
                transaction.setBookTitle(bookTitle); 
                
                transactions.add(transaction);
            }
        } catch (SQLException e) {
            System.err.println("Error reading outstanding transactions: " + e.getMessage());
            throw e; 
        } finally {
            if (resultSet != null) resultSet.close();
            if (state != null) state.close();
            if (link != null) link.close();
        }
        return transactions;
    }

    // ---------------------------------------------
    // --- NEW: AVAILABILITY CHECK ---
    // ---------------------------------------------
    /**
     * Checks if a book is currently borrowed (DateReturned is NULL).
     */
    public boolean isBookCurrentlyBorrowed(String bookID) throws SQLException {
        String sql = "SELECT 1 FROM transactions WHERE BookID = ? AND DateReturned IS NULL LIMIT 1";
        
        Connection link = null;
        PreparedStatement state = null;
        ResultSet resultSet = null;
        
        try {
            link = DBConnector.getConnection();
            state = link.prepareStatement(sql);
            state.setString(1, bookID);
            resultSet = state.executeQuery();
            
            // If resultSet.next() returns true, a record exists (the book is currently borrowed)
            return resultSet.next();
            
        } catch (SQLException e) {
            System.err.println("Error checking book availability: " + e.getMessage());
            throw e;
        } finally {
            if (resultSet != null) resultSet.close();
            if (state != null) state.close();
            if (link != null) link.close();
        }
    }
    
    // ---------------------------------------------
    // --- UPDATE/RETURN OPERATION (Modified) ---
    // ---------------------------------------------
    /**
     * Records the return date of a book based on TransactionID.
     * NOTE: Signature simplified to use LocalDate.now() in the implementation.
     */
    public void returnBook(String transactionId) throws SQLException {
        // Only update the DateReturned field
        String sql = "UPDATE transactions SET DateReturned = ? WHERE TransactionID = ?";
        
        Connection link = null;
        PreparedStatement state = null;
        try {
            link = DBConnector.getConnection();
            state = link.prepareStatement(sql);
            
            // Use current date for DateReturned
            String dateReturned = LocalDate.now().format(SQL_DATE_FORMATTER);
            
            // Map parameters
            state.setString(1, dateReturned);
            state.setString(2, transactionId);
            state.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error recording book return: " + e.getMessage());
            throw e;
        } finally {
            if (state != null) state.close();
            if (link != null) link.close();
        }
    }
    
    // ... (readAllTransactions is no longer strictly needed but can be kept) ...
}