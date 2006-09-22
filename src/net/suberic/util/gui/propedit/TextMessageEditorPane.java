package net.suberic.util.gui.propedit;
import javax.swing.*;
import net.suberic.util.*;

/**
 * An EditorPane which actually just displays some text.
 *
 */
public class TextMessageEditorPane extends SwingPropertyEditor {
  protected JLabel label;
  protected JTextField textField = null;

  /**
   * @param propertyName The property to be edited.
   * @param template The property that will define the layout of the
   *                 editor.
   * @param manager The PropertyEditorManager that will manage the
   *                   changes.
   * @param isEnabled Whether or not this editor is enabled by default.
   */
  public void configureEditor(String propertyName, String template, String propertyBaseName, PropertyEditorManager newManager, boolean isEnabled) {
    configureBasic(propertyName, template, propertyBaseName, newManager, isEnabled);

    String defaultLabel;
    int dotIndex = editorTemplate.lastIndexOf(".");
    if (dotIndex == -1)
      defaultLabel = new String(editorTemplate);
    else
      defaultLabel = property.substring(dotIndex+1);

    //JLabel mainLabel = new JLabel(manager.getProperty(editorTemplate + ".label", defaultLabel));

    //this.add(mainLabel);
    this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), manager.getProperty(editorTemplate + ".label", defaultLabel)));
    //System.err.println("radioeditorpane:  mainLabel = " + mainLabel.getText());
    //layout.putConstraint(SpringLayout.WEST, mainLabel, 0, SpringLayout.WEST, this);
    //layout.putConstraint(SpringLayout.NORTH, mainLabel, 0, SpringLayout.NORTH, this);

    textField = new JTextField(manager.getProperty(editorTemplate + ".message", "No message."));
    this.add(textField);

  }

  /**
   * This writes the currently configured value in the PropertyEditorUI
   * to the source VariableBundle.
   *
   * A no-op in this case.
   */
  public void setValue() {

  }

  /**
   * Returns the current values of the edited properties as a
   * java.util.Properties object.
   *
   * This implementation returns an empty array.
   */
  public java.util.Properties getValue() {
    java.util.Properties retProps = new java.util.Properties();

    return retProps;
  }

  /**
   * This resets the editor to the original (or latest set, if setValue()
   * has been called) value of the edited property.
   */
  public void resetDefaultValue() {
  }

  /**
   * Selects the given value.
   */
  public void setSelectedValue(String newValue) {
  }

  /**
   * Returns whether or not the current list selection has changed from
   * the last save.
   */
  public boolean isChanged() {
    return false;
  }

  /**
   * Sets the enabled property of the PropertyEditorUI.  Disabled
   * editors should not be able to do setValue() calls.
   */
  public void setEnabled(boolean newValue) {
    if (label != null)
      label.setEnabled(newValue);
    if (textField != null)
      textField.setEnabled(newValue);
  }

  /**
   * Gets the parent PropertyEditorPane for the given component.
   */
  public PropertyEditorPane getPropertyEditorPane() {
    return getPropertyEditorPane(this);
  }

  /**
   * Returns the display value for this property.
   */
  public String getDisplayValue() {
    return getProperty();
  }

}
