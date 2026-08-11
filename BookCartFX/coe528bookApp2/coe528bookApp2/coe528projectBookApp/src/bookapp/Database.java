package bookapp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

// Database.java
// Holds the single SQLite connection the app uses, creates the schema the first
// time it runs, and copies across whatever was already in customers.txt and
// books.txt. The text files are only ever read, never written or deleted.
public class Database {
    private static final String DB_FILE = "bookstore.db";
    private static final String DB_URL = "jdbc:sqlite:" + DB_FILE;

    // The old flat files, read once on first run.
    private static final String CUSTOMER_FILE = "customers.txt";
    private static final String BOOKS_FILE = "books.txt";

    private static Connection connection;

    private Database() {
    }

    public static Connection getConnection() {
        if (connection == null) {
            try {
                connection = DriverManager.getConnection(DB_URL);
                createTables(connection);
                addMissingColumns(connection);
                migrateTextFiles(connection);
                hashPlaintextPasswords(connection);
            } catch (SQLException e) {
                System.out.println("Error opening database: " + e.getMessage());
            }
        }
        return connection;
    }

    public static void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.out.println("Error closing database: " + e.getMessage());
            }
            connection = null;
        }
    }

    private static void createTables(Connection c) throws SQLException {
        try (Statement st = c.createStatement()) {
            // Foreign keys are off by default in SQLite and the setting is
            // per-connection, so it has to be turned on here.
            st.execute("PRAGMA foreign_keys = ON");

            // joined_at and category are not used by any screen. They exist so
            // that sql/analytics.sql can report on cohorts and on categories.
            st.execute("CREATE TABLE IF NOT EXISTS customers ("
                    + "username TEXT PRIMARY KEY, "
                    + "password TEXT NOT NULL, "
                    + "points INTEGER NOT NULL DEFAULT 0, "
                    + "joined_at TEXT)");

            st.execute("CREATE TABLE IF NOT EXISTS books ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "title TEXT NOT NULL, "
                    + "author TEXT, "
                    + "price REAL NOT NULL, "
                    + "category TEXT)");

            st.execute("CREATE TABLE IF NOT EXISTS orders ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "username TEXT NOT NULL REFERENCES customers(username) ON DELETE CASCADE, "
                    + "ordered_at TEXT NOT NULL DEFAULT (datetime('now','localtime')), "
                    + "total REAL NOT NULL, "
                    + "points_earned INTEGER NOT NULL DEFAULT 0, "
                    + "points_redeemed INTEGER NOT NULL DEFAULT 0)");

            // Title, author and price are copied into the row rather than read
            // back through book_id, so that a customer's history still reads
            // correctly after the owner deletes a book or changes its price.
            st.execute("CREATE TABLE IF NOT EXISTS order_items ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "order_id INTEGER NOT NULL REFERENCES orders(id) ON DELETE CASCADE, "
                    + "book_id INTEGER REFERENCES books(id) ON DELETE SET NULL, "
                    + "title TEXT NOT NULL, "
                    + "author TEXT, "
                    + "price REAL NOT NULL)");

            st.execute("CREATE INDEX IF NOT EXISTS idx_orders_username ON orders(username)");
            st.execute("CREATE INDEX IF NOT EXISTS idx_order_items_order ON order_items(order_id)");
        }
    }

    // Runs after the text-file import, so anything arriving from customers.txt
    // gets hashed too. Passwords already hashed are left alone, which makes this
    // safe to run on every start.
    private static void hashPlaintextPasswords(Connection c) throws SQLException {
        String select = "SELECT username, password FROM customers";
        String update = "UPDATE customers SET password = ? WHERE username = ?";
        int hashed = 0;

        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(select);
             PreparedStatement ps = c.prepareStatement(update)) {
            while (rs.next()) {
                String stored = rs.getString("password");
                if (PasswordHasher.isHashed(stored)) {
                    continue;
                }
                ps.setString(1, PasswordHasher.hash(stored));
                ps.setString(2, rs.getString("username"));
                ps.addBatch();
                hashed++;
            }
            if (hashed > 0) {
                ps.executeBatch();
                System.out.println("Hashed " + hashed + " plain text password(s)");
            }
        }
    }


    // CREATE TABLE IF NOT EXISTS leaves an older database on its original
    // columns, so anything added later has to be patched in separately.
    private static void addMissingColumns(Connection c) throws SQLException {
        addColumnIfMissing(c, "customers", "joined_at", "TEXT");
        addColumnIfMissing(c, "books", "category", "TEXT");
    }

    private static void addColumnIfMissing(Connection c, String table, String column,
            String definition) throws SQLException {
        try (ResultSet rs = c.getMetaData().getColumns(null, null, table, column)) {
            if (rs.next()) {
                return;
            }
        }

        // SQLite will not accept a non-constant DEFAULT on ADD COLUMN, so these
        // arrive as NULL and are filled in by whoever writes the row.
        try (Statement st = c.createStatement()) {
            st.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
            System.out.println("Added column " + table + "." + column);
        }
    }


    // Only runs when a table is still empty, so restarting the app does not
    // re-import rows that the owner has since deleted.
    private static void migrateTextFiles(Connection c) throws SQLException {
        if (isEmpty(c, "customers")) {
            migrateCustomers(c);
        }
        if (isEmpty(c, "books")) {
            migrateBooks(c);
        }
    }

    private static boolean isEmpty(Connection c, String table) throws SQLException {
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + table)) {
            return rs.next() && rs.getInt(1) == 0;
        }
    }

    private static void migrateCustomers(Connection c) throws SQLException {
        String sql = "INSERT OR IGNORE INTO customers (username, password, points, joined_at) "
                + "VALUES (?, ?, ?, datetime('now','localtime'))";
        int migrated = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(CUSTOMER_FILE));
             PreparedStatement ps = c.prepareStatement(sql)) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] customerData = line.split(",");
                if (customerData.length == 3) {
                    ps.setString(1, customerData[0]);
                    ps.setString(2, customerData[1]);
                    ps.setInt(3, Integer.parseInt(customerData[2]));
                    ps.addBatch();
                    migrated++;
                }
            }
            ps.executeBatch();
            System.out.println("Migrated " + migrated + " customers from " + CUSTOMER_FILE);
        } catch (IOException e) {
            System.out.println("No customers to migrate: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Skipped customer migration, bad points value: " + e.getMessage());
        }
    }

    private static void migrateBooks(Connection c) throws SQLException {
        String sql = "INSERT INTO books (title, author, price) VALUES (?, ?, ?)";
        int migrated = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(BOOKS_FILE));
             PreparedStatement ps = c.prepareStatement(sql)) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] bookData = line.split(",");
                if (bookData.length == 3) {
                    ps.setString(1, bookData[0]);
                    ps.setString(2, bookData[1]);
                    ps.setDouble(3, Double.parseDouble(bookData[2]));
                    ps.addBatch();
                    migrated++;
                }
            }
            ps.executeBatch();
            System.out.println("Migrated " + migrated + " books from " + BOOKS_FILE);
        } catch (IOException e) {
            System.out.println("No books to migrate: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Skipped book migration, bad price value: " + e.getMessage());
        }
    }
}
