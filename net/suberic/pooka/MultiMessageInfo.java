package net.suberic.pooka;
import javax.mail.*;

/**
 * This represents a bundle of MessageInfos.
 */
public class MultiMessageInfo extends MessageInfo {
  
  MessageInfo[] messages;
  
  /**
   * Creates a new MultiMessageInfo for the given newMessageInfos.
   */
  public MultiMessageInfo(MessageInfo[] newMessageInfos) {
    messages = newMessageInfos;
  }

  /**
   * Creates a new MultiMessageInfo for the given newMessageInfos,
   * where all the given MessageInfos are from the FolderInfo
   * newFolder.
   */
  public MultiMessageInfo(MessageInfo[] newMessageInfos, FolderInfo newFolder) {
    messages = newMessageInfos;
    folderInfo = newFolder;
  }
  
  
  /**
   * This implementation just throws an exception, since this is not
   * allowed on multiple messages.
   *
   * @overrides flagIsSet in MessageInfo
   */
  public boolean flagIsSet(String flagName) throws MessagingException {
    throw new MessagingException(Pooka.getProperty("error.MultiMessage.operationNotAllowed", "This operation is not allowed on multiple messages."));
  }

  /**
   * This implementation just throws an exception, since this is not
   * allowed on multiple messages.
   *
   * @overrides getFlags() in MessageInfo
   */
  public Flags getFlags() throws MessagingException {
    throw new MessagingException(Pooka.getProperty("error.MultiMessage.operationNotAllowed", "This operation is not allowed on multiple messages."));
  }
  
  /**
   * This implementation just throws an exception, since this is not
   * allowed on multiple messages.
   *
   * @overrides getMessageProperty() in MessageInfo
   */
  public Object getMessageProperty(String prop) throws MessagingException {
    throw new MessagingException(Pooka.getProperty("error.MultiMessage.operationNotAllowed", "This operation is not allowed on multiple messages."));
  }
  
  /**
   * Moves the Message into the target Folder.
   */
  public void moveMessage(FolderInfo targetFolder, boolean expunge) throws MessagingException {
    if (folderInfo != null) {
      folderInfo.copyMessages(messages, targetFolder);
      folderInfo.setFlags(messages, new Flags(Flags.Flag.DELETED), true);
      if (expunge)
	folderInfo.expunge();
    } else {
      for (int i = 0; i < messages.length; i++)
	messages[i].moveMessage(targetFolder, expunge);
    }
  }
  
  /**
   * Copies the Message into the target Folder.
   */
  public void copyMessage(FolderInfo targetFolder) throws MessagingException {
    if (folderInfo != null) {
      folderInfo.copyMessages(messages, targetFolder);
    } else {
      for (int i = 0; i < messages.length; i++)
	messages[i].copyMessage(targetFolder);
    }
  }
  
  /**
   * deletes all the messages in the MultiMessageInfo.
   */
  public void deleteMessage(boolean expunge) throws MessagingException {
    if (folderInfo != null) {
      FolderInfo trashFolder = folderInfo.getTrashFolder();
      if ((folderInfo.useTrashFolder()) && (trashFolder != null) && (trashFolder != folderInfo)) {
	try {
	  moveMessage(trashFolder, expunge);
	} catch (MessagingException me) {
	  throw new NoTrashFolderException(Pooka.getProperty("error.Messsage.DeleteNoTrashFolder", "No trash folder available."),  me);
	}
      } else {
	remove(expunge);
      }
    } else {
      for (int i = 0; i < messages.length; i++)
	messages[i].deleteMessage(expunge);
      
    }
  }
  
  /**
   * This actually marks the message as deleted, and, if autoexpunge is
   * set to true, expunges the folder.
   *
   * This should not be called directly; rather, deleteMessage() should
   * be used in order to ensure that the delete is done properly (using
   * trash folders, for instance).  If, however, the deleteMessage() 
   * throws an Exception, it may be necessary to follow up with a call
   * to remove().
   */
  public void remove(boolean autoExpunge) throws MessagingException {
    if (folderInfo != null) {
      folderInfo.setFlags(messages, new Flags(Flags.Flag.DELETED), true);
      if (autoExpunge)
	folderInfo.expunge();
    } else {
      for (int i = 0; i < messages.length; i++)
	messages[i].remove(autoExpunge);
    }
    
  }
  
  /**
   *  Caches the current messages.
   */
  public void cacheMessage() throws MessagingException {

    net.suberic.util.swing.ProgressDialog pd = Pooka.getUIFactory().createProgressDialog(0, messages.length, 0, Pooka.getProperty("message.cachingMessages", "Caching Messages..."), Pooka.getProperty("message.cachingMessages", "Caching Messages..."));
    
    pd.show();
    try {
      for (int i = messages.length - 1; i >= 0 && ! pd.isCancelled(); i--) {
	FolderInfo fi = messages[i].getFolderInfo();
	if (fi != null && fi instanceof net.suberic.pooka.cache.CachingFolderInfo) {
	  ((net.suberic.pooka.cache.CachingFolderInfo) fi).cacheMessage(messages[i], net.suberic.pooka.cache.MessageCache.MESSAGE);
	  
	}
	pd.setValue(messages.length - i -1);
      }
    } finally {
      pd.dispose();
    }

  }
  
  /**
   * This returns the MessageInfo at the given index.
   */
  public MessageInfo getMessageInfo(int index) {
    return messages[index];
  }
  
  /**
   * This returns the number of Messages wrapped by the MultiMessageInfo.
   */
  public int getMessageCount() {
    return messages.length;
  }
  
}



