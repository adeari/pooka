package net.suberic.pooka.gui;
import javax.swing.*;
import javax.mail.search.*;
import java.util.*;

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
    private JToggleButton useRegexpButton;

    // maps
    private HashMap labelToPropertyMap;
    
    /**
     * Constructs a new, empty SearchEntryForm.
     */
    public SearchEntryForm() {
	panel = new JPanel();
	labelToClassMap = createLabeltoPropertyMap();
	Vector searchFields = new Vector(labelToClassMap.keySet());

	searchFieldCombo = new JComboBox(searchFields);
	Vector operationFields = Pooka.getResources().getPropertyAsVector("SearchEntryForm.searchOperation", "");
	operationCombo = new JComboBox(operationFields);
	textField = new JTextField();
	useRegexpButton = new JToggleButton(Pooka.getProperty("SearchEntryForm.useRegexp.label", "Use Regexp"));
	
	panel.add(searchFieldCombo);
	panel.add(operationCombo);
	panel.add(textField);
	panel.add(useRegexpButton);
    }

    /**
     * Creates the labelToPropertyMap from the SearchEntryForm.searchFields 
     * property.
     */
    private HashMap createLabeltoPropertyMap() {
	Vector keys = Pooka.getPropertyAsVector("SearchEntryForm.searchFields", "");
	if (keys != null) {
	    HashMap returnValue = new HashMap();
	    for (int i = 0; i < keys.length; i++) {
		String thisValue = "SearchEntryForm.searchFields." + (String) keys.elementAt(i);
		returnValue.put(Pooka.getProperty(thisValue + ".label", (String)keys.elementAt(i)), thisValue);
	    }
	} else 
	    return null;
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
	SearchTerm st = null;
	String propertyName = (String)labelToPropertyMap.get(searchFieldCombo.getSelectedItem());
	/*
	  Class searchClass = Class.forName(Pooka.getProperty(propertyName + ".class", ""));
	  st = (SearchTerm) searchClass.newInstance();
	  if (st instanceof AddressTerm) {
	  
	  } else if (st instanceof StringTerm) {
	  
	  }
	*/

	if (Pooka.getProperty(propertyName + ".class", "").equals("javax.mail.search.BodyTerm")) {
	    st = new BodyTerm(textField.getText(), true);
	    return st;
	}
    }
}
