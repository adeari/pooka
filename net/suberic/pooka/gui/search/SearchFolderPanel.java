package net.suberic.pooka.gui.search;
import net.suberic.pooka.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.Vector;

/**
 * This is a panel which allows you to choose which folders you will
 * use for searches.
 */
public class SearchFolderPanel extends JPanel {

    Vector selected = new Vector();
    boolean isEditable = true;

    /**
     * Create a SearchFolderPanel with no folders selected.  By default, this
     * means that we have a SearchFolderPanel which can select from all of
     * the available Folders.
     */
    public SearchFolderPanel() {
	
    }

    /**
     * Create a SearchFolderPanel with all the Folders in the given Store(s)
     * selected.
     */
    public SearchFolderPanel(StoreInfo[] storeList, boolean editable) {
	isEditable = editable;
	if (!editable) {
	    selected = new Vector();
	    for (int i = 0; i < storeList.length; i++)
		selected.addAll(storeList[i].getAllFolders());

	    createStaticPanel(selected);

	}
    }

    /**
     * Create a SearchFolderPanel with all the Folders given selected.
     */
    public SearchFolderPanel(FolderInfo[] folderList, boolean editable) {
	isEditable = editable;
	if (!editable) {
	    for (int i = 0; i < folderList.length; i++) {
		selected.add(folderList[i]);
	    }

	    createStaticPanel(selected);
	}
    }

    /**
     * This creates a static panel.
     */
    public void createStaticPanel(Vector folderList) {
	StringBuffer msg = new StringBuffer();
	msg.append("Searching folders:\n");
	for (int i = 0; i < folderList.size(); i++) 
	    msg.append(((FolderInfo)folderList.elementAt(i)).getFolderID() + "\n");
	JTextArea label = new JTextArea(msg.toString());
	this.add(label);
    }

    /**
     * Returns a Vector of selected FolderInfos to search.
     */
    public Vector getSelectedFolders() {
	if (! isEditable) {
	    return selected;
	} else {
	    //get the selected folders from the gui component.
	    return null;
	}
    }
    
}
