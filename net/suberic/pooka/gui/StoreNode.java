package net.suberic.pooka.gui;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.JComponent;
import javax.mail.*;
import net.suberic.pooka.Pooka;
import net.suberic.pooka.FolderInfo;
import net.suberic.pooka.StoreInfo;
import net.suberic.util.thread.ActionWrapper;
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
	newStore.setStoreNode(this);
	displayName=Pooka.getProperty("Store." + store.getStoreID() + ".displayName", store.getStoreID());
	setCommands();
	loadChildren();
	defaultActions = new Action[] {
	    new ActionWrapper(new OpenAction(), getStoreInfo().getStoreThread()),
	    new ActionWrapper(new SubscribeAction(), getStoreInfo().getStoreThread()),
	    new ActionWrapper(new TestAction(), getStoreInfo().getStoreThread())
		};
	
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
	java.util.Vector storeChildren = getStoreInfo().getChildren();
    
	if (storeChildren != null)
	    for (int i = 0 ; i < storeChildren.size() ; i++) {
		FolderNode node = new FolderNode((FolderInfo)storeChildren.elementAt(i), getParentContainer());
		// we used insert here, since add() would make
		// another recursive call to getChildCount();
		insert(node, i);
	    } 
	
	hasLoaded=true;

	javax.swing.JTree folderTree = ((FolderPanel)getParentContainer()).getFolderTree();
	if (folderTree != null && folderTree.getModel() instanceof javax.swing.tree.DefaultTreeModel) {
	    ((javax.swing.tree.DefaultTreeModel)folderTree.getModel()).nodeStructureChanged(this);
	}
	
    }

    /**
     * This  creates the current PopupMenu if there is not one.  It then
     * will configure the PopupMenu with the current actions.
     *
     * Overrides MailTreeNode.configurePopupMenu();
     */

    public void configurePopupMenu() {
	if (popupMenu == null) {
	    popupMenu = new net.suberic.util.gui.ConfigurablePopupMenu();
	    popupMenu.configureComponent("StoreNode.popupMenu", Pooka.getResources());
	}

	popupMenu.setActive(getActions());
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

    public Action[] defaultActions;

    public Action[] getDefaultActions() {
	return defaultActions;
    }
    
    class OpenAction extends AbstractAction {
	
        OpenAction() {
            super("file-open");
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
	    /*
	    JFileChooser jfc =
		new JFileChooser("/", new net.suberic.pooka.gui.filechooser.MailFileSystemView(getStoreInfo().getStore()));
	    jfc.setMultiSelectionEnabled(true);
	    int returnValue =
		jfc.showDialog(getParentContainer(),
			       Pooka.getProperty("FolderEditorPane.Select",
						 "Select"));
	    if (returnValue == JFileChooser.APPROVE_OPTION) {
		net.suberic.pooka.gui.filechooser.FolderFileWrapper wrapper =
		    ((net.suberic.pooka.gui.filechooser.FolderFileWrapper)jfc.getSelectedFile());
		System.out.println("got folder " + wrapper.getPath());
		
	    */
	    

	    String newFolder = JOptionPane.showInternalInputDialog(((FolderPanel)getParentContainer()).getMainPanel().getMessagePanel(), "Subscribe to what folder?");
	    getStoreInfo().subscribeFolder(newFolder);
	}
    }
    
    class TestAction extends AbstractAction {
	
        TestAction() {
            super("test");
        }
	
        public void actionPerformed(java.awt.event.ActionEvent e) {
	    JFileChooser jfc = new JFileChooser("/", new net.suberic.pooka.gui.filechooser.MailFileSystemView(getStoreInfo().getStore()));
	    jfc.showOpenDialog(getParentContainer());
	}
    }

}

