package net.suberic.pooka.gui;

import javax.swing.*;
import java.util.*;
import net.suberic.pooka.Pooka;
import net.suberic.pooka.FolderInfo;
import net.suberic.util.*;

public class FolderMenu extends net.suberic.util.gui.ConfigurableMenu {

    Vector folderList;
    FolderPanel fPanel;

    /**
     * This creates a new FolderMenu.
     */
    public FolderMenu() {
    }

    public void configureComponent(String key, VariableBundle vars) {
	this.setActionCommand(vars.getProperty(key + ".Action", "message-move"));

	fPanel = Pooka.getMainPanel().getFolderPanel();

	MailTreeNode root =  (MailTreeNode)fPanel.getFolderTree().getModel().getRoot();
	folderList = getAllChildren(root);
	
	FolderNode currentFolder;

	for (int i = 0; i < folderList.size(); i++) {
	    currentFolder=(FolderNode)folderList.elementAt(i);
	    JMenuItem mi = new JMenuItem(currentFolder.getFolderID());
	    mi.setActionCommand(getActionCommand());
	    
	    this.add(mi);
	    }
    }
    
    /**
     * This recursively goes through the tree of MailTreeNodes and returns
     * only the leaf values.
     */
    public Vector getAllChildren(MailTreeNode mtn) {
	Vector current = new Vector();
	if (mtn.isLeaf()) {
	    current.addElement(mtn);
	} else {
	    Enumeration children = mtn.children();
	    while (children.hasMoreElements()) {
		current.addAll(getAllChildren((MailTreeNode)children.nextElement()));
	    }
	}
	return current;
    }

    public void setActiveMenuItems() {
	for (int j = 0; j < getItemCount(); j++) {
	    JMenuItem mi = getItem(j);
	    Action a = null;
	    // Action a = mp.getAction(getActionCommand());
	    if (a != null) {
		Action newAction = a;
		if (a instanceof net.suberic.util.DynamicAbstractAction) {
		    try {
			newAction = (Action)((net.suberic.util.DynamicAbstractAction)a).cloneDynamicAction();
		    } catch (CloneNotSupportedException cnse) {
			// sigh.  this is a really bad idea.  

			System.out.println("cnse hit.");
		    }
		}
		newAction.putValue("target", getTargetFolder(j));
		mi.addActionListener(newAction);
		mi.setEnabled(true);
	    } else {
		mi.setEnabled(false);
	    } 
	}
    }

    protected FolderInfo getTargetFolder(int folderNumber) {
	return (FolderInfo)((FolderNode)folderList.elementAt(folderNumber)).getFolderInfo();
    }
}


