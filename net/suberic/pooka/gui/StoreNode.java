package net.suberic.pooka.gui;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.JComponent;
import javax.mail.*;
import net.suberic.pooka.Pooka;
import net.suberic.pooka.FolderInfo;
import java.util.StringTokenizer;
import javax.swing.*;
import javax.mail.event.*;

public class StoreNode extends MailTreeNode {
    
    protected Store store = null;
    protected Folder folder = null;
    protected String storeID = null;
    protected String displayName = null;
    private boolean connected = false;
    protected boolean listSubscribedOnly;

    public StoreNode(Store newStore, String storePrefix, JComponent parent, boolean subscribedOnly) {
	super(newStore, parent);
	store = newStore;
	storeID=storePrefix;
	listSubscribedOnly=subscribedOnly;
	displayName=Pooka.getProperty("Store." + storeID + ".displayName", storeID);
	setCommands();
	if (listSubscribedOnly) {
	    store.addConnectionListener(new ConnectionAdapter() {
		    public void closed(ConnectionEvent e) {
			System.out.println("Store " + displayName + " closed.");
			if (connected == true) {
			    try {
				connectStore();
			    } catch (MessagingException me) {
				System.out.println("Store " + displayName + " closed and unable to reconnect:  " + me.getMessage());
			    }
			}
		    }

		    public void disconnected(ConnectionEvent e) {
			System.out.println("Folder " + displayName + " disconnected.");
			if (connected == true) {
			    try {
				connectStore();
			    } catch (MessagingException me) {
				System.out.println("Disconnected from store " + displayName + " and unable to reconnect:  " + me.getMessage());
			    }
			}
		    }
		});
	}
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
	if (folder == null) {
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
	if (folder == null) {
	    loadChildren();
	}
	return super.children();
    }

    protected void connectStore() throws MessagingException {
	if (isConnected())
	    return;

	store.connect();

	// get the default folder, too.

	try {
	    folder = store.getDefaultFolder();
	} catch (MessagingException meFolder) {
	    try {
		store.close();
	    } catch (MessagingException meClose) {
		// we don't care here.
	    }
	    folder=null;
	    throw meFolder;
	}
    }
	

	
    protected void loadChildren() {
	// connect to the Store if we need to

	if (!store.isConnected()) 
	    try {
		connectStore();

	    } catch (MessagingException me) {
		return;
	    }

	if (folder == null)
	    try {
		folder = store.getDefaultFolder();
	    } catch (MessagingException meFolder) {
		try {
		    store.close();
		} catch (MessagingException meClose) {
		    // we don't care here.
		}
		folder=null;
		return;
	    }


	if (listSubscribedOnly) {
	    Folder[] subscribed;
	    
	    String folderName;
	    
	    StringTokenizer tokens = new StringTokenizer(Pooka.getProperty("Store." + storeID + ".folderList", "INBOX"), ":");
	    
	    for (int i = 0 ; tokens.hasMoreTokens() ; i++) {
		try {
		    folderName = (String)tokens.nextToken();
		    subscribed = folder.list(folderName);
		    FolderNode node = new FolderNode(new FolderInfo(subscribed[0], storeID + "." + folderName ),getParentContainer(), listSubscribedOnly);
		    // we used insert here, since add() would mak
		    // another recursive call to getChildCount();
		    insert(node, i);
		} catch (MessagingException me) {
		    if (me instanceof FolderNotFoundException) {
			JOptionPane.showInternalMessageDialog(((FolderPanel)getParentContainer()).getMainPanel().getMessagePanel(), Pooka.getProperty("error.FolderWindow.folderNotFound", "Could not find folder.") + "\n" + me.getMessage());
		    } else {
			me.printStackTrace();
		    }
		}
	    }
	} else {
	    try {
		Folder[] folderList = folder.list();

		if (folderList != null) 
		    for (int i = 0 ; i < folderList.length ; i++) {
			FolderNode node = new FolderNode(new FolderInfo(folderList[i], storeID + "." + folderList[i].getName() ),getParentContainer(), false);
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
	}
    }
    public String getStoreID() {
	return storeID;
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

    /**
     * Here we set the connection value that we _want_ to have.  We
     * also try to open or close the connection, as appropriate.
     */
    public void setConnected(boolean newValue) {
	connected=newValue;
	if (store != null && store.isConnected() != newValue) {
	    if (newValue)
		try {
		    connectStore();
		} catch (MessagingException me) {
		    System.out.println("Error connecting to store:  " + me.getMessage());
		}
	}
    }

    /**
     * Subscribes to the given Folder.
     *
     */

    public void subscribeFolder(Folder f) {
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
	    if (!isConnected())
		try {
		    connectStore();
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
	    FolderChooser fc = new FolderChooser(store, getStoreID());
	    ((FolderPanel)getParentContainer()).getMainPanel().getMessagePanel().add((JInternalFrame)fc.getFrame());
	    fc.show();
	    try {
		((JInternalFrame)fc.getFrame()).setSelected(true);
	    } catch (java.beans.PropertyVetoException pve) {
	    }

	}
    }

}

