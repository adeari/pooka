package net.suberic.pooka.gui.propedit;
import net.suberic.pooka.Pooka;
import net.suberic.pooka.gui.FolderNode;
import net.suberic.pooka.gui.FolderPanel;
import net.suberic.pooka.gui.MailTreeNode;
import net.suberic.util.VariableBundle;
import net.suberic.util.gui.propedit.*;
import net.suberic.util.thread.ActionThread;

import java.io.File;
import java.util.*;
import javax.mail.MessagingException;
import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

/**
 * The controller class for the FirstRunWizard.
 */
public class FirstRunWizardController extends NewStoreWizardController {

  /**
   * Creates a FirstRunWizardController.
   */
  public FirstRunWizardController(String sourceTemplate, WizardEditorPane wep) {
    super(sourceTemplate, wep);
  }

  /**
   * Finsihes the wizard.
   */
  public void finishWizard() throws PropertyValueVetoException {
    saveProperties();

    getManager().commit();
    setupFolders();
    Pooka.getStoreManager().loadAllSentFolders();
    Pooka.getOutgoingMailManager().loadOutboxFolders();
    Pooka.getPookaManager().getResources().saveProperties();

    openInbox();

  }

  /**
   * Sets up your sent folder and outbox.
   */
  public void setupFolders() {
    boolean useLocalFiles = Pooka.getProperty("Pooka.useLocalFiles", "true").equalsIgnoreCase("true");
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
    String pookaDirName = Pooka.getProperty("FirstRunWizard.pookaDirectory", System.getProperty("user.home") + File.separator + ".pooka");
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


  Exception mOpenInboxException = null;
  boolean mOpenInboxSuccessful = false;
  /**
   * Opens up your inbox.
   */
  public void openInbox() {
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

                    openInboxSuccess();
                  }
                });
            } catch (MessagingException me) {
              int continueValue = handleInvalidEntry(me.getMessage());
              if (continueValue == JOptionPane.YES_OPTION) {
                mState = "storeConfig";
                getEditorPane().loadState("storeConfig");
              } else {
                getEditorPane().getWizardContainer().closeWizard();
              }
            }
          }
        };

      thread.addToQueue(connectionAction, new java.awt.event.ActionEvent(this, 0, "connectStore"));
    }

  }

  private void openInboxSuccess() {
    getEditorPane().getWizardContainer().closeWizard();
  }

  public int handleInvalidEntry(String message) {
    StringBuffer errorMessage = new StringBuffer(Pooka.getProperty("error.NewAccountPooka.invalidEntry", "invalid first entry."));
    if (message != null && message.length() > 0) {
      errorMessage.append("\n");
      errorMessage.append(message);
    }
    errorMessage.append("\n\n");
    errorMessage.append(Pooka.getProperty("error.NewAccountPooka.continueMessage", "Would you like to re-enter your information?"));

    JLabel jta = new JLabel(errorMessage.toString());
    int continueResponse = JOptionPane.showOptionDialog(Pooka.getMainPanel().getContentPanel().getUIComponent(), errorMessage.toString(), "Failed to connect to Store.", javax.swing.JOptionPane.YES_NO_OPTION, javax.swing.JOptionPane.WARNING_MESSAGE,  null, new Object[] { "Re-enter", "Continue" }, "Re-enter");
    if (continueResponse == 0) {
      return JOptionPane.YES_OPTION;
    } else {
      return JOptionPane.NO_OPTION;
    }

  }


}
