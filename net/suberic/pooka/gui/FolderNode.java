package net.suberic.pooka.gui;
import javax.swing.JComponent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import javax.swing.tree.*;
import javax.mail.Store;
import javax.mail.Folder;
import javax.mail.MessagingException;
import java.util.*;
import net.suberic.pooka.*;
import net.suberic.util.thread.*;
import javax.mail.FolderNotFoundException;
import javax.swing.JOptionPane;
import net.suberic.pooka.FolderInfo;
import javax.mail.event.*;

public class FolderNode extends MailTreeNode implements MessageChangedListener, UserProfileContainer, ConnectionListener {
    
    protected FolderInfo folderInfo = null;
    protected boolean hasLoaded = false;

    /**
     * creates a tree node that points to a folder
     *
     * @param newFolder	the store for this node
     * @param newParent the parent component
     */
    public FolderNode(FolderInfo newFolderInfo, JComponent newParent) {
	super(newFolderInfo, newParent);
	folderInfo = newFolderInfo;

	folderInfo.setFolderNode(this);

	commands = new Hashtable();
	
	defaultActions = new Action[] {
	    new ActionWrapper(new OpenAction(), folderInfo.getFolderThread()),
	    new ActionWrapper(new CloseAction(), folderInfo.getFolderThread()),
	    new ActionWrapper(new UnsubscribeAction(), folderInfo.getFolderThread())
		};

	Action[] actions = defaultActions;

	if (actions != null) {
	    for (int i = 0; i < actions.length; i++) {
		Action a = actions[i];
		commands.put(a.getValue(Action.NAME), a);
	    }
	}

	folderInfo.addMessageCountListener(new MessageCountAdapter() {
	    public void messagesAdded(MessageCountEvent e) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
			    getParentContainer().repaint();
			    if ( folderInfo.notifyNewMessagesMain()) {
				Pooka.getMainPanel().setNewMessageFlag(true);
			    }
			}
		    });
	    }
	    
	    public void messagesRemoved(MessageCountEvent e) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
			    getParentContainer().repaint();
			}
		    });
		
	    }
	    });
	
	folderInfo.addMessageChangedListener(this);
	folderInfo.addConnectionListener(this);
	loadChildren();

    }

    
    /**
     * a Folder is a leaf if it cannot contain sub folders
     */
    public boolean isLeaf() {
	if (getChildCount() < 1)
	    return true;
	else
	    return false;
    }
   
    /**
     * returns the folder for this node
     */
    public Folder getFolder() {
	return folderInfo.getFolder();
    }
    


    /**
     * return the number of children for this folder node. The first
     * time this method is called we load up all of the folders
     * under the store's defaultFolder
     */

    /**
    public int getChildCount() {
	if (!hasLoaded) {
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
	if (!hasLoaded) {
	    loadChildren();
	}
	return super.children();
    }

    */

    /**
     * This loads (or reloads) the children of the FolderNode from
     * the list of Children on the FolderInfo.
     */
    public void loadChildren() {

	Enumeration origChildren = super.children();
	Vector origChildrenVector = new Vector();
	while (origChildren.hasMoreElements())
	    origChildrenVector.add(origChildren.nextElement());

	Vector folderChildren = getFolderInfo().getChildren();
	
	if (folderChildren != null) {
	    for (int i = 0; i < folderChildren.size(); i++) {
		FolderNode node = popChild(((FolderInfo)folderChildren.elementAt(i)).getFolderName(), origChildrenVector);
		if (node == null) {
		    node = new FolderNode((FolderInfo)folderChildren.elementAt(i), getParentContainer());
		    // we used insert here, since add() would mak
		    // another recursive call to getChildCount();
		    insert(node, 0);
		}
	    }
	    
	}
	
	removeChildren(origChildrenVector);
	
	hasLoaded=true;
	
	//	((javax.swing.tree.DefaultTreeModel)(((FolderPanel)getParentContainer()).getFolderTree().getModel())).nodeStructureChanged(this);
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
     * This  creates the current PopupMenu if there is not one.  It then
     * will configure the PopupMenu with the current actions.
     *
     * Overrides MailTreeNode.configurePopupMenu();
     */

    public void configurePopupMenu() {
	if (popupMenu == null) {
	    popupMenu = new net.suberic.util.gui.ConfigurablePopupMenu();
	    if (getFolderInfo().isTrashFolder())
		popupMenu.configureComponent("TrashFolderNode.popupMenu", Pooka.getResources());
	    else
		popupMenu.configureComponent("FolderNode.popupMenu", Pooka.getResources());
	}

	popupMenu.setActive(getActions());
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
     * This makes the FolderNode visible in its parent JTree.
     */
    public void makeVisible() {
	javax.swing.SwingUtilities.invokeLater(new Runnable() {
		public void run() {
	javax.swing.JTree folderTree = ((FolderPanel)getParentContainer()).getFolderTree();
	TreeNode[] nodeList = ((DefaultTreeModel)folderTree.getModel()).getPathToRoot(FolderNode.this);
	TreePath path = new TreePath(nodeList);
	folderTree.makeVisible(path);
		}
	    });
    }

    public void messageChanged(MessageChangedEvent mce) {
	javax.swing.SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		    getParentContainer().repaint();
		}
	    });
    }

    public void closed(ConnectionEvent e) {
	javax.swing.SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		    getParentContainer().repaint();
		}
	    });
    }

    public void opened(ConnectionEvent e) {
	javax.swing.SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		    getParentContainer().repaint();
		}
	    });
    }

    public void disconnected(ConnectionEvent e) {
	javax.swing.SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		    getParentContainer().repaint();
		}
	    });
    }

    
    /**
     * This opens up a dialog asking if the user wants to unsubsribe to 
     * the current Folder.  If the user chooses 'yes', then
     * getFolderInfo().unsubscribe() is called.
     */
    public void unsubscribeFolder() {
	String message;
	if (isLeaf())
	    message = Pooka.getProperty("Folder.unsubscribeConfirm", "Do you really want to unsubscribe from the following folder?");
	else
	    message = Pooka.getProperty("Folder.unsubscribeConfirm.notLeaf", "Do you really want to unsubscribe from \nthis folder and all its children?");
	
	int response = Pooka.getUIFactory().showConfirmDialog(message + "\n" + getFolderInfo().getFolderName(), Pooka.getProperty("Folder.unsubscribeConfirm.title", "Unsubscribe from Folder"), JOptionPane.YES_NO_OPTION);
	
	if (response == JOptionPane.YES_OPTION)
	    getFolderInfo().unsubscribe();
    }

    public String getFolderID() {
	return getFolderInfo().getFolderID();
    }

    public FolderInfo getFolderInfo() {
	return folderInfo;
    }

    /**
     * override toString() since we only want to display a folder's
     * name, and not the full path of the folder
     */
    public String toString() {
	return getFolderInfo().getFolderName();
    }
    
    /**
     * As specified by interface net.suberic.pooka.UserProfileContainer
     */

    public UserProfile getDefaultProfile() {
	if (getFolderInfo() != null) 
	    return getFolderInfo().getDefaultProfile();
	else
	    return null;
    }
    public Action[] getActions() {
	if (getFolderInfo().getActions() != null)
	    return javax.swing.text.TextAction.augmentList(getFolderInfo().getActions(), defaultActions);
	else
	    return defaultActions;
    }

    public Action[] defaultActions;

    class OpenAction extends AbstractAction {

	OpenAction() {
	    super("file-open");
	}

	OpenAction(String nm) {
	    super(nm);
	}

	public void actionPerformed(ActionEvent e) {
	    SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
			((FolderPanel)getParentContainer()).getMainPanel().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		    }
		});

	    try {

		getFolderInfo().loadAllMessages();
		
		final int folderType = getFolderInfo().getType();

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
			    if ((folderType & Folder.HOLDS_MESSAGES) != 0) {
				if (getFolderInfo().getFolderDisplayUI() != null)
				    getFolderInfo().getFolderDisplayUI().openFolderDisplay();
				else {
				    getFolderInfo().setFolderDisplayUI(Pooka.getUIFactory().createFolderDisplayUI(getFolderInfo()));
				    getFolderInfo().getFolderDisplayUI().openFolderDisplay();
				}
				
			    }
			    if ((folderType & Folder.HOLDS_FOLDERS) != 0) {
				javax.swing.JTree folderTree = ((FolderPanel)getParentContainer()).getFolderTree();
				folderTree.expandPath(folderTree.getSelectionPath());
			    }
			}
		    });
	    }  catch (MessagingException me) {
		final MessagingException newMe = me;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
			    Pooka.getUIFactory().showError(Pooka.getProperty("error.Folder.openFailed", "Failed to open folder") + "\n", newMe);
			}
		    });
	    }
	    
	    SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
	    ((FolderPanel)getParentContainer()).getMainPanel().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		    }
		});
	}
	
    }
    
    class UnsubscribeAction extends AbstractAction {

	UnsubscribeAction() {
	    super("folder-unsubscribe");
	}

	public void actionPerformed(ActionEvent e) {
	    unsubscribeFolder();
	}

    }

    class CloseAction extends AbstractAction {

	CloseAction() {
	    super("folder-close");
	}

	public void actionPerformed(ActionEvent e) {
	    try {
		getFolderInfo().closeFolder(false);
	    } catch (Exception ex) {
		System.out.println("caught exception:  " + ex.getMessage());
	    }
	}
	
    }
}

