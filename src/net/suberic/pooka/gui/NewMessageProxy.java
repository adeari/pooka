package net.suberic.pooka.gui;
import javax.mail.*;
import javax.mail.internet.*;
import javax.swing.*;
import javax.activation.*;
import java.util.Hashtable;
import java.util.Vector;
import net.suberic.pooka.*;
import java.awt.event.ActionEvent;
import java.io.*;

import net.suberic.pooka.crypto.*;
import net.suberic.pooka.gui.crypto.*;

/**
 * This class represents a new message that is being written.
 */
public class NewMessageProxy extends MessageProxy {
  Hashtable commands;

  NewMessageCryptoInfo mCryptoInfo = null;

  private static Vector allUnsentProxies = new Vector();

  boolean sendLock = false;
  
  public NewMessageProxy(NewMessageInfo newMessage) {
    messageInfo = newMessage;
    messageInfo.setMessageProxy(this);

    mCryptoInfo = new NewMessageCryptoInfo(newMessage);

    commands = new Hashtable();
    
    Action[] actions = getActions();
    if (actions != null) {
      for (int i = 0; i < actions.length; i++) {
	Action a = actions[i];
	commands.put(a.getValue(Action.NAME), a);
      }
    }

    allUnsentProxies.add(this);
    
  }
  
  public void openWindow() {
    // shouldn't have to open window.
  }
  
  public void moveMessage(Folder targetFolder) {
    // shouldn't have to.  might want to implement this to move a message
    // to drafts, though.
  }
  
  /**
   * This sends the Message associated with this MessageProxy.
   *
   * If this MessageProxy has a MessageUI associated with it, it 
   * will try to load the information from it, and then send the message.
   * Otherwise, it will just try sending the message as-is.
   *
   * If the Message.sendImmediately property is set, then the method will 
   * also pop up and error window if there are any problems sending the 
   * queued messages.  
   *
   * If there is a MessageUI associated with this Proxy, and either 
   * there are no errors sending the message, or the Message is just added 
   * to the Queue and not sent yet, the Window will also be closed.
   *
   */
  public void send() {
    // thread:  AwtEvent

    synchronized(this) {
      if (sendLock) {
	Pooka.getUIFactory().showStatusMessage(Pooka.getProperty("info.sendMessage.alreadySendingMessage", "Already sending message."));
	return;
      } else
	sendLock = true;
    }
    
    try {
      if (getNewMessageUI() != null) { 
	getNewMessageUI().setBusy(true);
	UserProfile profile = getNewMessageUI().getSelectedProfile();
	InternetHeaders headers = getNewMessageUI().getMessageHeaders();
	
	String messageText = getNewMessageUI().getMessageText();
	
	String messageContentType = getNewMessageUI().getMessageContentType();
	
	if (getCryptoInfo().updateRecipientInfos(profile, headers)) {
	  getNewMessageInfo().sendMessage(profile, headers, getCryptoInfo(), messageText, messageContentType);
	}
      }
    } catch (MessagingException me) {
      sendLock=false;
      getMessageUI().showError(Pooka.getProperty("Error.sendingMessage", "Error sending message:  "), me);
    } 
  }

  /**
   * Called when the send succeeds.
   */
  public void sendSucceeded() {
    final NewMessageUI nmui = getNewMessageUI();
    Pooka.getUIFactory().clearStatus();
    if (nmui != null) {
      getNewMessageInfo().saveToSentFolder(getNewMessageUI().getSelectedProfile());
      
      Runnable runMe = new Runnable() {
	  public void run() {
	    nmui.setBusy(false);
	    nmui.setModified(false);
	    nmui.closeMessageUI();
	  }
	};
      SwingUtilities.invokeLater(runMe);
    }

  }

  /**
   * Called when the send fails.
   */
  public void sendFailed(OutgoingMailServer pMailServer, Exception e) {
    sendLock=false;
    Pooka.getUIFactory().clearStatus();
    final OutgoingMailServer mailServer = pMailServer;
    final Exception me = e;
    final NewMessageUI nmui = getNewMessageUI();
    if (nmui != null) {
      Runnable runMe = new Runnable() {
	  public void run() {
	    if (me instanceof MessagingException) {
	      if (mailServer != null) {
		SendFailedDialog sfd = getNewMessageUI().showSendFailedDialog(mailServer, (MessagingException) me);
		if (sfd.resendMessage()) {
		  OutgoingMailServer newServer = sfd.getMailServer();
		  if (newServer != null) {
		    String action = sfd.getMailServerAction();
		    UserProfile profile = getNewMessageUI().getSelectedProfile();
		    if (action == SendFailedDialog.S_SESSION_DEFAULT) {
		      profile.setTemporaryMailServer(newServer);
		    } else if (action == SendFailedDialog.S_CHANGE_DEFAULT) {
		      Pooka.setProperty(profile.getUserProperty() + ".mailServer", newServer.getItemID());
		    }
		    newServer.sendMessage(getNewMessageInfo());
		  }
		} else if (sfd.getSaveToOutbox()) {
		  try {
		    mailServer.saveToOutbox(getNewMessageInfo());
		  } catch (MessagingException outboxException) {
		    getMessageUI().showError(Pooka.getProperty("error.MessageUI.sendFailed", "Failed to send Message.") + "\n" + outboxException.getMessage());
		  }
		}
	      } else {
		getMessageUI().showError(Pooka.getProperty("error.MessageUI.sendFailed", "Failed to send Message.") + "\n" + me.getMessage());
		me.printStackTrace(System.out);
	      }
	    } else {
	      getMessageUI().showError(Pooka.getProperty("error.MessageUI.sendFailed", "Failed to send Message.") + "\n" + me.getMessage());
	      me.printStackTrace(System.out);
	    }
	    nmui.setBusy(false);
	  }
	};
      SwingUtilities.invokeLater(runMe);
    } else {
      Runnable runMe = new Runnable() {
	  public void run() {
	    Pooka.getUIFactory().showError(Pooka.getProperty("error.MessageUI.sendFailed", "Failed to send Message.") + "\n" + me.getMessage());
	    me.printStackTrace(System.out);
	  }
	};
      SwingUtilities.invokeLater(runMe);
    }
  }
  
  /**
   * Matches the currently selected UserProfile to the one set in the
   * NewMessageInfo.
   */
  public void matchUserProfile() {
    NewMessageUI nmui = getNewMessageUI();
    if (nmui != null) {
      try {
	String profileId = (String) getMessageInfo().getMessageProperty(Pooka.getProperty("Pooka.userProfileProperty", "X-Pooka-UserProfile"));
	if (profileId != null && ! profileId.equals("")) {
	  UserProfile profile = UserProfile.getProfile(profileId);
	  if (profile != null)
	    nmui.setSelectedProfile(profile);
	}
      } catch (MessagingException me) {
	// no big deal...  we can just have the default user selected.
      }
    }    
  }
  
  /**
   * This attaches a file to a given message.  Really, all it does is
   * calls getFileToAttach(), and then sends that to attachFile().
   */
  public void attach() {
    File[] f = getFileToAttach();
    if (f != null) {
      for (int i = 0; i < f.length; i++)
	attachFile(f[i]);
    }
  }
  
  /**
   * This calls on the MessageUI to bring up a FileDialog to choose
   * the file to attach to the message.  If no choice is made, this 
   * method returns null.
   */
  public File[] getFileToAttach() {
    return getNewMessageUI().getFiles(Pooka.getProperty("MessageUI.attachFileDialog.title", "Choose file to attach."), Pooka.getProperty("MessageUI.attachFileDialog.buttonText", "Attach"));
  }
  
  /**
   * This actually attaches the File to the Message.  Any errors are 
   * sent to the MessageUI to display.  
   *
   * This also sets the 'hasAttachment' property on the MessageUI
   * to true.
   */
  public void attachFile(File f) {
    try {
      getNewMessageInfo().attachFile(f);
      
      getNewMessageUI().attachmentAdded(getNewMessageInfo().getAttachments().size() -1);
    } catch (Exception e) {
      getMessageUI().showError(Pooka.getProperty("error.MessageUI.unableToAttachFile", "Unable to attach file."), Pooka.getProperty("error.MessageUI.unableToAttachFile.title", "Unable to Attach File."), e);
    }
    
  }
  
  /**
   * This removes the given Attachment from the list of attachments.
   * I figure that you're likely only to be removing attachments from 
   * the attachment list itself, so you should be able to get the
   * correct underlying object.
   */
  public void detachFile(Attachment a) {
    int index = getNewMessageInfo().removeAttachment(a);
    if (index != -1)
      getNewMessageUI().attachmentRemoved(index);
  }

  /**
   * Saves this message as a draft version, if there is an Outbox
   * configured.
   */
  public void saveDraft() {
    UserProfile profile = getNewMessageUI().getSelectedProfile();
    
    OutgoingMailServer mailServer = profile.getMailServer();

    final FolderInfo fi = mailServer.getOutbox();
    
    
    if (fi != null) {
      net.suberic.util.thread.ActionThread folderThread = fi.getFolderThread();
      Action runMe = new AbstractAction() {
	  public void actionPerformed(java.awt.event.ActionEvent e) {
	    try {
	      getNewMessageInfo().saveDraft(fi);
	      saveDraftSucceeded(fi);
	    } catch (MessagingException me) {
	      saveDraftFailed(me);
	    }
	  }
	};
      folderThread.addToQueue(runMe, new java.awt.event.ActionEvent(this, 0, "saveDraft"));
    } else {
      saveDraftFailed(new MessagingException ("No outbox specified for default mailserver " + mailServer.getItemID()));
    }
  }

  /**
   * Called when the save draft succeeds.
   */
  public void saveDraftSucceeded(FolderInfo outboxFolder) {
    final FolderInfo outbox = outboxFolder;
    final NewMessageUI nmui = getNewMessageUI();
    if (nmui != null) {
      Runnable runMe = new Runnable() {
	  public void run() {
	    nmui.setBusy(false);
	    nmui.setModified(false);
	    getMessageUI().showMessageDialog("Message saved to " +outbox.getFolderID(), "Draft Saved");
	    getMessageUI().closeMessageUI();
	  }
	};
      SwingUtilities.invokeLater(runMe);
    }

  }

  /**
   * Called when the send fails.
   */
  public void saveDraftFailed(Exception e) {
    final Exception me = e;
    final NewMessageUI nmui = getNewMessageUI();
    if (nmui != null) {
      Runnable runMe = new Runnable() {
	  public void run() {
	    getMessageUI().showError(Pooka.getProperty("error.MessageUI.saveDraftFailed", "Failed to save message.") + "\n" + me.getMessage());
	    nmui.setBusy(false);
	  }
	};
      SwingUtilities.invokeLater(runMe);
    }
  }
  

  /**
   * a convenience method which returns the current MessageUI as
   * a NewMessageUI.
   */
  public NewMessageUI getNewMessageUI() {
    if (getMessageUI() instanceof NewMessageUI)
      return (NewMessageUI)getMessageUI();
    else
      return null;
  }
  
  /** 
   * a convenience method which returns the current MessageInfo as
   * a NewMessageInfo.
   */
  public NewMessageInfo getNewMessageInfo() {
    return (NewMessageInfo) messageInfo;
  }

  /**
   * Returns the CryptoInfo for this proxy.
   */
  public NewMessageCryptoInfo getCryptoInfo() {
    return mCryptoInfo;
  }

  /**
   * Returns whether or not we should prompt the user to see if they really
   * want to close the window for this message.
   */
  public boolean promptForClose() {
    if (! Pooka.getProperty("Pooka.checkUnsentMessages", "false").equalsIgnoreCase("true")) {
      return false;
    }
    if (System.getProperty("java.version").compareTo("1.3") < 0) {
      return false;
    }

    NewMessageUI nmui = getNewMessageUI();

    if (nmui != null) {
      return nmui.isModified();
    }
    
    return false;
  }
  
  public Action[] defaultActions = {
    new SendAction(),
    new AttachAction(),
    new SaveDraftAction(),
    new EncryptAction(),
    new ClearEncryptAction(),
    new SelectEncryptionKeyAction(),
    new SignAction(),
    new ClearSignAction(),
    new SelectSignatureKeyAction(),
    new AttachKeyAction(),
    new RemoveKeyAction()
      };
  
  public Action getAction(String name) {
    return (Action)commands.get(name);
  }

  public Action[] getActions() {
    return defaultActions;
  }

  public static Vector getUnsentProxies() {
    return allUnsentProxies;
  }

  class SendAction extends AbstractAction {
    SendAction() {
      super("message-send");
    }
    
    public void actionPerformed(ActionEvent e) {
      send();
    }
  }
  
  class AttachAction extends AbstractAction {
    AttachAction() {
      super("message-attach-file");
    }
    
    public void actionPerformed(ActionEvent e) {
      attach();
    }
  }
  
  class SaveDraftAction extends AbstractAction {
    SaveDraftAction() {
      super("message-save-draft");
    }

    public void actionPerformed(ActionEvent e) {
      saveDraft();
    }
  }
  

  class EncryptAction extends AbstractAction {
    EncryptAction() {
      super("message-encrypt");
    }
    
    public void actionPerformed(ActionEvent e) {
      CryptoStatusDisplay csd = getMessageUI().getCryptoStatusDisplay();
      if (csd instanceof NewMessageCryptoDisplay) {
	((NewMessageCryptoDisplay) csd).setEncryptMessage(NewMessageCryptoInfo.CRYPTO_YES);
      }
      
      if (getCryptoInfo().getEncryptionKey() == null && getNewMessageUI().getSelectedProfile().getEncryptionKey() == null) {
	try {
	  java.security.Key cryptKey = selectPublicKey(Pooka.getProperty("Pooka.crypto.publicKey.forEncrypt", "Select key to encrypt this message."), Pooka.getProperty("Pooka.crypto.publicKey.title", "Select public key"));
	  if (cryptKey != null) {
	    if (csd instanceof NewMessageCryptoDisplay) {
	      ((NewMessageCryptoDisplay) csd).setEncryptionKey(cryptKey);
	    }
	  }
	} catch (Exception ex) {
	  getMessageUI().showError(ex.getMessage(), ex);
	}
      }
    }
  }

  class SignAction extends AbstractAction {
    SignAction() {
      super("message-sign");
    }
    
    public void actionPerformed(ActionEvent e) {
      CryptoStatusDisplay csd = getMessageUI().getCryptoStatusDisplay();
      if (csd instanceof NewMessageCryptoDisplay) {
	((NewMessageCryptoDisplay) csd).setSignMessage(NewMessageCryptoInfo.CRYPTO_YES);
      }
      
      if (getCryptoInfo().getSignatureKey() == null && (getDefaultProfile() == null || getDefaultProfile().getEncryptionKey() == null)) {
	try {
	  java.security.Key signKey = selectPrivateKey(Pooka.getProperty("Pooka.crypto.privateKey.forSig", "Select key to sign this message."), Pooka.getProperty("Pooka.crypto.privateKey.title", "Select private key"));
	  if (csd instanceof NewMessageCryptoDisplay) {
	    ((NewMessageCryptoDisplay) csd).setSignatureKey(signKey);
	  }
	} catch (Exception ex) {
	  getMessageUI().showError(ex.getMessage(), ex);
	}
	
      }
    }
  }
  
  class SelectSignatureKeyAction extends AbstractAction {
    SelectSignatureKeyAction() {
      super("message-select-sig-key");
    }
    
    public void actionPerformed(ActionEvent e) {
      CryptoStatusDisplay csd = getMessageUI().getCryptoStatusDisplay();
      if (csd instanceof NewMessageCryptoDisplay) {
	((NewMessageCryptoDisplay) csd).setSignMessage(NewMessageCryptoInfo.CRYPTO_YES);
      }
      
      try {
	java.security.Key signKey = selectPrivateKey(Pooka.getProperty("Pooka.crypto.privateKey.forSig", "Select key to sign this message."), Pooka.getProperty("Pooka.crypto.privateKey.title", "Select private key"));
	if (signKey != null) {
	  if (csd instanceof NewMessageCryptoDisplay) {
	    ((NewMessageCryptoDisplay) csd).setSignatureKey(signKey);
	  }
	}
      } catch (Exception ex) {
	getMessageUI().showError(ex.getMessage(), ex);
      }
    }
  }
  
  class SelectEncryptionKeyAction extends AbstractAction {
    SelectEncryptionKeyAction() {
      super("message-select-crypt-key");
    }
    
    public void actionPerformed(ActionEvent e) {
      CryptoStatusDisplay csd = getMessageUI().getCryptoStatusDisplay();
      if (csd instanceof NewMessageCryptoDisplay) {
	((NewMessageCryptoDisplay) csd).setEncryptMessage(NewMessageCryptoInfo.CRYPTO_YES);
      }
      
      try {
	java.security.Key cryptKey = selectPublicKey(Pooka.getProperty("Pooka.crypto.publicKey.forEncrypt", "Select key to encrypt this message."), Pooka.getProperty("Pooka.crypto.publicKey.title", "Select public key"));
	if (cryptKey != null) {
	  if (csd instanceof NewMessageCryptoDisplay) {
	    ((NewMessageCryptoDisplay) csd).setEncryptionKey(cryptKey);
	  }
	}
      } catch (Exception ex) {
	getMessageUI().showError(ex.getMessage(), ex);
      }
    }
  }
  
  class ClearEncryptAction extends AbstractAction {
    ClearEncryptAction() {
      super("message-clear-encrypt");
    }
    
    public void actionPerformed(ActionEvent e) {
      CryptoStatusDisplay csd = getMessageUI().getCryptoStatusDisplay();
      if (csd instanceof NewMessageCryptoDisplay) {
	((NewMessageCryptoDisplay) csd).setEncryptMessage(NewMessageCryptoInfo.CRYPTO_NO);	((NewMessageCryptoDisplay) csd).setEncryptionKey(null);
      }
    }
  }
  
  class ClearSignAction extends AbstractAction {
    ClearSignAction() {
      super("message-clear-signature");
    }
    
    public void actionPerformed(ActionEvent e) {
      CryptoStatusDisplay csd = getMessageUI().getCryptoStatusDisplay();
      
      if (csd instanceof NewMessageCryptoDisplay) {
	((NewMessageCryptoDisplay) csd).setSignMessage(NewMessageCryptoInfo.CRYPTO_NO);
	((NewMessageCryptoDisplay) csd).setSignatureKey(null);
      }
    }
  }

  class AttachKeyAction extends AbstractAction {
    AttachKeyAction() {
      super("message-attach-crypt-key");
    }
    
    public void actionPerformed(ActionEvent e) {
      CryptoStatusDisplay csd = getMessageUI().getCryptoStatusDisplay();
      
      if (csd instanceof NewMessageCryptoDisplay) {
	try {
	  java.security.Key cryptKey = selectPublicKey(Pooka.getProperty("Pooka.crypto.publicKey.forAttach", "Select key to attach to message."), Pooka.getProperty("Pooka.crypto.publicKey.title", "Select public key"));
	  ((NewMessageCryptoDisplay) csd).attachEncryptionKey(cryptKey);
	} catch (Exception ex) {
	  getMessageUI().showError(ex.getMessage(), ex);
	}
      }
    }
  }

  class RemoveKeyAction extends AbstractAction {
    RemoveKeyAction() {
      super("message-remove-crypt-key");
    }
    
    public void actionPerformed(ActionEvent e) {
      CryptoStatusDisplay csd = getMessageUI().getCryptoStatusDisplay();
      
      if (csd instanceof NewMessageCryptoDisplay) {
	try {
	  java.security.Key cryptKey = selectPublicKey(Pooka.getProperty("Pooka.crypto.publicKey.forRemove", "Select key to remove from message."), Pooka.getProperty("Pooka.crypto.publicKey.title", "Remove public key"));
	  ((NewMessageCryptoDisplay) csd).removeEncryptionKey(cryptKey);
	} catch (Exception ex) {
	  getMessageUI().showError(ex.getMessage(), ex);
	}
      }
    }
  }
    
  }






