package net.suberic.pooka.gui;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.JComponent;
import javax.mail.*;
import net.suberic.pooka.Pooka;
import net.suberic.pooka.FolderInfo;
import net.suberic.pooka.StoreInfo;
import net.suberic.util.thread.ActionWrapper;
import net.suberic.pooka.gui.search.*;
import net.suberic.pooka.gui.filechooser.*;
import java.io.File;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Enumeration;
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
	    new SubscribeAction(),
	    new TestAction(),
	    new ActionWrapper(new DisconnectAction(), getStoreInfo().getStoreThread()),
	    new EditAction()
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
	
	if (Pooka.isDebug())
	    System.out.println("calling loadChildren() for " + getStoreInfo().getStoreID());

	Enumeration origChildren = super.children();
	Vector origChildrenVector = new Vector();
	while (origChildren.hasMoreElements())
	    origChildrenVector.add(origChildren.nextElement());

	if (Pooka.isDebug())
	    System.out.println(getStoreInfo().getStoreID() + ":  origChildrenVector.size() = " + origChildrenVector.size());

	Vector storeChildren = getStoreInfo().getChildren();
	
	if (Pooka.isDebug())
	    System.out.println(getStoreInfo().getStoreID() + ":  storeChildren.size() = " + storeChildren.size());

	if (storeChildren != null) {
	    for (int i = 0; i < storeChildren.size(); i++) {
		FolderNode node = popChild(((FolderInfo)storeChildren.elementAt(i)).getFolderName(), origChildrenVector);
		if (node == null) {
		    node = new FolderNode((FolderInfo)storeChildren.elementAt(i), getParentContainer());
		    // we used insert here, since add() would mak
		    // another recursive call to getChildCount();
		    insert(node, 0);
		}
	    }
	    
	}
	
	removeChildren(origChildrenVector);
	
	hasLoaded=true;
	

	javax.swing.JTree folderTree = ((FolderPanel)getParentContainer()).getFolderTree();
	if (folderTree != null && folderTree.getModel() instanceof javax.swing.tree.DefaultTreeModel) {
	    ((javax.swing.tree.DefaultTreeModel)folderTree.getModel()).nodeStructureChanged(this);
	}

	/*
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
	*/
    }

    /**
     * This goes through the Vector of FolderNodes provided and 
     * returns the FolderNode for the given childName, if one exists.
     * It will also remove the Found FolderNode from the childrenList
     * Vector.
     *
     * If a FolderNode that corresponds with the given childName does
     * not exist, this returns null.
     *
     */
    public FolderNode popChild(String childName, Vector childrenList) {
	if (children != null) {
	    for (int i = 0; i < childrenList.size(); i++)
		if (((FolderNode)childrenList.elementAt(i)).getFolderInfo().getFolderName().equals(childName)) {
		    FolderNode fn = (FolderNode)childrenList.elementAt(i);
		    childrenList.remove(fn);
		    return fn;
		}
	}
	
	// no match.
	return null;
    }

    /**
     * This removes all the items in removeList from the list of this 
     * node's children.
     */
    public void removeChildren(Vector removeList) {
	for (int i = 0; i < removeList.size(); i++) {
	    if (removeList.elementAt(i) instanceof javax.swing.tree.MutableTreeNode)
		this.remove((javax.swing.tree.MutableTreeNode)removeList.elementAt(i));
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
		    Pooka.getUIFactory().showError(Pooka.getProperty("error.Store.connectionFailed", "Failed to open connection to Mail Store.") + "\n" + Pooka.getProperty("error.sourceException", "The underlying exception reads:  ") + "\n" + me.getMessage());
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

	    JFileChooser jfc =
		new JFileChooser(getStoreInfo().getStoreID(), new net.suberic.pooka.gui.filechooser.MailFileSystemView(getStoreInfo()));
	    jfc.setMultiSelectionEnabled(true);
	    jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
	    int returnValue =
		jfc.showDialog(getParentContainer(),
			       Pooka.getProperty("FolderEditorPane.Select",
						 "Select"));
	    if (returnValue == JFileChooser.APPROVE_OPTION) {
		net.suberic.pooka.gui.filechooser.FolderFileWrapper wrapper =
		    ((net.suberic.pooka.gui.filechooser.FolderFileWrapper)jfc.getSelectedFile());
		String absFileName = wrapper.getAbsolutePath();
		int firstSlash = absFileName.indexOf('/');
		String normalizedFileName = absFileName;
		if (firstSlash >= 0)
		    normalizedFileName = absFileName.substring(firstSlash);
		    
		getStoreInfo().subscribeFolder(normalizedFileName);
	    }
		/*
		  String newFolder = JOptionPane.showInternalInputDialog(((FolderPanel)getParentContainer()).getMainPanel().getMessagePanel(), "Subscribe to what folder?");
		  getStoreInfo().subscribeFolder(newFolder);
		*/
	}
    }
    
    class TestAction extends AbstractAction {
	
        TestAction() {
            super("file-test");
        }
	
        public void actionPerformed(java.awt.event.ActionEvent e) {
	    MailFileSystemView mfsv = new net.suberic.pooka.gui.filechooser.MailFileSystemView(getStoreInfo());
	    File f = mfsv.createFileObject("/");
	    File[] files = mfsv.getFiles(f, false);

	    /*
	    try {
		
		Store s = Pooka.getStoreManager().getStoreInfo("mailtest").getStore();
		System.out.println("got store.");
		if (!s.isConnected()) {
		    if (Pooka.isDebug()) {
			System.out.println("store is disconnected.  reconnecting.");
			s.connect();
		    }
		}
		if (!s.isConnected()) {
		    if (Pooka.isDebug()) {
			System.out.println("store is disconnected.  reconnecting.");
			s.connect();
		    }
		}
		Folder defaultFolder = s.getDefaultFolder();
		System.out.println("got default folder.");
		defaultFolder.list();
	    } catch (MessagingException me) {
		System.out.println("got messaging exception.");
		me.printStackTrace();
	    }
	    */

	    /*
	    JInternalFrame jif = new JInternalFrame();
	    
	    SearchEntryForm sef = new SearchEntryForm(Pooka.getSearchManager());
	    jif.getContentPane().add(sef.getPanel());

	    MessagePanel mp = ((FolderPanel)getParentContainer()).getMainPanel().getMessagePanel();
	    jif.pack();
	    mp.add(jif);
	    jif.setVisible(true);
	    try {
		jif.setSelected(true);
	    } catch (java.beans.PropertyVetoException pve) { }
	    */
	}
    }

    class DisconnectAction extends AbstractAction {
	
        DisconnectAction() {
            super("file-close");
        }
	
        public void actionPerformed(java.awt.event.ActionEvent e) {
	    try {
		getStoreInfo().disconnectStore();
	    } catch (Exception ex) {
		System.out.println("caught exception:  " + ex.getMessage());
	    }
	}
    }

    class EditAction extends AbstractAction {
	
        EditAction() {
            super("file-edit");
        }
	
        EditAction(String nm) {
            super(nm);
        }
	
        public void actionPerformed(java.awt.event.ActionEvent e) {
	    Pooka.getUIFactory().showEditorWindow(getStoreInfo().getStoreProperty(), getStoreInfo().getStoreProperty(), "Store.editableFields");
	}
    }    
}

