package net.suberic.util.gui;
import net.suberic.util.VariableBundle;

/**
 * An interface which defines a way of editing a property in a 
 * VariableBundle.
 */
public interface PropertyEditorUI {

    /**
     * This configures an editor for the given propertyName in the 
     * VariableBundle bundle.
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
     * "UserProfile.userOne.showHeaders", "UserProfile.showHeaders", bundle,
     * true)
     *
     */
    public void configureEditor(PropertyEditorFactory factory, String propertyName, String template, VariableBundle bundle, boolean isEnabled);

    /**
     * This configures an editor for the given propertyName in the 
     * VariableBundle bundle.
     */
    public void configureEditor(String propertyName, VariableBundle bundle, boolean isEnabled);

    /**
     * This configures an editor for the given propertyName in the 
     * VariableBundle bundle.
     */
    public void configureEditor(String propertyName, VariableBundle bundle);

    /**
     * This writes the currently configured value in the PropertyEditorUI
     * to the source VariableBundle.
     */
    public void setValue();

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
}
