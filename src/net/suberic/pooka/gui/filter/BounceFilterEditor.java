package net.suberic.pooka.gui.filter;

import javax.swing.*;
import java.util.Properties;

/**
 * This is a class that lets you choose your filter actions.
 */
public class BounceFilterEditor extends FilterEditor {
  JTextField addressField;
  JCheckBox deleteField;
  
  public static String FILTER_CLASS = "net.suberic.pooka.filter.BounceFilterAction";
  
  /**
   * Configures the given FilterEditor from the given VariableBundle and
   * property.
   */
  public void configureEditor(net.suberic.util.gui.propedit.PropertyEditorManager newManager, String propertyName) {
    property = propertyName;
    manager = newManager;

    String originalAddressString = manager.getProperty(propertyName + ".targetAddresses", "");
    
    addressField = new JTextField(originalAddressString);

    boolean originalDeleteValue = manager.getProperty(propertyName + ".removeBounced", "false").equalsIgnoreCase("true");
    
    deleteField = new JCheckBox();
    deleteField.setSelected(originalDeleteValue);

    this.add(addressField);
    this.add(deleteField);
  }
  
  /**
   * Gets the values that would be set by this FilterEditor.
   */
  public java.util.Properties getValue() {
    Properties props = new Properties();
    
    String oldClassName = manager.getProperty(property + ".class", "");
    if (!oldClassName.equals(FILTER_CLASS))
      props.setProperty(property + ".class", FILTER_CLASS);
    
    String originalAddressString = manager.getProperty(property + ".targetAddresses", "");
    if (addressField.getText() != originalAddressString) {
      props.setProperty(property + ".targetAddresses", addressField.getText());
    }

    boolean originalDeleteValue = manager.getProperty(property + ".removeBounced", "false").equalsIgnoreCase("true");
    if (originalDeleteValue != deleteField.isSelected()) {
      if (deleteField.isSelected())
	props.setProperty(property + ".removeBounced", "true");
      else
	props.setProperty(property + ".removeBounced", "false");
    }

    return props;
  }
  
  /**
   * Sets the values represented by this FilterEditor in the manager.
   */
  public void setValue() {
    
    String oldClassName = manager.getProperty(property + ".class", "");
    if (!oldClassName.equals(FILTER_CLASS))
      manager.setProperty(property + ".class", FILTER_CLASS);
    
    String originalAddressString = manager.getProperty(property + ".targetAddresses", "");
    if (addressField.getText() != originalAddressString) {
      manager.setProperty(property + ".targetAddresses", addressField.getText());
    }

    boolean originalDeleteValue = manager.getProperty(property + ".removeBounced", "false").equalsIgnoreCase("true");
    if (originalDeleteValue != deleteField.isSelected()) {
      if (deleteField.isSelected())
	manager.setProperty(property + ".removeBounced", "true");
      else
	manager.setProperty(property + ".removeBounced", "false");
    }

  }
  
}
