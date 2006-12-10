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

    } catch (Exception e) {
      Pooka.getUIFactory().showError("Error setting up new account", e);
    }

    PropertyEditorFactory factory = Pooka.getUIFactory().getEditorFactory();
    PropertyEditorManager manager = new PropertyEditorManager(factory.getSourceBundle(), factory, factory.getIconManager());
    factory.showNewEditorWindow(Pooka.getProperty("Pooka._firstRunWizard.label", "Create New Account"), "Pooka._firstRunWizard", "Pooka._firstRunWizard", "Pooka._firstRunWizard", manager, Pooka.getMainPanel().getParentFrame());

    if (Pooka.getProperty("Store", "").length() > 0) {
      showConfirmation();
    } else {
      showWizardCancelled();
    }

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

  /**
   * Shows a confirmation message saying that the wizard has completed
   * successfully.
   */
  public void showWizardCancelled() {
    Pooka.getUIFactory().showMessage(Pooka.getProperty("FirstRunWizard.cancelledMessage", ""), Pooka.getProperty("FirstRunWizard.cancelledMessage.title", "Wizard cancelled."));
    //JOptionPane.showMessageDialog(Pooka.getMainPanel(), Pooka.getProperty("FirstRunWizard.cancelledMessage", ""), Pooka.getProperty("FirstRunWizard.cancelledMessage.title", "Wizard cancelled."), JOptionPane.INFORMATION_MESSAGE);

  }

  /**
   * Shows a confirmation message saying that the wizard has completed
   * successfully.
   */
  public void showConfirmation() {
    Pooka.getUIFactory().showMessage(Pooka.getProperty("FirstRunWizard.finishedMessage", "Email account configured!  If you need to make changes,\nor to add new accounts, go to the Configuration menu."), Pooka.getProperty("FirstRunWizard.finishedMessage.title", "Done!"));
    //JOptionPane.showMessageDialog(Pooka.getMainPanel(), Pooka.getProperty("FirstRunWizard.finishedMessage", "Email account configured!  If you need to make changes,\nor to add new accounts, go to the Configuration menu."), Pooka.getProperty("FirstRunWizard.finishedMessage.title", "Done!"), JOptionPane.INFORMATION_MESSAGE);

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
