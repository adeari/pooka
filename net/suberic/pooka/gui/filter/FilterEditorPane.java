package net.suberic.pooka.gui.filter;
import net.suberic.pooka.gui.search.*;
import net.suberic.pooka.*;
import net.suberic.util.gui.*;
import net.suberic.util.VariableBundle;
import javax.swing.*;
import java.util.Vector;

/**
 * This is a class that lets you choose your filter actions.
 */
public class FilterEditorPane extends DefaultPropertyEditor implements java.awt.event.ItemListener {
    String property;
    String originalValue;
    VariableBundle sourceBundle;
    
    JLabel label;
    JComboBox typeCombo;
    JPanel filterConfigPanel;
    java.awt.CardLayout layout;
    
    java.util.HashMap editorTable;

    public FilterEditorPane(String newProperty, VariableBundle bundle, boolean isEnabled) {
	configureEditor(null, newProperty, newProperty, bundle, isEnabled);
    }

    public FilterEditorPane(String newProperty, String typeTemplate, VariableBundle bundle, boolean isEnabled) {
	configureEditor(null, newProperty, typeTemplate, bundle, isEnabled);
    }

    public FilterEditorPane(String newProperty, String typeTemplate, VariableBundle bundle) {
	configureEditor(null, newProperty, typeTemplate, bundle, true);
    }

    public FilterEditorPane(String newProperty, VariableBundle bundle) {
	this(newProperty, bundle, true);
    }

    /**
     * Configures the FilterEditorPane.
     */
    public void configureEditor(PropertyEditorFactory factory, String newProperty, String typeTemplate, VariableBundle bundle, boolean isEnabled) {
	property=newProperty;
	sourceBundle=bundle;
	originalValue = sourceBundle.getProperty(property, "");

	editorTable = new java.util.HashMap();

	// create the label
	label = new JLabel(sourceBundle.getProperty(typeTemplate + ".label", "Action"));

	// create the combo
	Vector filterLabels = Pooka.getSearchManager().getFilterLabels();
	
	typeCombo = new JComboBox(filterLabels);
	typeCombo.addItemListener(this);


	// create the filterConfigPanel.

	filterConfigPanel = new JPanel();
	layout = new java.awt.CardLayout();
	filterConfigPanel.setLayout(layout);
	for (int i = 0; i < filterLabels.size(); i++) {
	    String label = (String) filterLabels.elementAt(i);
	    FilterEditor currentEditor = Pooka.getSearchManager().getEditorForFilterLabel(label);
	    filterConfigPanel.add(label, currentEditor);
	    editorTable.put(label, currentEditor);
	}

	resetDefaultValue();
    }

    /**
     * Sets the value for this PropertyEditor.
     */
    public void setValue() {
	getFilterEditor().setValue();
    }

    /**
     * Returns the currently selected FilterEditor.
     */
    public FilterEditor getFilterEditor() {
	return (FilterEditor) editorTable.get(typeCombo.getSelectedItem());
    }

    /**
     * Gets the value that would be set by this PropertyEditor.
     */
    public java.util.Properties getValue() {
	return getFilterEditor().getValue();
    }

    /**
     * Resets the current Editor to its original value.
     */
    public void resetDefaultValue() {
	// get the current value, if any
	String currentLabel = Pooka.getSearchManager().getLabelForFilterClass(sourceBundle.getProperty(property + ".class", ""));
	if (currentLabel != null) {
	    typeCombo.setSelectedItem(currentLabel);
	} else {
	    typeCombo.setSelectedIndex(0);
	}
	
	FilterEditor currentEditor = getFilterEditor();
	currentEditor.configureEditor(sourceBundle, property);
	
    }

    /**
     * Enables or disables this editor.
     */
    public void setEnabled(boolean newValue) {
	typeCombo.setEnabled(newValue);
    }


    /**
     * This handles the switch of the filterConfigPanel when the typeCombo
     * value changes.
     */
    public void itemStateChanged(java.awt.event.ItemEvent e) {
	String selectedString = (String) typeCombo.getSelectedItem();
	
	layout.show(filterConfigPanel, selectedString);
    }
    
}
