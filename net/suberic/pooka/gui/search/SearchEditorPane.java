package net.suberic.pooka.gui.search;
import net.suberic.util.VariableBundle;
import net.suberic.util.gui.*;
import javax.swing.*;
import java.util.*;

/**
 * This is a class which lets you choose SearchTerms as properties.
 */
public class SearchEditorPane extends DefaultPropertyEditor {
    String property;
    String originalValue;
    VariableBundle sourceBundle;

    Properties originalProperties;

    SearchEntryPanel searchEntryPanel;

    /**
     * This creates a new SearchEditorPane.
     */
    public SearchEditorPane(String newProp, String newTemplate, VariableBundle newBundle) {
	configureEditor(null, newProp, newTemplate, newBundle, true);
    }

    /**
     * This configures an editor for the given property.
     */
   public void configureEditor(PropertyEditorFactory factory, String newProperty, String typeTemplate, VariableBundle bundle, boolean isEnabled) {
       property=newProperty;
       sourceBundle=bundle;
       originalValue = sourceBundle.getProperty(property, "");
       
       searchEntryPanel = new SearchEntryPanel(net.suberic.pooka.Pooka.getSearchManager(), property, sourceBundle);
       originalProperties = searchEntryPanel.generateSearchTermProperties(property);

       this.add(searchEntryPanel);
       labelComponent = new JLabel(sourceBundle.getProperty("title.search.where", "Where"));
       valueComponent = searchEntryPanel;

       this.setEnabled(isEnabled);
   }

    /**
     * Sets the value for this PropertyEditor.
     */
    public void setValue() {
	// we need to go through all of the new properties any only set
	// the ones that have changed.  we also need to remove any that
	// no longer exist.
	Properties newValues = searchEntryPanel.generateSearchTermProperties(property);
	Enumeration newKeys = newValues.keys();

	Set originalKeys = originalProperties.keySet();

	while (newKeys.hasMoreElements()) {
	    Object currentKey = newKeys.nextElement();
	    if (originalKeys.contains(currentKey)) {
		originalKeys.remove(currentKey);
		String originalValue = originalProperties.getProperty((String) currentKey);
		String newValue = newValues.getProperty((String) currentKey);
		if (originalValue == null ||  ! originalValue.equals(newValue))
		    sourceBundle.setProperty((String) currentKey, newValue);
	    } else {
		sourceBundle.setProperty((String) currentKey, newValues.getProperty((String) currentKey));
	    }
	}
	
	Iterator iter = originalKeys.iterator();
	while (iter.hasNext()) {
	    sourceBundle.removeProperty((String) iter.next());
	}
    }

    /**
     * Returns the values that would be set by this SearchEditorPane.
     */
    public Properties getValue() {
	return searchEntryPanel.generateSearchTermProperties(property);
    }

   /**
     * Resets the current Editor to its original value.
     */
    public void resetDefaultValue() {
	searchEntryPanel.setSearchTerm(property, sourceBundle);
    }

    /**
     * Enables or disables this editor.
     */
    public void setEnabled(boolean newValue) {
	searchEntryPanel.setEnabled(newValue);
    }
}
