package net.suberic.pooka;
import net.suberic.pooka.gui.*;
import net.suberic.util.VariableBundle;
import net.suberic.pooka.resource.*;
import net.suberic.pooka.messaging.*;

import java.awt.*;
import javax.swing.*;
import javax.help.*;
import java.util.logging.*;

/**
 * This manages all startup options for Pooka.
 */
public class StartupManager {
  
  // the PookaManager that we're using to startup.  for convenience.
  PookaManager mPookaManager = null;

  // settings
  public boolean mOpenFolders = true;
  public boolean mUseHttp = false;
  public boolean mUseLocalFiles = true;

  /**
   * Creates a new StartupManager.
   */
  public StartupManager(PookaManager pPookaManager) {
    mPookaManager = pPookaManager;
  }

  /**
   * Runs Pooka.
   */
  public void runPooka(String argv[]) {
    mStartTime = System.currentTimeMillis();

    Pooka.loadInitialResources();

    updateTime("intial resources parsed.");

    parseArgs(argv);

    updateTime("args parsed.");

    Pooka.loadResources(mUseLocalFiles, mUseHttp);

    mPookaManager.setLogManager(new PookaLogManager());

    updateTime("resources loaded.");

    if (! checkJavaVersion()) {
      versionError();
      System.exit(-1);
    }

    final net.suberic.pooka.gui.PookaStartup startup = new net.suberic.pooka.gui.PookaStartup();
    startup.show();

    updateTime("startup invoked.");

    startup.setStatus("Pooka.startup.ssl");
    updateTime("loading ssl");
    StoreManager.setupSSL();
    updateTime("ssl loaded.");

    try {
      UIManager.setLookAndFeel(Pooka.getProperty("Pooka.looknfeel", UIManager.getCrossPlatformLookAndFeelClassName()));
    } catch (Exception e) { System.out.println("Cannot set look and feel...");
    }
    
    updateTime("set looknfeel");
    startup.setStatus("Pooka.startup.addressBook");
    mPookaManager.setAddressBookManager(new AddressBookManager());
    updateTime("loaded address book");
    
    mPookaManager.setConnectionManager(new NetworkConnectionManager());
    updateTime("loaded connections");
    
    mPookaManager.setOutgoingMailManager(new OutgoingMailServerManager());
    updateTime("loaded mailservers");

    mPookaManager.setDateFormatter(new DateFormatter());

    startup.setStatus("Pooka.startup.profiles");
    UserProfile.createProfiles(mPookaManager.getResources());
    updateTime("created profiles");
    
    Pooka.getResources().addValueChangeListener(UserProfile.vcl, "UserProfile");
    
    String mailcapSource = null;
    if (System.getProperty("file.separator").equals("\\")) {
      mailcapSource = System.getProperty("user.home") + "\\pooka_mailcap.txt";
    } else {
      mailcapSource = System.getProperty("user.home") + System.getProperty("file.separator") + ".pooka_mailcap";
    }
    try {
      mPookaManager.setMailcap(Pooka.getResourceManager().createMailcap(mailcapSource));
    } catch (java.io.IOException ioe) {
      System.err.println("exception loading mailcap:  " + ioe);
    }

    updateTime("created mailcaps");
    mPookaManager.setFolderTracker(new net.suberic.pooka.thread.FolderTracker());
    mPookaManager.getFolderTracker().start();
    updateTime("started folderTracker");
    
    mPookaManager.setSearchThread(new net.suberic.util.thread.ActionThread(Pooka.getProperty("thread.searchThread", "Search Thread ")));
    mPookaManager.getSearchThread().start();
    updateTime("started search thread");
    
    javax.activation.CommandMap.setDefaultCommandMap(mPookaManager.getMailcap());
    javax.activation.FileTypeMap.setDefaultFileTypeMap(mPookaManager.getMimeTypesMap());
    updateTime("set command/file maps");

    startup.setStatus("Pooka.startup.crypto");
    mPookaManager.setCryptoManager(new PookaEncryptionManager(mPookaManager.getResources(), "EncryptionManager"));
    updateTime("loaded encryption manager");

    mPookaManager.setSearchManager(new SearchTermManager("Search"));
    updateTime("created search manager");
    
    if (Pooka.getProperty("Pooka.guiType", "Desktop").equalsIgnoreCase("Preview"))
      mPookaManager.setUIFactory(new PookaPreviewPaneUIFactory());
    else
      mPookaManager.setUIFactory(new PookaDesktopPaneUIFactory());
    
    updateTime("created ui factory");
    mPookaManager.getResources().addValueChangeListener(new net.suberic.util.ValueChangeListener() {
	public void valueChanged(String changedValue) {
	  if (Pooka.getProperty("Pooka.guiType", "Desktop").equalsIgnoreCase("Preview")) {
	    MessagePanel mp = (MessagePanel) Pooka.getMainPanel().getContentPanel();
	    mPookaManager.setUIFactory(new PookaPreviewPaneUIFactory());
	    ContentPanel cp = ((PookaPreviewPaneUIFactory) mPookaManager.getUIFactory()).createContentPanel(mp);
	    Pooka.getMainPanel().setContentPanel(cp);
	  } else {
	    PreviewContentPanel pcp = (PreviewContentPanel) Pooka.getMainPanel().getContentPanel();
	    mPookaManager.setUIFactory(new PookaDesktopPaneUIFactory());
	    ContentPanel mp = ((PookaDesktopPaneUIFactory) mPookaManager.getUIFactory()).createContentPanel(pcp);
	    Pooka.getMainPanel().setContentPanel(mp);
	  }
	}
      }, "Pooka.guiType");

    mPookaManager.getResources().addValueChangeListener(new net.suberic.util.ValueChangeListener() {
	public void valueChanged(String changedValue) {
	  try {
	    UIManager.setLookAndFeel(Pooka.getProperty("Pooka.looknfeel", UIManager.getCrossPlatformLookAndFeelClassName()));
	    javax.swing.SwingUtilities.updateComponentTreeUI(javax.swing.SwingUtilities.windowForComponent(Pooka.getMainPanel()));
	  } catch (Exception e) { 
	    System.out.println("Cannot set look and feel..."); }
	}
      }, "Pooka.looknfeel");
    
    updateTime("created resource listeners");
    // set up help
    startup.setStatus("Pooka.startup.help");
    try {
      ClassLoader cl = new Pooka().getClass().getClassLoader();
      java.net.URL hsURL = HelpSet.findHelpSet(cl, "net/suberic/pooka/doc/en/help/Master.hs");
      HelpSet hs = new HelpSet(cl, hsURL);
      mPookaManager.setHelpBroker(hs.createHelpBroker());
    } catch (Exception ee) {
      System.out.println("HelpSet net/suberic/pooka/doc/en/help/merge/Master.hs not found:  " + ee);
      ee.printStackTrace();
    }
    updateTime("loaded help");
    
    // create the MessageListener.
    PookaMessageListener pmlistener= new PookaMessageListener();

    JFrame frame = new JFrame("Pooka");
    updateTime("created frame");

    mPookaManager.setDefaultAuthenticator(new SimpleAuthenticator(frame));
    java.util.Properties sysProps = System.getProperties();
    sysProps.setProperty("mail.mbox.mailspool", mPookaManager.getResources().getProperty("Pooka.spoolDir", "/var/spool/mail"));
    mPookaManager.setDefaultSession (javax.mail.Session.getDefaultInstance(sysProps, mPookaManager.getDefaultAuthenticator()));
    if (Pooka.getProperty("Pooka.sessionDebug", "false").equalsIgnoreCase("true"))
      mPookaManager.getDefaultSession().setDebug(true);

    updateTime("created session.");    
    startup.setStatus("Pooka.startup.mailboxInfo");
    mPookaManager.setStoreManager(new StoreManager());
    updateTime("created store manager.");
    
    mPookaManager.getStoreManager().loadAllSentFolders();
    mPookaManager.getOutgoingMailManager().loadOutboxFolders();
    updateTime("loaded sent/outbox");
    
    final JFrame finalFrame = frame;

    startup.setStatus("Pooka.startup.configuringWindow");
    // do all of this on the awt event thread.
    Runnable createPookaUI = new Runnable() {
	public void run() {
	  finalFrame.setBackground(Color.lightGray);
	  finalFrame.getContentPane().setLayout(new BorderLayout());
	  MainPanel panel = new MainPanel(finalFrame);
	  mPookaManager.setMainPanel(panel);
	  finalFrame.getContentPane().add("Center", panel);

	  updateTime("created main panel");
	  startup.setStatus("Pooka.startup.starting");

	  panel.configureMainPanel();

	  updateTime("configured main panel");

	  finalFrame.getContentPane().add("North", panel.getMainToolbar());
	  finalFrame.setJMenuBar(panel.getMainMenu());
	  finalFrame.getContentPane().add("South", panel.getInfoPanel());
	  finalFrame.pack();
	  finalFrame.setSize(Integer.parseInt(Pooka.getProperty("Pooka.hsize", "800")), Integer.parseInt(Pooka.getProperty("Pooka.vsize", "600")));
	  
	  int x = Integer.parseInt(Pooka.getProperty("Pooka.lastX", "10"));
	  int y = Integer.parseInt(Pooka.getProperty("Pooka.lastY", "10"));

	  finalFrame.setLocation(x, y);
	  updateTime("configured frame");
	  startup.hide();
	  finalFrame.show();
	  updateTime("showed frame");
	  
	  mPookaManager.getUIFactory().setShowing(true);
	  
	  if (Pooka.getProperty("Store", "").equals("")) {
	    if (panel.getContentPanel() instanceof MessagePanel) {
	      SwingUtilities.invokeLater(new Runnable() {
		  public void run() {
		    NewAccountPooka nap = new NewAccountPooka((MessagePanel) Pooka.getMainPanel().getContentPanel());
		    nap.start();
		  }
		});
	    }
	  } else if (mOpenFolders && Pooka.getProperty("Pooka.openSavedFoldersOnStartup", "false").equalsIgnoreCase("true")) {
	    panel.getContentPanel().openSavedFolders(mPookaManager.getResources().getPropertyAsVector("Pooka.openFolderList", ""));
	  }
	  panel.refreshActiveMenus();
	}
      };
    
    try {
      javax.swing.SwingUtilities.invokeAndWait(createPookaUI);
    } catch (Exception e) {
      System.err.println("caught exception creating ui:  " + e);
      e.printStackTrace();
    }
  }
  
  /**
   * This parses any command line arguments, and makes the appropriate
   * changes.
   */
  public void parseArgs(String[] argv) {
    if (argv == null || argv.length < 1)
      return;

    for (int i = 0; i < argv.length; i++) {
      if (argv[i] != null) {
	if (argv[i].equals("-nf") || argv[i].equals("--noOpenSavedFolders")) {
	  mOpenFolders = false;
	} else if (argv[i].equals("-rc") || argv[i].equals("--rcfile")) {
	  String filename = argv[++i];
	  if (filename == null) {
	    System.err.println("error:  no startup file specified.");
	    System.err.println("Usage:  java net.suberic.pooka.Pooka [-rc <filename>]");
	    System.exit(-1);
	  }
	  
	  mPookaManager.setLocalrc(filename);
	} else if (argv[i].equals("--http")) {
	  mUseHttp = true;
	  mUseLocalFiles = false;
	} else if (argv[i].equals("--newmessage")) {
	  String mailAddress = argv[++i];
	  if (mailAddress == null) {
	    System.err.println("error:  no address specified.");
	    printUsage();
	    System.exit(-1);
	  }
	  
	  // if we have an address, try sending a mail message to it.
	  sendMessageTo(mailAddress);
	} else if (argv[i].equals("--help")) {
	  printUsage();
	  System.exit(0);
	}
      }
    }
  }
  
  /**
   * Prints the usage information.
   */
  public void printUsage() {
    System.out.println(Pooka.getProperty("info.startup.help", "\nUsage:  net.suberic.pooka.Pooka [OPTIONS]\n\n  -nf, --noOpenSavedFolders    don't open saved folders on startup.\n  -rc, --rcfile FILE           use the given file as the pooka startup file.\n  --http                       runs with a configuration file loaded via http\n  --newmessage ADDRESS         sends a new message to ADDRESS.\n  --help                       shows these options.\n"));
  }

  /**
   * Checks to make sure that the Java version is valid.
   */
  public boolean checkJavaVersion() {
    // Pooka 1.1 only runs on JDK 1.4 or higher.
    String javaVersion = System.getProperty("java.version");
    if (javaVersion.compareTo("1.4") >= 0) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Called if an incorrect version of Java is being used.
   */
  private void versionError() {
    Runnable runMe = new Runnable() {
	public void run() {
	  String errorString = Pooka.getProperty("error.incorrectJavaVersion", "Error running Pooka.  This version (1.0 beta) \nof Pooka requires a 1.2 or 1.3 JDK.  \n\nFor JDK 1.4, please use a release of Pooka 1.1.\n\nPooka can be downloaded from\nhttp://pooka.sourceforge.net/\n\nYour JDK version:  ");
	  javax.swing.JOptionPane.showMessageDialog(null, errorString + System.getProperty("java.version"));
	}
      };

    if (SwingUtilities.isEventDispatchThread())
      runMe.run();
    else {
      try {
	SwingUtilities.invokeAndWait(runMe);
      } catch (Exception ie) {
      }
    }
  }

  /**
   * Sends a message to the given mail address on startup.
   */
  public void sendMessageTo(String pAddress) {
    // first see if there's already a pooka instance running.
    /*
    PookaConnectionManager pcm = new PookaConnectionManager();
    if (pcm.isConnected()) {
      pcm.sendMessage("mailto:" + pAddress);
    }
    */
  }

  private long mStartTime = 0;
  private long mLastUpdate = 0;
  /**
   * debug.
   */
  public void updateTime(String message) {
    if (mPookaManager.getResources() != null && Pooka.isDebug()) {
      long current = System.currentTimeMillis();
      System.err.println(message + ", time " + (current - mLastUpdate) + ", total " + (current - mStartTime));
      mLastUpdate = current;
    }
  }

  /**
   * Gets the logger for this class.
   */
  public Logger getLogger() {
    return Logger.getLogger("Pooka.debug.startupManager");
  }

}
