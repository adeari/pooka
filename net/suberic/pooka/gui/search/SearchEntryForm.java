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
    private JPanel panel;
    private JComboBox searchFieldCombo;
    private JPanel selectionPanel; 

    private JPanel stringMatchPanel;
    private JComboBox operationCombo;
    private JTextField textField;

    private JPanel booleanPanel;
    private JComboBox booleanValueCombo;

    private JPanel datePanel;
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
	panel = new JPanel();

	searchFieldCombo = new JComboBox(manager.getTermLabels());

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
	stringMatchPanel = new JPanel();
	Vector operationFields = manager.getOperationLabels(SearchTermManager.STRING_MATCH);
	operationCombo = new JComboBox(operationFields);
	textField = new JTextField(30);
	
	stringMatchPanel.add(operationCombo);
	stringMatchPanel.add(textField);

	booleanPanel = new JPanel();
	booleanPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
	Vector booleanFields = manager.getOperationLabels(SearchTermManager.BOOLEAN_MATCH);
	booleanValueCombo = new JComboBox(booleanFields);
	booleanPanel.add(booleanValueCombo);

	datePanel = new JPanel();
	datePanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

	Vector dateFields = manager.getOperationLabels(SearchTermManager.DATE_MATCH);
	dateComparisonCombo = new JComboBox(dateFields);
	dateField = new JTextField(30);
	datePanel.add(dateComparisonCombo);
	datePanel.add(dateField);
    }

    /**
     * Returns the JPanel which shows the SearchEntryForm.
     */ 
    public JPanel getPanel() {
	return panel;
    }

    /**
     * This generates a SearchTerm from the information on the JPanel, and
     * returns that value.
     */
    public SearchTerm generateSearchTerm() {
	if (Pooka.isDebug())
	    System.out.println("SearchEntryForm:  generating SearchTerm from " + searchFieldCombo.getSelectedItem() + " and " + operationCombo.getSelectedItem());
	String searchProperty = (String)(manager.getLabelToPropertyMap().get(searchFieldCombo.getSelectedItem()));
	String operationProperty = (String)(manager.getLabelToOperationMap().get(operationCombo.getSelectedItem()));
	if (Pooka.isDebug())
	    System.out.println("using " + searchProperty + ", " + operationProperty);
	String pattern = textField.getText();

	return manager.generateSearchTerm(searchProperty, operationProperty, pattern);
    }

    public void itemStateChanged(java.awt.event.ItemEvent e) {
	String selectedString = (String)(manager.getLabelToPropertyMap().get(searchFieldCombo.getSelectedItem()));
	
	String selectedType = Pooka.getProperty(selectedString + ".type", "");
	layout.show(selectionPanel, selectedType);
    }
}




