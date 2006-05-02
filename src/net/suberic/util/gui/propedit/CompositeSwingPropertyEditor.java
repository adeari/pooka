package net.suberic.util.gui.propedit;
import javax.swing.*;
import java.util.Vector;
import java.util.List;
import java.util.logging.Logger;

/**
 * This will make an editor for a list of properties.
 */
public abstract class CompositeSwingPropertyEditor extends SwingPropertyEditor {
  protected List editors;
  protected Logger mLogger = Logger.getLogger("editors.debug");

  /**
   * This writes the currently configured values in the PropertyEditorUI
   * to the source VariableBundle.
   */
  public void setValue() throws PropertyValueVetoException {
    if (isEnabled()) {
      for (int i = 0; i < editors.size() ; i++) {
        ((PropertyEditorUI) editors.get(i)).setValue();
      }
    }
  }
    
  /**
   * This resets the editor to the original (or latest set, if setValue() 
   * has been called) value of the edited property.
   */
  public void resetDefaultValue() throws PropertyValueVetoException {
    if (isEnabled()) {
      for (int i = 0; i < editors.size() ; i++) {
        ((PropertyEditorUI) editors.get(i)).resetDefaultValue();
      }
    }
  }
  
  /**
   * Returns the current values of the edited properties as a 
   * java.util.Properties object.
   */
  public java.util.Properties getValue() {
    java.util.Properties currentRetValue = new java.util.Properties();
    java.util.Iterator iter = editors.iterator();
    while (iter.hasNext()) {
      currentRetValue.putAll(((SwingPropertyEditor)iter.next()).getValue());
    }
    
    return currentRetValue;
  }
  
  /**
   * Sets the enabled property of the PropertyEditorUI.  Disabled 
   * editors should not be able to do setValue() calls.
   */
  public void setEnabled(boolean newValue) {
    for (int i = 0; i < editors.size() ; i++) {
      ((PropertyEditorUI) editors.get(i)).setEnabled(newValue);
    }
    enabled=newValue;
  }
}
    


