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
  static public NetworkConnectionManager connectionManager;
  static public OutgoingMailServerManager outgoingMailManager;

  static public javax.mail.Session defaultSession;
  static public net.suberic.pooka.thread.FolderTracker folderTracker;
  
  static public StoreManager storeManager;
  
  static public PookaUIFactory uiFactory;
  
  static public boolean openFolders = true;
  
  static public javax.mail.Authenticator defaultAuthenticator = null;
  
  static public net.suberic.util.thread.ActionThread searchThread = null;

  static public AddressBookManager addressBookManager = null;
  
  static public void main(String argv[]) {
    parseArgs(argv);
    
    localrc = new String (System.getProperty("user.home") + System.getProperty("file.separator") + ".pookarc"); 
    
    try {
      resources = new net.suberic.util.VariableBundle(new java.io.File(localrc), new net.suberic.util.VariableBundle(new Object().getClass().getResourceAsStream("/net/suberic/pooka/Pookarc"), "net.suberic.pooka.Pooka"));
    } catch (Exception e) {
      resources = new net.suberic.util.VariableBundle(new Object().getClass().getResourceAsStream("/net/suberic/pooka/Pookarc"), "net.suberic.pooka.Pooka");
    }

    // set up the SSL socket factory.
    java.security.Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
    java.security.Security.setProperty("ssl.SocketFactory.provider","net.suberic.pooka.ssl.PookaSSLSocketFactory");

    try {
	UIManager.setLookAndFeel(getProperty("Pooka.looknfeel", UIManager.getCrossPlatformLookAndFeelClassName()));
    } catch (Exception e) { System.out.println("Cannot set look and feel..."); }

    addressBookManager = new AddressBookManager();

    connectionManager = new NetworkConnectionManager();

    outgoingMailManager = new OutgoingMailServerManager();

    dateFormatter = new DateFormatter();

    UserProfile.createProfiles(resources);
    
    resources.addValueChangeListener(UserProfile.vcl, "UserProfile");
    
    mailcap = new FullMailcapCommandMap();
    folderTracker = new net.suberic.pooka.thread.FolderTracker();
    folderTracker.start();
    
    searchThread = new net.suberic.util.thread.ActionThread(getProperty("thread.searchThread", "Search Thread "));
    searchThread.start();
    
    javax.activation.CommandMap.setDefaultCommandMap(mailcap);
    javax.activation.FileTypeMap.setDefaultFileTypeMap(mimeTypesMap);
    searchManager = new SearchTermManager("Search");
    
    if (Pooka.getProperty("Pooka.guiType", "Desktop").equalsIgnoreCase("Preview"))
      uiFactory=new PookaPreviewPaneUIFactory();
    else
      uiFactory = new PookaDesktopPaneUIFactory();
    
    resources.addValueChangeListener(new net.suberic.util.ValueChangeListener() {
	public void valueChanged(String changedValue) {
	  if (Pooka.getProperty("Pooka.guiType", "Desktop").equalsIgnoreCase("Preview")) {
	    MessagePanel mp = (MessagePanel) Pooka.getMainPanel().getContentPanel();
	    uiFactory=new PookaPreviewPaneUIFactory();
	    ContentPanel cp = ((PookaPreviewPaneUIFactory)uiFactory).createContentPanel(mp);
	    Pooka.getMainPanel().setContentPanel(cp);
	  } else {
	    PreviewContentPanel pcp = (PreviewContentPanel) Pooka.getMainPanel().getContentPanel();
	    uiFactory = new PookaDesktopPaneUIFactory();
	    ContentPanel mp = ((PookaDesktopPaneUIFactory)uiFactory).createContentPanel(pcp);
	    Pooka.getMainPanel().setContentPanel(mp);
	  }
	}
      }, "Pooka.guiType");
    
    JFrame frame = new JFrame("Pooka");
    defaultAuthenticator = new SimpleAuthenticator(frame);
    java.util.Properties sysProps = System.getProperties();
    sysProps.setProperty("mail.mbox.mailspool", resources.getProperty("Pooka.spoolDir", "/var/spool/mail"));
    defaultSession = javax.mail.Session.getDefaultInstance(sysProps, defaultAuthenticator);
    if (Pooka.getProperty("Pooka.sessionDebug", "false").equalsIgnoreCase("true"))
      defaultSession.setDebug(true);
    
    storeManager = new StoreManager();
    
    storeManager.loadAllSentFolders();
    outgoingMailManager.loadOutboxFolders();
    
    
    frame.setBackground(Color.lightGray);
    frame.getContentPane().setLayout(new BorderLayout());
    panel = new MainPanel(frame);
    frame.getContentPane().add("Center", panel);
    panel.configureMainPanel();
    frame.getContentPane().add("North", panel.getMainToolbar());
    frame.setJMenuBar(panel.getMainMenu());
    frame.getContentPane().add("South", panel.getInfoPanel());
    frame.pack();
    frame.setSize(Integer.parseInt(Pooka.getProperty("Pooka.hsize", "800")), Integer.parseInt(Pooka.getProperty("Pooka.vsize", "600")));
    frame.show();
    
    uiFactory.setShowing(true);
    
    if (getProperty("Store", "").equals("")) {
      if (panel.getContentPanel() instanceof MessagePanel) {
	NewAccountPooka nap = new NewAccountPooka((MessagePanel)panel.getContentPanel());
	nap.start();
      }
    } else if (openFolders && getProperty("Pooka.openSavedFoldersOnStartup", "false").equalsIgnoreCase("true")) {
      panel.getContentPanel().openSavedFolders(resources.getPropertyAsVector("Pooka.openFolderList", ""));
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
  
  static public PookaUIFactory getUIFactory() {
    return uiFactory;
  }
  
  static public net.suberic.util.thread.ActionThread getSearchThread() {
    return searchThread;
  }

  static public AddressBookManager getAddressBookManager() {
    return addressBookManager;
  }

  static public NetworkConnectionManager getConnectionManager() {
    return connectionManager;
  }

  static public OutgoingMailServerManager getOutgoingMailManager() {
    return outgoingMailManager;
  }
}


