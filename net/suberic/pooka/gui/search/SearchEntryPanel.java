package net.suberic.pooka.gui.search;
import net.suberic.pooka.*;
import javax.swing.*;
import java.util.Vector;
import java.awt.event.*;
import java.awt.FlowLayout;
import javax.mail.search.SearchTerm;

/**
 * This is a full panel for making search options.  This panel combines lots
 * of SearchEntryForms with some controls for adding and removing 
 * SearchEntryForms.  getSearchTerm() returns the composite SearchTerm
 * made from all of the SearchEntryForms.
 */
public class SearchEntryPanel extends JPanel {

    public static int FIRST = -1;
    public static int AND = 0;
    public static int OR = 1;

    public static String AND_LABEL = Pooka.getProperty("Search.button.and.label", "And");
    public static String OR_LABEL = Pooka.getProperty("Search.button.or.label", "Or");
    
    JPanel conditionPanel;
    JPanel entryPanel;
    JScrollPane entryScrollPane;
    Vector searchTerms = new Vector();
    SearchTermManager manager;

    class SearchEntryPair {
	SearchEntryForm form;
	SearchConnector connector;

	public SearchEntryPair(SearchEntryForm newForm, int newType) {
	    form = newForm;
	    connector = new SearchConnector(newType);
	}
    }

    class SearchConnector extends JPanel {
	
	JComboBox list;
	SearchConnector(int newType) {
	    String[] choices = new String[2];
	    choices[0] = AND_LABEL;
	    choices[1] = OR_LABEL;
	    list = new JComboBox(choices);

	    if (newType < 2)
		list.setSelectedIndex(newType);
	    else
		list.setSelectedIndex(0);

	    this.add(list);
	}

	public int getType() {
	    return list.getSelectedIndex();
	}
    }

    /**
     * Creates a new SearchEntryPanel.
     */
    public SearchEntryPanel(SearchTermManager newManager) {
	manager = newManager;
	populatePanel();
    }
    
    /**
     * Populates the panel with all the appropriate widgets.  Called by
     * the constructor.
     */
    public void populatePanel() {
	this.setLayout(new java.awt.BorderLayout());
	entryPanel = new JPanel();
	entryPanel.setLayout(new BoxLayout(entryPanel, BoxLayout.Y_AXIS));

	addSearchEntryForm(FIRST);

	createConditionPanel();

	entryScrollPane = new JScrollPane(entryPanel);
	int defaultHeight = entryPanel.getPreferredSize().height;
	entryScrollPane.setPreferredSize(new java.awt.Dimension(entryPanel.getPreferredSize().width + 15, defaultHeight * 3));

	this.add(conditionPanel, java.awt.BorderLayout.SOUTH);
	this.add(entryScrollPane, java.awt.BorderLayout.CENTER);
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
	if (type == FIRST) {
	    SearchEntryForm sef = new SearchEntryForm(manager);
	    searchTerms.add(new SearchEntryPair(sef, AND));
	    entryPanel.add(sef.getPanel());
	} else {
	    SearchEntryForm sef = new SearchEntryForm(manager);
	    SearchEntryPair pair = new SearchEntryPair(sef, AND);
	    searchTerms.add(pair);
	    entryPanel.add(pair.connector);
	    entryPanel.add(sef.getPanel());
	    entryPanel.revalidate();
	    //entryPanel.repaint();
	}
    }

    /**
     * Returns the SearchTerm specified by this SearchEntryPanel.
     */
    public SearchTerm getSearchTerm() throws java.text.ParseException {
	if (Pooka.isDebug())
	    System.out.println("calling SearchEntryPanel.getSearchTerm()");
	if (searchTerms.size() > 0) {
	    if (Pooka.isDebug())
		System.out.println("SearchEntryPanel:  searchTerms.size() > 0.");
	    SearchEntryPair pair = (SearchEntryPair) searchTerms.elementAt(0);
	    SearchTerm term = pair.form.generateSearchTerm();
	    if (Pooka.isDebug())
		System.out.println("SearchEntryPanel:  setting term to " + term);
	    for (int i = 1; i < searchTerms.size(); i++) {
		SearchEntryPair newPair = (SearchEntryPair) searchTerms.elementAt(i);
		SearchTerm newTerm = newPair.form.generateSearchTerm();
		if (newPair.connector.getType() == AND) {
		    term = new javax.mail.search.AndTerm(term, newTerm);
		} else if (newPair.connector.getType() == OR) {
		    term = new javax.mail.search.OrTerm(term, newTerm);
		}
	    }

	    return term;
	} else
	    return null;
    }
}
