package net.suberic.util.swing;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

/**
 * A panel which allows the user to select a Font from the list of all
 * available fonts on the system.
 */
public class JFontChooser extends JComponent {

  JList fontList = null;
  JList styleList = null;
  JList sizeList = null;

  private static String[] allowedFontSizes = new String[] {
    "6", "8", "9", "10", "11", "12", "14", "16", "18", "20", "22", "24", "36",
    "48", "72"
  };

  private static String[] allowedFontStyles = new String[] {
    "Regular", "Italic", "Bold", "Bold Italic"
  };

  /**
   * Creates a new JFontChooser with a default font.
   */
  public JFontChooser() {
    
  }

  /**
   * Creates a new JFontChooser with the specified font.
   */
  public JFontChooser(Font initialFont) {

  }

  /**
   * Creates a new JFontChooser with the given preview phrase and a
   * default font.
   */
  public JFontChooser(String previewPhrase) {

  }

  /**
   * Creates a new JFontChooser with the specified font and given
   * preview phrase.
   */
  public JFontChooser(Font initialFont, String previewPhrase) {

  }

  /**
   * Creates and returns a new dialog containing the specified
   * FontChooser pane along with "OK", "Cancel", and "Reset"
   * buttons.
   */
  public static JDialog createDialog(Component parent, String title, 
				     boolean modal,
				     JFontChooser chooserPane,
				     ActionListener okListener,
				     ActionListener cancelListener) {
    return null;
  }

  /**
   * Returns the currently selected Font.
   */
  public Font getFont() {
    return null;
  }

  /**
   * Returns the currently selected Font in a String form which is
   * compatible with <code>Font.decode(String)</code>.
   */
  public String getFontString() {
    return null;
  }

  /**
   * Sets the currently selected Font to the given Font.
   */
  public void setFont(Font newFont) {

  }

  /**
   * Shows a modal font-chooser dialog and blocks until the
   * dialog is hidden.  If the user presses the "OK" button, then
   * this method hides/disposes the dialog and returns the selected color.
   * If the user presses the "Cancel" button or closes the dialog without 
   * pressing "OK", then this method hides/disposes the dialog and returns
   * null.
   */
  public static Font showDialog(Component component, String title, 
				Font initialFont) {

    return null;
  }

  /* private functions */

  private Box createChooserPanel(Font f) {
    Box chooser = Box.createHorizontalBox();
    
    Box fontBox = Box.createVerticalBox();
    fontBox.add(new JLabel("Font"));
    fontList = new JList(getFontNames());
    JScrollPane fontNameScroller = new JScrollPane(fontList);
    fontBox.add(fontNameScroller);

    chooser.add(fontBox);
    chooser.add(Box.createHorizontalStrut(10));

    Box styleBox = Box.createVerticalBox();
    styleBox.add(new JLabel("Font Style"));
    styleList = new JList(getStyleNames());
    JScrollPane styleScroller = new JScrollPane(styleList);
    styleBox.add(styleScroller);

    chooser.add(styleBox);
    chooser.add(Box.createHorizontalStrut(10));

    Box sizeBox = Box.createVerticalBox();
    sizeBox.add(new JLabel("Size"));
    sizeList = new JList(getSizeNames());
    JScrollPane sizeScroller = new JScrollPane(sizeList);
    sizeBox.add(sizeScroller);

    chooser.add(sizeBox);

    return chooser;
  }

  private Box createPreviewPanel(Font f) {
    return null;
  }

  private Box createButtonPanel(ActionListener okListener, ActionListener cancelListener) {
    return null;
  }

  private String[] getFontNames() {
    return GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
  }

  private String[] getStyleNames() {
    return allowedFontStyles;
  }

  private String[] getSizeNames() {
    return allowedFontSizes;
  }

  
}
