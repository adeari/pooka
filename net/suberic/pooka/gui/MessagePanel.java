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

public class MessagePanel extends JDesktopPane implements UserProfileContainer {
    class ExtendedDesktopManager extends DefaultDesktopManager {
	/* ExtendedDesktopManager is just a Desktop Manager which also
	 * calls refreshActiveMenus() and refreshCurrentUser()  when the 
	 * focus is changed.  It also selects the last window selected when
	 * the currently selected window closes.
	 */
	ExtendedDesktopManager() {
	    super();
	}

	public void activateFrame(JInternalFrame f) {
	    super.activateFrame(f);
	    
	    f.requestFocus();
	    mainPanel.refreshActiveMenus(mainPanel.getMainMenu());
	    mainPanel.refreshCurrentUser();
	}
	
	public void closeFrame(JInternalFrame f) {
	    super.closeFrame(f);
	    JInternalFrame allFrames[] = getAllFrames();
	    if (allFrames.length > 0 && allFrames[0] != null)
		try {
		    allFrames[0].setSelected(true);
		} catch (java.beans.PropertyVetoException pve) {
		}
	    mainPanel.refreshActiveMenus(mainPanel.getMainMenu());
	    mainPanel.refreshCurrentUser();
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
	FolderWindow newFolderWindow;
	newFolderWindow = f.getFolderWindow();
	if (newFolderWindow == null) {
	    newFolderWindow = new FolderWindow(f, this);
	    f.setFolderWindow(newFolderWindow);
	    newFolderWindow.pack();
	    this.add(newFolderWindow);
	    newFolderWindow.setVisible(true);
	} else {
	    if (newFolderWindow.isIcon())
		try {
		    newFolderWindow.setIcon(false);
		} catch (java.beans.PropertyVetoException e) {
		} 
	}

	try {
	    newFolderWindow.setSelected(true);
	} catch (java.beans.PropertyVetoException e) {
	} 

	getMainPanel().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    /**
     * This opens up the MessageWindow for MessageProxy m and then sets
     * it to being the selected window.
     *
     * If no MessageWindow exists for the MessageProxy, a new MessageWindow
     * for it is created.  If one does exist, then that window is
     * de-iconified (if necessary) and selected.
     */
    public void openMessageWindow(MessageProxy m) {
	
	MessageWindow newMessageWindow;
	newMessageWindow = m.getMessageWindow();
	if (newMessageWindow == null) {
	    newMessageWindow = new ReadMessageWindow(this, m);
	    m.setMessageWindow(newMessageWindow);
	    this.add(newMessageWindow);
	    newMessageWindow.setVisible(true);
	} else {
	    if (newMessageWindow.isIcon())
		try {
		    newMessageWindow.setIcon(false);
		} catch (java.beans.PropertyVetoException e) {
		} 
	}

	try {
	    newMessageWindow.setSelected(true);
	} catch (java.beans.PropertyVetoException e) {
	}
    }

    public void createNewMessage() {
	MimeMessage m = new MimeMessage(getMainPanel().getSession());

	MessageWindow newMessageWindow = new NewMessageWindow(this, new NewMessageProxy(m));
	this.add(newMessageWindow);
	newMessageWindow.setVisible(true);
	try {
	    newMessageWindow.setSelected(true);
	} catch (java.beans.PropertyVetoException e) {
	}
    }

    public void createNewMessage(javax.mail.Message m) {
	MessageWindow newMessageWindow = new NewMessageWindow(this, new NewMessageProxy(m));
	this.add(newMessageWindow);
	newMessageWindow.setVisible(true);
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

    /**
     * As defined in net.suberic.pooka.UserProfileContainer.
     */

    public UserProfile getDefaultProfile() {
	JInternalFrame cw = getCurrentWindow();
	if (cw != null && cw instanceof UserProfileContainer)
	    return ((UserProfileContainer)cw).getDefaultProfile();
	else
	    return null;
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






