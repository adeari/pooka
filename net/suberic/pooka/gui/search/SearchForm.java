package net.suberic.pooka.gui.search;
import javax.swing.*;
import net.suberic.pooka.*;
import java.util.Vector;

public class SearchForm extends JDialog {
    SearchEntryPanel entryPanel;
    SearchFolderPanel folderPanel;
    int returnValue = JOptionPane.OK_OPTION;
    
    public SearchForm() {
	this.populatePanel();
    }

    public SearchForm(FolderInfo[] selectedFolders, boolean editable) {

    }

    public SearchForm(StoreInfo[] selectedStores, boolean editable) {
	folderPanel = new SearchFolderPanel(selectedStores, editable);
	populatePanel();
    }

    /**
     * Populates the SearchForm.
     */
    public void populatePanel() {
	this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));

	this.getContentPane().add(folderPanel);
	entryPanel = new SearchEntryPanel(Pooka.getSearchManager());
	this.getContentPane().add(entryPanel);
	this.getContentPane().add(createButtonPanel());
    }

    /**
     * Creates the button panel for this SearchForm.
     */
    public JPanel createButtonPanel() {
	JPanel buttonPanel = new JPanel();
	JButton okButton = new JButton(Pooka.getProperty("button.ok", "Ok"));
	JButton cancelButton = new JButton(Pooka.getProperty("button.cancel", "Cancel"));
	okButton.addActionListener(new AbstractAction() {
		public void actionPerformed (java.awt.event.ActionEvent e) {
		    returnValue = JOptionPane.OK_OPTION;
		    dispose();
		}
	    });
	cancelButton.addActionListener(new AbstractAction() {
		public void actionPerformed (java.awt.event.ActionEvent e) {
		    returnValue = JOptionPane.CANCEL_OPTION;
		    dispose();
		}
	    });
	buttonPanel.add(okButton);
	buttonPanel.add(cancelButton);
	return buttonPanel;
    }

    public int getReturnValue() {
	return returnValue;
    }

    public Vector getSelectedFolders() {
	return folderPanel.getSelectedFolders();
    }

    public javax.mail.search.SearchTerm getSearchTerm() {
	return entryPanel.getSearchTerm();
    }
}
