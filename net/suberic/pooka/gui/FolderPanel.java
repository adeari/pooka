package net.suberic.pooka.gui;
import net.suberic.pooka.*;
import net.suberic.util.ValueChangeListener;
import java.awt.*;
import java.awt.event.*;
import javax.mail.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.tree.*;
import net.suberic.pooka.gui.*;
import java.util.StringTokenizer;
import java.util.Vector;

public class FolderPanel extends JScrollPane implements ValueChangeListener, UserProfileContainer {
    MainPanel mainPanel=null;
    JTree folderTree;
    DefaultTreeModel folderModel;
    Session session;
    FolderInfo trashFolder = null;
    
    public FolderPanel(MainPanel newMainPanel) {
	mainPanel=newMainPanel;

	setPreferredSize(new Dimension(Integer.parseInt(Pooka.getProperty("Pooka.folderPanel.hsize", "200")), Integer.parseInt(Pooka.getProperty("Pooka.folderPanel.vsize", Pooka.getProperty("Pooka.vsize","570")))));

	folderModel = new DefaultTreeModel(createTreeRoot());
	folderTree = new JTree(folderModel);

	this.getViewport().add(folderTree);

	folderTree.addMouseListener(new MouseAdapter() {
		
	    public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
		    MailTreeNode tmpNode = getSelectedNode();
		    if (tmpNode != null) {
			String actionCommand = Pooka.getProperty("FolderPanel.2xClickAction", "folder-open");
			Action clickAction = getSelectedNode().getAction(actionCommand);
			if (clickAction != null ) {
			    clickAction.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, actionCommand));
			} 

		    }
		}
	    }

		public void mousePressed(MouseEvent e) {
		    if (SwingUtilities.isRightMouseButton(e)) {
			// see if anything is selected
			TreePath path = folderTree.getClosestPathForLocation(e.getX(), e.getY());
			if (folderTree.getPathBounds(path).contains(e.getX(), e.getY())) {
			    // this means that we're clicking on a node.  make
			    // sure that it's selected.

			    if (!folderTree.isPathSelected(path))
				folderTree.setSelectionPath(path);
			}

			MailTreeNode tmpNode = getSelectedNode();
			if (tmpNode != null) {
			    tmpNode.showPopupMenu(FolderPanel.this, e);
			    
			}
		    }
		}
	    });
	folderTree.addTreeSelectionListener(getMainPanel());
	folderTree.setCellRenderer(new EnhancedFolderTreeCellRenderer());
    }

    public MailTreeNode getSelectedNode() {
	TreePath tp = folderTree.getSelectionPath();

	if (tp != null) {
	    return (MailTreeNode)tp.getLastPathComponent();
	} else {
	    return null;
	}
    }

    private MailTreeNode createTreeRoot() {
	MailTreeNode root = new MailTreeNode("Pooka", this);

	// Get the stores we have listed.
	String storeID = null;

	StringTokenizer tokens =  new StringTokenizer(Pooka.getProperty("Store", ""), ":");

	while (tokens.hasMoreTokens()) {
	    storeID=(String)tokens.nextElement();

	    addStore(storeID, root);	    
	}
	
	return root;
    }

    /**
     * refreshStores() goes through the list of registered stores and 
     * compares these to the value of the "Store" property.  If any
     * stores are no longer listed in that property, they are removed
     * from the FolderPanel.  If any new stores are found, they are
     * added to the FolderPanel.
     *
     * This function does not add new subfolders to already existing 
     * Stores.  Use refreshStore(Store) for that.
     *
     * This function is usually called in response to a ValueChanged
     * action on the "Store" property.
     *
     */

    public void refreshStores() {
	String storeID = null;
	MailTreeNode root = (MailTreeNode)getFolderTree().getModel().getRoot();

	StringTokenizer tokens =  new StringTokenizer(Pooka.getProperty("Store", ""), ":");

	Vector allStores = new Vector();
	java.util.Enumeration storeEnum = root.children();
	while (storeEnum.hasMoreElements()) {
	    allStores.add(storeEnum.nextElement());
	}

	while (tokens.hasMoreTokens()) {
	    boolean found = false;
	    storeID=(String)tokens.nextElement();
	    for (int i=0; !(found) && i < allStores.size(); i++) {
		StoreNode currentStore = (StoreNode)allStores.elementAt(i);
		if (currentStore.getStoreID().equals(storeID)) {
		    found = true;
		    allStores.removeElement(currentStore);
		}
	    }
	    if (!(found) )
		this.addStore(storeID, root);
	}

	for (int i = 0; i < allStores.size() ; i++) {
	    this.removeStore(((StoreNode)allStores.elementAt(i)).getStoreID(), root);
	}

	getFolderTree().updateUI();
    }

    public void addStore(String storeID, MailTreeNode root) {
	StoreInfo store = new StoreInfo(storeID);
	StoreNode storenode = new StoreNode(store, this);
	root.add(storenode);
    }

    public void removeStore(String storeID, MailTreeNode root) {
	java.util.Enumeration children = root.children();
	boolean removed=false;

	while (children.hasMoreElements() && (removed == false)) {
	    StoreNode sn = (StoreNode)(children.nextElement());

	    if (sn.getStoreID().equals(storeID)) {
		root.remove(sn);
		removed = true;
	    }
	}
    }

    public MainPanel getMainPanel() {
	return mainPanel;
    }

    public JTree getFolderTree() {
	return folderTree;
    }

    /**
     * Specified by interface net.suberic.util.ValueChangeListener
     *
     */
    
    public void valueChanged(String changedValue) {
	if (changedValue.equals("Store"))
	    refreshStores();
    }

    /**
     * Specified by interface net.suberic.pooka.UserProfileContainer
     */
   
    public UserProfile getDefaultProfile() {
	MailTreeNode selectedNode = getSelectedNode();

	if (selectedNode != null && selectedNode instanceof UserProfileContainer) 
	    return ((UserProfileContainer)selectedNode).getDefaultProfile();
	else
	    return null;
    }

    public FolderInfo getTrashFolder() {
	return trashFolder;
    }

    public void setTrashFolder(FolderInfo f) {
	trashFolder = f;
    }

    public Action[] getActions() {
	if (getSelectedNode() != null) {
	    return (getSelectedNode().getActions());
	} else {
	    return null;
	}
    }
}





