package app.salvanota.ui;

import app.salvanota.model.Note;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;

/**
 * Finestra modale per la sola lettura di una nota.
 */
public final class NoteReaderDialog extends JDialog {

    public NoteReaderDialog(Window owner, Note note) {
        super(owner, note.getTitolo(), ModalityType.APPLICATION_MODAL);

        JTextArea body = new JTextArea(note.getDescrizione());
        body.setEditable(false);
        body.setLineWrap(true);
        body.setWrapStyleWord(true);
        body.setMargin(UiConstants.FIELD_INSETS);
        body.setCaretPosition(0);

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        JButton close = new JButton("Chiudi");
        close.addActionListener(e -> dispose());

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, UiConstants.GAP_SM, 0));
        footer.setOpaque(false);
        footer.add(close);

        setLayout(new BorderLayout(UiConstants.GAP_MD, UiConstants.GAP_MD));
        add(scroll, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);

        setMinimumSize(new Dimension(400, 240));
        setPreferredSize(new Dimension(520, 360));
        pack();
        setLocationRelativeTo(owner);
    }
}
