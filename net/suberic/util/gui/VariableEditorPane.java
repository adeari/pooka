package net.suberic.util.gui;
import javax.swing.*;
import java.util.Vector;
import java.util.HashMap;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import net.suberic.util.VariableBundle;

/**
 * This will made a button which will call up another editor.  The 
 * exact properties which are then edited will depend on the value of 
 * another propery. 
 */
public class VariableEditorPane extends DefaultPropertyEditor {
    PropertyEditorFactory factory;
    VariableBundle sourceBundle;

    String property;
    String template;
    String keyProperty;
    HashMap valueToTemplateMap = new HashMap();

    boolean scoped;

    public VariableEditorPane(String newProperty, String newTemplate, PropertyEditorFactory newFactory) {
	configureEditor(newFactory, newProperty, newTemplate, newFactory.getBundle(), true);
    }

    /**
     * This configures this editor with the following values.
     */
    public void configureEditor(PropertyEditorFactory newFactory, String propertyName, String templateType, VariableBundle bundle, boolean isEnabled) {

	property=propertyName;
	template=templateType;
	factory = newFactory;
	sourceBundle = bundle;
	enabled=isEnabled;

	String remove = sourceBundle.getProperty(template + ".removeString", "");
	if (! remove.equals(""))
	    property = property.substring(0, property.lastIndexOf(remove));
	System.out.println("property is " + property);

	scoped = sourceBundle.getProperty(template + ".scoped", "false").equalsIgnoreCase("true");
	if (scoped) {
	    System.out.println("keyProperty is " + property + "." + sourceBundle.getProperty(template + ".keyProperty", ""));
	    keyProperty = property + "." + sourceBundle.getProperty(template + ".keyProperty", "");
	} else {
	    keyProperty =  sourceBundle.getProperty(template + ".keyProperty", "");
	}

	Vector allowedValues = sourceBundle.getPropertyAsVector(template + ".allowedValues", "");
	for (int i = 0; i < allowedValues.size(); i++) {
	    String value = (String) allowedValues.elementAt(i);
	    System.out.println("for " + value + ", getting value for " + template + ".allowedValues." + value);
	    String editValue = sourceBundle.getProperty(template + ".allowedValues." + value,  "");
	    System.out.println("adding " + value + ", " + editValue);
	    valueToTemplateMap.put(value, editValue);
	}

	labelComponent = new JLabel();
	
	String label = sourceBundle.getProperty(template + ".label", template);
	JButton button = new JButton(label);
	button.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {

		    showEditorWindow();
		}
	    });

	valueComponent = button;
    }

    /**
     * This shows the editor window for the configured value.
     */
    public void showEditorWindow() {
	String currentValue = sourceBundle.getProperty(keyProperty, "");
	System.out.println("currentValue is " + currentValue);

	/*
	String editValue = (String) valueToTemplateMap.get(currentValue);
	if (editValue == null)
	    editValue = (String) valueToTemplateMap.get("default");
	*/
	
	String editValue = currentValue;

	if (scoped)
	    editValue = template + "." + editValue;

	Vector propertyVector = new Vector();
	propertyVector.add(property);
	Vector templateVector = new Vector();
	templateVector.add(editValue);

	System.out.println("making a new editor out of " + property + " plus " + editValue);
	Container container = factory.createEditorWindow(sourceBundle.getProperty(editValue + ".title", editValue), propertyVector, templateVector);
	if (container instanceof JFrame)
	    ((JFrame) container).show();
	else if (container instanceof JInternalFrame) {
	    JInternalFrame jif = (JInternalFrame)container;
	    ((net.suberic.pooka.gui.MessagePanel)net.suberic.pooka.Pooka.getMainPanel().getContentPanel()).add(jif);
	    jif.setVisible(true);
	    try {
		jif.setSelected(true);
	    } catch (java.beans.PropertyVetoException pve) {
	    }
	    
	}
    }

    public void setValue() {
    }

    public void resetDefaultValue() {
    }

    public java.util.Properties getValue() {
	java.util.Properties currentRetValue = new java.util.Properties();
	
	return currentRetValue;
    }

    public void setEnabled(boolean newValue) {
	((JButton) valueComponent).setEnabled(newValue);
	enabled=newValue;
    }
}
    


