package net.suberic.pooka.cache;
import javax.mail.*;
import javax.mail.event.MessageCountEvent;
import javax.mail.internet.MimeMessage;
import javax.mail.event.MessageChangedEvent;
import javax.mail.event.ConnectionEvent;
import java.util.Vector;
import java.util.StringTokenizer;
import java.util.HashMap;
import java.io.File;
import net.suberic.pooka.*;
import net.suberic.pooka.gui.MessageProxy;
import net.suberic.pooka.gui.FolderTableModel;

/**
 * A FolderInfo which caches its messages in a MessageCache.
 * @author Allen Petersen
 * @version $Revision$
 */

public class CachingFolderInfo extends net.suberic.pooka.UIDFolderInfo {
  private MessageCache cache = null;
  
  // the resource for the folder disconnected message
  protected static String disconnectedMessage = "error.CachingFolder.disconnected";
  
  boolean autoCache = false;

  public CachingFolderInfo(StoreInfo parent, String fname) {
    super(parent, fname);
    
    autoCache =  Pooka.getProperty(getFolderProperty() + ".autoCache", Pooka.getProperty(getParentStore().getStoreProperty() + ".autoCache", Pooka.getProperty("Pooka.autoCache", "false"))).equalsIgnoreCase("true");
  }
  
  public CachingFolderInfo(FolderInfo parent, String fname) {
    super(parent, fname);
    
    autoCache =  Pooka.getProperty(getFolderProperty() + ".autoCache", Pooka.getProperty(getParentStore().getStoreProperty() + ".autoCache", Pooka.getProperty("Pooka.autoCache", "false"))).equalsIgnoreCase("true");
  }
  
  /**
   * This actually loads up the Folder object itself.  This is used so 
   * that we can have a FolderInfo even if we're not connected to the
   * parent Store.
   *
   * Before we load the folder, the FolderInfo has the state of NOT_LOADED.
   * Once the parent store is connected, we can try to load the folder.  
   * If the load is successful, we go to a CLOSED state.  If it isn't,
   * then we can either return to NOT_LOADED, or INVALID.
   */
  public void loadFolder() {
    if (cache == null) {
      try {
	this.cache = new SimpleFileCache(this, getCacheDirectory());
	type =  type | Folder.HOLDS_MESSAGES;
	setStatus(DISCONNECTED);
      } catch (java.io.IOException ioe) {
	System.out.println("Error creating cache!");
	ioe.printStackTrace();
	return;
      }
    }
    
    if (isLoaded() || (loading && children == null)) 
      return;
    
    Folder[] tmpFolder;
    Folder tmpParentFolder;
    
    try {
      loading = true;
      if (getParentStore().isConnected()) {
	if (getParentFolder() == null) {
	  try {
	    if (Pooka.isDebug())
	      System.out.println(Thread.currentThread() + "loading folder " + getFolderID() + ":  checking parent store connection.");
	    
	    Store store = getParentStore().getStore();
	    tmpParentFolder = store.getDefaultFolder();
	    tmpFolder = tmpParentFolder.list(getFolderName());
	  } catch (MessagingException me) {
	    if (Pooka.isDebug()) {
	      System.out.println(Thread.currentThread() + "loading folder " + getFolderID() + ":  caught messaging exception from parentStore getting folder: " + me);
	      me.printStackTrace();
	    }
	    tmpFolder =null;
	  }
	} else {
	  if (!getParentFolder().isLoaded())
	    getParentFolder().loadFolder();
	  if (!getParentFolder().isLoaded()) {
	    tmpFolder = null;
	  } else {
	    tmpParentFolder = getParentFolder().getFolder();
	    if (tmpParentFolder != null) {
	      tmpFolder = tmpParentFolder.list(getFolderName());
	    } else {
	      tmpFolder = null;
	    }
	  }
	}
	if (tmpFolder != null && tmpFolder.length > 0) {
	  setFolder(tmpFolder[0]);
	  setStatus(CLOSED);
	  getFolder().addMessageChangedListener(this);
	} else {
	  if (cache != null)
	    setStatus(CACHE_ONLY);
	  else
	    setStatus(INVALID);
	  setFolder(new FolderProxy(getFolderName()));
	}
      } else {
	setFolder(new FolderProxy(getFolderName()));
      }
    } catch (MessagingException me) {
      if (Pooka.isDebug()) {
	System.out.println(Thread.currentThread() + "loading folder " + getFolderID() + ":  caught messaging exception; setting loaded to false:  " + me.getMessage() );
	me.printStackTrace();
      }
      setStatus(NOT_LOADED);
      setFolder(new FolderProxy(getFolderName()));
    } finally {
      initializeFolderInfo();
      loading = false;
    }
  }

  /**
   * called when the folder is opened.
   */
  public void opened (ConnectionEvent e) { 
    super.opened(e);
    rematchFilters();
  }

  /**
   * This method opens the Folder, and sets the FolderInfo to know that
   * the Folder should be open.  You should use this method instead of
   * calling getFolder().open(), because if you use this method, then
   * the FolderInfo will try to keep the Folder open, and will try to
   * reopen the Folder if it gets closed before closeFolder is called.
   *
   * This method can also be used to reset the mode of an already 
   * opened folder.
   */
  public void openFolder(int mode) throws MessagingException {
    try {
      if (Pooka.isDebug())
	System.out.println(this + ":  checking parent store.");
      
      
      if (!getParentStore().isConnected()) {
	if (Pooka.isDebug())
	  System.out.println(this + ":  parent store isn't connected.  trying connection.");
	getParentStore().connectStore();
      }
      
      if (Pooka.isDebug())
	System.out.println(this + ":  loading folder.");
      
      if (! isLoaded() && status != CACHE_ONLY)
	loadFolder();
      
      if (Pooka.isDebug())
	System.out.println(this + ":  folder loaded.  status is " + status);
      
      if (Pooka.isDebug())
	System.out.println(this + ":  checked on parent store.  trying isLoaded() and isAvailable().");
      
      if (status == CLOSED || status == LOST_CONNECTION || status == DISCONNECTED) {
	if (Pooka.isDebug())
	  System.out.println(this + ":  isLoaded() and isAvailable().");
	if (getFolder().isOpen()) {
	  if (getFolder().getMode() == mode)
	    return;
	  else { 
	    getFolder().close(false);
	    openFolder(mode);
	  }
	} else {
	  Folder f = getFolder();
	  getFolder().open(mode);
	  updateFolderOpenStatus(true);
	  resetMessageCounts();
	}
      } else if (status == INVALID) {
	throw new MessagingException(Pooka.getProperty("error.folderInvalid", "Error:  folder is invalid.  ") + getFolderID());
      }
    } catch (MessagingException me) {
      //System.err.println("error:  " + me);
      //me.printStackTrace();
      setStatus(DISCONNECTED);
      throw me;
    } finally {
      resetMessageCounts();
    }
  }
  
  
  /**
   * Called when the store in disconnected.
   */
  public void disconnected(ConnectionEvent e) {
    super.disconnected(e);
    rematchFilters();
  }

  /**
   * Called when the folder is closed.
   */
  public void closed(ConnectionEvent e) {
    super.closed(e);
    rematchFilters();
  }

  /**
   * gets all of the message proxies associated with this folder info
   * and notifies them that they need to rematch their filters.
   */
  protected void rematchFilters() {
    if (folderTableModel != null) {
      Vector allProxies = folderTableModel.getAllProxies();
      for (int i = 0; i < allProxies.size(); i++) {
	((MessageProxy) allProxies.get(i)).clearMatchedFilters();
      }
      loaderThread.loadMessages(allProxies);
    }
  }

  /**
   * Loads all Messages into a new FolderTableModel, sets this 
   * FolderTableModel as the current FolderTableModel, and then returns
   * said FolderTableModel.  This is the basic way to populate a new
   * FolderTableModel.
   */
  public synchronized void loadAllMessages() throws MessagingException {
    if (folderTableModel == null) {
      if (getFolderDisplayUI() != null) {
	getFolderDisplayUI().setBusy(true);
	getFolderDisplayUI().showStatusMessage(Pooka.getProperty("messages.CachingFolder.loading.starting", "Loading messages."));
      }
      
      if (!isLoaded())
	loadFolder();

      Vector messageProxies = new Vector();
      
      fetchProfile = createColumnInformation();
      if (loaderThread == null) 
	loaderThread = createLoaderThread();
      
      try {
		
	if (preferredStatus < DISCONNECTED && !(isConnected() && getParentStore().getConnection().getStatus() == NetworkConnection.CONNECTED )) {
	  try {
	    openFolder(Folder.READ_WRITE);
	  } catch (MessagingException me) {
	    uidValidity = cache.getUIDValidity();
	    preferredStatus = DISCONNECTED;
	  }
	}

	if (getStatus() > CONNECTED) {
	  uidValidity = cache.getUIDValidity();
	}
	
	if (isConnected()) {
	  try {
	    // load the list of uid's.
	    
	    FetchProfile uidFetchProfile = new FetchProfile();
	    uidFetchProfile.add(UIDFolder.FetchProfileItem.UID);
	    if (Pooka.isDebug())
	      System.out.println("getting messages.");
	    
	    Message[] messages = getFolder().getMessages();
	    if (Pooka.isDebug())
	      System.out.println("fetching messages.");
	    getFolder().fetch(messages, uidFetchProfile);
	    if (Pooka.isDebug())
	      System.out.println("done fetching messages.  getting uid's");
	    
	    long[] uids = new long[messages.length];
	    
	    for (int i = 0; i < messages.length; i++) {
	      uids[i] = getUID(messages[i]);
	    }
      
	    MessageInfo mi;
	    
	    for (int i = 0; i < uids.length; i++) {
	      Message m = new CachingMimeMessage(this, uids[i]);
	      mi = new MessageInfo(m, this);
	      
	      messageProxies.add(new MessageProxy(getColumnValues() , mi));
	      messageToInfoTable.put(m, mi);
	      uidToInfoTable.put(new Long(uids[i]), mi);
	    }
	    
	    FolderTableModel ftm = new FolderTableModel(messageProxies, getColumnNames(), getColumnSizes(), getColumnValues());
	    
	    setFolderTableModel(ftm);
	    
	    synchronizeCache();
	  } catch (Exception e) {
	    final Exception fe = e;
	    javax.swing.SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		  if (getFolderDisplayUI() != null) {
		    getFolderDisplayUI().showError(Pooka.getProperty("error.CachingFolder.synchronzing", "Error synchronizing with folder"), Pooka.getProperty("error.CachingFolder.synchronzing.title", "Error synchronizing with folder"), fe);
		  } else {
		    Pooka.getUIFactory().showError(Pooka.getProperty("error.CachingFolder.synchronzing", "Error synchronizing with folder"), Pooka.getProperty("error.CachingFolder.synchronzing.title", "Error synchronizing with folder"), fe);
		    
		  }
		}
	      });
	  }
	} else {
	  long[] uids = cache.getMessageUids();
	  MessageInfo mi;
	  
	  for (int i = 0; i < uids.length; i++) {
	    Message m = new CachingMimeMessage(this, uids[i]);
	    mi = new MessageInfo(m, this);
	    MessageProxy mp = new MessageProxy(getColumnValues() , mi);
	    mp.setRefresh(true);
	    messageProxies.add(mp);
	    messageToInfoTable.put(m, mi);
	    uidToInfoTable.put(new Long(uids[i]), mi);
	  }
	  
	  FolderTableModel ftm = new FolderTableModel(messageProxies, getColumnNames(), getColumnSizes(), getColumnValues());
	  
	  setFolderTableModel(ftm);
	  
	  
	}
	
	Vector loadImmediately = null;
	
	if (messageProxies.size() > 25) {
	  loadImmediately = new Vector();
	  for (int i = messageProxies.size() - 1; i > messageProxies.size() - 26; i--) {
	    loadImmediately.add(messageProxies.get(i));
	  }
	} else {
	  loadImmediately = new Vector(messageProxies);
	}
	
	loadMessageTableInfos(loadImmediately);
	
	loaderThread.loadMessages(messageProxies);
	
	if (!loaderThread.isAlive())
	  loaderThread.start();
	
      } finally {
	if (getFolderDisplayUI() != null) {
	  getFolderDisplayUI().setBusy(false);
	  getFolderDisplayUI().showStatusMessage(Pooka.getProperty("messages.CachingFolder.loading.finished", "Done loading messages."));
	}
	
      }
    }
  }
    
  /**
   * Fetches the information for the given messages using the given
   * FetchProfile.
   */
  public void fetch(MessageInfo[] messages, FetchProfile profile) throws MessagingException {
    // if we're connected, go ahead and fetch these.  why the hell not?

    int cacheStatus = -1;
    boolean doFlags = profile.contains(FetchProfile.Item.FLAGS);
    boolean doHeaders = (profile.contains(FetchProfile.Item.ENVELOPE) || profile.contains(FetchProfile.Item.CONTENT_INFO));
    
    if (doFlags && doHeaders) {
      cacheStatus = SimpleFileCache.FLAGS_AND_HEADERS;
    } else if (doFlags) {
      cacheStatus = SimpleFileCache.FLAGS;
    } else if (doHeaders) {
      cacheStatus = SimpleFileCache.HEADERS;
    }
    
    if (isConnected()) {
      super.fetch(messages, profile);
      
      if (cacheStatus != -1) {
	for (int i = 0; i < messages.length; i++) {
	  Message m = messages[i].getRealMessage();
	  if (m != null) {
	    long uid = getUID(m);
	    getCache().cacheMessage((MimeMessage)m, uid, cache.getUIDValidity(), cacheStatus);	
	  }
	}
      }
    } else {
      // if we're not connected, then go ahead and preload the cache for
      // these.
      for (int i = 0; i < messages.length; i++) {
	Message current = messages[i].getMessage();
	if (current != null && current instanceof UIDMimeMessage) {
	  long uid = ((UIDMimeMessage) current).getUID();
	  if (cacheStatus == SimpleFileCache.FLAGS_AND_HEADERS || cacheStatus == SimpleFileCache.FLAGS) {
	    getCache().getFlags(uid, cache.getUIDValidity());
	  }
	  
	  if (cacheStatus == SimpleFileCache.FLAGS_AND_HEADERS || cacheStatus == SimpleFileCache.HEADERS) {
	    getCache().getHeaders(uid, cache.getUIDValidity());
	  }
	}

	messages[i].setFetched(true);
      }
    }

  }
  
  /**
   * Refreshes the headers for the given MessageInfo.
   */
  public void refreshHeaders(MessageInfo mi) throws MessagingException {
    cacheMessage(mi, SimpleFileCache.HEADERS);
  }

  /**
   * Refreshes the flags for the given MessageInfo.
   */
  public void refreshFlags(MessageInfo mi) throws MessagingException {
    if (isConnected())
      cacheMessage(mi, SimpleFileCache.FLAGS);
  }
  
  
  /**
   * Gets the row number of the first unread message.  Returns -1 if
   * there are no unread messages, or if the FolderTableModel is not
   * set or empty.
   */
  
  public int getFirstUnreadMessage() {
    // one part brute, one part force, one part ignorance.
    
    if (Pooka.isDebug())
      System.out.println("getting first unread message");
    
    if (! tracksUnreadMessages()) 
      return -1;
    
    if (getFolderTableModel() == null)
      return -1;
    
    if (isConnected()) {
      return super.getFirstUnreadMessage();
    } else {
      try {
	int countUnread = 0;
	int i;
	
	int unreadCount = cache.getUnreadMessageCount();
	
	if (unreadCount > 0) {
	  long[] uids = getCache().getMessageUids();
	  
	  for (i = uids.length - 1; ( i >= 0 && countUnread < unreadCount) ; i--) {
	    if (!(getMessageInfoByUid(uids[i]).getFlags().contains(Flags.Flag.SEEN))) 
	      countUnread++;
	  }
	  if (Pooka.isDebug())
	    System.out.println("Returning " + i);
	  
	  return i + 1;
	} else { 
	  if (Pooka.isDebug())
	    System.out.println("Returning -1");
	return -1;
	}
      } catch (MessagingException me) {
	if (Pooka.isDebug())
	  System.out.println("Messaging Exception.  Returning -1");
	return -1;
      }
    }
    
  }

  public boolean hasUnread() {
    if (! tracksUnreadMessages()) 
      return false;
    else
      return (getUnreadCount() > 0);
  }

  /*
  public int getUnreadCount() {
    if (! tracksUnreadMessages()) 
      return -1;
    else {
      try {
        if (getCache() != null) 
	  unreadCount = getCache().getUnreadMessageCount();
      } catch (MessagingException me) {
      
      } 
      return unreadCount;
    }
  }
  
  public int getMessageCount() {
    try {
      if (getCache() != null) 
	messageCount = getCache().getMessageCount();
    } catch (MessagingException me) {
    }
    return messageCount;
  }
  */

  /**
   * This forces an update of both the total and unread message counts.
   */
  public void resetMessageCounts() {
    try {
      if (Pooka.isDebug()) {
	if (getFolder() != null)
	  System.out.println("running resetMessageCounts.  unread message count is " + getFolder().getUnreadMessageCount());
	else
	  System.out.println("running resetMessageCounts.  getFolder() is null.");
      }

      if (isConnected()) {
	if (tracksUnreadMessages()) 
	  unreadCount = getFolder().getUnreadMessageCount();
	messageCount = getFolder().getMessageCount();
      } else if (getCache() != null) { 
	messageCount = getCache().getMessageCount();
	if (tracksUnreadMessages()) 
	  unreadCount = getCache().getUnreadMessageCount();
      } else {
	// if there's no cache and no connection, don't do anything.
      }
    } catch (MessagingException me) {
      // if we lose the connection to the folder, we'll leave the old
      // messageCount and set the unreadCount to zero.
      unreadCount = 0;
    }

  }

  /**
   * This synchronizes the cache with the new information from the 
   * Folder.
   */
  public void synchronizeCache() throws MessagingException {
    
    if (Pooka.isDebug())
      System.out.println("synchronizing cache.");
    
    try {
      if (getFolderDisplayUI() != null) {
	getFolderDisplayUI().showStatusMessage(Pooka.getProperty("message.UIDFolder.synchronizing", "Re-synchronizing with folder..."));
	getFolderDisplayUI().setBusy(true);
      } else {
	Pooka.getUIFactory().showStatusMessage(Pooka.getProperty("message.UIDFolder.synchronizing", "Re-synchronizing with folder..."));
      }

      long cacheUidValidity = getCache().getUIDValidity();
      
      if (uidValidity != cacheUidValidity) {
	if (getFolderDisplayUI() != null)
	  getFolderDisplayUI().showStatusMessage(Pooka.getProperty("error.UIDFolder.validityMismatch", "Error:  validity not correct.  reloading..."));
	else 
	  Pooka.getUIFactory().showStatusMessage(Pooka.getProperty("error.UIDFolder.validityMismatch", "Error:  validity not correct.  reloading..."));
	
	getCache().invalidateCache();
	getCache().setUIDValidity(uidValidity);
	cacheUidValidity = uidValidity;
      }
      
      if (getFolderDisplayUI() != null)
	getFolderDisplayUI().showStatusMessage(Pooka.getProperty("message.CachingFolder.synchronizing.writingChanges", "Writing local changes to server..."));
      else
	Pooka.getUIFactory().showStatusMessage(Pooka.getProperty("message.CachingFolder.synchronizing.writingChanges", "Writing local changes to server..."));
      
      // first write all the changes that we made back to the server.
      getCache().writeChangesToServer(getFolder());
      
      if (getFolderDisplayUI() != null)
	getFolderDisplayUI().showStatusMessage(Pooka.getProperty("message.UIDFolder.synchronizing.loading", "Loading messages from folder..."));
      else
	Pooka.getUIFactory().showStatusMessage(Pooka.getProperty("message.UIDFolder.synchronizing.loading", "Loading messages from folder..."));
      
      // load the list of uid's.

      FetchProfile fp = new FetchProfile();
      fp.add(UIDFolder.FetchProfileItem.UID);
      if (Pooka.isDebug())
	System.out.println("getting messages.");

      Message[] messages = getFolder().getMessages();

      if (Pooka.isDebug())
	System.out.println("fetching messages.");
      getFolder().fetch(messages, fp);
      if (Pooka.isDebug())
	System.out.println("done fetching messages.  getting uid's");
      
      long[] uids = new long[messages.length];
      
      for (int i = 0; i < messages.length; i++) {
	uids[i] = getUID(messages[i]);
      }
      
      if (Pooka.isDebug())
	System.out.println("synchronizing--uids.length = " + uids.length);
      
      if (getFolderDisplayUI() != null)
	getFolderDisplayUI().showStatusMessage(Pooka.getProperty("message.UIDFolder.synchronizing", "Comparing new messages to current list..."));
      else
	Pooka.getUIFactory().showStatusMessage(Pooka.getProperty("message.UIDFolder.synchronizing", "Comparing new messages to current list..."));
      
      long[] addedUids = cache.getAddedMessages(uids, uidValidity);

      if (Pooka.isDebug())
	System.out.println("synchronizing--addedUids.length = " + addedUids.length);
      
      if (addedUids.length > 0) {
	Message[] addedMsgs = ((UIDFolder)getFolder()).getMessagesByUID(addedUids);
	MessageCountEvent mce = new MessageCountEvent(getFolder(), MessageCountEvent.ADDED, false, addedMsgs);
	messagesAdded(mce);
      }    
      
      long[] removedUids = cache.getRemovedMessages(uids, uidValidity);
      if (Pooka.isDebug())
	System.out.println("synchronizing--removedUids.length = " + removedUids.length);
      
      if (removedUids.length > 0) {
	Message[] removedMsgs = new Message[removedUids.length];
	for (int i = 0 ; i < removedUids.length; i++) {
	  MessageInfo mi =  getMessageInfoByUid(removedUids[i]);
	  if (mi != null)
	    removedMsgs[i] = mi.getRealMessage();
	  
	  if (removedMsgs[i] == null) {
	    removedMsgs[i] = new CachingMimeMessage(this, removedUids[i]);
	  }
	}
	MessageCountEvent mce = new MessageCountEvent(getFolder(), MessageCountEvent.REMOVED, false, removedMsgs);
	messagesRemoved(mce);
      }
      
      if (getFolderDisplayUI() != null)
	getFolderDisplayUI().showStatusMessage(Pooka.getProperty("message.UIDFolder.updatingFlags", "Updating flags..."));
      else
	Pooka.getUIFactory().showStatusMessage(Pooka.getProperty("message.UIDFolder.updatingFlags", "Updating flags..."));
      updateFlags(uids, messages, cacheUidValidity);
      
    } finally {
      if (getFolderDisplayUI() != null) {
	getFolderDisplayUI().clearStatusMessage();
	getFolderDisplayUI().setBusy(false);
      } else
	Pooka.getUIFactory().clearStatus();
    }
    
  }
  
  protected void runMessagesAdded(MessageCountEvent mce) {
    if (folderTableModel != null) {
      Message[] addedMessages = mce.getMessages();

      MessageInfo mi;
      Vector addedProxies = new Vector();
      for (int i = 0; i < addedMessages.length; i++) {
	if (addedMessages[i] instanceof CachingMimeMessage) {
	  long uid = ((CachingMimeMessage) addedMessages[i]).getUID();
	  if (getMessageInfoByUid(uid) != null) {
	    if (Pooka.isDebug())
	      System.out.println(getFolderID() + ":  this is a duplicate.  not making a new messageinfo for it.");
	  } else {
	    mi = new MessageInfo(addedMessages[i], CachingFolderInfo.this);
	    addedProxies.add(new MessageProxy(getColumnValues(), mi));
	    messageToInfoTable.put(addedMessages[i], mi);
	    uidToInfoTable.put(new Long(((CachingMimeMessage) addedMessages[i]).getUID()), mi);
	    try {
	      if (autoCache) {
		getCache().cacheMessage((MimeMessage)addedMessages[i], ((CachingMimeMessage)addedMessages[i]).getUID(), getUIDValidity(), SimpleFileCache.MESSAGE, false);
	      } else {
		getCache().cacheMessage((MimeMessage)addedMessages[i], ((CachingMimeMessage)addedMessages[i]).getUID(), getUIDValidity(), SimpleFileCache.FLAGS_AND_HEADERS, false);
	      }
	    } catch (MessagingException me) {
	      System.out.println("caught exception:  " + me);
	      me.printStackTrace();
	    }
	  }
	} else {
	  // it's a 'real' message from the server.
	  
	  long uid = -1;
	  try {
	    uid = getUID(addedMessages[i]);
	  } catch (MessagingException me) {
	  }

	  if (getMessageInfoByUid(uid) != null) {
	    if (Pooka.isDebug())
	      System.out.println(getFolderID() + ":  this is a duplicate.  not making a new messageinfo for it.");
	  } else {
	    
	  
	    CachingMimeMessage newMsg = new CachingMimeMessage(CachingFolderInfo.this, uid);
	    mi = new MessageInfo(newMsg, CachingFolderInfo.this);
	    addedProxies.add(new MessageProxy(getColumnValues(), mi));
	    messageToInfoTable.put(newMsg, mi);
	    uidToInfoTable.put(new Long(uid), mi);
	    try {
	      if (autoCache) {
		getCache().cacheMessage((MimeMessage)addedMessages[i], uid, getUIDValidity(), SimpleFileCache.MESSAGE, false);
	      } else {
		getCache().cacheMessage((MimeMessage)addedMessages[i], uid, getUIDValidity(), SimpleFileCache.FLAGS_AND_HEADERS, false);
	      }
	    } catch (MessagingException me) {
	      System.out.println("caught exception:  " + me);
	      me.printStackTrace();
	    }
	  }
	}
      }

      getCache().writeMsgFile();

      addedProxies.removeAll(applyFilters(addedProxies));
      if (addedProxies.size() > 0) {
	if (getFolderTableModel() != null) 
	  getFolderTableModel().addRows(addedProxies);
	setNewMessages(true);
	resetMessageCounts();

	// notify the message loaded thread.
	MessageProxy[] addedArray = (MessageProxy[]) addedProxies.toArray(new MessageProxy[0]);
	loaderThread.loadMessages(addedArray);

	// change the Message objects in the MessageCountEvent to 
	// our UIDMimeMessages.
	Message[] newMsgs = new Message[addedProxies.size()];
	for (int i = 0; i < addedProxies.size(); i++) {
	  newMsgs[i] = ((MessageProxy)addedProxies.elementAt(i)).getMessageInfo().getMessage();
	}
	MessageCountEvent newMce = new MessageCountEvent(getFolder(), mce.getType(), mce.isRemoved(), newMsgs);
	fireMessageCountEvent(newMce);
      }
      
    }
  }
  
 /**
   * This does the real work when messages are removed.
   *
   * This method should always be run on the FolderThread.
   */
  protected void runMessagesRemoved(MessageCountEvent mce) {
    Message[] removedMessages = mce.getMessages();
    Message[] removedCachingMessages = new Message[removedMessages.length];
    
    if (Pooka.isDebug())
      System.out.println("removedMessages was of size " + removedMessages.length);
    MessageInfo mi;
    Vector removedProxies=new Vector();
    
    for (int i = 0; i < removedMessages.length; i++) {
      if (Pooka.isDebug())
	System.out.println("checking for existence of message.");
      
      if (removedMessages[i] != null && removedMessages[i] instanceof CachingMimeMessage) {
	removedCachingMessages[i] = removedMessages[i];
	mi = getMessageInfo(removedMessages[i]);
	
	if (mi != null) {
	  if (Pooka.isDebug())
	    System.out.println("message exists--removing");
	  if ( mi.getMessageProxy() != null) {
	    mi.getMessageProxy().close();
	    removedProxies.add(mi.getMessageProxy());
	  }
	  messageToInfoTable.remove(mi);
	  uidToInfoTable.remove(new Long(((CachingMimeMessage) removedMessages[i]).getUID()));
	}

	getCache().invalidateCache(((CachingMimeMessage) removedMessages[i]).getUID(), SimpleFileCache.MESSAGE);
	
      } else {
	// not a CachingMimeMessage.
	long uid = -1;
	try {
	  uid = getUID(removedMessages[i]);
	} catch (MessagingException me) {
	  
	}
	
	mi = getMessageInfoByUid(uid);
	if (mi != null) {
	  removedCachingMessages[i] = mi.getMessage();
	  if (mi.getMessageProxy() != null)
	    mi.getMessageProxy().close();
	  
	  if (Pooka.isDebug())
	    System.out.println("message exists--removing");
	  
	  Message localMsg = mi.getMessage();
	  removedProxies.add(mi.getMessageProxy());
	  messageToInfoTable.remove(localMsg);
	  uidToInfoTable.remove(new Long(uid));
      	} else {
	  removedCachingMessages[i] = removedMessages[i];
	}
	getCache().invalidateCache(uid, SimpleFileCache.MESSAGE);
      }
    }
    
    MessageCountEvent newMce = new MessageCountEvent(getFolder(), mce.getType(), mce.isRemoved(), removedCachingMessages);
    
    
    if (getFolderDisplayUI() != null) {
      if (removedProxies.size() > 0) {
	getFolderDisplayUI().removeRows(removedProxies);
      }
      resetMessageCounts();
      fireMessageCountEvent(newMce);
    } else {
      resetMessageCounts();
      fireMessageCountEvent(newMce);
      if (removedProxies.size() > 0)
	getFolderTableModel().removeRows(removedProxies);
    }
  }
  
  /**
   * This updates the TableInfo on the changed messages.
   * 
   * As defined by java.mail.MessageChangedListener.
   */
  
  public void runMessageChanged(MessageChangedEvent mce) {
    // if the message is getting deleted, then we don't
    // really need to update the table info.  for that 
    // matter, it's likely that we'll get MessagingExceptions
    // if we do, anyway.
    try {
      if (!mce.getMessage().isSet(Flags.Flag.DELETED) || ! Pooka.getProperty("Pooka.autoExpunge", "true").equalsIgnoreCase("true")) {
	Message msg = mce.getMessage();
	long uid = -1;
	uid = getUID(msg);

	if (msg != null){
	  if (mce.getMessageChangeType() == MessageChangedEvent.FLAGS_CHANGED)
	    getCache().cacheMessage((MimeMessage)msg, uid, uidValidity, SimpleFileCache.FLAGS);
	  else if (mce.getMessageChangeType() == MessageChangedEvent.ENVELOPE_CHANGED)
	    getCache().cacheMessage((MimeMessage)msg, uid, uidValidity, SimpleFileCache.HEADERS);
	}
	
	MessageInfo mi = getMessageInfoByUid(uid);
	MessageProxy mp = mi.getMessageProxy();
	if (mp != null) {
	  mp.unloadTableInfo();
	  mp.loadTableInfo();
	}
	
      }
    } catch (MessagingException me) {
      // if we catch a MessagingException, it just means
      // that the message has already been expunged.
    }
    
    fireMessageChangedEvent(mce);
  }
  
    /**
     * This sets the given Flag for all the MessageInfos given.
     */
    public void setFlags(MessageInfo[] msgs, Flags flag, boolean value) throws MessagingException {
	// no optimization here.
	for (int i = 0; i < msgs.length; i++) {
	    msgs[i].getRealMessage().setFlags(flag, value);
	}
    }

  /**
   * This copies the given messages to the given FolderInfo.
   */
  public void copyMessages(MessageInfo[] msgs, FolderInfo targetFolder) throws MessagingException {
    if (isConnected())
      super.copyMessages(msgs, targetFolder);
    else
      targetFolder.appendMessages(msgs);
  }

  /**
   * This appends the given message to the given FolderInfo.
   */
  public void appendMessages(MessageInfo[] msgs) throws MessagingException {
    if (isAvailable()) {
      if (isConnected()) {
	super.appendMessages(msgs);
      } else {
	// make sure we've loaded
	if (! isLoaded())
	  loadFolder();
	getCache().appendMessages(msgs);
      }
    } else {
      throw new MessagingException("cannot append messages to an unavailable folder.");
    }
  }
    
    /**
     * This expunges the deleted messages from the Folder.
     */
    public void expunge() throws MessagingException {
	if (isConnected())
	    getFolder().expunge();
	else if (shouldBeConnected()) {
	    openFolder(Folder.READ_WRITE);
	    getFolder().expunge();
	} else {
	    getCache().expungeMessages();
	}
    }

  /**
   * This attempts to cache the message represented by this MessageInfo.
   */
  public void cacheMessage (MessageInfo info, int cacheStatus) throws MessagingException {
    if (status == CONNECTED) {
      Message m = info.getMessage();
      if (m instanceof CachingMimeMessage) {
	long uid = ((CachingMimeMessage)m).getUID();
	MimeMessage realMessage = getRealMessageById(uid);
	getCache().cacheMessage(realMessage, uid, uidValidity, cacheStatus);
      } else if (m instanceof MimeMessage) {
	long uid = getUID(m);
	getCache().cacheMessage((MimeMessage)m, uid, uidValidity, cacheStatus);
      } else {
	throw new MessagingException(Pooka.getProperty("error.CachingFolderInfo.unknownMessageType", "Error:  unknownMessageType."));
      }
    } else { 
      throw new MessagingException(Pooka.getProperty("error.CachingFolderInfo.cacheWhileDisconnected", "Error:  You cannot cache messages unless you\nare connected to the folder."));
    }
  }
  
  
  /**
   * This updates the children of the current folder.  Generally called
   * when the folderList property is changed.
   */
  
  public void updateChildren() {
    Vector newChildren = new Vector();
    
    String childList = Pooka.getProperty(getFolderProperty() + ".folderList", "");
    if (childList != "") {
      StringTokenizer tokens = new StringTokenizer(childList, ":");
      
      String newFolderName;
      
      for (int i = 0 ; tokens.hasMoreTokens() ; i++) {
	newFolderName = (String)tokens.nextToken();
	FolderInfo childFolder = getChild(newFolderName);
	if (childFolder == null) {
	  if (! Pooka.getProperty(getFolderProperty() + "." + newFolderName + ".cacheMessages", "true").equalsIgnoreCase("false"))
	    childFolder = new CachingFolderInfo(this, newFolderName);
	  else if (Pooka.getProperty(getParentStore().getStoreProperty() + ".protocol", "mbox").equalsIgnoreCase("imap")) {
	    childFolder = new UIDFolderInfo(this, newFolderName);
	  } else
	    childFolder = new FolderInfo(this, newFolderName);
	  
	  newChildren.add(childFolder);
	} else {
	  newChildren.add(childFolder);
	}
      }
      
      children = newChildren;
      
      if (folderNode != null) 
	folderNode.loadChildren();
    }
  }
    
  /**
   * This method closes the Folder.  If you open the Folder using 
   * openFolder (which you should), then you should use this method
   * instead of calling getFolder.close().  If you don't, then the
   * FolderInfo will try to reopen the folder.
   */
  public void closeFolder(boolean expunge) throws MessagingException {
    closeFolder(expunge, false);
  }

  /**
   * This method closes the Folder.  If you open the Folder using 
   * openFolder (which you should), then you should use this method
   * instead of calling getFolder.close().  If you don't, then the
   * FolderInfo will try to reopen the folder.
   */
  public void closeFolder(boolean expunge, boolean closeDisplay) throws MessagingException {
    
    if (closeDisplay && getFolderDisplayUI() != null)
      getFolderDisplayUI().closeFolderDisplay();
    
    if (isLoaded() && isAvailable()) {
      if (isConnected()) {
	try {
	  getFolder().close(expunge);
	} catch (java.lang.IllegalStateException ise) {
	  throw new MessagingException(ise.getMessage(), ise);
	}
      }

      if (getCache() != null) {
	setStatus(DISCONNECTED);
      } else {
	setStatus(CLOSED);
      }
    }
    
  }
  
  /**
   * This unsubscribes this FolderInfo and all of its children, if 
   * applicable.
   *
   * For the CachingFolderInfo, this calls super.unsubscribe() and 
   * getCache().invalidateCache().
   */
  public void unsubscribe() {
    super.unsubscribe();
    getCache().invalidateCache();
  }

  /**
   * Searches for messages in this folder which match the given
   * SearchTerm.
   *
   * Basically wraps the call to Folder.search(), and then wraps the
   * returned Message objects as MessageInfos.
   */
  public MessageInfo[] search(javax.mail.search.SearchTerm term) 
    throws MessagingException {
    if (isConnected()) {
      return super.search(term);
    } else {
      return getCache().search(term);
    }
  }
  
  /**
   * The resource for the default display filters.
   */
  protected String getDefaultDisplayFiltersResource() {
    if (isSentFolder())
      return "CachingFolderInfo.sentFolderDefaultDisplayFilters";
    else
      return "CachingFolderInfo.defaultDisplayFilters";
  }

  /**
   * Returns whether or not a given message is fully cached.
   */
  public boolean isCached(long uid) {
    return getCache().isFullyCached(uid);
  }

  /**
   * This returns the MessageCache associated with this FolderInfo,
   * if any.
   */
  public MessageCache getCache() {
    return cache;
  }
  
  /**
   * Returns whether or not we should be showing cache information in 
   * the FolderDisplay.  Uses the FolderProperty.showCacheInfo property
   * to determine--if this is set to true, we will show the cache info.
   * Otherwise, if we're connected, don't show the info, and if we're
   * not connected, do.
   */
  public boolean showCacheInfo() {
    if (Pooka.getProperty(getFolderProperty() + ".showCacheInfo", "false").equalsIgnoreCase("true")) 
      return true;
    else {
      if (getStatus() == CONNECTED) {
	return false;
      } else
	return true;
    }
  }
  
  /**
   * Returns the cache directory for this FolderInfo.
   */
  public String getCacheDirectory() {
    String localDir = Pooka.getProperty(getFolderProperty() + ".cacheDir", "");
    if (!localDir.equals(""))
      return localDir;
    
    localDir = Pooka.getProperty("Pooka.defaultMailSubDir", "");
    if (localDir.equals(""))
      localDir = System.getProperty("user.home") + File.separator + ".pooka";
    
    localDir = localDir + File.separatorChar + "cache";
    FolderInfo currentFolder = this;
    StringBuffer subDir = new StringBuffer();
    subDir.insert(0, currentFolder.getFolderName());
    subDir.insert(0, File.separatorChar);
    while (currentFolder.getParentFolder() != null) {
      currentFolder = currentFolder.getParentFolder();
      subDir.insert(0, currentFolder.getFolderName());
      subDir.insert(0, File.separatorChar);
    } 
    
    subDir.insert(0, currentFolder.getParentStore().getStoreID());
    subDir.insert(0, File.separatorChar);
    
    return localDir + subDir.toString();
  }

  public boolean isLoaded() {
    return (getFolder() != null && ( ! (getFolder() instanceof FolderProxy)) && cache != null);
  }

  /**
   * Gets the UID for the given Message.
   */
  public long getUID(Message m) throws MessagingException {
    if (m instanceof SimpleFileCache.LocalMimeMessage) {
      return ((SimpleFileCache.LocalMimeMessage) m).getUID();
    } else {
      return super.getUID(m);
    }
  }
}

