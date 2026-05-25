# Panoramica progetto

Data analisi: 2026-05-25

## Sintesi

Il progetto e una piccola applicazione desktop Java chiamata "Salva nota".
Serve a creare, visualizzare ed eliminare note personali salvate in un database SQLite locale.
L'interfaccia e realizzata con Swing e FlatLaf; la persistenza e incapsulata dietro una interfaccia `NoteRepository`.

## Stack e build

- Linguaggio: Java 17 (`maven.compiler.release` = 17).
- Build tool: Maven.
- Artifact: jar normale e jar standalone tramite `maven-shade-plugin`.
- Main class: `app.salvanota.SalvaNotaApplication`.
- Dipendenze principali:
  - `com.formdev:flatlaf:3.5.4` per look and feel Swing moderno.
  - `org.xerial:sqlite-jdbc:3.45.1.0` per SQLite.
- Plugin utili:
  - `maven-compiler-plugin`
  - `maven-jar-plugin`
  - `maven-shade-plugin`
  - `exec-maven-plugin`

Comandi attesi:

```powershell
mvn test
mvn package
mvn exec:java
java -jar target/salva-nota-1.0.0-standalone.jar
```

## Struttura principale

```text
src/main/java/app/salvanota/
  SalvaNotaApplication.java        # Entry point: tema, repository, finestra
  ApplicationDirectories.java      # Directory dati utente
  model/
    Note.java                      # Modello immutabile della nota
  persistence/
    NoteRepository.java            # Contratto di persistenza
    SqliteNoteRepository.java      # Implementazione SQLite
  runtime/
    KeepAwakeController.java       # Movimento mouse periodico opzionale
  ui/
    MainWindow.java                # JFrame principale con tab
    NotesPanel.java                # UI archivio/editor note
    OptionsPanel.java              # UI opzioni/sessione
    NoteReaderDialog.java          # Dialog lettura nota
    Dialogs.java                   # JOptionPane centralizzati
    UiConstants.java               # Spaziature, font, bordi
```

Sono presenti anche directory generate o IDE:

- `.idea/` e `first.iml`: configurazione IntelliJ.
- `target/`: output Maven.
- `out/`: output IntelliJ.
- `lib/sqlite-jdbc-3.45.1.0.jar`: jar SQLite locale, probabilmente residuo o usato dall'IDE; Maven scarica la dipendenza dal `pom.xml`.
- `notes.db`: database SQLite nella root del progetto, ma l'app in runtime usa `~/.salva-nota/notes.db`.

## Flusso applicativo

1. `SalvaNotaApplication.main` imposta FlatLaf e apre la UI su Event Dispatch Thread.
2. Calcola il database in `ApplicationDirectories.dataDirectory()/notes.db`, quindi normalmente:
   - Windows: `C:\Users\<utente>\.salva-nota\notes.db`
   - generico Java: `System.getProperty("user.home")/.salva-nota/notes.db`
3. Crea `SqliteNoteRepository`.
4. Apre `MainWindow` passando il repository.
5. `MainWindow` crea due tab:
   - `Note`: gestione note.
   - `Opzioni`: controllo keep-awake.
6. Alla chiusura della finestra ferma il keep-awake e chiude la connessione SQLite.

## Dominio dati

Il modello `Note` contiene:

- `id` (`long`): 0 se non ancora persistita.
- `titolo` (`String`): obbligatorio lato modello, validato non vuoto in UI.
- `descrizione` (`String`): obbligatoria lato modello, ma puo essere vuota.

`Note` e immutabile: tutti i campi sono `final`; per cambiare id esiste `withId`.
`equals` e `hashCode` si basano solo sull'id.

## Persistenza

`NoteRepository` espone:

- `save(Note note)`: inserisce se `id <= 0`, aggiorna se `id > 0`.
- `findAll()`: restituisce tutte le note.
- `findById(long id)`: cerca una nota.
- `delete(long id)`: elimina una nota.
- `close()`: chiude risorse.

`SqliteNoteRepository`:

- apre una singola `Connection` JDBC SQLite;
- abilita autocommit;
- crea la tabella se manca;
- ordina le note per `id DESC`.

Schema corrente:

```sql
CREATE TABLE IF NOT EXISTS notes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    titolo TEXT NOT NULL,
    descrizione TEXT NOT NULL
)
```

Nota importante: non esiste ancora un sistema di migrazioni. Se si aggiungono campi, servira gestire `ALTER TABLE`, versioning schema o una libreria/migrazione semplice.

## Interfaccia utente

La UI e Swing classica con FlatLaf.

`MainWindow`:

- `JFrame` principale.
- `JTabbedPane` con tab "Note" e "Opzioni".
- dimensione minima 880x560.

`NotesPanel`:

- layout a split orizzontale:
  - sinistra: lista note salvate e pulsante elimina;
  - destra: form nuova nota.
- salvataggio e cancellazione avvengono in `SwingWorker`, quindi non bloccano la UI.
- doppio/click su una nota apre `NoteReaderDialog` in sola lettura.
- dopo save/delete ricarica la lista dal repository.

`OptionsPanel`:

- mostra stato sessione: "in pausa" o "attivo".
- pulsante "Avvia"/"Ferma" per `KeepAwakeController`.

`UiConstants` e `Dialogs` centralizzano dettagli ricorrenti di stile e messaggi.

## Keep awake

`KeepAwakeController` usa `java.awt.Robot` per muovere leggermente il mouse ogni 30-40 secondi.

Dettagli:

- stato gestito con `AtomicBoolean`;
- thread daemon chiamato `salvanota-keepawake`;
- ogni ciclo:
  - legge posizione mouse corrente;
  - muove di pochi pixel;
  - aspetta 120-350 ms;
  - riporta il mouse alla posizione iniziale;
  - aspetta 30-40 secondi.
- se `Robot` o l'ambiente grafico falliscono, spegne lo stato senza mostrare errore.

Questo componente e separato dalla persistenza e agganciato solo alla tab Opzioni.

## Punti forti

- Struttura semplice e leggibile.
- Separazione gia buona tra UI, modello, persistenza e runtime.
- Repository astratto: facilita test futuri o cambio storage.
- Operazioni DB chiamate da `SwingWorker`: buon punto di partenza per non congelare Swing.
- Jar standalone gia configurato con Maven Shade.

## Limiti e rischi tecnici

- Mancano test automatici.
- Mancano migrazioni DB: ogni evoluzione dello schema va progettata.
- `Note.equals` considera uguali due note con id 0; puo creare comportamenti inattesi se si maneggiano piu note non salvate in collezioni.
- Il repository usa una sola `Connection`; va bene per app piccola, ma attenzione se in futuro aumentano thread o operazioni concorrenti.
- Non ci sono timestamp (`created_at`, `updated_at`), ricerca, categorie, preferiti o ordinamenti custom.
- Non esiste editing di note esistenti dalla UI, anche se il repository supporta update.
- Il database `notes.db` nella root non e quello usato dall'app a runtime, salvo avvii/configurazioni esterne.
- Alcuni testi letti in console appaiono con mojibake, ma probabilmente i sorgenti sono UTF-8 e il problema e la code page della shell.

## Direzioni naturali per estenderlo

Possibili prossimi passi, in ordine ragionevole:

1. Aggiungere test al repository usando un database temporaneo.
2. Introdurre migrazioni schema leggere.
3. Aggiungere campi a `Note`: data creazione, ultima modifica, tag/categoria, preferito.
4. Implementare modifica note esistenti nella UI.
5. Aggiungere ricerca full-text o filtro titolo/descrizione.
6. Migliorare gestione errori e logging.
7. Separare meglio stato UI e servizi se la UI cresce molto.
8. Aggiungere export/import delle note.
9. Rendere configurabile il percorso database per test, backup o profili.
10. Valutare un layer applicativo tra UI e repository se arrivano molte regole di dominio.

## File da toccare spesso in futuro

- Nuove funzionalita note: `model/Note.java`, `persistence/*`, `ui/NotesPanel.java`.
- Nuove schermate o tab: `ui/MainWindow.java` e nuovi pannelli in `ui/`.
- Cambi di stile coerenti: `ui/UiConstants.java`.
- Persistenza e schema: `persistence/SqliteNoteRepository.java`.
- Configurazione build o dipendenze: `pom.xml`.

## Stato iniziale git

Al momento dell'analisi `git status --short` non mostrava modifiche pendenti.

## Verifica eseguita

- `mvn test` non e eseguibile in questa sessione perche `mvn` non e nel `PATH` e non e presente un Maven wrapper (`mvnw`).
- Java disponibile: OpenJDK/Temurin 17.0.18.
- `javac --release 17` sui sorgenti principali e passato usando le dipendenze gia presenti nella cache Maven locale:
  - FlatLaf 3.5.4
  - SQLite JDBC 3.45.1.0
