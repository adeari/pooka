package net.suberic.pooka.gui.search;
import javax.swing.*;
import javax.swing.event.*;
import javax.mail.search.*;
import java.util.*;
import net.suberic.pooka.Pooka;
import net.suberic.pooka.SearchTermManager;

/**
 * This is a gui component which lets you configure a 
 * javax.mail.search.SearchTerm.  It can be used either to configure mail
 * filters, or to search folders.
 */
public class SearchEntryForm implements java.awt.event.ItemListener {
    // ui objects
    private Box panel;
    private JComboBox searchFieldCombo;
    private JPanel selectionPanel; 

    private Box stringMatchPanel;
    private JComboBox operationCombo;
    private JTextField textField;

    private Box booleanPanel;
    private JComboBox booleanValueCombo;

    private Box datePanel;
    private JComboBox dateComparisonCombo;
    private JTextField dateField;
    private java.awt.CardLayout layout;

    // the source SearchTermManager.
    SearchTermManager manager;

    /**
     * Constructs a new SearchEntryForm.
     */
    public SearchEntryForm(SearchTermManager newManager) { 
	manager = newManager;
	panel = new Box(BoxLayout.X_AXIS);

	searchFieldCombo = new JComboBox(manager.getTermLabels());
	searchFieldCombo.setPreferredSize(searchFieldCombo.getMinimumSize());

	searchFieldCombo.addItemListener( this );

	createPanels();

	selectionPanel = new JPanel();
	layout = new java.awt.CardLayout();
	selectionPanel.setLayout(layout);
	selectionPanel.add(stringMatchPanel, SearchTermManager.STRING_MATCH);
	selectionPanel.add(booleanPanel, SearchTermManager.BOOLEAN_MATCH);
	selectionPanel.add(datePanel, SearchTermManager.DATE_MATCH);

	panel.add(searchFieldCombo);
	panel.add(selectionPanel);

    }

    /**
     * Creates all of the selection panels.
     */
    public void createPanels() {
	stringMatchPanel = new Box(BoxLayout.X_AXIS);
	Vector operationFields = manager.getOperationLabels(SearchTermManager.STRING_MATCH);
	operationCombo = new JComboBox(operationFields);
	operationCombo.setPreferredSize(operationCombo.getMinimumSize());
	textField = new JTextField(20);
	textField.setMaximumSize(new java.awt.Dimension(1000, textField.getPreferredSize().height));

	stringMatchPanel.add(operationCombo);
	stringMatchPanel.add(textField);
	stringMatchPanel.add(Box.createGlue());

	booleanPanel = new Box(BoxLayout.X_AXIS);
	Vector booleanFields = manager.getOperationLabels(SearchTermManager.BOOLEAN_MATCH);
	booleanValueCombo = new JComboBox(booleanFields);
	booleanValueCombo.setPreferredSize(booleanValueCombo.getMinimumSize());
	booleanPanel.add(booleanValueCombo);
	booleanPanel.add(Box.createGlue());

	datePanel = new Box(BoxLayout.X_AXIS);

	Vector dateFields = manager.getOperationLabels(SearchTermManager.DATE_MATCH);
	dateComparisonCombo = new JComboBox(dateFields);
	dateComparisonCombo.setPreferredSize(dateComparisonCombo.getMinimumSize());
	dateField = new JTextField(10);
	dateField.setMaximumSize(new java.awt.Dimension(1000, dateField.getPreferredSize().height));

	JLabel dateFormatLabel = new JLabel(Pooka.getProperty("Search.dateFormat", "mm/dd/yyyy"));
	datePanel.add(dateComparisonCombo);
	datePanel.add(dateField);
	datePanel.add(dateFormatLabel);
	datePanel.add(Box.createGlue());
    }

    /**
     * Returns the Box which shows the SearchEntryForm.
     */ 
    public Box getPanel() {
	return panel;
    }

    /**
     * This generates a SearchTerm from the information on the Box, and
     * returns that value.
     */
    public SearchTerm generateSearchTerm() throws java.text.ParseException {
	if (Pooka.isDebug())
	    System.out.println("SearchEntryForm:  generating SearchTerm from " + searchFieldCombo.getSelectedItem() + " and " + operationCombo.getSelectedItem());

	String searchProperty = (String)(manager.getLabelToPropertyMap().get(searchFieldCombo.getSelectedItem()));
	String selectedType = Pooka.getProperty(searchProperty + ".type", "");
	String operationProperty = null;
	String pattern = null;

	if (selectedType.equalsIgnoreCase(SearchTermManager.STRING_MATCH)) {
	    operationProperty = (String)(manager.getLabelToOperationMap().get(operationCombo.getSelectedItem()));
	    if (Pooka.isDebug())
		System.out.println("using " + searchProperty + ", " + operationProperty);
	    pattern = textField.getText();

	} else 	if (selectedType.equalsIgnoreCase(SearchTermManager.BOOLEAN_MATCH)) {
	    operationProperty = (String)(manager.getLabelToOperationMap().get(booleanValueCombo.getSelectedItem()));
	    if (Pooka.isDebug())
		System.out.println("using " + searchProperty + ", " + operationProperty);
	    pattern = null;

	} else	if (selectedType.equalsIgnoreCase(SearchTermManager.DATE_MATCH)) {
	    operationProperty = (String)(manager.getLabelToOperationMap().get(dateComparisonCombo.getSelectedItem()));
	    if (Pooka.isDebug())
		System.out.println("using " + searchProperty + ", " + operationProperty);
	    pattern = dateField.getText();
	}
	
	return manager.generateSearchTerm(searchProperty, operationProperty, pattern);
    }

    /**
     * This generates the Properties for the given SearchTerm.
     */
    public java.util.Properties generateSearchTermProperties(String rootProperty) {
	if (Pooka.isDebug())
	    System.out.println("SearchEntryForm:  generating SearchTerm property from " + searchFieldCombo.getSelectedItem() + " and " + operationCombo.getSelectedItem() + " using rootProperty " + rootProperty);
	
	String searchProperty = (String)(manager.getLabelToPropertyMap().get(searchFieldCombo.getSelectedItem()));
	String selectedType = Pooka.getProperty(searchProperty + ".type", "");
	String operationProperty = null;
	String pattern = null;

	if (selectedType.equalsIgnoreCase(SearchTermManager.STRING_MATCH)) {
	    operationProperty = (String)(manager.getLabelToOperationMap().get(operationCombo.getSelectedItem()));
	    if (Pooka.isDebug())
		System.out.println("using " + searchProperty + ", " + operationProperty);
	    pattern = textField.getText();

	} else 	if (selectedType.equalsIgnoreCase(SearchTermManager.BOOLEAN_MATCH)) {
	    operationProperty = (String)(manager.getLabelToOperationMap().get(booleanValueCombo.getSelectedItem()));
	    if (Pooka.isDebug())
		System.out.println("using " + searchProperty + ", " + operationProperty);
	    pattern = null;

	} else	if (selectedType.equalsIgnoreCase(SearchTermManager.DATE_MATCH)) {
	    operationProperty = (String)(manager.getLabelToOperationMap().get(dateComparisonCombo.getSelectedItem()));
	    if (Pooka.isDebug())
		System.out.println("using " + searchProperty + ", " + operationProperty);
	    pattern = dateField.getText();
	}
	
	Properties returnValue = new java.util.Properties();
	returnValue.setProperty(rootProperty, searchProperty);
	returnValue.setProperty(rootProperty + ".operation", operationProperty);
	returnValue.setProperty(rootProperty + ".pattern", pattern);
	returnValue.setProperty(rootProperty + ".type", "single");

	return returnValue;
    }


    /**
     * This handles the switch of the selectionPanel when the searchFieldCombo
     * value changes.
     */
    public void itemStateChanged(java.awt.event.ItemEvent e) {
	String selectedString = (String)(manager.getLabelToPropertyMap().get(searchFieldCombo.getSelectedItem()));
	
	String selectedType = Pooka.getProperty(selectedString + ".type", "");
	layout.show(selectionPanel, selectedType);
    }
}




