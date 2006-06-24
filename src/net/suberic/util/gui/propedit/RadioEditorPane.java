package net.suberic.util.gui.propedit;
import javax.swing.*;
import net.suberic.util.*;
import java.awt.event.*;
import java.util.*;

/**
 * An EditorPane which allows a user to select from a set of choices.
 *
 */
public class RadioEditorPane extends SwingPropertyEditor {
  protected int originalIndex;
  protected String mOriginalValue;
  protected JLabel label;
  protected ButtonGroup buttonGroup;
  JButton addButton;
  protected HashMap labelToValueMap = new HashMap();
  protected int currentIndex = -1;

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

    SpringLayout layout = new  SpringLayout();
    this.setLayout(layout);

    String defaultLabel;
    int dotIndex = property.lastIndexOf(".");
    if (dotIndex == -1)
      defaultLabel = new String(property);
    else
      defaultLabel = property.substring(dotIndex+1);

    JLabel mainLabel = new JLabel(manager.getProperty(editorTemplate + ".label", defaultLabel));

    this.add(mainLabel);
    layout.putConstraint(SpringLayout.WEST, mainLabel, 0, SpringLayout.WEST, this);
    layout.putConstraint(SpringLayout.NORTH, mainLabel, 0, SpringLayout.NORTH, this);

    List<String> allowedValues = manager.getPropertyAsList(editorTemplate + ".allowedValues", "");

    JComponent previous = mainLabel;
    Spring widthSpring = layout.getConstraints(mainLabel).getWidth();
    for(String allowedValue: allowedValues) {
      String label = manager.getProperty(editorTemplate + ".listMapping." + allowedValue + ".label", allowedValue);
      JRadioButton button = new JRadioButton(label);
      button.setActionCommand(allowedValue);
      buttonGroup.add(button);
      this.add(button);
      layout.putConstraint(SpringLayout.WEST, button, 5, SpringLayout.WEST, this);
      layout.putConstraint(SpringLayout.NORTH, button, 0, SpringLayout.NORTH, previous);
      previous = button;
      widthSpring = Spring.max(Spring.sum(layout.getConstraints(button).getWidth(), Spring.constant(5)), widthSpring);
    }

    layout.putConstraint(SpringLayout.SOUTH, this, 5, SpringLayout.SOUTH, previous);
    layout.putConstraint(SpringLayout.EAST, this, widthSpring, SpringLayout.WEST, this);

  }


  /**
   * This writes the currently configured value in the PropertyEditorUI
   * to the source VariableBundle.
   */
  public void setValue() {

  }

  /**
   * Returns the current values of the edited properties as a
   * java.util.Properties object.
   */
  public java.util.Properties getValue() {
    java.util.Properties retProps = new java.util.Properties();

    retProps.setProperty(property, "foo");

    return retProps;
  }

  /**
   * This resets the editor to the original (or latest set, if setValue()
   * has been called) value of the edited property.
   */
  public void resetDefaultValue() {

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

  }

  /**
   * Gets the parent PropertyEditorPane for the given component.
   */
  public PropertyEditorPane getPropertyEditorPane() {
    return getPropertyEditorPane(this);
  }


}
