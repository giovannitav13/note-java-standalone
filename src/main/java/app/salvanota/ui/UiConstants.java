package app.salvanota.ui;

import javax.swing.BorderFactory;
import javax.swing.UIManager;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;

/**
 * Spaziature, font e bordi coerenti nell'interfaccia.
 */
public final class UiConstants {

    public static final int GAP_SM = 6;
    public static final int GAP_MD = 10;
    public static final int GAP_LG = 16;
    public static final int GAP_XL = 22;

    public static final Insets FIELD_INSETS = new Insets(GAP_SM, GAP_MD, GAP_SM, GAP_MD);

    private UiConstants() {
    }

    public static Font sectionTitleFont() {
        Font base = UIManager.getFont("Label.font");
        if (base == null) {
            base = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
        }
        return base.deriveFont(Font.BOLD, base.getSize2D());
    }

    public static Font captionFont() {
        Font base = UIManager.getFont("Label.font");
        if (base == null) {
            return new Font(Font.SANS_SERIF, Font.PLAIN, 12);
        }
        return base.deriveFont(base.getSize2D() - 1f);
    }

    public static Border panelPadding() {
        return BorderFactory.createEmptyBorder(GAP_LG, GAP_LG, GAP_LG, GAP_LG);
    }

    public static Border subtleLineBorder() {
        Color c = UIManager.getColor("Component.borderColor");
        if (c == null) {
            c = new Color(0, 0, 0, 40);
        }
        return BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, c),
                BorderFactory.createEmptyBorder(0, 0, GAP_MD, 0)
        );
    }
}
