package net.suberic.pooka;
import net.suberic.pooka.gui.*;
import java.awt.*;
import javax.swing.*;

public class Pooka {
    static public net.suberic.util.VariableBundle resources;
    static public String localrc;
    static public DateFormatter dateFormatter;
    static public javax.activation.CommandMap mailcap;
    static public javax.activation.MimetypesFileTypeMap mimeTypesMap = new javax.activation.MimetypesFileTypeMap();
    static public net.suberic.pooka.gui.MainPanel panel;

    static public javax.mail.Session defaultSession;
    static public net.suberic.pooka.thread.FolderTracker folderTracker;

    static public void main(String argv[]) {
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

	JFrame frame = new JFrame("Pooka");
	SimpleAuthenticator auth = new SimpleAuthenticator(frame);
	defaultSession = javax.mail.Session.getDefaultInstance(System.getProperties(), auth);

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
	    if (getProperty("Pooka.openSavedFoldersOnStartup", "false").equalsIgnoreCase("true"))
		panel.getMessagePanel().openSavedFolders(resources.getPropertyAsVector("Pooka.openFolderList", ""));
	}

	//UserProfile.loadAllSentFolders();
	panel.refreshActiveMenus();
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

    /**
     * This returns a Vector with all the currently registered StoreInfo
     * objects.
     */
    static java.util.Vector getAllStoreInfos() {
	// i really should store all the StoreInfos as a static variable
	// on the StoreInfo class, or something like that.  instead, i'm
	// getting it from the FolderPanel.  *sigh*

	return getMainPanel().getFolderPanel().getAllStoreInfos();
    }

    /**
     * This returns the StoreInfo which corresponds with the storeName.
     */
    static public StoreInfo getStore(String storeName) {
	return StoreInfo.getStoreInfo(storeName);
    }

    /**
     * This returns the FolderInfo which corresponds to the given folderName.
     * The folderName should be in the form "/storename/folder/subfolder".
     */
    static public FolderInfo getFolder(String folderName) {
	if (folderName.length() < 1) {
	    int divider = folderName.indexOf('/', 1);
	    if (divider > 0) {
		String storeName = folderName.substring(1, divider);
		StoreInfo store = getStore(storeName);
		if (store != null) {
		    return store.getChild(folderName.substring(divider +1));
		} 
	    }
	}

	return null;
    }

    /**
     * This returns the FolderInfo which corresponds to the given folderID.
     * The folderName should be in the form "storename.folderID.folderID".
     */
    static public FolderInfo getFolderById(String folderID) {
	// hurm.  the problem here is that '.' is a legal value in a name...

	java.util.Vector allStores = getAllStoreInfos();

	for (int i = 0; i < allStores.size(); i++) {
	    StoreInfo currentStore = (StoreInfo) allStores.elementAt(i);
	    if (folderID.startsWith(currentStore.getStoreID())) {
		FolderInfo possibleMatch = currentStore.getFolderById(folderID);
		if (possibleMatch != null) {
		    return possibleMatch;
		}
	    }
	}

	return null;
    }

    
}










