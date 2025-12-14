package library.utilities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import configuration.DBConnector;
import library.models.Book;
public class BookDAO {
    private static final String BOOK_PREFIX = "BK-";

    // --- ID GENERATION LOGIC ---
    private String generateNextBookId(Connection conn) throws SQLException {
        // Find the highest BookID that starts with the defined prefix
        String sql = "SELECT BookID FROM books WHERE BookID LIKE ? ORDER BY BookID DESC LIMIT 1";
        
        int nextSequence = 1;
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, BOOK_PREFIX + "%"); // e.g., "B%"
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String lastId = rs.getString("BookID"); // e.g., "B0005"
                    
                    // Extract the number part (4 digits)
                    String numberPart = lastId.substring(BOOK_PREFIX.length());
                    
                    // Convert to integer and increment
                    nextSequence = Integer.parseInt(numberPart) + 1;
                }
            }
        }
        
        // Format the new sequence number with zero-padding (0001)
        DecimalFormat df = new DecimalFormat("0000"); // 4 sequential digits
        String newSequence = df.format(nextSequence); 

        return BOOK_PREFIX + newSequence; // e.g., "B0006"
    }
    //  ---------- CRUD OPERATIONS ----------

    public Book createBook(Book book) throws SQLException { 
        String sql = "INSERT INTO books (BookID, Title, Author, ISBN, PublicationYear, CategoryID) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        
        Connection link = null;
        PreparedStatement state = null;
        try {
            link = DBConnector.getConnection();
            
            // --- CRUCIAL: Generate ID here and set it on the object ---
            String newId = generateNextBookId(link);
            book.setBookID(newId);
            // -----------------------------------------------------------
            
            state = link.prepareStatement(sql);
            
            // Map fields to parameters
            state.setString(1, book.getBookID()); // Use the generated ID
            state.setString(2, book.getTitle());
            state.setString(3, book.getAuthor());
            state.setString(4, book.getISBN());
            state.setInt(5, book.getPublicationYear());
            state.setString(6, book.getCategoryID());
            state.executeUpdate();
            
            return book; // Return the book object with the new ID
            
        } catch (SQLException e) {
            System.err.println("Error creating book: " + e.getMessage());
            throw e;
        } finally {
            if (state != null) state.close();
            if (link != null) link.close();
        }
    }

    //  ---------- READ ----------
        public List<Book> readAllBooks() throws SQLException {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books";
        
        Connection link = null;
        PreparedStatement state = null;
        ResultSet resultSet = null;
        try {
            link = DBConnector.getConnection();
            state = link.prepareStatement(sql);
            resultSet = state.executeQuery();
            
            while (resultSet.next()) {
                // map database columns to the Book model
                String bookId = resultSet.getString("BookID");
                String title = resultSet.getString("Title");
                String author = resultSet.getString("Author");
                String isbn = resultSet.getString("ISBN");
                int publicationYear = resultSet.getInt("PublicationYear");
                String categoryId = resultSet.getString("CategoryID");
                
                Book book = new Book(bookId, title, author, isbn, publicationYear, categoryId);
                books.add(book);
            }
        } catch (SQLException e) {
            System.err.println("Error reading all books: " + e.getMessage());
            throw e; 
        } finally {
            //  prevent memory leaks
            if (resultSet != null) resultSet.close();
            if (state != null) state.close();
            if (link != null) link.close();
        }
        return books;
    }

    //  ---------- UPDATE ----------
    public void updateBook(Book book) throws SQLException {
    String sql = "UPDATE books SET Title = ?, Author = ?, ISBN = ?, PublicationYear = ?, CategoryID = ? " +
                "WHERE BookID = ?";
        
        Connection link = null;
        PreparedStatement state = null;

        try {
            link = DBConnector.getConnection();
            state = link.prepareStatement(sql);
            
            // Map updated fields to parameters (1-5)
            state.setString(1, book.getTitle());
            state.setString(2, book.getAuthor());
            state.setString(3, book.getISBN());
            state.setInt(4, book.getPublicationYear());
            state.setString(5, book.getCategoryID());
            // Map BookID to the WHERE clause (6)
            state.setString(6, book.getBookID());
            state.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error updating book: " + e.getMessage());
            throw e;
        } finally {
            if (state != null) state.close();
            if (link != null) link.close();
        }
    }

    //  ---------- DELETE ----------
    public void deleteBook(String bookId) throws SQLException {
        String sql = "DELETE FROM books WHERE BookID = ?";

        Connection link = null;
        PreparedStatement state = null;
        try {
            link = DBConnector.getConnection();
            state = link.prepareStatement(sql);
            
            state.setString(1, bookId);
            
            int rowsAffected = state.executeUpdate();
            if (rowsAffected == 0) {
                System.out.println("No book found with ID: " + bookId);
            }
        } catch (SQLException e) {
            // Handle Foreign Key Constraint error (if the book is currently on loan)
            if (e.getSQLState().startsWith("23")) { 
                System.err.println("Cannot delete book. It is referenced in a transaction record.");
            }
            throw e;
        } finally {
            if (state != null) state.close();
            if (link != null) link.close();
        }
    }
}
