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
import net.suberic.pooka.UserProfile;
import net.suberic.util.gui.*;

/**
 * The main panel for PookaMail
 * 
 * @author  Allen Petersen
 * @version 0.7 02/28/2000
 */

public class MainPanel extends JSplitPane implements javax.swing.event.TreeSelectionListener, net.suberic.pooka.UserProfileContainer {
    private ConfigurableMenuBar mainMenu;
    private ConfigurableToolbar mainToolbar;
    private FolderPanel folderPanel;
    private MessagePanel messagePanel;
    private JScrollPane messageScrollPane;
    private Session session;
    private MailQueue mailQueue;
    private UserProfile currentUser = null;
    private PropertyEditorFactory editorFactory = new PropertyEditorFactory(Pooka.getResources());
    private ConfigurableKeyBinding keyBindings;

    public MainPanel(JFrame frame) {
	super(JSplitPane.HORIZONTAL_SPLIT);

	SimpleAuthenticator auth = new SimpleAuthenticator(frame);

	session = Session.getDefaultInstance(System.getProperties(), auth);
	
	if (Pooka.getProperty("Pooka.sessionDebug", "false").equalsIgnoreCase("true"))
	    session.setDebug(true);

	mailQueue = new MailQueue(Pooka.getDefaultSession());
    }
    
    /**
     * This actually sets up the main panel.
     */
    public void configureMainPanel() {
	// set supported actions
	// this.setLayout(new BorderLayout());
	// create the menu bar.

	messagePanel = new MessagePanel(this);
	messagePanel.setSize(1000,1000);
	messageScrollPane = new JScrollPane(messagePanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	messagePanel.setDesktopManager(messagePanel.new ExtendedDesktopManager(messagePanel, messageScrollPane));
	folderPanel = new FolderPanel(this);
	Pooka.getResources().addValueChangeListener(folderPanel, "Store");

	this.setLeftComponent(folderPanel);
	this.setRightComponent(messageScrollPane);
	this.setDividerLocation(folderPanel.getPreferredSize().width);

	mainMenu = new ConfigurableMenuBar("MenuBar", Pooka.getResources());
	mainToolbar = new ConfigurableToolbar("MainToolbar", Pooka.getResources());

	keyBindings = new ConfigurableKeyBinding(this, "MainPanel.keyBindings", Pooka.getResources());
	keyBindings.setCondition(JComponent.WHEN_IN_FOCUSED_WINDOW);

	// set the default active menus.
	mainMenu.setActive(getActions());
	mainToolbar.setActive(getActions());
	keyBindings.setActive(getActions());
	
	// set the initial currentUser
	refreshCurrentUser();
    }

    /**
     * This gets all the actinos associated with this panel.  Useful for
     * populating the MenuBar and Toolbar.
     *
     * The method actually returns the Panel's defaultActions plus the
     * actions of the folderPanel and messagePanel.
     */    
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

    /*
    private void setWindowsMenu(JMenuBar menuBar) {
	// TODO:  fix this.  Currently we just recreate the Windows menu
	// every time.  It works, and doesn't seem all that slow, but still....

	JMenu clearWindowMenu = createMenu("MenuBar.Window");
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
    */

    /**
     * This method shows a help screen.  At the moment, it just takes the
     * given URL, creates a JInteralFrame and a JEditorPane, and then shows
     * the doc with those components.
     */
    public void showHelpScreen(String title, java.net.URL url) {
	JInternalFrame jif = new JInternalFrame(title, true, true, true);
	JEditorPane jep = new JEditorPane();
	try {
	    jep.setPage(url);
	} catch (IOException ioe) {
	    jep.setText(Pooka.getProperty("err.noHelpPage", "No help available."));
	}
	jep.setEditable(false);
	jif.setSize(500,500);
	jif.getContentPane().add(new JScrollPane(jep));
	getMessagePanel().add(jif);
	jif.setVisible(true);
	try {
	    jif.setSelected(true);
	} catch (java.beans.PropertyVetoException e) {
	} 
	
    }

    /**
     * Called by ExtendedDesktopManager every time the focus on the windows
     * changes.  Resets the Actions associated with the menu items and toolbar
     * to the ones in the active window.
     *
     * Also called when the selected message in a FolderWindow is changed.
     */

    protected void refreshActiveMenus(JMenuBar menuBar) {
	mainMenu.setActive(getActions());
	mainToolbar.setActive(getActions());
	keyBindings.setActive(getActions());
    }

    /**
     * refreshCurrentUser() is called to get a new value for the currently
     * selected item.  In MainPanel, all it does is tries to get a 
     * UserProfile from the currently selected object in the MessagePanel.
     * If there is no object in the MessagePanel which gives a default
     * UserProfile, it then checks the FolderPanel.  If neither of these
     * returns a UserProfile, then the default UserProfile is returned.
     */
    protected void refreshCurrentUser() {
	UserProfile selectedProfile = getDefaultProfile();
	if (selectedProfile != null) {
	    currentUser = selectedProfile;
	} else {
	    currentUser = UserProfile.getDefaultProfile();
	}
    }

    public void valueChanged(javax.swing.event.TreeSelectionEvent e) { 
	refreshActiveMenus(mainMenu);
    }

    /**
     * As defined in net.suberic.pooka.UserProfileContainer.
     *
     * Note that this method can return null, and is primarily used to 
     * get the currentUser.  If you want to get the current default 
     * profile, use getCurrentUser() instead.
     */
    public UserProfile getDefaultProfile() {
	UserProfile returnValue = null;

	if (messagePanel != null) {
	    returnValue = messagePanel.getDefaultProfile();
	}

	if (returnValue != null)
	    return returnValue;

	if (folderPanel != null)
	    returnValue = folderPanel.getDefaultProfile();

	return returnValue;

    }

    public UserProfile getCurrentUser() {
	return currentUser;
    }

    // Accessor methods.
    // These shouldn't all be public.

    public ConfigurableMenuBar getMainMenu() {
	return mainMenu;
    }
    
    public void setMainMenu(ConfigurableMenuBar newMainMenu) {
	mainMenu=newMainMenu;
    }

    public ConfigurableToolbar getMainToolbar() {
	return mainToolbar;
    }

    public ConfigurableKeyBinding getKeyBindings() {
	return keyBindings;
    }
    
    public void setKeyBindings(ConfigurableKeyBinding newKeyBindings) {
	keyBindings = newKeyBindings;
    }

    public void setMainToolbar(ConfigurableToolbar newMainToolbar) {
        mainToolbar = newMainToolbar;
    }

    public MessagePanel getMessagePanel() {
	return messagePanel;
    }

    public JScrollPane getMessageScrollPane() {
	return messageScrollPane;
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
	new EditStoreConfigAction(),
	new HelpAboutAction(),
	new HelpLicenseAction()
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
	    jif.setVisible(true);
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
	    jif.setVisible(true);
	    try {
		jif.setSelected(true);
	    } catch (java.beans.PropertyVetoException pve) {
	    }
	    
	}
    }

    class HelpAboutAction extends AbstractAction {
	
	HelpAboutAction() {
	    super("help-about");
	}
	
	public void actionPerformed(ActionEvent e) {
	    String fileName="About.html";
	    String dir="/net/suberic/pooka/doc";
	    showHelpScreen(Pooka.getProperty("MenuBar.Help.About.Label", "About Pooka"), this.getClass().getResource(dir + "/" + java.util.Locale.getDefault().getLanguage() + "/" + fileName));
	    
	}
    }
    
    class HelpLicenseAction extends AbstractAction {
	
	HelpLicenseAction() {
	    super("help-license");
	}

	public void actionPerformed(ActionEvent e) {
	    String fileName="COPYING";
	    String dir="/net/suberic/pooka";
	    showHelpScreen(Pooka.getProperty("MenuBar.Help.License.Label", "License"), this.getClass().getResource(dir + "/" + fileName));
	}
    }
}
