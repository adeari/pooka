package net.suberic.pooka.gui;

import net.suberic.pooka.Pooka;

import javax.swing.*;
import javax.mail.*;
import java.util.*;
import java.awt.*;
import javax.swing.tree.*;

public class FolderChooser {

    protected JInternalFrame frame;
    protected Store store;
    protected String storeID;
    protected JTree tree;
    protected DefaultTreeModel treeModel;
    protected DefaultTreeModel subscribedTree;

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
	tree.setCellRenderer(new DefaultFolderTreeCellRenderer(DefaultFolderTreeCellRenderer.SUBSCRIBED_FOLDER));

	JScrollPane jsp = new JScrollPane();
	jsp.getViewport().add(tree);

	frame.getContentPane().add(jsp);

    }


    /**
     * creates the tree root.
     */
    private MailTreeNode createTreeRoot() {
	StoreNode sn = new StoreNode(store, storeID, frame, false);
	return sn;
    }

    /**
     * This adds the Folder represented by node to the list of subscribed
     * folders.
     *
     * Note that changes are not made until saveSubscribedList() is called.
     */
    public void subscribeFolder(FolderNode node) {
	node.setSubscribed(true);
	
    }

    /**
     * This removes the Folder represented by node from the lsit of 
     * subscribed folders.
     *
     * Note that changes are not made until saveSubscribedList() is called.
     */
    public void unsubscribeFolder(FolderNode node) {
	node.setSubscribed(false);
    }

    /**
     * This saves the subscribed list described in the FolderChooser to
     * the main Pooka configuration.  Called by the 'Ok' button.
     */
    public void saveSubscribedList() {
	
    }

    /**
     * This takes a TreeModel which represents the entire Folder 
     * hierarchy and sets the Subscribed flag on each subscribed
     * Folder.
     */

    public void subscribeNodes(TreeModel tm) {
	StoreNode sn = (StoreNode) tm.getRoot();
	parseNodes(sn, "Store." + storeID);
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
		FolderNode fn = findNode(parent, nextName);
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
    private FolderNode findNode(TreeNode parent, String folderName) {
	Enumeration children = parent.children();
	FolderNode currentFolder;
	while (children.hasMoreElements()) {
	    currentFolder = (FolderNode) children.nextElement();
	    if (folderName.equals(currentFolder.getFolder().getName()))
		return currentFolder;
	}

	return null;
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
