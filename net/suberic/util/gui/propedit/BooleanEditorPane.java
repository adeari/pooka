package net.suberic.util.gui.propedit;
import javax.swing.*;
import javax.swing.event.*;
import net.suberic.util.*;
import java.awt.FlowLayout;

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
  public void configureEditor(String propertyName, String template, PropertyEditorManager newManager, boolean isEnabled) {
    
    property=propertyName;
    manager=newManager;
    originalValue = manager.getProperty(property, manager.getProperty(template, "false"));
    originalBoolean = originalValue.equalsIgnoreCase("true");

    label = createLabel();

    inputField = new JCheckBox();
    
    inputField.setSelected(originalBoolean);

    inputField.addChangeListener(new ChangeListener() {
	public void stateChanged(ChangeEvent e) {
	  String newValue;
	  if (inputField.isSelected()) {
	    newValue = "true";
	  } else {
	    newValue = "false";
	  }
	  try {
	    firePropertyChangingEvent(newValue);
	  } catch (PropertyValueVetoException pvve) {
	    manager.getFactory().showError(inputField, "Error changing value " + label.getText() + " to " + newValue+ ":  " + pvve.getReason());
	    inputField.setSelected(! inputField.isSelected());
	    
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
   * as defined in net.suberic.util.gui.PropertyEditorUI
   */
  public void setValue() {
    if (isEnabled()) {
      if (inputField.isSelected() != originalBoolean) {
	String newValue;
	if (inputField.isSelected())
	  newValue = "true";
	else
	  newValue = "false";

	manager.setProperty(property, newValue);
	firePropertyChangedEvent(newValue);
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
    inputField.setSelected(originalBoolean);
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

