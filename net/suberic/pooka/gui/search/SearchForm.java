package net.suberic.pooka.gui.search;
import javax.swing.*;
import net.suberic.pooka.*;
import java.util.Vector;

public class SearchForm extends JPanel {
    SearchEntryPanel entryPanel;
    SearchFolderPanel folderPanel;
    
    public SearchForm() {
	this.populatePanel();
    }

    public SearchForm(FolderInfo[] selectedFolders) {
	folderPanel = new SearchFolderPanel(selectedFolders);
	populatePanel();
    }

    public SearchForm(StoreInfo[] selectedStores) {
	folderPanel = new SearchFolderPanel(selectedStores);
	populatePanel();
    }

    /**
     * Populates the SearchForm.
     */
    public void populatePanel() {
	this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

	this.add(folderPanel);
	entryPanel = new SearchEntryPanel(Pooka.getSearchManager());
	this.add(entryPanel);
    }

    public Vector getSelectedFolders() {
	return folderPanel.getSelectedFolders();
    }

    public javax.mail.search.SearchTerm getSearchTerm() {
	return entryPanel.getSearchTerm();
    }
}
