package net.suberic.pooka.gui;

import net.suberic.pooka.Pooka;

import javax.swing.*;
import javax.mail.*;
import java.util.*;
import java.awt.*;
import javax.swing.tree.*;
import java.awt.event.*;

public class FolderChooser {

    protected JInternalFrame frame;
    protected Store store;
    protected String storeID;
    protected JTree tree;
    protected DefaultTreeModel treeModel;
    protected DefaultTreeModel subscribedTree;
    protected Vector unsubscribed = new Vector();
    protected Vector subscribed = new Vector();

    /**
     * Creates a new FolderChooser for the selected Store.
     */
    public FolderChooser(Store sourceStore, String sourceStoreID) {
	store=sourceStore;
	storeID = sourceStoreID;

	frame = new JInternalFrame(Pooka.getProperty("FolderChooser.title", "Choose Folder"), true, true, true, false);
	frame.setSize(500,300);
	treeModel = new DefaultTreeModel(createTreeRoot());
	subscribeNodes(treeModel);
	tree = new JTree(treeModel);
	tree.setCellRenderer(new FolderChooserTreeCellRenderer());

	tree.addMouseListener(new MouseAdapter() {
		public void mouseClicked(MouseEvent e) {
		    if (e.getClickCount() == 2) {
			MailTreeNode tmpNode = getSelectedNode();
			if (tmpNode != null && tmpNode instanceof ChooserFolderNode)
			    toggleSubscribed((ChooserFolderNode)tmpNode);
			tree.repaint();
		    } 
		    
		}

	    });


	JScrollPane jsp = new JScrollPane();
	jsp.getViewport().add(tree);

	frame.getContentPane().setLayout(new BorderLayout());
	frame.getContentPane().add("Center", jsp);
	frame.getContentPane().add("South", createButtonBar());
    }


    /**
     * Gets the currently selected node.
     */

    public MailTreeNode  getSelectedNode() {
	TreePath tp = tree.getSelectionPath();

	if (tp != null) {
	    return (MailTreeNode)tp.getLastPathComponent();
	} else {
	    return null;
	}
    }

    /**
     * creates the tree root.
     */
    private MailTreeNode createTreeRoot() {
	ChooserStoreNode csn = new ChooserStoreNode(store, storeID, frame);
	return csn;
    }

    /**
     * This adds the Folder represented by node to the list of subscribed
     * folders.
     *
     * Note that changes are not made until saveSubscribedList() is called.
     */
    public void subscribeFolder(ChooserFolderNode node) {
	if (unsubscribed.contains(node))
	    unsubscribed.removeElement(node);
	else
	    subscribed.add(node);
	node.setSubscribed(true);
    }

    /**
     * This removes the Folder represented by node from the list of 
     * subscribed folders.
     *
     * Note that changes are not made until saveSubscribedList() is called.
     */
    public void unsubscribeFolder(ChooserFolderNode node) {
	if (subscribed.contains(node))
	    subscribed.removeElement(node);
	else
	    unsubscribed.add(node);
	node.setSubscribed(false);
    }

    /**
     * This toggles the Subscribed field for the given ChooserFolderNode.
     */

    public void toggleSubscribed(ChooserFolderNode node) {
	if (node.isSubscribed())
	    unsubscribeFolder(node);
	else
	    subscribeFolder(node);
    }

    /**
     * This saves the subscribed list described in the FolderChooser to
     * the main Pooka configuration.  Called by the 'Ok' button.
     */
    public void saveSubscribedList() {
	unsubscribeOldFolders();
	subscribeNewFolders();
    }
    
    /**
     * This unsubscribes to the old folders.
     */

    private void unsubscribeOldFolders() {
	for (int i = 0; i < unsubscribed.size(); i++) {
	    unsubscribeNode((ChooserFolderNode)unsubscribed.elementAt(i));
	}
    }

    /**
     * This unsubscribes to the node and, if the parent is now empty,
     * unsubscribes it and on up the tree, also.
     */
    
    private void unsubscribeNode(ChooserFolderNode node) {
	TreeNode parent = node.getParent();
	if (parent instanceof ChooserFolderNode) {
	    ChooserFolderNode parentNode = (ChooserFolderNode)parent;
	    Vector parentList = Pooka.getResources().getPropertyAsVector(parentNode.getFolderProperty() + ".folderList", "");
	    Vector keptFolders = new Vector();

	    for (int i = 0; i < parentList.size(); i++) {
		String current = (String)parentList.elementAt(i);
		if (!(current.equals(node.getFolderName())))
		    keptFolders.add(current);
	    }

	    if (keptFolders.size() == 0)
		unsubscribeNode(parentNode);
	    else {
		String newParentList = new String();
		for (int j = 0; j < keptFolders.size(); j++) 
		    newParentList = newParentList.concat(((String)keptFolders.elementAt(j)) + ":");
		
		if (newParentList.endsWith(":"))
		    newParentList = newParentList.substring(0, newParentList.length() - 1);

		Pooka.setProperty(parentNode.getFolderProperty() + ".folderList", newParentList);
	    }
	}

	Pooka.getResources().removeProperty(node.getFolderProperty());
	Pooka.getResources().removeProperty(node.getFolderProperty() + ".folderList");
	
    }
	
    /**
     * Subscribes to all the new folders.
     */
    private void subscribeNewFolders() {
	for (int i = 0; i < subscribed.size(); i++) {
	    subscribeNode((ChooserFolderNode)subscribed.elementAt(i));
	}
    }

    /**
     * Subscribes to the provided ChooserFolderNode.  If the parent also
     * isn't subscribed, calls itself recursively to subscribe to it.
     */

    private void subscribeNode(ChooserFolderNode node) {
	TreeNode parent = node.getParent();
	if (parent instanceof ChooserFolderNode) {
	    ChooserFolderNode parentNode = (ChooserFolderNode)parent;
	    String parentList = Pooka.getProperty(parentNode.getFolderProperty() + ".folderList", "");
	    
	    if (parentList.equals("")) {
		subscribeNode(parentNode);
		Pooka.setProperty(parentNode.getFolderProperty() + ".folderList", node.getFolderName());
	    } else {
		Pooka.setProperty(parentNode.getFolderProperty() + ".folderList", parentList + ":" + node.getFolderName());
	    }
	    
	} else if (parent instanceof ChooserStoreNode) {
	    ChooserStoreNode parentNode = (ChooserStoreNode)parent;
	    String parentList = Pooka.getProperty(parentNode.getStoreProperty() + ".folderList", "");
	    if (parentList.equals("")) {
		Pooka.setProperty(parentNode.getStoreProperty() + ".folderList", node.getFolderName());
	    } else {
		Pooka.setProperty(parentNode.getStoreProperty() + ".folderList", parentList + ":" + node.getFolderName());
	    }
	}
    }


    /**
     * This takes a TreeModel which represents the entire Folder 
     * hierarchy and sets the Subscribed flag on each subscribed
     * Folder.
     */

    public void subscribeNodes(TreeModel tm) {
	ChooserStoreNode csn = (ChooserStoreNode) tm.getRoot();
	parseNodes(csn, "Store." + storeID);
    }

    /**
     * This parses the tree and makes sure that we subscribe to each
     * node below it.
     */
    public void parseNodes(TreeNode parent, String key) {
	String subscribedSubFolderList = Pooka.getProperty(key + ".folderList" , "");
	String subkey;
	if (! (subscribedSubFolderList.equals("") )) {
	    // we have to get these values, too.
	    StringTokenizer tokens = new StringTokenizer(subscribedSubFolderList, ":");
	    while (tokens.hasMoreTokens()) {
		String nextName = tokens.nextToken();
		ChooserFolderNode fn = findNode(parent, nextName);
		if (fn != null) {
		    fn.setSubscribed(true);
		    if (! (fn.isLeaf()))
			parseNodes(fn, key + "." + nextName);
		}
	    }
	    
	}
    }

    /**
     * This takes a Node and a String, and if there is a child of the
     * given node whose folder has the given name, it returns that
     * Node.
     */
    private ChooserFolderNode findNode(TreeNode parent, String folderName) {
	Enumeration children = parent.children();
	ChooserFolderNode currentFolder;
	while (children.hasMoreElements()) {
	    currentFolder = (ChooserFolderNode) children.nextElement();
	    if (folderName.equals(currentFolder.getFolder().getName()))
		return currentFolder;
	}

	return null;
    }

    /**
     * This creates the buttonbar.  Sigh.
     */

    private Box createButtonBar() {
	
	Box buttonBox = new Box(BoxLayout.X_AXIS);
	
	buttonBox.add(createButton("Ok", new AbstractAction() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
		    saveSubscribedList();
		    try {
			frame.setClosed(true);
		    } catch (java.beans.PropertyVetoException pve) {
		    }
		}
	    }, true));

	buttonBox.add(createButton("Cancel", new AbstractAction() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
		    try {
			frame.setClosed(true);
		    } catch (java.beans.PropertyVetoException pve) {
		    }
		}
	    }, true));

	return buttonBox;
    }

    private JButton createButton(String label, Action e, boolean isDefault) {
	JButton thisButton;
	
        thisButton = new JButton(Pooka.getProperty("label." + label, label));
	try {
	    thisButton.setMnemonic(Pooka.getProperty("label." + label + ".mnemonic").charAt(0));
	} catch (java.util.MissingResourceException mre) {
	}
	
	thisButton.setSelected(isDefault);
	
	thisButton.addActionListener(e);
	
	return thisButton;
    }

    /**
     * Shows the FolderChooser.  This depends on the implementation.
     */
    public void show() {
	frame.show();
    }

    public Container getFrame() {
	return frame;
    }

}



