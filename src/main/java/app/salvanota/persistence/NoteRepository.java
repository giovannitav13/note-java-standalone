package app.salvanota.persistence;

import app.salvanota.model.Note;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Accesso in lettura/scrittura alle note.
 */
public interface NoteRepository extends AutoCloseable {

    long save(Note note) throws SQLException;

    List<Note> findAll() throws SQLException;

    Optional<Note> findById(long id) throws SQLException;

    boolean delete(long id) throws SQLException;

    @Override
    void close() throws SQLException;
}
