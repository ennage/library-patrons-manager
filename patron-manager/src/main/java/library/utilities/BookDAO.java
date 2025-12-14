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
            pstmt.setString(1, BOOK_PREFIX + "%"); // e.g., "BK-%"
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String lastId = rs.getString("BookID"); // e.g., "BK-0005"
                    
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

        return BOOK_PREFIX + newSequence; // e.g., "BK-0006"
    }
    
    //  ---------- CRUD OPERATIONS ----------

    public Book createBook(Book book) throws SQLException { 
        String sql = "INSERT INTO books (BookID, Title, Author, ISBN, PublicationYear, CategoryID) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        
        // FIX: Using try-with-resources for Connection and PreparedStatement.
        // Removed redundant finally block cleanup.
        try (Connection link = DBConnector.getConnection()) {
            
            // --- CRUCIAL: Generate ID here and set it on the object ---
            String newId = generateNextBookId(link);
            book.setBookID(newId);
            // -----------------------------------------------------------
            
            try (PreparedStatement state = link.prepareStatement(sql)) {
                
                // Map fields to parameters
                state.setString(1, book.getBookID()); // Use the generated ID
                state.setString(2, book.getTitle());
                state.setString(3, book.getAuthor());
                state.setString(4, book.getIsbn()); // Corrected getter casing
                state.setInt(5, book.getPublicationYear());
                state.setString(6, book.getCategoryID());
                state.executeUpdate();
            }
            
            return book; // Return the book object with the new ID
            
        } catch (SQLException e) {
            System.err.println("Error creating book: " + e.getMessage());
            throw e;
        }
    }

    //  ---------- READ (FIXED RESOURCE MANAGEMENT) ----------
    public List<Book> readAllBooks() throws SQLException {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT b.*, c.CategoryName " +
                    "FROM books b " +
                    "JOIN categories c ON b.CategoryID = c.CategoryID " +
                    "ORDER BY b.BookID";

        // FIX: Relying solely on try-with-resources. Removed all external declarations 
        // and the manual finally block to prevent NullPointerExceptions on close.
        try (Connection link = DBConnector.getConnection();
            PreparedStatement state = link.prepareStatement(sql);
            ResultSet resultSet = state.executeQuery()) {

            while (resultSet.next()) {
                String bookID = resultSet.getString("BookID");
                String title = resultSet.getString("Title");
                String author = resultSet.getString("Author");
                // The name "ISBN" from the database is retrieved correctly.
                String isbn = resultSet.getString("ISBN"); 
                int publicationYear = resultSet.getInt("PublicationYear");
                String categoryID = resultSet.getString("CategoryID");
                
                // 2. RETRIEVE THE NEW FIELD
                String categoryName = resultSet.getString("CategoryName"); 
                
                // 3. USE THE NEW 7-ARGUMENT CONSTRUCTOR
                Book book = new Book(
                    bookID, title, author, isbn, publicationYear, categoryID, categoryName
                );
                books.add(book);
            }
        } catch (SQLException e) {
            System.err.println("Error reading all books: " + e.getMessage());
            throw e; 
        } 
        
        return books;
    }

    //  ---------- UPDATE (FIXED RESOURCE MANAGEMENT) ----------
    public void updateBook(Book book) throws SQLException {
        String sql = "UPDATE books SET Title = ?, Author = ?, ISBN = ?, PublicationYear = ?, CategoryID = ? " +
                    "WHERE BookID = ?";
        
        // FIX: Using try-with-resources for Connection and PreparedStatement.
        try (Connection link = DBConnector.getConnection();
            PreparedStatement state = link.prepareStatement(sql)) {
            
            // Map updated fields to parameters (1-5)
            state.setString(1, book.getTitle());
            state.setString(2, book.getAuthor());
            state.setString(3, book.getIsbn());
            state.setInt(4, book.getPublicationYear());
            state.setString(5, book.getCategoryID());
            // Map BookID to the WHERE clause (6)
            state.setString(6, book.getBookID());
            state.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error updating book: " + e.getMessage());
            throw e;
        }
    }

    //  ---------- DELETE (FIXED RESOURCE MANAGEMENT) ----------
    public void deleteBook(String bookId) throws SQLException {
        String sql = "DELETE FROM books WHERE BookID = ?";

        // FIX: Using try-with-resources for Connection and PreparedStatement.
        try (Connection link = DBConnector.getConnection();
            PreparedStatement state = link.prepareStatement(sql)) {
            
            state.setString(1, bookId);
            
            int rowsAffected = state.executeUpdate();
            if (rowsAffected == 0) {
                System.out.println("No book found with ID: " + bookId);
            }
        } catch (SQLException e) {
            // Handle Foreign Key Constraint error (if the book is currently on loan)
            if (e.getSQLState() != null && e.getSQLState().startsWith("23")) { 
                System.err.println("Cannot delete book. It is referenced in a transaction record.");
            }
            throw e;
        }
    }
}