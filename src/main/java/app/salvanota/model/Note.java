package app.salvanota.model;

import java.util.Objects;

/**
 * Nota con identificativo opzionale (0 se non ancora persistita).
 */
public final class Note {

    private final long id;
    private final String titolo;
    private final String descrizione;

    public Note(String titolo, String descrizione) {
        this(0, titolo, descrizione);
    }

    public Note(long id, String titolo, String descrizione) {
        this.id = id;
        this.titolo = Objects.requireNonNull(titolo, "titolo");
        this.descrizione = Objects.requireNonNull(descrizione, "descrizione");
    }

    public long getId() {
        return id;
    }

    public String getTitolo() {
        return titolo;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public Note withId(long newId) {
        return new Note(newId, titolo, descrizione);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Note other)) {
            return false;
        }
        return id == other.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    @Override
    public String toString() {
        return "Note{id=" + id + ", titolo='" + titolo + "'}";
    }
}
