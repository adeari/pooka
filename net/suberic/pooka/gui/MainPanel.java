package net.suberic.pooka.gui;
import net.suberic.pooka.Pooka;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.border.*;
import javax.mail.Session;
import net.suberic.pooka.MailQueue;
import net.suberic.util.gui.PropertyEditorFactory;

/**
 * The main panel for PookaMail
 * 
 * @author  Allen Petersen
 * @version 0.3 12/15/99
 */

public class MainPanel extends JSplitPane implements javax.swing.event.TreeSelectionListener {
    private JMenuBar mainMenu;
    private JToolBar mainToolbar;
    private FolderPanel folderPanel;
    private MessagePanel messagePanel;
    private Action[] actions;
    private Hashtable commands;
    private Session session;
    private MailQueue mailQueue;
    private PropertyEditorFactory editorFactory = new PropertyEditorFactory(Pooka.getResources());

    public MainPanel(JFrame frame) {
	super(JSplitPane.HORIZONTAL_SPLIT);

	// set supported actions
	// this.setLayout(new BorderLayout());

	// create the menu bar.
	SimpleAuthenticator auth = new SimpleAuthenticator(frame);
	session = Session.getDefaultInstance(System.getProperties(), auth);
	mailQueue = new MailQueue(session);

	messagePanel = new MessagePanel(this);
	folderPanel = new FolderPanel(this, session);
	Pooka.getResources().addValueChangeListener(folderPanel, "Store");

	this.setLeftComponent(new JScrollPane(folderPanel));
	this.setRightComponent(new JScrollPane(messagePanel));

	setActions();

	mainMenu = createMenubar();
	mainToolbar = createToolbar();

	setActiveMenus(mainMenu);
	setActiveToolbarItems();

    }
    

    /**
     * Create the menuBar.  This reads the values from the resources
     * ResourceBundle and creates menus appropriately.
     */
    
    protected JToolBar createToolbar() {
	JToolBar tBar = new JToolBar();
	if (Pooka.getProperty("MainToolbar", "") == "") {
	    return null;
	}
	
	StringTokenizer tokens = new StringTokenizer(Pooka.getProperty("MainToolbar", ""), ":");
	while (tokens.hasMoreTokens()) {
	  JButton b = createToolButton(tokens.nextToken());
	  if (b != null) {
	    tBar.add(b);
	  }
	}
	return tBar;
    }

    /**
     * This creates the toolbar buttons themselves--called by createToolbar()
     */

    protected JButton createToolButton(String key) {
	JButton bi;
	try {
	    java.net.URL url =this.getClass().getResource(Pooka.getProperty("MainToolbar." + key + ".Image"));
	    bi = new JButton(new ImageIcon(url));

	} catch (MissingResourceException mre) {
	    return null;
	}
	
	try {
	    bi.setToolTipText(Pooka.getProperty("MainToolbar." +key+ ".ToolTip"));
	} catch (MissingResourceException mre) {
	}
	
	String cmd = Pooka.getProperty("MainToolbar." + key + ".Action", key);
	
	bi.setActionCommand(cmd);
	    
	return bi;
    }
    
    // creates the Menubar.

    protected JMenuBar createMenubar() {
	JMenuItem mItem;
	JMenuBar mBar = new JMenuBar();

	if (Pooka.getProperty("MenuBar", "") == "") {
	  System.err.println("Fatal:  no resource MenuBar.  Exiting.");
	  System.exit(-1);
	}
	
	StringTokenizer tokens = new StringTokenizer(Pooka.getProperty("MenuBar", ""), ":");
	while (tokens.hasMoreTokens()) {
	  JMenu m = createMenu(tokens.nextToken());
	  if (m != null) {
	    mBar.add(m);
	  }
	}
	
	return mBar;
    }
    
  /**
   * Create a menu for the app.  By default this pulls the
   * definition of the menu from the associated resource file.
   */

    protected JMenu createMenu(String key) {
	StringTokenizer iKeys = null;
	try {
	    iKeys = new StringTokenizer(Pooka.getProperty(key), ":");
	} catch (MissingResourceException mre) {
	    try {
		System.err.println(Pooka.getProperty("error.NoSuchResource") + " " + mre.getKey());
	    } catch (MissingResourceException mretwo) {
		System.err.println("Unable to load resource " + mre.getKey());
	    } finally {
	      return null;
	    }
	}
	String currentToken;
	JMenu menu;
	
	try {
	    menu = new JMenu(Pooka.getProperty(key + ".Label"));
	} catch (MissingResourceException mre) {
	    menu = new JMenu(key);
    }
	
    while (iKeys.hasMoreTokens()) {
	currentToken=iKeys.nextToken();
	if (currentToken.equals("-")) {
	    menu.addSeparator();
	} else {
	    JMenuItem mi = createMenuItem(key, currentToken);
	    menu.add(mi);
	}
    }
    return menu;
    }
    
    /**
     * And this actually creates the menu items themselves.
     */
    protected JMenuItem createMenuItem(String menuID, String menuItemID) {
    // TODO:  should also make these undo-able.
	
	if (Pooka.getProperty(menuID + "." + menuItemID, "") == "") {
	    JMenuItem mi;
	    try {
		mi = new JMenuItem(Pooka.getProperty(menuID + "." + menuItemID + ".Label"));
	    } catch (MissingResourceException mre) {
		mi = new JMenuItem(menuItemID);
	    }
	    
	    java.net.URL url = null;
	    
	    try {
		url = this.getClass().getResource(Pooka.getProperty(menuID + "." + menuItemID + ".Image"));
	    } catch (MissingResourceException mre) {
	    } /*catch (java.net.MalformedURLException mue) {
		System.out.println("malformedURL for " + menuID + "." + menuItemID + ".Image");
		}*/
	    if (url != null) {
		mi.setHorizontalTextPosition(JButton.RIGHT);
		mi.setIcon(new ImageIcon(url));
	    }
	    
	    String cmd = Pooka.getProperty(menuID + "." + menuItemID + ".Action", menuItemID);
	    
	    mi.setActionCommand(cmd);	
	    return mi;
	} else 
	    if (Pooka.getProperty(menuID + "." + menuItemID, "").equals("folderList")) {
		return new FolderMenu(menuID + "." + menuItemID, getFolderPanel());
	    }
	    else
		return createMenu(menuID + "." + menuItemID );
    }
    
    public Action[] getActions() {
	Action[] actions = getDefaultActions();
	if (folderPanel != null) 
	    if (folderPanel.getActions() != null)
		actions = TextAction.augmentList(folderPanel.getActions(), actions);
	if (messagePanel != null) 
	    if (messagePanel.getActions() != null) 
		actions = TextAction.augmentList(messagePanel.getActions(), actions);
	return actions;
    }
    
    /**
     * This gets an action from the supported commands.  If there is no
     * supported action, it returns null
     */

    public Action getAction(String command) {
	return (Action)commands.get(command);
    }

    private void setActiveMenuItems(JMenu men) {
	if (men instanceof net.suberic.util.DynamicMenu) {
	    ((net.suberic.util.DynamicMenu)men).setActiveMenus(this);
	} else {
	    for (int j = 0; j < men.getItemCount(); j++) {
		if ((men.getItem(j)) instanceof JMenu) {
		    setActiveMenuItems((JMenu)(men.getItem(j)));
		} else {
		    JMenuItem mi = men.getItem(j);
		    Action a = getAction(mi.getActionCommand());
		    if (a != null) {
			//mi.removeActionListener(a);
			mi.addActionListener(a);
			mi.setEnabled(true);
		    } else {
			mi.setEnabled(false);
		    }
		}
	    }
	}
    }	    

    private void setActiveToolbarItems() {
	for (int i = 0; i < mainToolbar.getComponentCount(); i++) {
	    JButton bi = (JButton)(mainToolbar.getComponentAtIndex(i));

	    Action a = getAction(bi.getActionCommand());
	    if (a != null) {
		bi.addActionListener(a);
		bi.setEnabled(true);
	    } else {
		bi.setEnabled(false);
	    }
	}
    }
    private void setActiveMenus(JMenuBar menuBar) {
	for (int i = 0; i < menuBar.getMenuCount(); i++) {
	    setActiveMenuItems(menuBar.getMenu(i));
	}
	setWindowsMenu(menuBar);
    }

    private void setWindowsMenu(JMenuBar menuBar) {
	// TODO:  fix this.  Currently we just recreate the Windows menu
	// every time.  It works, and doesn't seem all that slow, but still....

	JMenu clearWindowMenu = createMenu("Window");
	int origCount = clearWindowMenu.getMenuComponentCount();

	for (int i = menuBar.getMenuCount() - 1; i >= 0; i--) {
	    if (menuBar.getMenu(i).getText().equals("Window")) {
		JMenu windowMenu = menuBar.getMenu(i);
		for (int k = windowMenu.getMenuComponentCount(); k > origCount; k--)
		    windowMenu.remove(k-1);
		
		JInternalFrame[] allFrames = messagePanel.getAllFrames();
		for(int j = 0; j < allFrames.length; j++) {
		    JMenuItem mi = new JMenuItem(allFrames[j].getTitle());
		    mi.addActionListener(new ActivateWindowAction());
		    mi.setActionCommand(String.valueOf(messagePanel.getIndexOf(allFrames[j])));
		    windowMenu.add(mi);
		}
	    }
	}
    }

    private void removeActiveMenuItems(JMenu men) {
	for (int j = 0; j < men.getItemCount(); j++) {
	    if ((men.getItem(j)) instanceof JMenu) {
		removeActiveMenuItems((JMenu)(men.getItem(j)));
	    } else {
		JMenuItem mi = men.getItem(j);
		Action a = getAction(mi.getActionCommand());
		if (a != null) {
		    //                    mi.removeActionListener(a);
		    mi.removeActionListener(a);
		} else {
		    mi.setEnabled(false);
		}
	    }
	    
	}
    }
    
    private void removeActionListeners(JMenuBar menuBar) {
	for (int i = 0; i < menuBar.getMenuCount(); i++) {
	    removeActiveMenuItems(menuBar.getMenu(i));
	}
    }

    private void removeActionListeners(JToolBar toolBar) {
	for (int i = 0; i < toolBar.getComponentCount(); i++) {
	    if ((toolBar.getComponentAtIndex(i)) instanceof JButton) {
		JButton button = (JButton)(toolBar.getComponentAtIndex(i));
		Action a = getAction(button.getActionCommand());
		if (a != null) {
		    //                    mi.removeActionListener(a);
		    button.removeActionListener(a);
		} else {
		    button.setEnabled(false);
		}
	    }
	    
	}   
    }

    /* Called by ExtendedDesktopManager every time the focus on the windows
       changes.  Resets the Actions associated with the menu items and toolbar
       to the ones in the active window.

       Also called by the 
    */

    protected void refreshActiveMenus(JMenuBar menuBar) {
	removeActionListeners(menuBar);
	removeActionListeners(mainToolbar);
	setActions();
	setActiveMenus(menuBar);
	setActiveToolbarItems();

    }


    public void valueChanged(javax.swing.event.TreeSelectionEvent e) { 
	refreshActiveMenus(mainMenu);
    }

    /* Set the actions supported by the current windows.
     */
    public void setActions() {
	actions = getActions();
	
	commands = new Hashtable();
	for (int i = 0; i < actions.length; i++) {
	    Action a = actions[i];
	    commands.put(a.getValue(Action.NAME), a);

	}
    }	

    // Accessor methods.
    // These shouldn't all be public.

    public JMenuBar getMainMenu() {
	return mainMenu;
    }
    
    public void setMainMenu(JMenuBar newMainMenu) {
	mainMenu=newMainMenu;
    }

    public JToolBar getMainToolbar() {
	return mainToolbar;
    }

    public void setMainToolbar(JToolBar newMainToolbar) {
        mainToolbar = newMainToolbar;
    }

    public MessagePanel getMessagePanel() {
	return messagePanel;
    }

    public FolderPanel getFolderPanel() {
	return folderPanel;
    }

    public Session getSession() {
	return session;
    }

    public MailQueue getMailQueue() {
	return mailQueue;
    }

    public PropertyEditorFactory getEditorFactory() {
	return editorFactory;
    }

    public Action[] getDefaultActions() {
	return defaultActions;
    }


    /**
     * Find the hosting frame, for the file-chooser dialog.
     */
    protected Frame getFrame() {
	for (Container p = getParent(); p != null; p = p.getParent()) {
	    if (p instanceof Frame) {
		return (Frame) p;
	    }
	}
	return null;
    }


    //-----------actions----------------
    // Actions supported by the main Panel itself.  These should always
    // be available, even when no documents are open.

    private Action[] defaultActions = {
	new ExitAction(),
	new EditUserConfigAction(),
	new EditStoreConfigAction()
    };


    /*
     * TODO:  This really needs to check and ask if you want to save any
     * modified documents.  Of course, we don't check to see if the docs
     * are modified yet, so this will do for now.
     */
    class ExitAction extends AbstractAction {

	ExitAction() {
	    super("file-exit");
	}

        public void actionPerformed(ActionEvent e) {
	    Pooka.resources.saveProperties(new File(Pooka.localrc));
	    System.exit(0);
	}
    }

    class ActivateWindowAction extends AbstractAction {

	ActivateWindowAction() {
	    super("activate-window");
	}

        public void actionPerformed(ActionEvent e) {
	    try { 
		((JInternalFrame)(messagePanel.getComponent(Integer.parseInt(e.getActionCommand())))).setSelected(true);
	    } catch (java.beans.PropertyVetoException pve) {
	    } catch (NumberFormatException nfe) {
	    }
	}
    }
    
    class EditUserConfigAction extends AbstractAction {

	EditUserConfigAction() {
	    super("cfg-users");
	}

        public void actionPerformed(ActionEvent e) {
	    JDesktopPane msgPanel = getMessagePanel();
	    Vector valuesToEdit = new Vector();
	    valuesToEdit.add("UserProfile");
	    valuesToEdit.add("UserProfile.default");

	    JInternalFrame jif = (JInternalFrame)getEditorFactory().createEditorWindow(Pooka.getProperty("title.userConfig", "Edit User Information"), valuesToEdit);
	    msgPanel.add(jif);
	    try {
		jif.setSelected(true);
	    } catch (java.beans.PropertyVetoException pve) {
	    }
	    
	}
    }


    class EditStoreConfigAction extends AbstractAction {

	EditStoreConfigAction() {
	    super("cfg-stores");
	}

        public void actionPerformed(ActionEvent e) {
	    JDesktopPane msgPanel = getMessagePanel();
	    Vector valuesToEdit = new Vector();
	    valuesToEdit.add("Store");

	    JInternalFrame jif = (JInternalFrame)getEditorFactory().createEditorWindow(Pooka.getProperty("title.storeConfig", "Edit Mailbox Information"), valuesToEdit);
	    msgPanel.add(jif);
	    try {
		jif.setSelected(true);
	    } catch (java.beans.PropertyVetoException pve) {
	    }
	    
	}
    }
}

