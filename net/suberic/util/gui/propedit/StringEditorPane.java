package net.suberic.util.gui.propedit;
import javax.swing.*;
import net.suberic.util.*;
import java.awt.FlowLayout;

/**
 * The default EditorPane.  Just shows a text field in which a user
 * can enter a String.
 */
public class StringEditorPane extends SwingPropertyEditor {
  
  JLabel label;
  JTextField inputField;

  /**
   * @param propertyName The property to be edited.  
   * @param template The property that will define the layout of the 
   *                 editor.
   * @param manager The PropertyEditorManager that will manage the
   *                   changes.
   * @param isEnabled Whether or not this editor is enabled by default. 
   */
  public void configureEditor(String propertyName, String template, PropertyEditorManager newManager, boolean isEnabled) {
    property=propertyName;
    manager=newManager;
    editorTemplate = template;
    originalValue = manager.getProperty(property, "");
    
    String defaultLabel;
    int dotIndex = property.lastIndexOf(".");
    if (dotIndex == -1) 
      defaultLabel = new String(property);
    else
      defaultLabel = property.substring(dotIndex+1);
    
    if (debug) {
      System.out.println("property is " + property + "; editorTemplate is " + editorTemplate);
    }
    label = new JLabel(manager.getProperty(editorTemplate + ".label", defaultLabel));
    inputField = new JTextField(originalValue);
    inputField.setPreferredSize(new java.awt.Dimension(150, inputField.getMinimumSize().height));
    this.add(label);
    this.add(inputField);
    this.setEnabled(isEnabled);
    
    labelComponent = label;
    valueComponent = inputField;
  }
  
  /**
   * This writes the currently configured value in the PropertyEditorUI
   * to the source VariableBundle.
   */
  public void setValue() throws PropertyValueVetoException {
    if (isEnabled() && !(inputField.getText().equals(originalValue))) {
      firePropertyChangingEvent(inputField.getText());
      manager.setProperty(property, inputField.getText());
      firePropertyChangedEvent(inputField.getText());
    }
  }
  
  /**
   * Returns the current values of the edited properties as a 
   * java.util.Properties object.
   */
  public java.util.Properties getValue() {
    java.util.Properties retProps = new java.util.Properties();
    retProps.setProperty(property, inputField.getText());
    return retProps;
  }

  
  /**
   * This resets the editor to the original (or latest set, if setValue() 
   * has been called) value of the edited property.
   */
  public void resetDefaultValue() {
    inputField.setText(originalValue);
  }

  /**
   * Sets the enabled property of the PropertyEditorUI.  Disabled 
   * editors should not be able to do setValue() calls.
   */
  public void setEnabled(boolean newValue) {
    if (inputField != null) {
      inputField.setEnabled(newValue);
      enabled=newValue;
    }
  }
}
