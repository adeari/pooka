package net.suberic.pooka.gui;

import net.suberic.pooka.Pooka;
import net.suberic.util.*;
import net.suberic.util.thread.*;
import net.suberic.util.gui.propedit.*;
import javax.swing.*;
import java.util.*;
import java.io.File;
import java.net.*;

import javax.mail.MessagingException;

/**
 * This is effectively a wizard to run at startup if there's no UserProfile or
 * Store configured.
 */

public class NewAccountPooka {

  // this is such a mess.

  private MessagePanel messagePanel = null;
  private PropertyEditorManager manager = null;
  private PropertyEditorFactory factory = null;
  private String accountName = null;
  
  public NewAccountPooka() {
  };
  
  public NewAccountPooka(MessagePanel newMP) {
    setMessagePanel(newMP);
  }
  
  public void start() {
    
    /**
     * I really should make this easier to configure, shouldn't I?
     */
    
    // first set up the default connection.
    setupDefaultConnection();
    
    if (JOptionPane.showInternalConfirmDialog(getMessagePanel(), Pooka.getProperty("NewAccountPooka.introMessage", "Welcome to Pooka.\nIt seems that you don't yet have an Email account configured.\n\nWould you like to configure one now?"), Pooka.getProperty("NewAccountPooka.introMessage.title", "New Account Pooka"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
      
      PropertyEditorFactory factory = Pooka.getUIFactory().getEditorFactory();
      setFactory(factory);
      
      manager = new PropertyEditorManager(Pooka.getResources(), factory);
      manager.setWriteChanges(false);

      showFirstEntryWindow();
    }
  }
  
  /**
   * Shows the user information entry area.
   */
  public void showFirstEntryWindow() {
    /**
     * This takes the username, fullname, password, servername, and type 
     * (imap, etc.) and then passes it on to handleFirstEntry().
     */
    
    java.util.Vector propertyVector = new java.util.Vector();
    
    //propertyVector.add("NewAccountPooka.firstPanel");
    propertyVector.add("NewAccountPooka");

    JInternalFrame firstEntryWindow = new JInternalFrame(Pooka.getProperty("NewAccountPooka.entryWindowMessage.title", "Enter Email Account Information"), true, false, false, false);
    JComponent contentPane = (JComponent) firstEntryWindow.getContentPane();
    contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
    
    // is there a way to make this wrap automatically, without adding
    // explicit newlines?
    JTextArea jta = new JTextArea(Pooka.getProperty("NewAccountPooka.entryWindowMessage", "Please enter the following \ninformation in order\nto configure your client."));
    jta.setMargin(new java.awt.Insets(5, 15, 5, 15));
    jta.setEditable(false);

    JLabel jl = new JLabel("test");
    jta.setBackground(jl.getBackground());
    //jta.setForeground(jl.getForeground());
    jta.setFont(jl.getFont());

    contentPane.add(jta);
    
    contentPane.add(new PropertyEditorPane(manager,
					   propertyVector,
					   propertyVector,
					   firstEntryWindow));

    firstEntryWindow.pack();
    firstEntryWindow.show();
    firstEntryWindow.addInternalFrameListener(new javax.swing.event.InternalFrameAdapter() {
	
	public void internalFrameClosed(javax.swing.event.InternalFrameEvent e) {
	  SwingUtilities.invokeLater( new Runnable() {
	      public void run() {
		handleFirstEntry();
	      }
	    });
	}
      });
    

    java.awt.Point p = getMessagePanel().getNewWindowLocation(firstEntryWindow, true);
    firstEntryWindow.setLocation(p);
    getMessagePanel().add(firstEntryWindow);
    firstEntryWindow.setVisible(true);
    try {
      firstEntryWindow.setSelected(true);
    } catch (java.beans.PropertyVetoException pve) {
    }
    
  }
  
  /**
   * handles the entries.
   */
  public void handleFirstEntry() {
    Properties props = new java.util.Properties();
    
    try {
      String smtpName = configureSMTP(manager, props);
      String accountName = configureUserStore(manager, props, smtpName);

      testConnections(props);

      createFiles(props);

      setupFolders(props);

      setupAddressBook(props);

      saveProperties(props);

      Pooka.getStoreManager().loadAllSentFolders();
      Pooka.getOutgoingMailManager().loadOutboxFolders();

      openInbox();

    } catch (Exception e) {
      e.printStackTrace();
      handleInvalidEntry(e.getMessage());
    }

  }

  /**
   * Configures the outgoing mail server for the new user.
   */
  public String configureSMTP(PropertyEditorManager mgr, Properties props) throws Exception {

    String smtpServerName = mgr.getProperty("NewAccountPooka.smtpServer", "");

    if (smtpServerName.equals("")) {
      throw new Exception("Must have an Outgoing mail server set.");
    }

    // set up the smtp server
    
    props.setProperty("OutgoingServer", smtpServerName);
    props.setProperty("OutgoingServer." + smtpServerName + ".server", smtpServerName);
    props.setProperty("OutgoingServer." + smtpServerName + ".connection", Pooka.getProperty("Pooka.connection.defaultName", "default"));
    
    props.setProperty("OutgoingServer._default", smtpServerName);

    return smtpServerName;
  }

  /**
   * Configures the store and user.
   */
  public String configureUserStore(PropertyEditorManager mgr, Properties props, String smtpServerName) throws Exception {
    String localUser = System.getProperty("user.name");

    /*
     * this converts the initial entires into an appropriate UserProfile
     * and Store entry.
     */
    String protocol = manager.getProperty("NewAccountPooka.protocol", "");
    String fullName = manager.getProperty("NewAccountPooka.fullName", "");
    String userName;
    String accountName;

    if (! protocol.equalsIgnoreCase("mbox")) {
      userName = manager.getProperty("NewAccountPooka.userName", "");
      String password = manager.getProperty("NewAccountPooka.password", "");
      String serverName = manager.getProperty("NewAccountPooka.serverName", "");
      
      if (userName.equals("")) {
	throw new Exception("Must have a username.");
      } else if (serverName.equals("")) {
	throw new Exception("Must have a servername.");
      } else if (protocol.equals("")) {
	throw new Exception("Must have a valid protocol.");
      }
      
      accountName = userName + "@" + serverName;
      
      props.setProperty("Store." + accountName + ".server", serverName);
      props.setProperty("Store." + accountName + ".user", userName);
      props.setProperty("Store." + accountName + ".password", password);
      props.setProperty("Store." + accountName + ".defaultProfile", accountName);
      props.setProperty("Store." + accountName + ".connection", Pooka.getProperty("Pooka.connection.defaultName", "default"));
    } else {
      userName = localUser;
      accountName = userName + "_local";
    }

    // set up the user.
    
    props.setProperty("UserProfile", accountName);
    props.setProperty("UserProfile." + accountName + ".mailHeaders.From", accountName);
    props.setProperty("UserProfile." + accountName + ".mailHeaders.FromPersonal", fullName);
    props.setProperty("UserProfile." + accountName + ".mailServer", smtpServerName);
    
    props.setProperty("UserProfile.default", accountName);
    
    // set up mail server information
    
    props.setProperty("Store", accountName);
    props.setProperty("Store." + accountName + ".protocol", protocol);

    if (protocol.equalsIgnoreCase("imap")) {
      props.setProperty("Store." + accountName + ".useSubscribed", "true");
      props.setProperty("Store." + accountName + ".SSL", manager.getProperty("NewAccountPooka.useSSL", "false"));
      props.setProperty("Store." + accountName + ".cachingEnabled", manager.getProperty("NewAccountPooka.enableDisconnected", "false"));
    } else if (protocol.equalsIgnoreCase("pop3")) {
      props.setProperty("OutgoingServer." + smtpServerName + ".sendOnConnect", "true");
      props.setProperty("Store." + accountName + ".SSL", manager.getProperty("NewAccountPooka.useSSL", "false"));
      props.setProperty("Store." + accountName + ".leaveMessagesOnServer", manager.getProperty("NewAccountPooka.leaveOnServer", "true"));
      props.setProperty("Store." + accountName + ".useMaildir", "true");
      if (manager.getProperty("NewAccountPooka.leaveOnServer", "true").equalsIgnoreCase("true")) {
	props.setProperty("Store." + accountName + ".deleteOnServerOnLocalDelete", "true");
      }
    } else if (protocol.equalsIgnoreCase("mbox")) {
      props.setProperty("Store." + accountName + ".inboxLocation", manager.getProperty("NewAccountPooka.inboxLocation", "/var/spool/mail/" + System.getProperty("user.name")));
    }
    
    return accountName;
  }

  /**
   * Tests the connections to the servers.
   */
  public void testConnections(Properties props) throws Exception {
    String smtpServer = props.getProperty("OutgoingServer");
    
    Pooka.getUIFactory().showStatusMessage("Creating mail store " + smtpServer + "...");
    
    Pooka.getUIFactory().showStatusMessage("Connecting to mailserver " + smtpServer + "...");
    
    testConnection(smtpServer, 25);

    String mailServerId = props.getProperty("Store");
    String protocol = props.getProperty("Store." + mailServerId + ".protocol");
    if (! protocol.equalsIgnoreCase("mbox")) {
      String mailServerName = props.getProperty("Store." + mailServerId + ".server");
      String useSSL = props.getProperty("Store." + mailServerId + ".SSL");
      int port = 0;
      if (protocol.equalsIgnoreCase("pop3")) {
	if (useSSL.equalsIgnoreCase("true")) 
	  port = 995;
	else
	  port = 110;
	
      } else if (protocol.equalsIgnoreCase("imap")) {
	if (useSSL.equalsIgnoreCase("true")) 
	  port = 993;
	else
	  port = 143;
      }
      testConnection(mailServerName, port);
    } else {
      // setup maildir connection
    }
  }

  /**
   * Tests the connection to the given server and port.
   */
  public void testConnection(String serverName, int port) throws Exception {
    try {
      InetAddress addr = InetAddress.getByName(serverName);
      Socket testSocket = new Socket(addr, port);
      testSocket.close();
    } catch (UnknownHostException uhe) {
      throw new Exception("Unknown host:  " + serverName);
    }
  }

  /**
   * Sets up your sent folder and outbox.
   */
  public void setupFolders(Properties props) throws Exception {
    String storeName = props.getProperty("Store");
    String protocol = props.getProperty("Store." + storeName + ".protocol");
    String localStoreName = storeName;

    if (protocol.equalsIgnoreCase("imap")) {
      // if we have an imap connection, then we actually have to do some
      // work.
      localStoreName = "local";
      props.setProperty("Store", storeName + ":local");
      props.setProperty("Store.local.useInbox", "false");
      props.setProperty("Store.local.folderList", "sent:outbox");
      props.setProperty("Store.local.protocol", "maildir");
    } else {
      // we're fine if not.
      props.setProperty("Store." + localStoreName + ".folderList", "INBOX:sent:outbox");
    }
    String pookaDirName = props.getProperty("Pooka.cacheDirectory");
    String mailDirName = pookaDirName + File.separator + localStoreName;
    String subFolderDirName = mailDirName + File.separator + manager.getProperty("Pooka.subFolderName", "folders");
    
    File mailDir = new File(mailDirName);
    if (! mailDir.exists())
      mailDir.mkdirs();
    
    File subFolderDir = new File(subFolderDirName);
    if (! subFolderDir.exists())
      subFolderDir.mkdirs();
    
    File sentFile = new File(subFolderDirName + File.separator + ".sent");
    if (! sentFile.exists()) {
      sentFile.mkdir();
      
      // i should probably have the maildir store do this.
      new File(sentFile, "cur").mkdir();
      new File(sentFile, "new").mkdir();
      new File(sentFile, "tmp").mkdir();
    }

    File outboxFile = new File(subFolderDirName + File.separator + ".outbox");
    if (! outboxFile.exists()) {
      outboxFile.mkdir();

      new File(outboxFile, "cur").mkdir();
      new File(outboxFile, "new").mkdir();
      new File(outboxFile, "tmp").mkdir();
    }

    props.setProperty("Store.local.mailDir", mailDirName);
    
    
    // actually configure said folders.
    
    String outgoingServer = props.getProperty("OutgoingServer");
    props.setProperty("OutgoingServer." + outgoingServer + ".outbox", localStoreName + "/outbox");

    String userName = props.getProperty("UserProfile");
    props.setProperty("UserProfile." + userName + ".sentFolder", localStoreName + "/sent");
  }

  /**
   * Creates any other necessary files.
   */
  public void createFiles(Properties props) throws Exception {
    String pookaDirName = manager.getProperty("NewAccountPooka.pookaDirectory", System.getProperty("user.home") + File.separator + ".pooka");

    File pookaDir = new File(pookaDirName);
    if (! pookaDir.exists())
      pookaDir.mkdirs();

    String sslFileName = pookaDirName + File.separator + "sslCertificates";

    File sslFile = new File(sslFileName);
    if (!sslFile.exists())
      sslFile.createNewFile();

    props.setProperty("Pooka.cacheDirectory", pookaDirName);
    props.setProperty("Pooka.defaultMailSubDir", pookaDirName);

    props.setProperty("Pooka.sslCertFile", sslFileName);

  }

  /**
   * Sets up a default address book.
   */
  public void setupAddressBook(Properties props) throws java.io.IOException {
    String pookaDirName = manager.getProperty("NewAccountPooka.pookaDirectory", System.getProperty("user.home") + File.separator + ".pooka");

    String addressBookFileName = pookaDirName + File.separatorChar + "defaultAddressBook";
    File addressBookFile = new File(addressBookFileName);
    addressBookFile.createNewFile();  
    
    props.setProperty("AddressBook", "defaultBook");
    props.setProperty("AddressBook.defaultBook.type", "file");
    props.setProperty("AddressBook.defaultBook.filename", addressBookFileName);

  }

  /**
   * Opens up your inbox.
   */
  public void openInbox() throws Exception {
    java.util.Vector allStores = Pooka.getStoreManager().getStoreList();
    net.suberic.pooka.StoreInfo si = null;
    if (allStores.size() > 0) {
      si = (net.suberic.pooka.StoreInfo) allStores.get(0);
    }
    
    if (si != null) {
      ActionThread thread = si.getStoreThread();
      final net.suberic.pooka.StoreInfo storeInfo = si;
      
      javax.swing.Action connectionAction = new javax.swing.AbstractAction() {
	  public void actionPerformed(java.awt.event.ActionEvent ae) {
	    try {
	      storeInfo.connectStore();
	      javax.swing.SwingUtilities.invokeLater( new Runnable() {
		  
		  public void run() {
		    MailTreeNode mtn = null;
		    net.suberic.pooka.FolderInfo fi = storeInfo.getChild("INBOX");
		    if (fi != null) {
		      FolderNode fn = fi.getFolderNode();
		      Action openAction = fn.getAction("file-open");
		      openAction.actionPerformed(new java.awt.event.ActionEvent(this, 0, "file-open"));
		      mtn = fn;
		    } else {
		      mtn = storeInfo.getStoreNode();
		    }
		    if (mtn != null) {
		      javax.swing.JTree folderTree = ((FolderPanel)mtn.getParentContainer()).getFolderTree();
		      folderTree.scrollPathToVisible(new javax.swing.tree.TreePath(mtn.getPath()));
		    }
		    
		    Pooka.getUIFactory().clearStatus();
		    
		    showConfirmation();
		  }
		});
	    } catch (MessagingException me) {
	      final MessagingException error = me;
	      me.printStackTrace();
	      javax.swing.SwingUtilities.invokeLater( new Runnable() {
		  
		  public void run() {
		    Pooka.getUIFactory().clearStatus();
		    StringBuffer errorMessage = new StringBuffer(Pooka.getProperty("error.NewAccountPooka.connectingToStore", "Failed to connect to store.  \nReceived the following error:\n"));
		    errorMessage.append(error.getMessage());
		    errorMessage.append("\n\n");
		    errorMessage.append(Pooka.getProperty("error.NewAccountPooka.continueMessage", "Would you like to re-enter your information?"));
		    
		    JTextArea jta = new JTextArea(errorMessage.toString());
		      JLabel jl = new JLabel("test");
		      jta.setBackground(jl.getBackground());
		      jta.setFont(jl.getFont());
		      
		      int continueResponse = Pooka.getUIFactory().showConfirmDialog(new Object[] { jta }, "Failed to connect to Store.", javax.swing.JOptionPane.OK_CANCEL_OPTION);
		      if (continueResponse == javax.swing.JOptionPane.OK_OPTION)
			showFirstEntryWindow();
		  }
		});
	    }
	  }
	};
      
      thread.addToQueue(connectionAction, new java.awt.event.ActionEvent(this, 0, "connectStore"));
    }

  }

  public void showConfirmation() {
    JOptionPane.showInternalMessageDialog(getMessagePanel(), Pooka.getProperty("NewAccountPooka.finishedMessage", "Email account configured!  If you need to make changes,\nor to add new accounts, go to the Configuration menu."), Pooka.getProperty("NewAccountPooka.finishedMessage.title", "Done!"), JOptionPane.INFORMATION_MESSAGE);
    
  }
  
  public void handleInvalidEntry(String message) {
    StringBuffer errorMessage = new StringBuffer(Pooka.getProperty("error.NewAccountPooka.invalidEntry", "invalid first entry."));
    if (message != null && message.length() > 0) {
      errorMessage.append("\n");
      errorMessage.append(message);
    }
    errorMessage.append("\n\n");
    errorMessage.append(Pooka.getProperty("error.NewAccountPooka.continueMessage", "Would you like to re-enter your information?"));
    
    JTextArea jta = new JTextArea(errorMessage.toString());
    JLabel jl = new JLabel("test");
    jta.setBackground(jl.getBackground());
    jta.setFont(jl.getFont());
    
    int continueResponse = Pooka.getUIFactory().showConfirmDialog(new Object[] { jta }, "Failed to connect to Store.", javax.swing.JOptionPane.OK_CANCEL_OPTION);
    if (continueResponse == javax.swing.JOptionPane.OK_OPTION)
      showFirstEntryWindow();
    
  }

  /**
   * Saves the tagged properties from our local VariableBundle to the 
   * main Pooka properties list.
   */
  public void saveProperties(Properties props) {
    Enumeration names = props.propertyNames();
    while (names.hasMoreElements()) {
      String propertyName = (String) names.nextElement();
      if (propertyName.equals("UserProfile") || propertyName.equals("Store") || propertyName.equals("OutgoingServer")) {
	// skip
      } else {
	transferProperty(props, propertyName);
      }
    }

    transferProperty(props, "UserProfile");
    
    transferProperty(props, "Store");

    transferProperty(props, "OutgoingServer");
  }

  /**
   * Transfers the given property from our local PropertyEditorManager
   * main Pooka properties list.
   */
  public void transferProperty(Properties props, String propertyName) {
    if (!(props.getProperty(propertyName, "").equals(""))) {
      Pooka.setProperty(propertyName, props.getProperty(propertyName, ""));
    }
  }

  /**
   * Sets up the default NetworkConnection.
   */
  private void setupDefaultConnection() {
    String defaultName = Pooka.getProperty("Pooka.connection.defaultName", "default");
    Pooka.setProperty("Connection." + defaultName + ".valueOnStartup", "Connected");
    Pooka.setProperty("Connection", defaultName);
    Pooka.setProperty("Connection._default", defaultName);
  }
  
  
  public MessagePanel getMessagePanel() {
    return messagePanel;
  }
  
  public void setMessagePanel(MessagePanel newValue) {
    messagePanel=newValue;
  }
  
  public PropertyEditorFactory getFactory() {
    return factory;
  }
  
  public void setFactory(PropertyEditorFactory newValue) {
    factory=newValue;
  }
  
}
