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

/**
 * This class represents a new message that is being written.
 */
public class NewMessageProxy extends MessageProxy {
  Hashtable commands;

  private static Vector allUnsentProxies = new Vector();
  
  public NewMessageProxy(NewMessageInfo newMessage) {
    messageInfo = newMessage;
    messageInfo.setMessageProxy(this);

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
    if (getNewMessageUI() != null) { 
      getNewMessageUI().setBusy(true);
      try {
	UserProfile profile = getNewMessageUI().getSelectedProfile();
	InternetHeaders headers = getNewMessageUI().getMessageHeaders();
	
	String messageText = getNewMessageUI().getMessageText();
	
	String messageContentType = getNewMessageUI().getMessageContentType();
	getNewMessageInfo().sendMessage(profile, headers, messageText, messageContentType);
	getNewMessageInfo().saveToSentFolder(profile);
	
      } catch (MessagingException me) {
	getMessageUI().showError(Pooka.getProperty("Error.sendingMessage", "Error sending message:  "), me);
      }
    }
  }

  /**
   * Called when the send succeeds.
   */
  public void sendSucceeded() {
    final NewMessageUI nmui = getNewMessageUI();
    if (nmui != null) {
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
  public void sendFailed(Exception e) {
    final Exception me = e;
    final NewMessageUI nmui = getNewMessageUI();
    if (nmui != null) {
      Runnable runMe = new Runnable() {
	  public void run() {
	    if (me instanceof SendFailedException) {
	      getMessageUI().showError(Pooka.getProperty("error.MessageUI.sendFailed", "Failed to send Message.") + "\n" + me.getMessage());
	      me.printStackTrace(System.out);
	    } else {
	      getMessageUI().showError(Pooka.getProperty("error.MessageUI.sendFailed", "Failed to send Message.") + "\n" + me.getMessage());
	      me.printStackTrace(System.out);
	    }
	    nmui.setBusy(false);
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
    new SelectEncryptionKeyAction(),
    new SignAction(),
    new SelectSignatureKeyAction()
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
      getNewMessageInfo().setEncryptMessage(NewMessageInfo.CRYPTO_YES);
    }
  }

  class SignAction extends AbstractAction {
    SignAction() {
      super("message-sign");
    }
    
    public void actionPerformed(ActionEvent e) {
      getNewMessageInfo().setSignMessage(NewMessageInfo.CRYPTO_YES);
    }
  }

  class SelectSignatureKeyAction extends AbstractAction {
    SelectSignatureKeyAction() {
      super("message-select-sig-key");
    }
    
    public void actionPerformed(ActionEvent e) {
      selectPublicKey();
    }
  }

  class SelectEncryptionKeyAction extends AbstractAction {
    SelectEncryptionKeyAction() {
      super("message-select-crypt-key");
    }
    
    public void actionPerformed(ActionEvent e) {
      selectPrivateKey();
    }
  }
  
}






