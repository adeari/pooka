package net.suberic.pooka;
import net.suberic.pooka.gui.*;
import java.awt.*;
import javax.swing.*;
import java.util.Vector;

public class Pooka {

  // globals

  // the resources for Pooka
  static public net.suberic.util.VariableBundle resources;

  // the startup/configuration file
  static public String localrc = null;

  // mail globals
  static public javax.mail.Session defaultSession;
  static public javax.activation.CommandMap mailcap;
  static public javax.activation.MimetypesFileTypeMap mimeTypesMap = new javax.activation.MimetypesFileTypeMap();
  static public javax.mail.Authenticator defaultAuthenticator = null;

  // the DateFormatter, which we cache for convenience.
  static public DateFormatter dateFormatter;

  // threads
  static public net.suberic.util.thread.ActionThread searchThread = null;
  static public net.suberic.pooka.thread.FolderTracker folderTracker;

  // Pooka managers and factories
  static public AddressBookManager addressBookManager = null;
  static public StoreManager storeManager;
  static public PookaUIFactory uiFactory;
  static public SearchTermManager searchManager;
  static public NetworkConnectionManager connectionManager;
  static public OutgoingMailServerManager outgoingMailManager;

  // the main Pooka panel.
  static public net.suberic.pooka.gui.MainPanel panel;

  // settings
  static public boolean openFolders = true;
  static public String pookaHome = null;

  /**
   * Runs Pooka.  Takes the following arguments:
   *
   * -nf 
   * --noOpenSavedFolders    don't open saved folders on startup.
   * 
   * -rc <filename>
   * --rcfile <filename>     use the given file as the pooka startup file.
   */
  static public void main(String argv[]) {
    parseArgs(argv);
    
    // if localrc hasn't been set, use the user's home directory.
    if (localrc == null)
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
    } catch (Exception e) { System.out.println("Cannot set look and feel...");
    }
    
    addressBookManager = new AddressBookManager();
    
    connectionManager = new NetworkConnectionManager();
    
    outgoingMailManager = new OutgoingMailServerManager();

    dateFormatter = new DateFormatter();

    UserProfile.createProfiles(resources);
    
    resources.addValueChangeListener(UserProfile.vcl, "UserProfile");
    
    String mailcapSource = null;
    if (System.getProperty("file.separator").equals("\\")) {
      mailcapSource = System.getProperty("user.home") + "\\pooka_mailcap.txt";
    } else {
      mailcapSource = System.getProperty("user.home") + System.getProperty("file.separator") + ".pooka_mailcap";
    }

    try {
      mailcap = new FullMailcapCommandMap(mailcapSource);
    } catch (java.io.IOException ioe) {
      System.err.println("exception loading mailcap:  " + ioe);
    }

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

    resources.addValueChangeListener(new net.suberic.util.ValueChangeListener() {
	public void valueChanged(String changedValue) {
	  try {
	    UIManager.setLookAndFeel(getProperty("Pooka.looknfeel", UIManager.getCrossPlatformLookAndFeelClassName()));
	    javax.swing.SwingUtilities.updateComponentTreeUI(javax.swing.SwingUtilities.windowForComponent(getMainPanel()));
	  } catch (Exception e) { 
	    System.out.println("Cannot set look and feel..."); }
	}
      }, "Pooka.looknfeel");
    
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
	
	
	if (argv[i].equals("-rc") || argv[i].equals("--rcfile")) {
	  String filename = argv[++i];
	  if (filename == null) {
	    System.err.println("error:  no startup file specified.");
	    System.err.println("Usage:  java net.suberic.pooka.Pooka [-rc <filename>]");
	    System.exit(-1);
	  }

	  localrc = filename;
	}
      }
    }
  }
  
  /**
   * Convenience method for getting Pooka configuration properties.  Calls
   * getResources().getProperty(propName, defVal).
   */
  static public String getProperty(String propName, String defVal) {
    return (resources.getProperty(propName, defVal));
  }
  
  /**
   * Convenience method for getting Pooka configuration properties.  Calls
   * getResources().getProperty(propName).
   */
  static public String getProperty(String propName) {
    return (resources.getProperty(propName));
  }
  
  /**
   * Convenience method for setting Pooka configuration properties.  Calls
   * getResources().setProperty(propName, propValue).
   */
  static public void setProperty(String propName, String propValue) {
    resources.setProperty(propName, propValue);
  }
  
  /**
   * Returns the VariableBundle which provides all of the Pooka resources.
   */
  static public net.suberic.util.VariableBundle getResources() {
    return resources;
  }
  
  /**
   * Returns whether or not debug is enabled for this Pooka instance.
   */
  static public boolean isDebug() {
    if (resources.getProperty("Pooka.debug").equals("true"))
      return true;
    else
      return false;
  }
  
  /**
   * Returns the DateFormatter used by Pooka.
   */
  static public DateFormatter getDateFormatter() {
    return dateFormatter;
  }
  
  /**
   * Returns the mailcap command map.  This is what is used to determine
   * which external programs are used to handle files of various MIME
   * types.
   */
  static public javax.activation.CommandMap getMailcap() {
    return mailcap;
  }
  
  /**
   * Returns the Mime Types map.  This is used to map file extensions to
   * MIME types.
   */
  static public javax.activation.MimetypesFileTypeMap getMimeTypesMap() {
    return mimeTypesMap;
  }
  
  /**
   * Gets the default mail Session for Pooka.
   */
  static public javax.mail.Session getDefaultSession() {
    return defaultSession;
  }
  
  /**
   * Gets the Folder Tracker thread.  This is the thread that monitors the
   * individual folders and checks to make sure that they stay connected,
   * checks for new email, etc.
   */
  static public net.suberic.pooka.thread.FolderTracker getFolderTracker() {
    return folderTracker;
  }
  
  /**
   * Gets the Pooka Main Panel.  This is the root of the entire Pooka UI.
   */
  static public MainPanel getMainPanel() {
    return panel;
  }
  
  /**
   * The Store Manager.  This tracks all of the Mail Stores that Pooka knows
   * about.
   */
  static public StoreManager getStoreManager() {
    return storeManager;
  }
  
  /**
   * The Search Manager.  This manages the Search Terms that Pooka knows 
   * about, and also can be used to construct Search queries from sets
   * of properties.
   */
  static public SearchTermManager getSearchManager() {
    return searchManager;
  }
  
  /**
   * The UIFactory for Pooka.  This is used to create just about all of the
   * graphical UI components for Pooka.  Usually this is either an instance
   * of PookaDesktopPaneUIFactory or PookaPreviewPaneUIFactory, for the
   * Desktop and Preview UI styles, respectively.
   */
  static public PookaUIFactory getUIFactory() {
    return uiFactory;
  }
  
  /**
   * The Search Thread.  This is the thread that folder searches are done
   * on.
   */
  static public net.suberic.util.thread.ActionThread getSearchThread() {
    return searchThread;
  }

  /**
   * The Address Book Manager keeps track of all of the configured Address 
   * Books.
   */
  static public AddressBookManager getAddressBookManager() {
    return addressBookManager;
  }

  /**
   * The ConnectionManager tracks the configured Network Connections.
   */
  static public NetworkConnectionManager getConnectionManager() {
    return connectionManager;
  }

  /**
   * The OutgoingMailManager tracks the various SMTP server that Pooka can
   * use to send mail.
   */
  static public OutgoingMailServerManager getOutgoingMailManager() {
    return outgoingMailManager;
  }

}


