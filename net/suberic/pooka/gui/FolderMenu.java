package net.suberic.pooka.gui;

import javax.swing.*;
import java.util.*;
import javax.mail.Folder;
import net.suberic.pooka.Pooka;

public class FolderMenu extends net.suberic.util.DynamicMenu {

    Vector folderList;
    FolderPanel fPanel;
    
    public FolderMenu(String key, FolderPanel newFPanel) {
	super(Pooka.getProperty(key + ".Label", key));

	setActionCommand(Pooka.getProperty(key + ".Action", key));

	fPanel = newFPanel;
	createMenus();
    }

    public void createMenus() {
	/*	folderList = fPanel.getFolderList();
	
	Folder currentFolder;
	
	for (int i = 0; i < folderList.size(); i++) {
	    currentFolder=(Folder)folderList.elementAt(i);
	    JMenuItem mi = new JMenuItem(currentFolder.getName());
	    mi.setActionCommand(getActionCommand());
	    
	    this.add(mi);
	    }*/
    }
    
    public void setActiveMenus(JComponent mainPanel) {
	MainPanel mp = (MainPanel)mainPanel;

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

    protected Folder getTargetFolder(int folderNumber) {
	return (Folder)folderList.elementAt(folderNumber);
    }
}


