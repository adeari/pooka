package net.suberic.util.gui.propedit;
import javax.swing.*;
import java.util.Vector;
import java.util.List;

/**
 * This will made an editor for a list of properties.
 *
 * Options:
 * .propertyScoped - add the subproperty to the property instead
 *                             of using it as its own property
 * .templateScoped - add subproperty to the template
 *
 */
public abstract class CompositeSwingPropertyEditor extends SwingPropertyEditor {
  List editors;
  
  protected boolean templateScoped = false;
  protected boolean propertyScoped = false;
  
  /**
   * Sets the templateScoped and propertyScoped settings.
   */
  public void calculateScope() {
    templateScoped = manager.getProperty(editorTemplate + ".templateScoped", "false").equalsIgnoreCase("true");
    propertyScoped = manager.getProperty(editorTemplate + ".propertyScoped", "false").equalsIgnoreCase("true");
  }

  /**
   * Creates an editor pane for a group of values.
   */
  private SwingPropertyEditor createEditorPane(String subProperty, String subTemplate) {
    //return new CompositeEditorPane(manager, subProperty, subTemplate);
    return null;
  }
  
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
  public void resetDefaultValue() {
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
      enabled=newValue;
    }
  }
}
    


