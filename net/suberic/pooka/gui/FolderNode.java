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
import net.suberic.pooka.*;
import javax.mail.FolderNotFoundException;
import javax.swing.JOptionPane;
import net.suberic.pooka.FolderInfo;
import javax.mail.event.*;

public class FolderNode extends MailTreeNode implements MessageChangedListener, UserProfileContainer {
    
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
	
	Action[] actions = defaultActions;

	if (actions != null) {
	    for (int i = 0; i < actions.length; i++) {
		Action a = actions[i];
		commands.put(a.getValue(Action.NAME), a);
	    }
	}

	folderInfo.addMessageCountListener(new MessageCountAdapter() {
	    public void messagesAdded(MessageCountEvent e) {
		getParentContainer().repaint();
	    }
	    
	    public void messagesRemoved(MessageCountEvent e) {
		getParentContainer().repaint();
	    }
	});
	
	folderInfo.addMessageChangedListener(this);
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
		if (!getFolderInfo().isOpen())
		    getFolderInfo().openFolder(Folder.READ_WRITE);

		if (getFolderInfo().isOpen()) {
		    if ((getFolder().getType() & Folder.HOLDS_MESSAGES) != 0)
			((FolderPanel)getParentContainer()).getMainPanel().getMessagePanel().openFolderWindow(folderInfo);
		    if ((getFolder().getType() & Folder.HOLDS_FOLDERS) != 0) {
			javax.swing.JTree folderTree = ((FolderPanel)getParentContainer()).getFolderTree();
			folderTree.expandPath(folderTree.getSelectionPath());
		    }
		}
	    } catch (MessagingException me) {
	    }
	}
    }
}

