package net.suberic.util.gui.propedit;
import javax.swing.*;
import java.awt.event.*;
import net.suberic.util.*;
import java.awt.FlowLayout;

/**
 * The default EditorPane.  Just shows a text field in which a user
 * can enter a String.
 */
public class StringEditorPane extends SwingPropertyEditor {
  
  JLabel label;
  JTextField inputField;
  String currentValue;

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
    currentValue = originalValue;

    if (debug) {
      System.out.println("property is " + property + "; editorTemplate is " + editorTemplate);
    }

    label = createLabel();
    inputField = new JTextField(originalValue);
    inputField.setPreferredSize(new java.awt.Dimension(150, inputField.getMinimumSize().height));
    inputField.addFocusListener(new FocusAdapter() {
	public void focusLost(FocusEvent e) {
	  if (! inputField.getText().equals(currentValue)) {
	    try {
	      firePropertyChangingEvent(inputField.getText());
	      currentValue = inputField.getText();
	      firePropertyChangedEvent(currentValue);
	    } catch (PropertyValueVetoException pvve) {
	      manager.getFactory().showError(inputField, "Error changing value " + label.getText() + " to " + inputField.getText() + ":  " + pvve.getReason());
	      inputField.setText(currentValue);
	    }
	  }
	}
      });
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
    if (isEnabled() && !(inputField.getText().equals(currentValue))) {
      firePropertyChangingEvent(inputField.getText());
      firePropertyChangedEvent(inputField.getText());
    }

    if (isEnabled() && !(inputField.getText().equals(originalValue))) {
      manager.setProperty(property, inputField.getText());
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
    String fieldValue = inputField.getText();
    if (! (fieldValue.equals(currentValue) && fieldValue.equals(originalValue))) {
      // something has changed, so we'll have to deal with it.
      try {
	if (! currentValue.equals(originalValue)) {
	  firePropertyChangingEvent(originalValue);
	  currentValue = originalValue;
	  firePropertyChangedEvent(originalValue);
	}
	inputField.setText(originalValue);
      } catch (PropertyValueVetoException pvve) {
	manager.getFactory().showError(inputField, "Error changing value " + label.getText() + " to " + originalValue + ":  " + pvve.getReason());
      }
    }
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
