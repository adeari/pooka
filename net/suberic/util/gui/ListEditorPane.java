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

    public ListEditorPane(String newProperty, VariableBundle bundle, boolean isEnabled) {
	property=newProperty;
	sourceBundle=bundle;

	String defaultLabel;
	int dotIndex = property.lastIndexOf(".");
	if (dotIndex == -1) 
	    defaultLabel = new String(property);
	else
	    defaultLabel = property.substring(dotIndex+1);

	label = new JLabel(sourceBundle.getProperty(property + ".label", defaultLabel));
	inputField = createComboBox();

	this.add(label);
	this.add(inputField);
	this.setEnabled(isEnabled);
    }
    
    public ListEditorPane(String newProperty, VariableBundle bundle) {
	this(newProperty, bundle, true);
    }


    private JComboBox createComboBox() {
	String originalValue = sourceBundle.getProperty(property, "");
	String currentItem;
	originalIndex=-1;
	Vector items = new Vector();
	StringTokenizer tokens;

	if (sourceBundle.getProperty(sourceBundle.getProperty(property + ".allowedValues", ""), "") != "")
	    tokens = new StringTokenizer(sourceBundle.getProperty(sourceBundle.getProperty(property + ".allowedValues", ""), ""), ":");
	else
	    tokens = new StringTokenizer(sourceBundle.getProperty(property + ".allowedValues", ""), ":");

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

    //  as defined in net.suberic.util.gui.AkpPropertyEditor

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
