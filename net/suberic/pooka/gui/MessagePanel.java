package net.suberic.pooka.gui;
import net.suberic.pooka.*;
import net.suberic.util.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.mail.internet.MimeMessage;
import javax.mail.Session;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.border.*;

/**
 * The message panel
 *
 */

public class MessagePanel extends JDesktopPane {
    class ExtendedDesktopManager extends DefaultDesktopManager {
	/* ExtendedDesktopManager is just a Desktop Manager which also
	   calls refreshActiveMenus() when the focus is changed. 
	*/
	ExtendedDesktopManager() {
	    super();
	}

	public void activateFrame(JInternalFrame f) {
	    super.activateFrame(f);
	    
	    mainPanel.refreshActiveMenus(mainPanel.getMainMenu());
	}
	
	public void closeFrame(JInternalFrame f) {
	    super.closeFrame(f);
	    mainPanel.refreshActiveMenus(mainPanel.getMainMenu());
	}
    }

    MainPanel mainPanel;

    public MessagePanel(MainPanel newMainPanel) {
	mainPanel=newMainPanel;
	this.setDesktopManager(new ExtendedDesktopManager());
    }

    /**
     * This opens a new FolderWindow for the given FolderInfo.
     */
    public void openFolderWindow(FolderInfo f) {

	getMainPanel().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	FolderWindow newFolderWindow = new FolderWindow(f, this);
	newFolderWindow.pack();
	this.add(newFolderWindow);
	try {
	    newFolderWindow.setSelected(true);
	} catch (java.beans.PropertyVetoException e) {
	} 

	getMainPanel().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    public void openMessageWindow(MessageProxy m) {
	MessageWindow newMessageWindow = new MessageWindow(m, false);
	newMessageWindow.pack();
	this.add(newMessageWindow);
	try {
	    newMessageWindow.setSelected(true);
	} catch (java.beans.PropertyVetoException e) {
	}
    }

    public void createNewMessage() {
	MimeMessage m = new MimeMessage(getMainPanel().getSession());

	MessageWindow newMessageWindow = new MessageWindow(new NewMessageProxy(m), true);
	//newMessageWindow.pack();
	this.add(newMessageWindow);
	try {
	    newMessageWindow.setSelected(true);
	} catch (java.beans.PropertyVetoException e) {
	}
    }

    public void createNewMessage(javax.mail.Message m) {
	MessageWindow newMessageWindow = new MessageWindow(new NewMessageProxy(m), true);
	//newMessageWindow.pack();
	this.add(newMessageWindow);
	try {
	    newMessageWindow.setSelected(true);
	} catch (java.beans.PropertyVetoException e) {
	}
    }

    public JInternalFrame getCurrentWindow() {
	JInternalFrame[] allFrames = getAllFrames();
	
	for(int i = 0; i < allFrames.length; i++) {
	    if (allFrames[i].isSelected())
		return allFrames[i];
	}
	
	return null;
    }

    public MainPanel getMainPanel() {
	return mainPanel;
    }

    public Action[] defaultActions = {
	new newMessageAction()
    };

    public Action[] getDefaultActions() {
	return defaultActions;
    }
    
    public Action[] getActions() {
	JInternalFrame cw = getCurrentWindow();
	if (cw != null) {
	    if (cw instanceof FolderWindow) {
		return TextAction.augmentList(((FolderWindow)cw).getActions(), getDefaultActions());
	    } else if (cw instanceof MessageWindow) {
		return TextAction.augmentList(((MessageWindow)cw).getActions(), getDefaultActions());
	    }
	}
	return getDefaultActions();
    }

    public class newMessageAction extends AbstractAction {
	newMessageAction() {
	    super("message-new");
	}

	public void actionPerformed(ActionEvent e) {
	    createNewMessage();
	}

    }
}






