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
 * The message panel.
 *
 */

public class MessagePanel extends JDesktopPane implements UserProfileContainer {
    /**
     * ExtendedDesktopManager is just a Desktop Manager which also
     * calls refreshActiveMenus() and refreshCurrentUser()  when the 
     * focus is changed.  It also selects the last window selected when
     * the currently selected window closes.
     *
     * Oh, and I guess now it also resizes the DesktopPane when you move
     * InternalFrames, so that you get scrollbars when you move a window off
     * the edge of the frame.
     */
    class ExtendedDesktopManager extends DefaultDesktopManager {

	ExtendedDesktopManager() {
	    super();
	}

	/**
	 * This refreshes the active menus and the current user to match
	 * the newly selected frame.
	 *
	 * Overrides DefaultDesktopManager.activateFrame(JInternalFrame f).
	 */
	public void activateFrame(JInternalFrame f) {
	    super.activateFrame(f);
	    
	    f.requestFocus();
	    mainPanel.refreshActiveMenus(mainPanel.getMainMenu());
	    mainPanel.refreshCurrentUser();
	}
	
	/**
	 * In addition to closing the current Frame, this also activates
	 * another InternalFrame and updates the active menus.
	 *
	 * Overrides DefaultDesktopManager.closeFrame(JInternalFrame f).
	 */
	public void closeFrame(JInternalFrame f) {
	    super.closeFrame(f);
	    JInternalFrame allFrames[] = getAllFrames();
	    if (allFrames.length > 0 && allFrames[0] != null)
		try {
		    allFrames[0].setSelected(true);
		} catch (java.beans.PropertyVetoException pve) {
		}

	    // is this necessary?
	    mainPanel.refreshActiveMenus(mainPanel.getMainMenu());
	    mainPanel.refreshCurrentUser();
	}

	/**
	 * This extends the default behaviour of this method by tracking
	 * the new location and size of the Frame, so that the Desktop size
	 * can be adjusted if the Frame goes outside of the visible area.
	 *
	 * Overrides DefaultDesktopManager.minimizeFrame(JInternalFrame f).
	 */
	public void minimizeFrame(JInternalFrame f) {
	    super.minimizeFrame(f);
	}

	/**
	 * This extends the default behaviour of this method by tracking
	 * the new location and size of the Frame, so that the Desktop size
	 * can be adjusted if the Frame goes outside of the visible area.
	 *
	 * Overrides DefaultDesktopManager.iconifyFrame(JInternalFrame f).
	 */
	public void iconifyFrame(JInternalFrame f) {
	    super.iconifyFrame(f);
	}

	/**
	 * This extends the default behaviour of this method by tracking
	 * the new location and size of the Frame, so that the Desktop size
	 * can be adjusted if the Frame goes outside of the visible area.
	 *
	 * Overrides DefaultDesktopManager.deiconifyFrame(JInternalFrame f).
	 */
	public void deiconifyFrame(JInternalFrame f) {
	    super.deiconifyFrame(f);
	}

	/**
	 * This extends the default behaviour of this method by tracking
	 * the new location and size of the Frame, so that the Desktop size
	 * can be adjusted if the Frame goes outside of the visible area.
	 *
	 * Overrides DefaultDesktopManager.endDraggingFrame(JComponent f).
	 */
	public void endDraggingFrame(JComponent f) {
	    super.endDraggingFrame(f);
	    updateDesktopSize();
	}

	/**
	 * This extends the default behaviour of this method by tracking
	 * the new location and size of the Frame, so that the Desktop size
	 * can be adjusted if the Frame goes outside of the visible area.
	 *
	 * Overrides DefaultDesktopManager.endResizingFrame(JComponent f).
	 */
	public void endResizingFrame(JComponent f) {
	    super.endResizingFrame(f);
	}

	public void updateDesktopSize() {
	    MessagePanel mp = getMainPanel().getMessagePanel();
	    if (mp.getSize().getWidth() < 1000)
		mp.setSize(1000, 1000);

	    JScrollPane scrollPane = getMainPanel().getMessageScrollPane();

	    JScrollBar hsb = scrollPane.getHorizontalScrollBar();
	    JScrollBar vsb = scrollPane.getVerticalScrollBar();

	    // calculate the min and max locations for all the frames.
	    JInternalFrame[] allFrames = mp.getAllFrames();
	    int min_x = 0, min_y = 0, max_x = 0, max_y = 0;
	    Rectangle bounds = null;
	    for (int i = 0; i < allFrames.length; i++) {
		bounds = allFrames[i].getBounds(bounds);
		min_x = Math.min(min_x, bounds.x);
		min_y = Math.min(min_y, bounds.y);
		max_x = Math.max(max_x, bounds.width + bounds.x);
		max_y = Math.max(max_y, bounds.height + bounds.y);
	    }
	    
	    // add to this the current viewable area.
	    bounds = scrollPane.getViewport().getBounds(bounds);
	    min_x = Math.min(min_x, bounds.x + hsb.getValue());
	    min_y = Math.min(min_y, bounds.y + vsb.getValue());
	    max_x = Math.max(max_x, bounds.width + bounds.x + hsb.getValue());
	    max_y = Math.max(max_y, bounds.height + bounds.y + vsb.getValue());

	    bounds = mp.getBounds(bounds);
	    int xdiff = 0;
	    int ydiff = 0;
	    if (min_x != bounds.x || min_y != bounds.y) {
		xdiff = bounds.x - min_x;
		ydiff = bounds.y - min_y;

		hsb = scrollPane.getHorizontalScrollBar();
		vsb = scrollPane.getVerticalScrollBar();

		min_x = min_x + xdiff;
		min_y = min_y + ydiff;
		max_x = max_x + xdiff;
		max_y = max_y + ydiff;
		for (int i = 0; i < allFrames.length; i++) {
		    allFrames[i].setLocation(allFrames[i].getX() + xdiff, allFrames[i].getY() + ydiff);
		}
		
	    }

	    hsb = scrollPane.getHorizontalScrollBar();
	    vsb = scrollPane.getVerticalScrollBar();

	    int hval = hsb.getValue();
	    int vval = vsb.getValue();

	    mp.setSize(max_x - min_x, max_y - min_y);

	    hsb = scrollPane.getHorizontalScrollBar();
	    vsb = scrollPane.getVerticalScrollBar();

	    if (hval + xdiff + hsb.getModel().getExtent() > hsb.getMaximum())
		hsb.setMaximum(hval + xdiff + hsb.getModel().getExtent());
	    if (vval + ydiff + vsb.getModel().getExtent() > vsb.getMaximum())
		vsb.setMaximum(vval + ydiff + vsb.getModel().getExtent());
	    
	    hsb.setValue(hval + xdiff);
	    vsb.setValue(vval + ydiff);
	}

	public void printstats(String message) {
	    System.out.println("\n" + message);
	    MessagePanel mp = getMainPanel().getMessagePanel();
	    JScrollPane scrollPane = getMainPanel().getMessageScrollPane();
	    JViewport viewport = scrollPane.getViewport();
	    JInternalFrame[] allFrames = mp.getAllFrames();
	    
	    System.out.println("getBounds() of MessagePanel is " + mp.getBounds());
	    System.out.println("getBounds() of JViewport is " + viewport.getBounds());
	    System.out.println("getViewRect() is JViewport is " + viewport.getViewRect());
	    System.out.println("HSB.getValue() is " + scrollPane.getHorizontalScrollBar().getValue());
	    System.out.println("VSB.getValue() is " + scrollPane.getVerticalScrollBar().getValue());
	    for (int i = 0; i < allFrames.length; i++)
		System.out.println("allFrames[i] = " + allFrames[i].getLocation());
	}
    }

    // end internal class ExtendedDesktopManager

    MainPanel mainPanel;

    /**
     * Creates a new MessagePanel to go in the given MainPanel.
     */
    public MessagePanel(MainPanel newMainPanel) {
	mainPanel=newMainPanel;
	this.setDesktopManager(new ExtendedDesktopManager());
	//this.setAutoscrolls(true);
	this.setSize(1000, 1000);
	
    }

    /**
     * This opens a new FolderWindow for the given FolderInfo.
     */
    public void openFolderWindow(FolderInfo f) {

	FolderWindow newFolderWindow;
	newFolderWindow = f.getFolderWindow();
	if (newFolderWindow == null) {
	    newFolderWindow = new FolderWindow(f, this);
	    f.setFolderWindow(newFolderWindow);
	    newFolderWindow.pack();
	    newFolderWindow.setVisible(false);
	    this.add(newFolderWindow);
	    try {
		newFolderWindow.setSelected(true);
	    } catch (java.beans.PropertyVetoException e) {
	    } 
	    
	    newFolderWindow.setVisible(true);
	} else {
	    if (newFolderWindow.isIcon())
		try {
		    newFolderWindow.setIcon(false);
		} catch (java.beans.PropertyVetoException e) {
		} 
	    try {
		newFolderWindow.setSelected(true);
	    } catch (java.beans.PropertyVetoException e) {
	    } 
	}

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






