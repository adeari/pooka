package net.suberic.pooka.gui;
import net.suberic.pooka.*;
import net.suberic.util.*;
import net.suberic.util.swing.RunnableAdapter;
import net.suberic.util.gui.ConfigurableKeyBinding;
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
     */
    class ExtendedDesktopManager extends net.suberic.util.swing.ScrollingDesktopManager {

	public ExtendedDesktopManager(JDesktopPane pane, JScrollPane scrollPane) {
	    super(pane, scrollPane);
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
	    mainPanel.refreshActiveMenus();
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
	    mainPanel.refreshActiveMenus();
	    mainPanel.refreshCurrentUser();
	}

    }

    // end internal class ExtendedDesktopManager

    MainPanel mainPanel;
    ConfigurableKeyBinding keyBindings;
    boolean savingWindowLocations = false;
    boolean savingOpenFolders = false;

    /**
     * Creates a new MessagePanel to go in the given MainPanel.
     */
    public MessagePanel(MainPanel newMainPanel) {
	mainPanel=newMainPanel;
	//this.setAutoscrolls(true);
	this.setSize(1000, 1000);
	
	keyBindings = new ConfigurableKeyBinding(this, "MessagePanel.keyBindings", Pooka.getResources());
	keyBindings.setCondition(JComponent.WHEN_IN_FOCUSED_WINDOW);
	keyBindings.setActive(getActions());

	// if the MessagePanel itself ever gets focus, pass it on to the
	// selected JInternalFrame.

	this.addFocusListener(new FocusAdapter() {
		public void focusGained(FocusEvent e) {
		    JInternalFrame selectedFrame = getCurrentWindow();
		    if (selectedFrame != null)
			selectedFrame.requestFocus();
		}
	    });

	this.setSavingWindowLocations(Pooka.getProperty("Pooka.saveFolderWindowLocationsOnExit", "false").equalsIgnoreCase("true"));
	this.setSavingOpenFolders(Pooka.getProperty("Pooka.saveOpenFoldersOnExit", "false").equalsIgnoreCase("true"));
    }

    /**
     * This opens a new FolderWindow for the given FolderInfo, and sets
     * it as the selected window.
     */
    public void openFolderWindow(FolderInfo f) {
	openFolderWindow(f, true);
    }

    /**
     * This opens a new FolderWindow for the given FolderInfo.  If
     * selectWindow is set to true, then the window is also automatically
     * selected; if set to false, the folderID.windowLocation.selected 
     * property is used, if set.
     */
    public void openFolderWindow(FolderInfo f, boolean selectWindow) {

	FolderWindow newFolderWindow;
	newFolderWindow = f.getFolderWindow();
	if (newFolderWindow == null) {
	    newFolderWindow = new FolderWindow(f, this);
	    f.setFolderWindow(newFolderWindow);
	    newFolderWindow.pack();
	    newFolderWindow.setVisible(false);
	    this.add(newFolderWindow);
	    try {
		String folderID = f.getFolderID();
		int x = Integer.parseInt(Pooka.getProperty(folderID + ".windowLocation.x"));
		int y = Integer.parseInt(Pooka.getProperty(folderID + ".windowLocation.y"));
		int layer = Integer.parseInt(Pooka.getProperty(folderID + ".windowLocation.layer"));
		int position = Integer.parseInt(Pooka.getProperty(folderID + ".windowLocation.position"));

		newFolderWindow.setLocation(x, y);
		setLayer(newFolderWindow, layer, position);
		
		if ( selectWindow || Pooka.getProperty(folderID + ".windowLocation.selected", "false").equalsIgnoreCase("true"))
		    try {
			newFolderWindow.setSelected(true);
		    } catch (java.beans.PropertyVetoException e) {
		    } 
	    } catch (Exception e) {
		newFolderWindow.setLocation(getNewWindowLocation(newFolderWindow));
		if (selectWindow)
		    try {
			newFolderWindow.setSelected(true);
		    } catch (java.beans.PropertyVetoException pve) {
		    } 
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
     * This returns an available location for JComponent c to be placed
     * in the MessageWindow.
     *
     * At the moment it just returns 0,0.  :)
     */
    public Point getNewWindowLocation(JComponent c) {
	return new Point(0,0);
    }

    /**
     * This gets the FolderInfo associated with each name in the
     * folderList Vector, and attempts to open the FolderWindow for 
     * each.
     *
     * Normally called at startup if Pooka.openSavedFoldersOnStartup
     * is set.
     */
    public void openSavedFolders(Vector folderList) {
	if (folderList != null) 
	    for (int i = 0; i < folderList.size(); i++) {
		FolderInfo fInfo = Pooka.getStoreManager().getFolderById((String)folderList.elementAt(i));
		if (fInfo != null && fInfo.getFolderNode() != null) {
		    FolderNode fNode = fInfo.getFolderNode();
		    Action a = fNode.getAction("file-open");
		    a.actionPerformed(new ActionEvent(this, 0, "file-open"));
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
	boolean newMessage = false;
	MessageWindow messageWindow = m.getMessageWindow();
	if (messageWindow == null) {
	    messageWindow = new ReadMessageWindow(this, m);
	    m.setMessageWindow(messageWindow);
	    newMessage = true;
	}

	final MessageWindow newMessageWindow = messageWindow;
	final boolean isNew = newMessage;

	Runnable openWindowCommand = new RunnableAdapter() {
		public void run() {
		    if (isNew) {
			MessagePanel.this.add(newMessageWindow);
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
	    };
	if (SwingUtilities.isEventDispatchThread())
	    openWindowCommand.run();
	else 
	    try {
		SwingUtilities.invokeAndWait(openWindowCommand);
	    } catch (Exception e) {
		// shouldn't happen.
	    }
	
    }

    /**
     * Calls createNewMessage(Message m) with a new MimeMessage object.
     */
    public void createNewMessage() {
	createNewMessage(new MimeMessage(getMainPanel().getSession()));
    }

    /**
     * Creates a NewMessageProxy and NewMessageWindow for the given 
     * Message object.  Also will open the NewMessageWindow on the
     * MessagePanel and set it as Active.
     */
    public void createNewMessage(javax.mail.Message m) {
	final MessageWindow newMessageWindow = new NewMessageWindow(this, new NewMessageProxy(m));


	Runnable openWindowCommand = new RunnableAdapter() {
		public void run() {

		    MessagePanel.this.add(newMessageWindow);

		    newMessageWindow.setVisible(true);
		    try {
			newMessageWindow.setSelected(true);
		    } catch (java.beans.PropertyVetoException e) {
		    }
		}
	    };
	if (SwingUtilities.isEventDispatchThread())
	    openWindowCommand.run();
	else 
	    try {
		SwingUtilities.invokeAndWait(openWindowCommand);
	    } catch (Exception e) {
		// shouldn't happen.
	    }

    }

    /**
     * This saves the location of all FolderWindows, so that the next
     * time we start up, we can put the windows in the proper places.
     */

    public void saveWindowLocations() {
	JInternalFrame[] allFrames = getAllFrames();
	
	for(int i = 0; i < allFrames.length; i++) {
	    if (allFrames[i] instanceof FolderWindow)
		saveWindowLocation((FolderWindow)allFrames[i]);
	}
	
    }

    /**
     * This saves the location of this FolderWindow, so that the next
     * time we start up, we can put it in the same place.
     */

    public void saveWindowLocation(FolderWindow current) {
	String folderID = current.getFolderInfo().getFolderID();

	// we have to do these as absolute values.
	int x = current.getX() + getMainPanel().getMessageScrollPane().getHorizontalScrollBar().getValue();
	int y = current.getY() + getMainPanel().getMessageScrollPane().getVerticalScrollBar().getValue();
	int layer = getLayer(current);
	int position = getPosition(current);
	boolean selected = current.isSelected();
	
	Pooka.setProperty(folderID + ".windowLocation.x", Integer.toString(x));
	Pooka.setProperty(folderID + ".windowLocation.y", Integer.toString(y));
	Pooka.setProperty(folderID + ".windowLocation.layer", Integer.toString(layer));
	Pooka.setProperty(folderID + ".windowLocation.position", Integer.toString(position));

	if (selected)
	    Pooka.setProperty(folderID + ".windowLocation.selected", "true");
	else
	    Pooka.setProperty(folderID + ".windowLocation.selected", "false");
    }

    /**
     * This saves a list of open folders, so that on future startup we
     * can automatically reopen them.
     */

    public void saveOpenFolders() {
	JInternalFrame[] allFrames = getAllFrames();
	boolean isFirst = true;

	StringBuffer savedFolderValues = new StringBuffer();
	for(int i = 0; i < allFrames.length; i++) {
	    if (allFrames[i] instanceof FolderWindow) {
		String folderID = ((FolderWindow)allFrames[i]).getFolderInfo().getFolderID();
		if (! isFirst)
		    savedFolderValues.append(":");

		isFirst = false;
		
		savedFolderValues.append(folderID);
	    }
	}

	Pooka.setProperty("Pooka.openFolderList", savedFolderValues.toString());
	
    }

    /**
     * This returns the currently selected window for this JDesktopPane.
     */
    public JInternalFrame getCurrentWindow() {
	JInternalFrame[] allFrames = getAllFrames();
	
	for(int i = 0; i < allFrames.length; i++) {
	    if (allFrames[i].isSelected())
		return allFrames[i];
	}
	
	return null;
    }

    /**
     * This makes the next JInternalFrame in the list be selected.
     */
    public void selectNextWindow() {
	JInternalFrame[] allFrames = getAllFrames();

	if (allFrames.length > 0) {
	    for(int i = 0; i < allFrames.length; i++) {
		if (allFrames[i].isSelected()) {
		    JInternalFrame selected = allFrames[i];
		    JInternalFrame newSelected = allFrames[i + 1 % allFrames.length];
 		    try {
			setPosition(selected, allFrames.length -1);
			newSelected.setSelected(true);
		    } catch (java.beans.PropertyVetoException e) {
		    } 		    

		    return;
		}
	    }

	    // if we get to this point, it means that there are windows,
	    // but none of them are selected.

	    try {
		allFrames[0].setSelected(true);
	    } catch (java.beans.PropertyVetoException e) {
	    } 		    
	}
    }

    /**
     * This makes the previous JInternalFrame in the list be selected.
     */
    public void selectPreviousWindow() {
	JInternalFrame[] allFrames = getAllFrames();

	if (allFrames.length > 0) {
	    for(int i = 0; i < allFrames.length; i++) {
		if (allFrames[i].isSelected()) {
		    int j;
		    if (i > 0)
			j = i-1;
		    else
			j = allFrames.length -1;
		    try {
			allFrames[j].setSelected(true);
		    } catch (java.beans.PropertyVetoException e) {
		    } 		    
		    
		    return;
		}
	    }

	    // if we get to this point, it means that there are windows,
	    // but none of them are selected.

	    try {
		allFrames[0].setSelected(true);
	    } catch (java.beans.PropertyVetoException e) {
	    } 		    
	}
    }
    
    /**
     * This moves the current window either 1 or 10 spaces up, down,
     * left, or right, depending on the source of the event.
     */

    public void moveWindow(int modifiers, String cmd) {
	JInternalFrame current = getCurrentWindow();

	if (current != null) {
	    int x = current.getX();
	    int y = current.getY();
	    
	    int moveValue = 1;
	    
	    if ((modifiers & ActionEvent.SHIFT_MASK) != 0)
		moveValue = 10;

	    if (cmd.equals("left"))
		x = x - moveValue;
	    else if (cmd.equals("right"))
		x = x + moveValue;
	    else if (cmd.equals("up"))
		y = y - moveValue;
	    else if (cmd.equals("down"))
		y = y + moveValue;
	    
	    current.setLocation(x, y);
	}
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

    /**
     * This returns whether or not we want to save the location of windows
     * so we can use them again at startup.
     */

    public boolean isSavingWindowLocations() {
	return savingWindowLocations;
    }

    /**
     * This sets whether or not we want to save the locations of windows
     * for later use.
     */
    public void setSavingWindowLocations(boolean newValue) {
	savingWindowLocations = newValue;
    }

    /**
     * This returns whether or not we want to save which folders are open
     * so we can use them again at startup.
     */

    public boolean isSavingOpenFolders() {
	return savingOpenFolders;
    }

    /**
     * This sets whether or not we want to save which folders are open
     * for later use.
     */
    public void setSavingOpenFolders(boolean newValue) {
	savingOpenFolders = newValue;
    }

    public Action[] defaultActions = {
	new NextWindowAction(),
	new PreviousWindowAction(),
	new MoveWindowAction("move-window-left"),
	new MoveWindowAction("move-window-right"),
	new MoveWindowAction("move-window-up"),
	new MoveWindowAction("move-window-down")
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

    public class NextWindowAction extends AbstractAction {
	NextWindowAction() {
	    super("window-next");
	}

	public void actionPerformed(ActionEvent e) {
	    selectNextWindow();
	}
    }

    public class PreviousWindowAction extends AbstractAction {
	PreviousWindowAction() {
	    super("window-previous");
	}

	public void actionPerformed(ActionEvent e) {
	    selectPreviousWindow();
	}
    }

    public class MoveWindowAction extends AbstractAction {
	MoveWindowAction() {
	    super("move-window");
	}

	MoveWindowAction(String cmdString) {
	    super(cmdString);
	}

	public void actionPerformed(ActionEvent e) {
	    String cmdString = e.getActionCommand();
	    moveWindow(e.getModifiers(), cmdString.substring(cmdString.lastIndexOf("-") +1));
	}
    }

}






