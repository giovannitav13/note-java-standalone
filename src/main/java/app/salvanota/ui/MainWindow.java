package app.salvanota.ui;

import app.salvanota.persistence.NoteRepository;
import app.salvanota.runtime.KeepAwakeController;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;

/**
 * Finestra principale dell'applicazione.
 */
public final class MainWindow extends JFrame {

    private final NoteRepository repository;
    private final KeepAwakeController keepAwake = new KeepAwakeController();

    public MainWindow(NoteRepository repository) {
        this.repository = repository;

        setTitle("Salva nota");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JTabbedPane tabs = new JTabbedPane();
        tabs.putClientProperty("JTabbedPane.showTabSeparators", Boolean.TRUE);
        tabs.addTab("Note", new NotesPanel(this, repository));
        tabs.addTab("Opzioni", new OptionsPanel(keepAwake));

        add(tabs, BorderLayout.CENTER);

        setMinimumSize(new Dimension(880, 560));
        pack();
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                keepAwake.stop();
                try {
                    MainWindow.this.repository.close();
                } catch (SQLException ignored) {
                }
            }
        });
    }
}
