package net.suberic.util.gui;
import javax.swing.*;
import java.util.Vector;
import java.awt.*;
import net.suberic.util.VariableBundle;

/**
 * This is a Property Editor which displays a group of properties.
 * These properties should all be definte by a single property.
 *
 * An example:
 *
 * Configuration=foo:bar
 * Configuration.propertyType=Composite
 * Configuration.scoped=false
 * foo=zork
 * bar=frobozz
 */
public class CompositeEditorPane extends DefaultPropertyEditor {
    Vector editors;
    PropertyEditorFactory factory;
    String property;
    String template;

    boolean scoped;

    /**
     * This contructor creates a PropertyEditor for the list of 
     * properties represented by the given property.
     */     
    public CompositeEditorPane(PropertyEditorFactory newFactory, String 
			       newProperty, String newTemplate) {
	super(BoxLayout.Y_AXIS);
	configureEditor(newFactory, newProperty, newTemplate, newFactory.getBundle(), true);
        
    }

    /**
     * This configures an editor for the given propertyName in the 
     * VariableBundle bundle.
     *
     */
    public void configureEditor(PropertyEditorFactory newFactory, String newProperty, String newTemplate, VariableBundle bundle, boolean isEnabled) {

	factory = newFactory;
	property = newProperty;
	template = newTemplate;

	scoped = bundle.getProperty(template + ".scoped", "false").equalsIgnoreCase("true");

	Vector properties = new Vector();
	Vector templates = new Vector();

	if (scoped) {
	    Vector templateNames = bundle.getPropertyAsVector(template, "");
	    for (int i = 0; i < templateNames.size() ; i++) {
		properties.add(property + "." + (String) templateNames.elementAt(i));
		templates.add(template + "." + (String) templateNames.elementAt(i));
	    }
	} else {
	    properties = bundle.getPropertyAsVector(property, "");
	    templates = bundle.getPropertyAsVector(template, "");
	}

	DefaultPropertyEditor currentEditor;

	editors = new Vector();

	for (int i = 0; i < properties.size(); i++) {
	    currentEditor =
              factory.createEditor((String)properties.elementAt(i), (String) templates.elementAt(i));
	    editors.add(currentEditor);
	    this.add(currentEditor);
	}
    }

    public void setValue() {
	if (isEnabled()) {
	    for (int i = 0; i < editors.size(); i++) {
		((DefaultPropertyEditor)(editors.elementAt(i))).setValue();
	    }
	}
    }

    public java.util.Properties getValue() {
	java.util.Properties currentRetValue = new java.util.Properties();
	for (int i = 0; i < editors.size(); i++) {
	    currentRetValue = new java.util.Properties(((DefaultPropertyEditor)(editors.elementAt(i))).getValue());
	}

	return currentRetValue;
    }

    public void resetDefaultValue() {
	for (int i = 0; i < editors.size(); i++) {
	    ((DefaultPropertyEditor)(editors.elementAt(i))).resetDefaultValue();
	}
    }

    public void setEnabled(boolean newValue) {
	if (editors != null && editors.size() > 0) {
	    for (int i = 0; i < editors.size(); i++) {
		PropertyEditorUI currentEditor = (PropertyEditorUI) editors.elementAt(i);
		currentEditor.setEnabled(newValue);
	    }
	}
    }
}
    


