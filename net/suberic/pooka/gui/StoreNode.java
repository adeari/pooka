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

public class StoreNode extends MailTreeNode {
    
    protected StoreInfo store = null;
    protected String displayName = null;
    protected boolean hasLoaded = false;

    public StoreNode(StoreInfo newStore, JComponent parent) {
	super(newStore, parent);
	store = newStore;
	displayName=Pooka.getProperty("Store." + store.getStoreID() + ".displayName", store.getStoreID());
	setCommands();
	loadChildren();
    }
    
    /**
     * this method returns false--a store is never a leaf.
     */
    public boolean isLeaf() {
	return false;
    }
   

    /**
     * return the number of children for this store node. The first
     * time this method is called we load up all of the folders
     * under the store's defaultFolder
     */
    /**
    public int getChildCount() {
	if (hasLoaded == false) {
	    loadChildren();
	}
	return super.getChildCount();
    }
    */
    /**
     * returns the children of this folder node.  The first
     * time this method is called we load up all of the folders
     * under the store's defaultFolder
     */
    /*
    public java.util.Enumeration children() {
	if (hasLoaded == false) {
	    loadChildren();
	}
	return super.children();
    }

    */

    /**
     * This loads or updates the top-level children of the Store.
     */
    public void loadChildren() {
	/*
	//   connect to the Store if we need to
	
	if (!store.isConnected()) 
	    return; 
	*/
	
	String folderName;
    
	StringTokenizer tokens = new StringTokenizer(Pooka.getProperty("Store." + store.getStoreID() + ".folderList", "INBOX"), ":");
	
	for (int i = 0 ; tokens.hasMoreTokens() ; i++) {
	    folderName = (String)tokens.nextToken();
	    FolderNode node = new FolderNode(new FolderInfo(store, folderName ), getParentContainer());
	    // we used insert here, since add() would mak
	    // another recursive call to getChildCount();
	    insert(node, i);
	} 

	hasLoaded=true;

	javax.swing.JTree folderTree = ((FolderPanel)getParentContainer()).getFolderTree();
	/*	if (folderTree.getModel() instanceof javax.swing.tree.DefaultTreeModel) {
	    ((javax.swing.tree.DefaultTreeModel)folderTree.getModel()).nodeStructureChanged(this);
	}
	*/
    }

    public String getStoreID() {
	if (store != null)
	    return store.getStoreID();
	else
	    return null;
    }

    public StoreInfo getStoreInfo() {
	return store;
    }

    /**
     * We override toString() so we can display the store URLName
     * without the password.
     */

    public String toString() {
	return displayName;
    }

    public boolean isConnected() {
	if (store != null) {
	    return store.isConnected();
	} else 
	    return false;
    }

    public Action[] defaultActions = new Action[] {
	new OpenAction(),
	new SubscribeAction()
	};

    public Action[] getDefaultActions() {
	return defaultActions;
    }
    
    class OpenAction extends AbstractAction {
	
        OpenAction() {
            super("folder-open");
        }
	
        OpenAction(String nm) {
            super(nm);
        }
	
        public void actionPerformed(java.awt.event.ActionEvent e) {
	    if (!store.isConnected())
		try {
		    store.connectStore();
		} catch (MessagingException me) {
		    // I should make this easier.
		    JOptionPane.showInternalMessageDialog(((FolderPanel)getParentContainer()).getMainPanel().getMessagePanel(), Pooka.getProperty("error.Store.connectionFailed", "Failed to open connection to Mail Store.") + "\n" + Pooka.getProperty("error.sourceException", "The underlying exception reads:  ") + "\n" + me.getMessage());
		}
	    javax.swing.JTree folderTree = ((FolderPanel)getParentContainer()).getFolderTree();
	    folderTree.expandPath(folderTree.getSelectionPath());
	}
    }

    class SubscribeAction extends AbstractAction {
	
        SubscribeAction() {
            super("folder-subscribe");
        }
	
        public void actionPerformed(java.awt.event.ActionEvent e) {
	    FolderChooser fc = new FolderChooser(store.getStore(), getStoreID());
	    ((FolderPanel)getParentContainer()).getMainPanel().getMessagePanel().add((JInternalFrame)fc.getFrame());
	    fc.show();
	    try {
		((JInternalFrame)fc.getFrame()).setSelected(true);
	    } catch (java.beans.PropertyVetoException pve) {
	    }

	}
    }

}

