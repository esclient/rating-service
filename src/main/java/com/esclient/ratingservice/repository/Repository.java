import java.sql.*;

@Repository
public class Repository {

    private final Connection conn;

    public Repository(Connection conn) {
        this.conn = conn;
    }

    public void connect() throws SQLException {
        String url = System.getenv("DATABASE_URL");        

        connection = DriverManager.getConnection(url);
    }


    public long addRate(long modId, long authorId, int rate) throws SQLException {
        String sql = "INSERT INTO rates (author_id, mod_id, rate) VALUES (?, ?, ?)";
        
        // Return generated keys
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, authorId);
            stmt.setLong(2, modId);
            stmt.setInt(3, rate);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating rating failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                } else {
                    throw new SQLException("Creating rating failed, no ID obtained.");
                }
            }
        }
    }
}
