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

    public StringEditorPane(String newProperty, String typeTemplate, VariableBundle bundle, boolean isEnabled) {
	configureEditor(null, newProperty, typeTemplate, bundle, isEnabled);
    }

    public StringEditorPane(String newProperty, String typeTemplate, VariableBundle bundle) {
	configureEditor(null, newProperty, typeTemplate, bundle, true);
    }

    public void configureEditor(PropertyEditorFactory factory, String newProperty, String typeTemplate, VariableBundle bundle, boolean isEnabled) {
	property=newProperty;
	sourceBundle=bundle;
	originalValue = sourceBundle.getProperty(newProperty, "");

	String defaultLabel;
	int dotIndex = property.lastIndexOf(".");
	if (dotIndex == -1) 
	    defaultLabel = new String(property);
	else
	    defaultLabel = property.substring(dotIndex+1);

	//System.out.println("property is " + property + "; typeTemplate is " + typeTemplate);
	label = new JLabel(sourceBundle.getProperty(typeTemplate + ".label", defaultLabel));
	inputField = new JTextField(originalValue);
	inputField.setPreferredSize(new java.awt.Dimension(150, inputField.getMinimumSize().height));
	this.add(label);
	this.add(inputField);
	this.setEnabled(isEnabled);

	labelComponent = label;
	valueComponent = inputField;
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
