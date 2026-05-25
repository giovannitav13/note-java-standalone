package app.salvanota;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Percorsi dati applicazione (directory utente, nome file database).
 */
public final class ApplicationDirectories {

    public static final String DATABASE_FILE_NAME = "notes.db";
    private static final String APP_FOLDER = ".salva-nota";

    private ApplicationDirectories() {
    }

    /**
     * Directory persistente sotto la home utente ({@code ~/.salva-nota}).
     */
    public static Path dataDirectory() throws IOException {
        Path dir = Path.of(System.getProperty("user.home"), APP_FOLDER);
        Files.createDirectories(dir);
        return dir;
    }
}
