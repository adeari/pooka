package net.suberic.util.gui;
import javax.swing.*;
import net.suberic.util.*;
import java.awt.FlowLayout;

public class BooleanEditorPane extends DefaultPropertyEditor {
    String property;
    boolean originalValue;
    JCheckBox inputField;
    VariableBundle sourceBundle;

    public BooleanEditorPane(String newProperty, VariableBundle bundle, boolean isEnabled) {
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

	inputField = new JCheckBox(sourceBundle.getProperty(property + ".label", defaultLabel));

	this.add(inputField);
	this.setEnabled(isEnabled);
    }

    public BooleanEditorPane(String newProperty, VariableBundle bundle) {
	this(newProperty, bundle, true);
    }


    // as defined in net.suberic.util.gui.AkpPropertyEditor

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

