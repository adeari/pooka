package net.suberic.util.gui;
import javax.swing.*;
import net.suberic.util.*;
import java.awt.FlowLayout;

public class StringEditorPane extends DefaultPropertyEditor {
    String property;
    String originalValue;
    JLabel label;
    JTextField inputField;
    VariableBundle sourceBundle;

    public StringEditorPane(String newProperty, VariableBundle bundle, boolean isEnabled) {
	configureEditor(null, newProperty, newProperty, bundle, isEnabled);
    }

    public void configureEditor(PropertyEditorFactory factory, String newProperty, String templateType, VariableBundle bundle, boolean isEnabled) {
	property=newProperty;
	sourceBundle=bundle;
	originalValue = sourceBundle.getProperty(newProperty, "");

	String defaultLabel;
	int dotIndex = property.lastIndexOf(".");
	if (dotIndex == -1) 
	    defaultLabel = new String(property);
	else
	    defaultLabel = property.substring(dotIndex+1);

	label = new JLabel(sourceBundle.getProperty(property + ".label", defaultLabel));
	inputField = new JTextField(originalValue);
	this.add(label);
	this.add(inputField);
	this.setEnabled(isEnabled);
    }

    public StringEditorPane(String newProperty, VariableBundle bundle) {
	this(newProperty, bundle, true);
    }

    public void setValue() {
	if (isEnabled() && !(inputField.getText().equals(originalValue)))
	sourceBundle.setProperty(property, inputField.getText());
    }

    public java.util.Properties getValue() {
	java.util.Properties retProps = new java.util.Properties();
	retProps.setProperty(property, inputField.getText());
	return retProps;
    }

    public void resetDefaultValue() {
	inputField.setText(originalValue);
    }

    public void setEnabled(boolean newValue) {
	if (inputField != null) {
	    inputField.setEnabled(newValue);
	    enabled=newValue;
	}
    }
}
