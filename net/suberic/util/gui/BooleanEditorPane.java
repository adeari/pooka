package net.suberic.util.gui;
import javax.swing.*;
import net.suberic.util.*;
import java.awt.FlowLayout;

/**
 * This is a Swing implemenation of a boolean PropertyEditorUI.
 */
public class BooleanEditorPane extends DefaultPropertyEditor {
    String property;
    boolean originalValue;
    JCheckBox inputField;
    JLabel label;
    VariableBundle sourceBundle;

    public BooleanEditorPane() {
    }

    public BooleanEditorPane(String newProperty, String templateType, VariableBundle bundle, boolean isEnabled) {
	configureEditor(newProperty, templateType, bundle, isEnabled);
    }
    public BooleanEditorPane(String newProperty, VariableBundle bundle, boolean isEnabled) {
	configureEditor(newProperty, bundle, isEnabled);
    }
    
    public BooleanEditorPane(String newProperty, VariableBundle bundle) {
	this(newProperty, bundle, true);
    }

    public void configureEditor(PropertyEditorFactory factory, String newProperty, String templateType, VariableBundle bundle, boolean isEnabled) {
	property=newProperty;
	sourceBundle=bundle;
	if (sourceBundle.getProperty(newProperty, "").equalsIgnoreCase("true"))
	    originalValue = true;
	else
	    originalValue=false;
	
	String defaultLabel;
	int dotIndex = property.lastIndexOf(".");
	if (dotIndex == -1) 
	    defaultLabel = new String(property);
	else
	    defaultLabel = property.substring(dotIndex+1);
	
	inputField = new JCheckBox();
	label = new JLabel(sourceBundle.getProperty(property + ".label", defaultLabel));
	
	inputField.setSelected(originalValue);

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
	    if (inputField.isSelected() != originalValue) {
		if (inputField.isSelected())
		    sourceBundle.setProperty(property, "true");
		else
		    sourceBundle.setProperty(property, "false");
	    }
	}
    }

    public java.util.Properties getValue() {
	java.util.Properties retProps = new java.util.Properties();

	if (inputField.isSelected())
	    retProps.setProperty(property, "true");
	else
	    retProps.setProperty(property, "false");
	return retProps;
    }

    public void resetDefaultValue() {
	inputField.setSelected(originalValue);
    }

    public void setEnabled(boolean newValue) {
	if (inputField != null) {
	    inputField.setEnabled(newValue);
	    enabled=newValue;
	}
    }
}

