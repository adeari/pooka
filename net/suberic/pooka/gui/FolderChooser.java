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
    protected JTree tree;
    protected DefaultTreeModel treeModel;

    /**
     * Creates a new FolderChooser for the selected Store.
     */
    public FolderChooser(Store sourceStore) {
	store=sourceStore;

	frame = new JInternalFrame(Pooka.getProperty("FolderChooser.title", "Choose Folder"), true, true, true, false);
	frame.setSize(500,300);
	treeModel = new DefaultTreeModel(createTreeRoot());
	tree = new JTree(treeModel);

	JScrollPane jsp = new JScrollPane();
	jsp.getViewport().add(tree);

	frame.getContentPane().add(jsp);

    }


    /**
     * creates the tree root.
     */
    private MailTreeNode createTreeRoot() {
	StoreNode sn = new StoreNode(store, "test", frame, false);
	return sn;
    }

    /**
     * This adds the Folder represented by node to the list of subscribed
     * folders.
     *
     * Note that changes are not made until saveSubscribedList() is called.
     */
    public void subscribeFolder(FolderNode node) {
    }

    /**
     * This removes the Folder represented by node from the lsit of 
     * subscribed folders.
     *
     * Note that changes are not made until saveSubscribedList() is called.
     */
    public void unsubscribeFolder(FolderNode node) {
    }

    /**
     * This saves the subscribed list described in the FolderChooser to
     * the main Pooka configuration.  Called by the 'Ok' button.
     */
    public void saveSubscribedList() {
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
