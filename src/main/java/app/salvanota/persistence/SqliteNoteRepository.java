package app.salvanota.persistence;

import app.salvanota.model.Note;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementazione SQLite della {@link NoteRepository}.
 */
public final class SqliteNoteRepository implements NoteRepository {

    private static final String CREATE_SQL = """
            CREATE TABLE IF NOT EXISTS notes (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                titolo TEXT NOT NULL,
                descrizione TEXT NOT NULL
            )
            """;

    private final Connection connection;

    public SqliteNoteRepository(Path databaseFile) throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver JDBC SQLite non disponibile.", e);
        }
        Path normalized = databaseFile.toAbsolutePath().normalize();
        String pathForUrl = normalized.toString().replace('\\', '/');
        String url = "jdbc:sqlite:" + pathForUrl;
        this.connection = DriverManager.getConnection(url);
        connection.setAutoCommit(true);
        try (Statement st = connection.createStatement()) {
            st.execute(CREATE_SQL);
        }
    }

    @Override
    public long save(Note note) throws SQLException {
        if (note.getId() > 0) {
            return update(note);
        }
        String sql = "INSERT INTO notes (titolo, descrizione) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, note.getTitolo());
            ps.setString(2, note.getDescrizione());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }
        throw new SQLException("Inserimento senza chiave generata.");
    }

    private long update(Note note) throws SQLException {
        String sql = "UPDATE notes SET titolo = ?, descrizione = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, note.getTitolo());
            ps.setString(2, note.getDescrizione());
            ps.setLong(3, note.getId());
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Aggiornamento senza righe per id=" + note.getId());
            }
            return note.getId();
        }
    }

    @Override
    public List<Note> findAll() throws SQLException {
        String sql = "SELECT id, titolo, descrizione FROM notes ORDER BY id DESC";
        List<Note> out = new ArrayList<>();
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                out.add(mapRow(rs));
            }
        }
        return out;
    }

    @Override
    public Optional<Note> findById(long id) throws SQLException {
        String sql = "SELECT id, titolo, descrizione FROM notes WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean delete(long id) throws SQLException {
        String sql = "DELETE FROM notes WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private static Note mapRow(ResultSet rs) throws SQLException {
        return new Note(rs.getLong("id"), rs.getString("titolo"), rs.getString("descrizione"));
    }

    @Override
    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}
