package net.suberic.pooka.gui;
import javax.swing.JComponent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import java.awt.event.ActionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.mail.Store;
import javax.mail.Folder;
import javax.mail.MessagingException;
import java.util.*;
import net.suberic.pooka.Pooka;
import javax.mail.FolderNotFoundException;
import javax.swing.JOptionPane;
import net.suberic.pooka.FolderInfo;
import javax.mail.event.*;

public class FolderNode extends MailTreeNode implements MessageChangedListener {
    
    protected FolderInfo folderInfo = null;
    protected boolean hasLoaded = false;
    protected boolean listSubscribedOnly;

    /**
     * creates a tree node that points to a folder
     *
     * @param newFolder	the store for this node
     * @param newParent the parent component
     * @param subscribedOnly whether or not the children of this node are
     * just the subscribed folders, or all folders.
     */
    public FolderNode(FolderInfo newFolderInfo, JComponent newParent, boolean subscribedOnly) {
	super(newFolderInfo, newParent);
	listSubscribedOnly=subscribedOnly;
	folderInfo = newFolderInfo;

	folderInfo.setFolderNode(this);

	commands = new Hashtable();
	
	Action[] actions = defaultActions;

	if (actions != null) {
	    for (int i = 0; i < actions.length; i++) {
		Action a = actions[i];
		commands.put(a.getValue(Action.NAME), a);
	    }
	}

	if (listSubscribedOnly) {
	    folderInfo.addMessageCountListener(new MessageCountAdapter() {
		    public void messagesAdded(MessageCountEvent e) {
			getParentContainer().repaint();
		    }

		    public void messageRemoved(MessageCountEvent e) {
			getParentContainer().repaint();
		    }
		});
	    
	    folderInfo.addMessageChangedListener(this);
	}

    }

    
    /**
     * a Folder is a leaf if it cannot contain sub folders
     */
    public boolean isLeaf() {
	try {
	    if (getFolder() == null || (getFolder().getType() & Folder.HOLDS_FOLDERS) == 0) {
	    	return true;
	    }
	} catch (MessagingException me) { }
	
	// otherwise it does hold folders, and therefore not
	// a leaf
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

    public int getChildCount() {
	if (!hasLoaded) {
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
	if (!hasLoaded) {
	    loadChildren();
	}
	return super.children();
    }

    /**
     * This loads (or reloads) the children of the FolderNode from
     * the list of Children on the FolderInfo.
     */
    public void loadChildren() {
	// if it is a leaf, just say we have loaded them
	if (isLeaf()) {
	    hasLoaded = true;
	    return;
	}

	Enumeration origChildren = super.children();
	Vector origChildrenVector = new Vector();
	while (origChildren.hasMoreElements())
	    origChildrenVector.add(origChildren.nextElement());

	if (listSubscribedOnly) {
	    Vector folderChildren = getFolderInfo().getChildren();

	    if (folderChildren != null) {
		for (int i = 0; i < folderChildren.size(); i++) {
		    FolderNode node = popChild(((FolderInfo)folderChildren.elementAt(i)).getFolderName(), origChildrenVector);
		    if (node == null) {
			node = new FolderNode((FolderInfo)folderChildren.elementAt(i), getParentContainer(), true);
			// we used insert here, since add() would mak
			// another recursive call to getChildCount();
			insert(node, 0);
		    }
		}
		
	    }

	    removeChildren(origChildrenVector);

	    hasLoaded=true;

	} else {
	    // get the default folder, and list the
	    // subscribed folders on it
	    
	    Folder folder = getFolder();
	    
	    try {
		Folder[] folderList = folder.list();
		
		for (int i = 0 ; i < folderList.length ; i++) {
		    
		    FolderNode node = new FolderNode(new FolderInfo(getFolderInfo(), folderList[i].getName()), getParentContainer(), false);
		    // we used insert here, sincedd() would mak
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

	hasLoaded = true;

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
		if (((FolderNode)childrenList.elementAt(i)).getFolderInfo().getFolderName().equals(childName))
		    return (FolderNode)childrenList.elementAt(i);
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

    public void messageChanged(MessageChangedEvent mce) {
	getParentContainer().repaint();
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
    
    public Action[] getActions() {
	return defaultActions;
    }

    public Action[] defaultActions = {
	new OpenAction()
    };

    class OpenAction extends AbstractAction {

	OpenAction() {
	    super("folder-open");
	}

	OpenAction(String nm) {
	    super(nm);
	}

	public void actionPerformed(ActionEvent e) {
	    try {
		if ((getFolder().getType() & Folder.HOLDS_MESSAGES) != 0)
		    ((FolderPanel)getParentContainer()).getMainPanel().getMessagePanel().openFolderWindow(folderInfo);
		if ((getFolder().getType() & Folder.HOLDS_FOLDERS) != 0) {
		    javax.swing.JTree folderTree = ((FolderPanel)getParentContainer()).getFolderTree();
		    folderTree.expandPath(folderTree.getSelectionPath());
		}
	    } catch (MessagingException me) {
	    }
	}
    }
}

