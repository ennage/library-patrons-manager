package library.utilities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import library.models.Transaction;
import tools.DBConnector;

public class TransactionDAO {
    //  ---------- CRUD OPERATIONS ----------

    //  ---------- CREATE ----------
    public void borrowBook(Transaction transaction) throws SQLException {
        // Note: DateReturned is NULL initially for a new loan.
        String sql = "INSERT INTO transactions (TransactionID, BookID, PatronID, DateBorrowed, DueDate, DateReturned) " +
                    "VALUES (?, ?, ?, ?, ?, NULL)"; 
        
        Connection link = null;
        PreparedStatement state = null;

        try {
            link = DBConnector.getConnection();
            state = link.prepareStatement(sql);
            
            // Map fields to parameters
            state.setString(1, transaction.getTransactionID());
            state.setString(2, transaction.getBookID());
            state.setString(3, transaction.getPatronID());
            state.setString(4, transaction.getDateBorrowed());
            state.setString(5, transaction.getDueDate());
            state.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error recording book loan: " + e.getMessage());
            throw e;
        } finally {
            if (state != null) state.close();
            if (link != null) link.close();
        }
    }

    //  ---------- READ ----------
    public List<Transaction> readAllTransactions() throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        // Note: You will likely join with patrons/books later, but for now, SELECT * is fine.
        String sql = "SELECT * FROM transactions"; 
        
        Connection link = null;
        PreparedStatement state = null;
        ResultSet resultSet = null;
        
        try {
            link = DBConnector.getConnection();
            state = link.prepareStatement(sql);
            resultSet = state.executeQuery();
            
            while (resultSet.next()) {
                String transactionId = resultSet.getString("TransactionID");
                String bookId = resultSet.getString("BookID");
                String patronId = resultSet.getString("PatronID");
                String dateBorrowed = resultSet.getString("DateBorrowed");
                String dueDate = resultSet.getString("DueDate");
                // DateReturned can be NULL, so we retrieve it and let the model handle NULL/String mapping
                String dateReturned = resultSet.getString("DateReturned"); 
                
                Transaction transaction = new Transaction(transactionId, bookId, patronId, dateBorrowed, dueDate, dateReturned);
                transactions.add(transaction);
            }
        } catch (SQLException e) {
            System.err.println("Error reading all transactions: " + e.getMessage());
            throw e; 
        } finally {
            if (resultSet != null) resultSet.close();
            if (state != null) state.close();
            if (link != null) link.close();
        }
        return transactions;
    }

    //  ---------- UPDATE ----------
    public void returnBook(String transactionId, String dateReturned) throws SQLException {
        // Only update the DateReturned field
        String sql = "UPDATE transactions SET DateReturned = ? WHERE TransactionID = ?";
        
        Connection link = null;
        PreparedStatement state = null;
        try {
            link = DBConnector.getConnection();
            state = link.prepareStatement(sql);
            
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
    
    // Note: A DELETE method for transactions is usually only needed for system cleanup.
}