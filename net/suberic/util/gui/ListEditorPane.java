package net.suberic.util.gui;
import javax.swing.*;
import net.suberic.util.*;
import java.awt.FlowLayout;
import java.util.*;

public class ListEditorPane extends DefaultPropertyEditor {
    String property;
    int originalIndex;
    JLabel label;
    JComboBox inputField;
    VariableBundle sourceBundle;
    String templateType;

    public ListEditorPane(String newProperty, String newTemplateType, VariableBundle bundle, boolean isEnabled) {
	configureEditor(newProperty, newTemplateType, bundle, isEnabled);
    }

    public void configureEditor(PropertyEditorFactory factory, String newProperty, String newTemplateType, VariableBundle bundle, boolean isEnabled) {
	property=newProperty;
	templateType=newTemplateType;
	sourceBundle=bundle;

	String defaultLabel;
	int dotIndex = property.lastIndexOf(".");
	if (dotIndex == -1) 
	    defaultLabel = new String(property);
	else
	    defaultLabel = property.substring(dotIndex+1);

	label = new JLabel(sourceBundle.getProperty(templateType + ".label", defaultLabel));
	inputField = createComboBox();

	this.add(label);
	this.add(inputField);
	this.setEnabled(isEnabled);

	labelComponent = label;
	valueComponent = new JPanel();
	valueComponent.setLayout(new FlowLayout(FlowLayout.LEFT));
	valueComponent.add(inputField);
    }
    
    public ListEditorPane(String newProperty, VariableBundle bundle, boolean isEnabled) {
	this(newProperty, newProperty, bundle, isEnabled);
    }

    public ListEditorPane(String newProperty, VariableBundle bundle) {
	this(newProperty, bundle, true);
    }

    public ListEditorPane(String newProperty, String templateType, VariableBundle bundle) {
	this(newProperty, templateType, bundle, true);
    }

    private JComboBox createComboBox() {
	String originalValue = sourceBundle.getProperty(property, "");
	String currentItem;
	originalIndex=-1;
	Vector items = new Vector();
	StringTokenizer tokens;

	if (sourceBundle.getProperty(sourceBundle.getProperty(templateType + ".allowedValues", ""), "") != "")
	    tokens = new StringTokenizer(sourceBundle.getProperty(sourceBundle.getProperty(templateType + ".allowedValues", ""), ""), ":");
	else
	    tokens = new StringTokenizer(sourceBundle.getProperty(templateType + ".allowedValues", ""), ":");

	for (int i=0; tokens.hasMoreTokens(); i++) {
	    currentItem = tokens.nextToken();
	    
	    if (currentItem.equals(originalValue))
		originalIndex=i;
	    items.add(currentItem);
	}
			      
	if (originalIndex == -1) {
	    items.add(originalValue);
	    originalIndex = items.size() - 1;
	}

	JComboBox jcb = new JComboBox(items);
	jcb.setSelectedIndex(originalIndex);
	return jcb;
    }

    //  as defined in net.suberic.util.gui.PropertyEditorUI

    public void setValue() {
	if (isEnabled() && isChanged())
	    sourceBundle.setProperty(property, (String)inputField.getSelectedItem());
    }

    public java.util.Properties getValue() {
	java.util.Properties retProps = new java.util.Properties();

	retProps.setProperty(property, (String)inputField.getSelectedItem());

	return retProps;
    }

    public void resetDefaultValue() {
	inputField.setSelectedIndex(originalIndex);
    }

    public boolean isChanged() {
	return (!(originalIndex == inputField.getSelectedIndex()));
    }

 public void setEnabled(boolean newValue) {
        if (inputField != null) {
            inputField.setEnabled(newValue);
            enabled=newValue;
        }
    }

}
