package net.suberic.pooka.gui.search;
import javax.swing.*;
import javax.mail.search.*;
import java.util.*;
import net.suberic.pooka.Pooka;
import net.suberic.pooka.SearchTermManager;

/**
 * This is a gui component which lets you configure a 
 * javax.mail.search.SearchTerm.  It can be used either to configure mail
 * filters, or to search folders.
 */
public class SearchEntryForm {
    // ui objects
    private JPanel panel;
    private JComboBox searchFieldCombo;
    private JComboBox operationCombo;
    private JTextField textField;

    // the source SearchTermManager.
    SearchTermManager manager;

    /**
     * Constructs a new SearchEntryForm.
     */
    public SearchEntryForm(SearchTermManager newManager) {
	manager = newManager;
	panel = new JPanel();

	searchFieldCombo = new JComboBox(manager.getTermLabels());
	Vector operationFields = manager.getOperationLabels();
	operationCombo = new JComboBox(operationFields);
	textField = new JTextField(20);
	
	panel.add(searchFieldCombo);
	panel.add(operationCombo);
	panel.add(textField);
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
	System.out.println("SearchEntryForm:  generating SearchTerm from " + searchFieldCombo.getSelectedItem() + " and " + operationCombo.getSelectedItem());
	String searchProperty = (String)(manager.getLabelToPropertyMap().get(searchFieldCombo.getSelectedItem()));
	String operationProperty = (String)(manager.getLabelToOperationMap().get(operationCombo.getSelectedItem()));
	System.out.println("using " + searchProperty + ", " + operationProperty);
	String pattern = textField.getText();

	return manager.generateSearchTerm(searchProperty, operationProperty, pattern);
    }
}
