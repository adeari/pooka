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

public class FirstRunWizard {

  boolean useLocalFiles = true;
  String pookaDirName;

  /**
   * Runs the wizard.
   */
  public void start() {

    try {
      createFiles();

      // first set up the default connection.
      setupDefaultConnection();
      // then set up the default files
      setupAddressBook();

      PropertyEditorFactory factory = Pooka.getUIFactory().getEditorFactory();
      PropertyEditorManager manager = new PropertyEditorManager(factory.getSourceBundle(), factory, factory.getIconManager());
      factory.showNewEditorWindow(Pooka.getProperty("Pooka._firstRunWizard.label", "Create New Account"), "Pooka._firstRunWizard", "Pooka._firstRunWizard", "Pooka._firstRunWizard", manager, Pooka.getMainPanel().getParentFrame());

      manager.commit();
      //useLocalFiles = Pooka.getProperty("Pooka.useLocalFiles", "true").equalsIgnoreCase("true");

      setupFolders();

      Pooka.getStoreManager().loadAllSentFolders();
      Pooka.getOutgoingMailManager().loadOutboxFolders();
      Pooka.getPookaManager().getResources().saveProperties();
    } catch (Exception e) {
      Pooka.getUIFactory().showError("Error setting up new account", e);
    }

    // now open the inbox.

    try {
      openInbox();
    } catch (Exception e) {
      Pooka.getUIFactory().showError("Error opening inbox", e);
    }
    showConfirmation();

  }

  /**
   * Tests the connections to the servers.
   */
  public void testConnections(Properties props) throws Exception {
    /*
    String smtpServer = props.getProperty("OutgoingServer");

    Pooka.getUIFactory().showStatusMessage("Creating mail store " + smtpServer + "...");

    Pooka.getUIFactory().showStatusMessage("Connecting to mailserver " + smtpServer + "...");

    try {
      testConnection(smtpServer, 25);
    } catch (Exception smtpException) {
      throw new Exception("Error connection to SMTP server:\n" + smtpException.getMessage(), smtpException);
    }

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
      try {
        testConnection(mailServerName, port);
      } catch (Exception mailServerException) {
        throw new Exception("Error connecting to mail server:\n" + mailServerException.getMessage(), mailServerException);
      }
    } else {
      // setup maildir connection
    }
    */
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
  public void setupFolders() throws Exception {
    String storeName = Pooka.getProperty("Store");
    String protocol = Pooka.getProperty("Store." + storeName + ".protocol");
    String localStoreName = storeName;

    if (protocol.equalsIgnoreCase("imap") && useLocalFiles) {
      // if we have an imap connection, then we actually have to do some
      // work.
      localStoreName = "local";
      Pooka.setProperty("Store.local.useInbox", "false");
      Pooka.setProperty("Store.local.folderList", "sent:outbox");
      Pooka.setProperty("Store.local.protocol", "maildir");
      Pooka.setProperty("Store", storeName + ":local");
    } else {
      // we're fine if not.
      Pooka.setProperty("Store." + localStoreName + ".folderList", "INBOX:sent:outbox");
    }
    //String pookaDirName = props.getProperty("Pooka.cacheDirectory");
    String mailDirName = pookaDirName + File.separator + localStoreName;
    String subFolderDirName = mailDirName + File.separator + Pooka.getProperty("Pooka.subFolderName", "folders");

    if (useLocalFiles) {
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
    }

    if (useLocalFiles) {
      File outboxFile = new File(subFolderDirName + File.separator + ".outbox");
      if (! outboxFile.exists()) {
        outboxFile.mkdir();

        new File(outboxFile, "cur").mkdir();
        new File(outboxFile, "new").mkdir();
        new File(outboxFile, "tmp").mkdir();
      }

      Pooka.setProperty("Store.local.mailDir", mailDirName);

    }


    // actually configure said folders.

    String outgoingServer = Pooka.getProperty("OutgoingServer");
    Pooka.setProperty("OutgoingServer." + outgoingServer + ".outbox", localStoreName + "/outbox");

    String userName = Pooka.getProperty("UserProfile");
    Pooka.setProperty("UserProfile." + userName + ".sentFolder", localStoreName + "/sent");
  }

  /**
   * Creates any other necessary files.
   */
  public void createFiles() throws Exception {
    if (useLocalFiles) {
      pookaDirName = Pooka.getProperty("FirstRunWizard.pookaDirectory", System.getProperty("user.home") + File.separator + ".pooka");

      File pookaDir = new File(pookaDirName);
      if (! pookaDir.exists())
        pookaDir.mkdirs();

      String sslFileName = pookaDirName + File.separator + "sslCertificates";

      File sslFile = new File(sslFileName);
      if (!sslFile.exists())
        sslFile.createNewFile();

      Pooka.setProperty("Pooka.cacheDirectory", pookaDirName);
      Pooka.setProperty("Pooka.defaultMailSubDir", pookaDirName);

      Pooka.setProperty("Pooka.sslCertFile", sslFileName);
    }

  }

  /**
   * Sets up a default address book.
   */
  public void setupAddressBook() throws java.io.IOException {
    if (useLocalFiles) {
      String pookaDirName = System.getProperty("user.home") + File.separator + ".pooka";

      String addressBookFileName = pookaDirName + File.separatorChar + "defaultAddressBook";

      File addressBookFile = new File(addressBookFileName);
      addressBookFile.createNewFile();

      Pooka.setProperty("AddressBook", "defaultBook");
      Pooka.setProperty("AddressBook.defaultBook.type", "file");
      Pooka.setProperty("AddressBook.defaultBook.filename", addressBookFileName);
    }

  }

  Exception mOpenInboxException = null;
  boolean mOpenInboxSuccessful = false;
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

      // set our local variables to track what's going on.
      mOpenInboxException = null;
      mOpenInboxSuccessful = false;

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
                  }
                });
            } catch (MessagingException me) {
              final MessagingException error = me;
              me.printStackTrace();
              javax.swing.SwingUtilities.invokeLater( new Runnable() {
                  public void run() {
                    Pooka.getUIFactory().clearStatus();
                    Pooka.getUIFactory().showError("Error opening inbox", error);
                  }
                });
            }
          }
        };

      thread.addToQueue(connectionAction, new java.awt.event.ActionEvent(this, 0, "connectStore"));
    }

  }

  public void showConfirmation() {
    JOptionPane.showMessageDialog(Pooka.getMainPanel(), Pooka.getProperty("FirstRunWizard.finishedMessage", "Email account configured!  If you need to make changes,\nor to add new accounts, go to the Configuration menu."), Pooka.getProperty("FirstRunWizard.finishedMessage.title", "Done!"), JOptionPane.INFORMATION_MESSAGE);

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

}
