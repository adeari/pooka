package net.suberic.pooka;
import net.suberic.pooka.gui.*;
import java.awt.*;
import javax.swing.*;
import java.util.Vector;

public class Pooka {
    static public net.suberic.util.VariableBundle resources;
    static public String localrc;
    static public DateFormatter dateFormatter;
    static public javax.activation.CommandMap mailcap;
    static public javax.activation.MimetypesFileTypeMap mimeTypesMap = new javax.activation.MimetypesFileTypeMap();
    static public net.suberic.pooka.gui.MainPanel panel;
    static public SearchTermManager searchManager;

    static public javax.mail.Session defaultSession;
    static public net.suberic.pooka.thread.FolderTracker folderTracker;

    static public StoreManager storeManager;

    static public boolean openFolders = true;

    static public void main(String argv[]) {

	parseArgs(argv);

	localrc = new String (System.getProperty("user.home") + System.getProperty("file.separator") + ".pookarc"); 

	try {
	    resources = new net.suberic.util.VariableBundle(new java.io.FileInputStream(localrc), new net.suberic.util.VariableBundle(new Object().getClass().getResourceAsStream("/net/suberic/pooka/Pookarc"), "net.suberic.pooka.Pooka"));
       	} catch (Exception e) {
	    resources = new net.suberic.util.VariableBundle(new Object().getClass().getResourceAsStream("/net/suberic/pooka/Pookarc"), "net.suberic.pooka.Pooka");
	}

	dateFormatter = new DateFormatter();

	UserProfile.createProfiles(resources);

	resources.addValueChangeListener(UserProfile.vcl, "UserProfile");

	mailcap = new FullMailcapCommandMap();
	folderTracker = new net.suberic.pooka.thread.FolderTracker();
	folderTracker.start();

	javax.activation.CommandMap.setDefaultCommandMap(mailcap);
	javax.activation.FileTypeMap.setDefaultFileTypeMap(mimeTypesMap);
	searchManager = new SearchTermManager("Search");

	JFrame frame = new JFrame("Pooka");
	SimpleAuthenticator auth = new SimpleAuthenticator(frame);
	defaultSession = javax.mail.Session.getDefaultInstance(System.getProperties(), auth);
	if (Pooka.getProperty("Pooka.sessionDebug", "false").equalsIgnoreCase("true"))
	    defaultSession.setDebug(true);

	storeManager = new StoreManager();

	storeManager.loadAllSentFolders();

	frame.setBackground(Color.lightGray);
	frame.getContentPane().setLayout(new BorderLayout());
	panel = new MainPanel(frame);
	frame.getContentPane().add("Center", panel);
	panel.configureMainPanel();
	frame.getContentPane().add("North", panel.getMainToolbar());
	frame.setJMenuBar(panel.getMainMenu());
	frame.pack();
	frame.setSize(Integer.parseInt(Pooka.getProperty("Pooka.hsize", "800")), Integer.parseInt(Pooka.getProperty("Pooka.vsize", "600")));
        frame.show();

	if (getProperty("Store", "").equals("")) {
	    NewAccountPooka nap = new NewAccountPooka(panel.getMessagePanel());
	    nap.start();
	} else {
	    if (openFolders && getProperty("Pooka.openSavedFoldersOnStartup", "false").equalsIgnoreCase("true"))
		panel.getMessagePanel().openSavedFolders(resources.getPropertyAsVector("Pooka.openFolderList", ""));
	}

	panel.refreshActiveMenus();
    }

    /**
     * This parses any command line arguments, and makes the appropriate
     * changes.
     */
    public static void parseArgs(String[] argv) {
	if (argv == null || argv.length < 1)
	    return;

	for (int i = 0; i < argv.length; i++) {
	    if (argv[i] != null) {
		if (argv[i].equals("-nf") || argv[i].equals("--noOpenSavedFolders"))
		    openFolders = false;
	    }
	}
    }

    static public String getProperty(String propName, String defVal) {
	return (resources.getProperty(propName, defVal));
    }

    static public String getProperty(String propName) {
	return (resources.getProperty(propName));
    }

    static public void setProperty(String propName, String propValue) {
	resources.setProperty(propName, propValue);
    }

    static public net.suberic.util.VariableBundle getResources() {
	return resources;
    }

    static public boolean isDebug() {
	if (resources.getProperty("Pooka.debug").equals("true"))
	    return true;
	else
	    return false;
    }

    static public DateFormatter getDateFormatter() {
	return dateFormatter;
    }

    static public javax.activation.CommandMap getMailcap() {
	return mailcap;
    }

    static public javax.activation.MimetypesFileTypeMap getMimeTypesMap() {
	return mimeTypesMap;
    }

    static public javax.mail.Session getDefaultSession() {
	return defaultSession;
    }

    static public net.suberic.pooka.thread.FolderTracker getFolderTracker() {
	return folderTracker;
    }

    static public MainPanel getMainPanel() {
	return panel;
    }

    static public StoreManager getStoreManager() {
	return storeManager;
    }

    static public SearchTermManager getSearchManager() {
	return searchManager;
    }
}










