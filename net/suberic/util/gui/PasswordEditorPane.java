package net.suberic.util.gui;
import javax.swing.*;
import net.suberic.util.*;
import java.awt.FlowLayout;

public class PasswordEditorPane extends DefaultPropertyEditor {
    String property;
    String originalValue;
    JLabel label;
    JPasswordField inputField;
    VariableBundle sourceBundle;

    public PasswordEditorPane(String newProperty, VariableBundle bundle, boolean isEnabled) {
	configureEditor(newProperty, bundle, isEnabled);
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
	inputField = new JPasswordField(originalValue);
	this.add(label);
	this.add(inputField);
	this.setEnabled(isEnabled);
    }

    public PasswordEditorPane(String newProperty, VariableBundle bundle) {
	this(newProperty, bundle, true);
    }

    public void setValue() {
	String value = new String(inputField.getPassword());
	if (isEnabled() && !(value.equals(originalValue)))
	sourceBundle.setProperty(property, value);
    }

    public java.util.Properties getValue() {
	String value = new String(inputField.getPassword());
	java.util.Properties retProps = new java.util.Properties();
	retProps.setProperty(property, value);
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
