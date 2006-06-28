package net.suberic.util.gui.propedit;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import net.suberic.util.*;

/**
 * This is a Swing implemenation of a boolean PropertyEditorUI.
 */
public class BooleanEditorPane extends SwingPropertyEditor {
  JCheckBox inputField;
  JLabel label;
  boolean originalBoolean = false;

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
    debug = newManager.getProperty("editors.debug", "false").equalsIgnoreCase("true");

    if (debug) {
      System.out.println("configuring Boolean editor with property " + propertyName + ", editorTemplate " + editorTemplate);
    }

    originalBoolean = originalValue.equalsIgnoreCase("true");

    if (debug) {
      System.out.println("configuring with value getProperty(" + property + ", manager.getProperty(" + template + ", \"false\")) = " + originalBoolean);
    }


    String defaultLabel;
    int dotIndex = property.lastIndexOf(".");
    if (dotIndex == -1)
      defaultLabel = new String(property);
    else
      defaultLabel = property.substring(dotIndex+1);

    label = new JLabel(manager.getProperty(editorTemplate + ".label", defaultLabel));

    inputField = new JCheckBox();

    inputField.setSelected(originalBoolean);

    inputField.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent e) {
          String newValue = null;
          if (e.getStateChange() == ItemEvent.SELECTED) {
            newValue = "true";
          } else if (e.getStateChange() == ItemEvent.DESELECTED) {
            newValue = "false";
          }

          try {
            if (newValue != null) {
              firePropertyChangingEvent(newValue);
              System.err.println("firing propertyChangedEvent; newValue = " + newValue);
              firePropertyChangedEvent(newValue);
            } else {
              System.err.println("neither selected nor deselected.");
            }
          } catch (PropertyValueVetoException pvve) {
            manager.getFactory().showError(inputField, "Error changing value " + label.getText() + " to " + newValue+ ":  " + pvve.getReason());
            inputField.setSelected(! inputField.isSelected());
          }
        }
      });

    this.add(inputField);
    this.add(label);
    this.setEnabled(isEnabled);

    //labelComponent = label;
    //valueComponent = inputField;

    manager.registerPropertyEditor(property, this);
  }

  /**
   * as defined in net.suberic.util.gui.PropertyEditorUI
   */
  public void setValue() {
    if (isEnabled()) {
      if (inputField.isSelected() != originalBoolean || manager.getProperty(property, "unset").equals("unset")) {
        String newValue;
        if (inputField.isSelected())
          newValue = "true";
        else
          newValue = "false";

        manager.setProperty(property, newValue);
      }
    }
  }

  /**
   * Returns the current values of the edited properties as a
   * java.util.Properties object.
   */
  public java.util.Properties getValue() {
    java.util.Properties retProps = new java.util.Properties();

    if (inputField.isSelected())
      retProps.setProperty(property, "true");
    else
      retProps.setProperty(property, "false");
    return retProps;
  }

  /**
   * This resets the editor to the original (or latest set, if setValue()
   * has been called) value of the edited property.
   */
  public void resetDefaultValue() {
    // this will be handled by the listener on the inputField, so we don't
    // have to send any events here.
    inputField.setSelected(originalBoolean);
  }

  /**
   * Sets the enabled property of the PropertyEditorUI.  Disabled
   * editors should not be able to do setValue() calls.
   */
  public void setEnabled(boolean newValue) {
    if (inputField != null) {
      inputField.setEnabled(newValue);
    }
    if (label != null) {
      label.setEnabled(newValue);
    }
    enabled=newValue;
  }

  /**
   * Gets the parent PropertyEditorPane for the given component.
   */
  public PropertyEditorPane getPropertyEditorPane() {
    return getPropertyEditorPane(this);
  }

}

