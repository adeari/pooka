package net.suberic.pooka.gui;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.JComponent;
import javax.mail.*;
import net.suberic.pooka.Pooka;
import net.suberic.pooka.FolderInfo;
import net.suberic.pooka.StoreInfo;
import java.util.StringTokenizer;
import javax.swing.*;
import javax.mail.event.*;

public class ChooserStoreNode extends MailTreeNode {
    protected Store store = null;
    protected String displayName = null;
    protected boolean hasLoaded = false;
    protected String storeID;

    public ChooserStoreNode(Store newStore, String newStoreID, JComponent parent) {
	super(newStore, parent);
	storeID = newStoreID;
	store = newStore;
	displayName=Pooka.getProperty(getStoreProperty() + ".displayName", storeID);
	setCommands();
    }
    
    /**
     * a Store is never a leaf node.  It can always contain stuff
     */
    public boolean isLeaf() {
	return false;
    }
   

    /**
     * return the number of children for this store node. The first
     * time this method is called we load up all of the folders
     * under the store's defaultFolder
     */

    public int getChildCount() {
	if (hasLoaded == false) {
	    loadChildren();
	}
	return super.getChildCount();
    }
    
    /**
     * returns the children of this folder node.  The first
     * time this method is called we load up all of the folders
     * under the store's defaultFolder
     */
    
    public java.util.Enumeration children() {
	if (hasLoaded == false) {
	    loadChildren();
	}
	return super.children();
    }

    /**
     * This loads or updates the top-level children of the Store.
     */
    public void loadChildren() {
	// connect to the Store if we need to

	if (!store.isConnected()) 
	    try {
		store.connect();
	    } catch (MessagingException me) {
		return;
	    }
	
	try {
	    Folder[] folderList = store.getDefaultFolder().list();
		
	    if (folderList != null) 
		for (int i = 0 ; i < folderList.length ; i++) {
		    ChooserFolderNode node = new ChooserFolderNode(folderList[i], getStoreProperty() + "." + folderList[i].getName(), getParentContainer());
		    // we used insert here, since add() would mak
		    // another recursive call to getChildCount();
		    insert(node, i);
		}
	} catch (MessagingException me) {
	    if (me instanceof FolderNotFoundException) {
		JOptionPane.showInternalMessageDialog(((FolderPanel)getParentContainer()).getMainPanel().getMessagePanel(), Pooka.getProperty("error.FolderWindow.folderNotFound", "Could not find folder.") + "\n" + me.getMessage());
	    } else {
		me.printStackTrace();
	    }
	}

	hasLoaded=true;
    }

    public String getStoreID() {
	return storeID;
    }

    public String getStoreProperty() {
	return "Store." + storeID;
    }

    /**
     * We override toString() so we can display the store URLName
     * without the password.
     */

    public String toString() {
	return displayName;
    }

    /**
     * Subscribes to the given Folder.
     *
     */

    public Action[] getDefaultActions() {
	return null;
    }
    
}

