package app.salvanota.ui;

import app.salvanota.runtime.KeepAwakeController;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * Tab impostazioni generiche e controlli di sessione.
 */
public final class OptionsPanel extends JPanel {

    public OptionsPanel(KeepAwakeController keepAwake) {
        setLayout(new BorderLayout());
        setBorder(UiConstants.panelPadding());

        JLabel heading = new JLabel("Sessione");
        heading.setFont(UiConstants.sectionTitleFont());
        heading.setBorder(UiConstants.subtleLineBorder());

        JLabel stato = new JLabel("Stato: in pausa", SwingConstants.LEFT);
        stato.setFont(stato.getFont().deriveFont(Font.PLAIN, 14f));
        stato.setForeground(UIManager.getColor("Label.disabledForeground"));

        JButton toggle = new JButton("Avvia");
        toggle.putClientProperty("JButton.buttonType", "roundRect");

        toggle.addActionListener(e -> {
            if (keepAwake.isRunning()) {
                keepAwake.stop();
                stato.setText("Stato: in pausa");
                stato.setForeground(UIManager.getColor("Label.disabledForeground"));
                toggle.setText("Avvia");
            } else {
                keepAwake.start();
                stato.setText("Stato: attivo");
                stato.setForeground(activeColor());
                toggle.setText("Ferma");
            }
        });

        JPanel card = new JPanel(new GridBagLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                UIManager.getBorder("TextField.border"),
                new EmptyBorder(UiConstants.GAP_LG, UiConstants.GAP_LG, UiConstants.GAP_LG, UiConstants.GAP_LG)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, UiConstants.GAP_MD, 0);
        card.add(stato, gbc);

        gbc.gridy++;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.insets = new Insets(UiConstants.GAP_MD, 0, 0, 0);
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        row.setOpaque(false);
        row.add(toggle);
        card.add(row, gbc);

        JPanel north = new JPanel(new BorderLayout(0, UiConstants.GAP_MD));
        north.setOpaque(false);
        north.add(heading, BorderLayout.NORTH);

        add(north, BorderLayout.NORTH);
        add(card, BorderLayout.CENTER);
    }

    private static Color activeColor() {
        Color c = UIManager.getColor("Component.focusedBorderColor");
        return c != null ? c : new Color(0, 120, 72);
    }
}
