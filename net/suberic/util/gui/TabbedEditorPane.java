package net.suberic.util.gui;
import javax.swing.*;
import java.util.Vector;
import java.awt.*;
import net.suberic.util.VariableBundle;

/**
 * This will made an editor for a list of properties.  Each property, in
 * turn, should have a list of properties wihch it itself edits.  Each
 * top-level property will be a tab, with its values shown on the panel.
 *
 * So an example of a property definition for this would be:
 *
 * TabbedList=tabOne:tabTwo:tabThree
 * TabbedList.tabOne=prop1:prop2:prop3:prop4
 * TabbedList.tabTwo=prop5:prop6
 * TabbedList.tabThree=prop7:prop8:prop9
 *
 */
public class TabbedEditorPane extends DefaultPropertyEditor implements PropertyEditorUI {
    Vector editors;
    PropertyEditorFactory factory;

    
    /**
     * This configures this editor with the following values.
     */
    public void configureEditor(PropertyEditorFactory factory, String newProperty, String templateType, VariableBundle bundle, boolean isEnabled) {
	factory = newFactory;

	DefaultPropertyEditor currentEditor;

	editors = new Vector();

	for (int i = 0; i < properties.size(); i++) {
	    currentEditor =
              factory.createEditor((String)properties.elementAt(i), (String)templateTypes.elementAt(i));
	    editors.add(currentEditor);
	    this.add(currentEditor);
	}
    }

    public void configureEditor(String newProperty, String templateType, VariableBundle bundle, boolean isEnabled) {
	configureEditor(newProperty, templateType, bundle, isEnabled);
    }

    public void configureEditor(String newProperty, VariableBundle bundle, boolean isEnabled) {
	configureEditor(newProperty, newProperty, bundle, isEnabled);
    }

    public void configureEditor(String newProperty, VariableBundle bundle) {
	configureEditor(newProperty, newProperty, bundle, true);
    }

    /**
     * This creates a PropertyEditor for the given set of properties.
     */
    public DefaultPropertyEditor createPanel(String rootProperty, String subProperty, String templateRoot, String templateSub) {
	String propertyName = rootProperty + "." + subProperty;
	String templateName = templateRoot + "." + templateSub;
	VariableBundle vb = factory.getBundle();
	return new PropertyEditorPane(factory, vb.getPropertyAsVector(propertyName, ""), vb.getPropertyAsVector(templateName, ""), null);
    }

}
    


