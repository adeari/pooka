package net.suberic.pooka.gui.search;
import net.suberic.pooka.*;
import javax.swing.*;

/**
 * This is a full panel for making search options.  This panel combines lots
 * of SearchEntryForms with some controls for adding and removing 
 * SearchEntryForms.  getSearchTerm() returns the composite SearchTerm
 * made from all of the SearchEntryForms.
 */
public class SearchEntryPanel extends JPanel {

    // bottom panel
    JPanel conditionPanel;
    JScrollPane conditionScrollPane;
    SearchTermPane[] searchTerms;

    /**
     * Creates a new SearchTermPanel.
     */
    public SearchTermPanel() {
	populatePanel();
    }
    
    /**
     * Populates the panel with all the appropriate widgets.  Called by
     * the constructor.
     */
    public void populatePanel() {
	
    }
    
    
    public SearchTerm getSearchTerm() {
	
    }
}
