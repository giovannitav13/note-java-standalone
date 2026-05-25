package app.salvanota;

import app.salvanota.persistence.NoteRepository;
import app.salvanota.persistence.SqliteNoteRepository;
import app.salvanota.ui.Dialogs;
import app.salvanota.ui.MainWindow;
import com.formdev.flatlaf.FlatIntelliJLaf;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;

/**
 * Punto d'ingresso: tema, database in {@link ApplicationDirectories}, finestra principale.
 */
public final class SalvaNotaApplication {

    private SalvaNotaApplication() {
    }

    public static void main(String[] args) {
        FlatIntelliJLaf.setup();
        UIManager.put("ScrollBar.showButtons", Boolean.FALSE);

        SwingUtilities.invokeLater(() -> {
            try {
                Path db = ApplicationDirectories.dataDirectory()
                        .resolve(ApplicationDirectories.DATABASE_FILE_NAME);
                NoteRepository repository = new SqliteNoteRepository(db);
                new MainWindow(repository).setVisible(true);
            } catch (IOException | SQLException e) {
                String msg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
                Dialogs.error(null, msg);
            }
        });
    }
}
