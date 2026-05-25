package app.salvanota.ui;

import app.salvanota.model.Note;
import app.salvanota.persistence.NoteRepository;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Editor note e archivio con layout a due pannelli.
 */
public final class NotesPanel extends JPanel {

    private final Window window;
    private final NoteRepository repository;

    public NotesPanel(Window window, NoteRepository repository) {
        this.window = window;
        this.repository = repository;
        setLayout(new BorderLayout());
        setBorder(UiConstants.panelPadding());

        JTextField titleField = new JTextField();
        titleField.putClientProperty("JTextField.placeholderText", "Titolo della nota");

        JTextArea bodyField = new JTextArea(8, 32);
        bodyField.setLineWrap(true);
        bodyField.setWrapStyleWord(true);
        bodyField.setMargin(UiConstants.FIELD_INSETS);
        bodyField.putClientProperty("JTextArea.placeholderText", "Testo, promemoria, dettagli…");

        JButton saveButton = new JButton("Salva nota");
        saveButton.putClientProperty("JButton.buttonType", "roundRect");

        DefaultListModel<Note> listModel = new DefaultListModel<>();
        JList<Note> list = new JList<>(listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setFixedCellHeight(36);
        list.setCellRenderer(new NoteListCellRenderer());

        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int idx = list.locationToIndex(e.getPoint());
                if (idx < 0 || idx >= listModel.getSize()) {
                    return;
                }
                Rectangle cell = list.getCellBounds(idx, idx);
                if (cell == null || !cell.contains(e.getPoint())) {
                    return;
                }
                new NoteReaderDialog(window, listModel.getElementAt(idx)).setVisible(true);
            }
        });

        JButton deleteButton = new JButton("Elimina nota");
        deleteButton.setEnabled(false);
        deleteButton.putClientProperty("JButton.buttonType", "roundRect");
        list.addListSelectionListener(e -> updateDeleteButtonState(e, list, deleteButton));
        deleteButton.addActionListener(e ->
                deleteSelectedNote(list, listModel, deleteButton));

        JPanel editorCard = buildEditorCard(titleField, bodyField, saveButton);
        JScrollPane listScroll = new JScrollPane(list);
        listScroll.setMinimumSize(new Dimension(200, 120));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                wrapArchive(listScroll, deleteButton), editorCard);
        split.setResizeWeight(0.32);
        split.setContinuousLayout(true);
        split.setBorder(BorderFactory.createEmptyBorder());

        add(split, BorderLayout.CENTER);

        saveButton.addActionListener(e -> saveNote(titleField, bodyField, saveButton, listModel));
        reloadList(listModel);

        JLabel banner = new JLabel("Archivio locale · SQLite");
        banner.setFont(UiConstants.captionFont());
        banner.setForeground(UIManager.getColor("Label.disabledForeground"));
        add(banner, BorderLayout.SOUTH);
    }

    private JPanel wrapArchive(JScrollPane listScroll, JButton deleteButton) {
        JPanel wrap = new JPanel(new BorderLayout(0, UiConstants.GAP_SM));
        TitledBorder tb = BorderFactory.createTitledBorder(
                BorderFactory.createEmptyBorder(),
                " Note salvate ",
                TitledBorder.LEADING,
                TitledBorder.DEFAULT_POSITION,
                UiConstants.sectionTitleFont()
        );
        wrap.setBorder(tb);
        wrap.add(listScroll, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, UiConstants.GAP_SM, 0));
        actions.setOpaque(false);
        actions.add(deleteButton);
        wrap.add(actions, BorderLayout.SOUTH);

        return wrap;
    }

    private void deleteSelectedNote(JList<Note> list, DefaultListModel<Note> listModel, JButton deleteButton) {
        Note selected = list.getSelectedValue();
        if (selected == null) {
            Dialogs.warn(window, "Seleziona una nota dall'elenco.");
            return;
        }
        String title = selected.getTitolo();
        if (!Dialogs.confirm(window,
                "Eliminare la nota «" + title + "»? L'operazione non può essere annullata.")) {
            return;
        }

        long id = selected.getId();
        deleteButton.setEnabled(false);
        new SwingWorker<List<Note>, Void>() {
            @Override
            protected List<Note> doInBackground() throws Exception {
                repository.delete(id);
                return repository.findAll();
            }

            @Override
            protected void done() {
                try {
                    applyList(listModel, get());
                    list.clearSelection();
                } catch (Exception ex) {
                    Throwable t = ex.getCause() != null ? ex.getCause() : ex;
                    Dialogs.error(window, t.getMessage());
                } finally {
                    deleteButton.setEnabled(list.getSelectedValue() != null);
                }
            }
        }.execute();
    }

    private static void updateDeleteButtonState(ListSelectionEvent e, JList<Note> list, JButton deleteButton) {
        if (e.getValueIsAdjusting()) {
            return;
        }
        deleteButton.setEnabled(list.getSelectedValue() != null);
    }

    private JPanel buildEditorCard(JTextField titleField, JTextArea bodyField, JButton saveButton) {
        JPanel card = new JPanel(new BorderLayout(0, UiConstants.GAP_MD));
        TitledBorder tb = BorderFactory.createTitledBorder(
                BorderFactory.createEmptyBorder(),
                " Nuova nota ",
                TitledBorder.LEADING,
                TitledBorder.DEFAULT_POSITION,
                UiConstants.sectionTitleFont()
        );
        card.setBorder(BorderFactory.createCompoundBorder(
                tb,
                new EmptyBorder(UiConstants.GAP_MD, UiConstants.GAP_LG, UiConstants.GAP_LG, UiConstants.GAP_LG)
        ));

        JPanel fields = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, UiConstants.GAP_SM, 0);
        JLabel l1 = new JLabel("Titolo");
        l1.setFont(UiConstants.captionFont());
        fields.add(l1, gbc);

        gbc.gridy++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.insets = new Insets(0, 0, UiConstants.GAP_MD, 0);
        fields.add(titleField, gbc);

        gbc.gridy++;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.insets = new Insets(0, 0, UiConstants.GAP_SM, 0);
        JLabel l2 = new JLabel("Descrizione");
        l2.setFont(UiConstants.captionFont());
        fields.add(l2, gbc);

        gbc.gridy++;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.insets = new Insets(0, 0, UiConstants.GAP_MD, 0);
        fields.add(new JScrollPane(bodyField), gbc);

        gbc.gridy++;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weighty = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(UiConstants.GAP_SM, 0, 0, 0);
        fields.add(saveButton, gbc);

        card.add(fields, BorderLayout.CENTER);
        return card;
    }

    private void saveNote(JTextField titleField, JTextArea bodyField, JButton saveButton,
                          DefaultListModel<Note> listModel) {
        String titolo = titleField.getText().trim();
        String descrizione = bodyField.getText().trim();
        if (titolo.isEmpty()) {
            Dialogs.warn(window, "Inserisci un titolo prima di salvare.");
            return;
        }

        saveButton.setEnabled(false);
        new SwingWorker<List<Note>, Void>() {
            @Override
            protected List<Note> doInBackground() throws Exception {
                repository.save(new Note(titolo, descrizione));
                return repository.findAll();
            }

            @Override
            protected void done() {
                saveButton.setEnabled(true);
                try {
                    applyList(listModel, get());
                    titleField.setText("");
                    bodyField.setText("");
                } catch (Exception ex) {
                    Throwable t = ex.getCause() != null ? ex.getCause() : ex;
                    Dialogs.error(window, t.getMessage());
                }
            }
        }.execute();
    }

    private void reloadList(DefaultListModel<Note> listModel) {
        new SwingWorker<List<Note>, Void>() {
            @Override
            protected List<Note> doInBackground() throws Exception {
                return repository.findAll();
            }

            @Override
            protected void done() {
                try {
                    applyList(listModel, get());
                } catch (Exception ex) {
                    Throwable t = ex.getCause() != null ? ex.getCause() : ex;
                    Dialogs.error(window, t.getMessage());
                }
            }
        }.execute();
    }

    private static void applyList(DefaultListModel<Note> listModel, List<Note> notes) {
        listModel.clear();
        for (Note n : notes) {
            listModel.addElement(n);
        }
    }

    private static final class NoteListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setBorder(new EmptyBorder(4, 12, 4, 12));
            label.setText(value instanceof Note n ? n.getTitolo() : " ");
            if (!isSelected) {
                label.setFont(label.getFont().deriveFont(Font.PLAIN));
            }
            return label;
        }
    }
}
