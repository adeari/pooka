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

	tree.addMouseListener(new MouseAdapter() {
		public void mouseClicked(MouseEvent e) {
		    if (e.getClickCount() == 2) {
			MailTreeNode tmpNode = getSelectedNode();
			if (tmpNode != null && tmpNode instanceof FolderNode)
			    toggleSubscribed((FolderNode)tmpNode);
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
     * This toggles the Subscribed field for the given FolderNode.
     */

    public void toggleSubscribed(FolderNode node) {
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
	Properties p = new Properties();
	FolderNode node;

	Enumeration children = ((MailTreeNode)treeModel.getRoot()).children();

	Vector subscribed = new Vector();

	String prefix = "Store." + storeID;

	while (children.hasMoreElements()) {
	    node = (FolderNode)children.nextElement();
	    if (node.isLeaf()) {
		if (node.isSubscribed())
		    subscribed.add(node.getFolder().getName());
	    } else {
		if (readSubscribedTree(node, prefix + "." + node.getFolder().getName(), p))
		    subscribed.add(node.getFolder().getName());
	    }
	}    

	if (!subscribed.isEmpty()) {
	    String propValue = new String();
	    
	    for (int i = 0; i < subscribed.size(); i++) {
		propValue = propValue.concat((String)subscribed.elementAt(i) + ":");
	    }
	    

	    if (propValue.endsWith(":"))
		propValue=propValue.substring(0, propValue.length() - 1);

	    p.setProperty(prefix + ".folderList", propValue);
	}

	// at this point, i should really go through and make sure to
	// remove all the old properties.  i'll do that later.

	Enumeration propsSet = p.propertyNames();
	String propName;
	while (propsSet.hasMoreElements()) {
	    propName = (String)propsSet.nextElement();
	    Pooka.setProperty(propName, p.getProperty(propName));
	}
	    
    }

    /**
     * This method take a FolderNode which has children and then sets the
     * appropriate properties.  If the FolderNode is not a leaf node and
     * has 
     */
    private boolean readSubscribedTree(FolderNode parentNode, String prefix, Properties properties) {

	FolderNode node;

	Enumeration children = parentNode.children();

	Vector subscribed = new Vector();

	while (children.hasMoreElements()) {
	    node = (FolderNode)children.nextElement();
	    if (node.isLeaf()) {
		if (node.isSubscribed())
		    subscribed.add(node.getFolder().getName());
	    } else {
		if (readSubscribedTree(node, prefix + "." + node.getFolder().getName(), properties))
		    subscribed.add(node.getFolder().getName());
	    }
	}    
	
	if (!subscribed.isEmpty()) {
	    String propValue = new String();
	    
	    for (int i = 0; i < subscribed.size(); i++) {
		propValue = propValue.concat((String)subscribed.elementAt(i) + ":");
	    }
	    

	    if (propValue.endsWith(":"))
		propValue=propValue.substring(0, propValue.length() - 1);
	    
	    properties.setProperty(prefix + ".folderList", propValue);

	    return true;
	} else
	    return false;
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
