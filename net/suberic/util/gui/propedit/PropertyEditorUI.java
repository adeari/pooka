package net.suberic.util.gui.propedit;
import net.suberic.util.VariableBundle;

/**
 * An interface which defines a way of editing a property.
 */
public interface PropertyEditorUI {

  /**
   * This configures an editor for the given propertyName using the 
   * PropertyManager mgr.
   *
   * This version usees the template property to definte all things about
   * the editor for propertyName.  This is useful if you want to be able
   * to edit, for instace, the properties of a particular user:
   *
   * UserProfile.userOne.showHeaders
   * UserProfile.userTwo.showHeaders
   *
   * UserProfile.showHeaders.propertyType=boolean
   *
   * So you can use this just to call configureEditor(factory, 
   * "UserProfile.userOne.showHeaders", "UserProfile.showHeaders", mgr,
   * true)
   *
   */
  public void configureEditor(PropertyEditorFactory factory, String propertyName, String template, PropertyEditorManager mgr, boolean isEnabled);
  
  /**
   * This configures an editor for the given propertyName in the 
   * PropertyEditorManager mgr.
   */
  public void configureEditor(String propertyName, String template, PropertyEditorManager mgr, boolean isEnabled);
  
  /**
   * This configures an editor for the given propertyName in the 
   * PropertyEditorManager mgr.
   */
  public void configureEditor(String propertyName, PropertyEditorManager mgr, boolean isEnabled);
  
  /**
   * This configures an editor for the given propertyName in the 
   * PropertyEditorManager mgr.
   */
  public void configureEditor(String propertyName, PropertyEditorManager mgr);
  
  /**
   * This writes the currently configured value in the PropertyEditorUI
   * to the source VariableBundle.
   */
  public void setValue() throws PropertyValueVetoException;
  
  /**
   * This resets the editor to the original (or latest set, if setValue() 
   * has been called) value of the edited property.
   */
  public void resetDefaultValue();
  
  /**
   * Returns the current values of the edited properties as a 
   * java.util.Properties object.
   */
  public java.util.Properties getValue();
  
  /**
   * Sets the enabled property of the PropertyEditorUI.  Disabled 
   * editors should not be able to do setValue() calls.
   */
  public void setEnabled(boolean newValue);
  
  /**
   * Returns whether or not this editor is enabled.
   */
  public boolean isEnabled();

  /**
   * Adds a PropertyEditorListener to the ListenerList.
   */
  public void addPropertyEditorListener(PropertyEditorListener pel);

  /**
   * Removes a PropertyEditorListener from the ListenerList.
   */
  public void removePropertyEditorListener(PropertyEditorListener pel);

}
