package net.suberic.pooka.gui.search;
import javax.swing.*;
import net.suberic.pooka.*;

public class SearchForm extends JPanel {
    SearchEntryPanel entryPanel;
    SearchFolderPanel folderPanel;
    
    public SearchForm() {
	this.populatePanel();
    }

    public SearchForm(FolderInfo[] selectedFolders, boolean editable) {

    }

    public SearchForm(StoreInfo[] selectedStores, boolean editable) {

    }

    /**
     * Populates the SearchForm.
     */
    public void populatePanel() {
	this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

	this.add(new JLabel("No folder selected."));
	folderPanel = new SearchFolderPanel();
	entryPanel = new SearchEntryPanel(Pooka.getSearchManager());
	this.add(entryPanel);
	this.add(createButtonPanel());
    }

    /**
     * Creates the button panel for this SearchForm.
     */
    public JPanel createButtonPanel() {
	JPanel buttonPanel = new JPanel();
	JButton okButton = new JButton(Pooka.getProperty("button.ok", "Ok"));
	JButton cancelButton = new JButton(Pooka.getProperty("button.cancel", "Cancel"));
	okButton.addActionListener(new Action() {
		public void actionPerformed (java.awt.event.ActionEvent e) {
		    
		}
	    });
	buttonPanel.add(okButton);
	buttonPanel.add(cancelButton);
	return buttonPanel;
    }
}
