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

    public static int AND = 0;
    public static int OR = 1;

    JPanel conditionPanel;
    JPanel entryPanel;
    JScrollPane entryScrollPane;
    Vector searchTerms;
    SearchTermManager manager;

    class SearchEntryPair {
	SearchEntryForm form;
	int type;

	public SearchEntryPair(SearchEntryForm newForm, int newType) {
	    form = newForm;
	    type = newType;
	}
    }

    /**
     * Creates a new SearchTermPanel.
     */
    public SearchTermPanel(SearchTermManager newManager) {
	manager = newManager;
	populatePanel();
    }
    
    /**
     * Populates the panel with all the appropriate widgets.  Called by
     * the constructor.
     */
    public void populatePanel() {
	entryPanel = new JPanel();
	entryPanel.setLayout(new BoxLayout(entryPanel, BoxLayout.Y_AXIS));

	addSearchEntryForm(AND);

	entryScrollPane = new JScrollPane(entryPanel);
	this.add(entryScrollPane);
    }

    /**
     * This creates the conditionPanel.  This is always the bottom panel,
     * and contains the and / or buttons.  Pressing one of these buttons
     * will create a new SearchEntryForm.
     */
    private void createConditionPanel() {
	JPanel jp = new JPanel();
	jp.setLayout(new FlowLayout());
	
	JButton buttonOne = new JButton(Pooka.getProperty("Search.button.and.label", "And"));
	buttonOne.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    addSearchEntryForm(AND);
		}
	    });

	JButton buttonTwo = new JButton(Pooka.getProperty("Search.button.or.label", "Or"));
	buttonTwo.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    addSearchEntryForm(OR);
		}
	    });

	jp.add(buttonOne);
	jp.add(buttonTwo);

	conditionPanel=jp;
    }

    public void addSearchEntryForm(int type) {
	SearchEntryForm sef = new SearchEntryForm(manager);
	
	searchTerms.add(new SearchEntryPair(sef, type));
	
    }

    public SearchTerm getSearchTerm() {
	
    }
}
