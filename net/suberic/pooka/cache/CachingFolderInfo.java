package net.suberic.pooka.cache;
import javax.mail.*;
import javax.mail.event.MessageCountEvent;
import javax.mail.internet.MimeMessage;
import javax.mail.event.MessageChangedEvent;
import javax.mail.event.ConnectionEvent;
import net.suberic.pooka.*;
import java.util.Vector;
import java.util.StringTokenizer;
import java.util.HashMap;
import net.suberic.pooka.gui.MessageProxy;
import net.suberic.pooka.gui.FolderTableModel;

/**
 * A FolderInfo which caches its messages in a MessageCache.
 */

public class CachingFolderInfo extends net.suberic.pooka.UIDFolderInfo {
    protected MessageCache cache = null;

    public CachingFolderInfo(StoreInfo parent, String fname) {
	super(parent, fname);
	
    }

    public CachingFolderInfo(FolderInfo parent, String fname) {
	super(parent, fname);
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
		cache = new SimpleFileCache(this, Pooka.getProperty(getFolderProperty() + ".cacheDir", ""));
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
		    setFolder(null);
		}
	    } 
	} catch (MessagingException me) {
	    if (Pooka.isDebug()) {
		System.out.println(Thread.currentThread() + "loading folder " + getFolderID() + ":  caught messaging exception; setting loaded to false:  " + me.getMessage() );
		me.printStackTrace();
	    }
	    setStatus(NOT_LOADED);
	    setFolder(null);
	} finally {
	    initializeFolderInfo();
	    loading = false;
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
		getFolderDisplayUI().showStatusMessage(Pooka.getProperty("messages.CachingFolder.loading.starting", "Loading messages from cache."));
	    }
	    
	    if (!isLoaded())
		loadFolder();

	    Vector messageProxies = new Vector();
	    
	    FetchProfile fp = createColumnInformation();
	    if (loaderThread == null) 
		loaderThread = createLoaderThread();
	    
	    try {
		
		if (preferredStatus < DISCONNECTED && !(isConnected())) {
		    openFolder(Folder.READ_WRITE);
		} else {
		    uidValidity = cache.getUIDValidity();
		}
		
		long[] uids = cache.getMessageUids();
		MessageInfo mi;
		
		for (int i = 0; i < uids.length; i++) {
		    Message m = new CachingMimeMessage(this, uids[i]);
		    mi = new MessageInfo(m, this);
		    
		    messageProxies.add(new MessageProxy(getColumnValues() , mi));
		    messageToInfoTable.put(m, mi);
		    uidToInfoTable.put(new Long(uids[i]), mi);
		}
		
		FolderTableModel ftm = new FolderTableModel(messageProxies, getColumnNames(), getColumnSizes());
		
		setFolderTableModel(ftm);
		
		loaderThread.loadMessages(messageProxies);
		
		if (!loaderThread.isAlive())
		    loaderThread.start();
		
		if (preferredStatus == CONNECTED)
		    getFolderThread().addToQueue(new net.suberic.util.thread.ActionWrapper(new javax.swing.AbstractAction() {
			    
			    public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
				try {
				    synchronizeCache();
				} catch (Exception e) {
				    if (getFolderDisplayUI() != null) 
					getFolderDisplayUI().showError(Pooka.getProperty("error.CachingFolder.synchronzing", "Error synchronizing with folder"), Pooka.getProperty("error.CachingFolder.synchronzing.title", "Error synchronizing with folder"), e);
				}
			    }
			}, getFolderThread()), new java.awt.event.ActionEvent(this, 1, "message-count-changed"));
		
		
	    } finally {
		if (getFolderDisplayUI() != null) {
		    getFolderDisplayUI().setBusy(false);
		    getFolderDisplayUI().showStatusMessage(Pooka.getProperty("messages.CachingFolder.loading.finished", "Done loading messages."));
		}
		
	    }
	}
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

	if (getFolderTableModel() == null)
	    return -1;

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
	    } else
		Pooka.getUIFactory().showStatusMessage(Pooka.getProperty("message.UIDFolder.synchronizing", "Re-synchronizing with folder..."));
	    
	    long cacheUidValidity = getCache().getUIDValidity();
	    
	    if (uidValidity != cacheUidValidity) {
		if (getFolderDisplayUI() != null)
		    getFolderDisplayUI().showStatusMessage(Pooka.getProperty("error.UIDFolder.validityMismatch", "Error:  validity not correct.  reloading..."));
		else 
		    Pooka.getUIFactory().showStatusMessage(Pooka.getProperty("error.UIDFolder.validityMismatch", "Error:  validity not correct.  reloading..."));
		
		getCache().invalidateCache();
		getCache().setUIDValidity(uidValidity);
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
	    
	    FetchProfile fp = new FetchProfile();
	    fp.add(FetchProfile.Item.ENVELOPE);
	    fp.add(FetchProfile.Item.FLAGS);
	    fp.add(UIDFolder.FetchProfileItem.UID);
	    Message[] messages = getFolder().getMessages();
	    getFolder().fetch(messages, fp);
	    
	    long[] uids = new long[messages.length];
	    
	    for (int i = 0; i < messages.length; i++) {
		uids[i] = ((UIDFolder)getFolder()).getUID(messages[i]);
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
		    removedMsgs[i] = getMessageInfoByUid(removedUids[i]).getRealMessage();
		}
		MessageCountEvent mce = new MessageCountEvent(getFolder(), MessageCountEvent.REMOVED, false, removedMsgs);
		messagesRemoved(mce);
		
	    }
	    
	    updateFlags(uids, messages, cacheUidValidity);
	    
	} finally {
	    if (getFolderDisplayUI() != null) {
		getFolderDisplayUI().clearStatusMessage();
		getFolderDisplayUI().setBusy(false);
	    } else
		Pooka.getUIFactory().clearStatus();
	}

    }

    protected void updateFlags(long[] uids, Message[] messages, long uidValidity) throws MessagingException {
	for (int i = 0; i < messages.length; i++) {
	    getCache().cacheMessage((MimeMessage)messages[i], uids[i], uidValidity, SimpleFileCache.FLAGS);
	}
	
    }

    
    protected void runMessagesAdded(MessageCountEvent mce) {
	if (folderTableModel != null) {
	    Message[] addedMessages = mce.getMessages();
	    MessageInfo mp;
	    Vector addedProxies = new Vector();
	    for (int i = 0; i < addedMessages.length; i++) {
		if (addedMessages[i] instanceof CachingMimeMessage) {
		    mp = new MessageInfo(addedMessages[i], CachingFolderInfo.this);
		    addedProxies.add(new MessageProxy(getColumnValues(), mp));
		    messageToInfoTable.put(addedMessages[i], mp);
		    uidToInfoTable.put(new Long(((CachingMimeMessage) addedMessages[i]).getUID()), mp);
		    try {
			getCache().cacheMessage((MimeMessage)addedMessages[i], ((CachingMimeMessage)addedMessages[i]).getUID(), getUIDValidity(), SimpleFileCache.FLAGS_AND_HEADERS);
		    } catch (MessagingException me) {
			System.out.println("caught exception:  " + me);
			me.printStackTrace();
			
		    }
		    
		} else {
		    // it's a 'real' message from the server.
		    
		    long uid = -1;
		    try {
			uid = ((UIDFolder)getFolder()).getUID(addedMessages[i]);
		    } catch (MessagingException me) {
		    }
		    
		    CachingMimeMessage newMsg = new CachingMimeMessage(CachingFolderInfo.this, uid);
		    mp = new MessageInfo(newMsg, CachingFolderInfo.this);
		    addedProxies.add(new MessageProxy(getColumnValues(), mp));
		    messageToInfoTable.put(newMsg, mp);
		    uidToInfoTable.put(new Long(uid), mp);
		    try {
			getCache().cacheMessage((MimeMessage)addedMessages[i], uid, getUIDValidity(), SimpleFileCache.FLAGS_AND_HEADERS);
		    } catch (MessagingException me) {
			System.out.println("caught exception:  " + me);
			me.printStackTrace();
		    }
		}
	    }
	    addedProxies.removeAll(applyFilters(addedProxies));
	    if (addedProxies.size() > 0) {
		if (getFolderTableModel() != null) 
		    getFolderTableModel().addRows(addedProxies);
		setNewMessages(true);
		resetMessageCounts();
		fireMessageCountEvent(mce);
	    }
	
	}
    }

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
		if (mi.getMessageProxy() != null)
		    mi.getMessageProxy().close();
		
		if (mi != null) {
		    if (Pooka.isDebug())
			System.out.println("message exists--removing");
		    removedProxies.add(mi.getMessageProxy());
		    messageToInfoTable.remove(mi);
		    uidToInfoTable.remove(new Long(((CachingMimeMessage) removedMessages[i]).getUID()));
		    getCache().invalidateCache(((CachingMimeMessage) removedMessages[i]).getUID(), SimpleFileCache.MESSAGE);
		    
		}
	    } else {
		// not a CachingMimeMessage.
		long uid = -1;
		try {
		    uid =((UIDFolder)getFolder()).getUID(removedMessages[i]);
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
		    getCache().invalidateCache(uid, SimpleFileCache.MESSAGE);
		} else {
		    removedCachingMessages[i] = removedMessages[i];
		}
	    }
	}
	
	MessageCountEvent newMce = new MessageCountEvent(getFolder(), mce.getType(), mce.isRemoved(), removedCachingMessages);
	if (getFolderDisplayUI() != null) {
	    getFolderDisplayUI().removeRows(removedProxies);
	    resetMessageCounts();
	    fireMessageCountEvent(newMce);
	} else {
	    resetMessageCounts();
	    fireMessageCountEvent(newMce);
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
		if (msg != null && msg instanceof CachingMimeMessage) {
		    uid = ((CachingMimeMessage) msg).getUID();
		} else {
		    uid = ((UIDFolder)getFolder()).getUID(msg);
		}
		MessageInfo mi = getMessageInfoByUid(uid);
		MessageProxy mp = mi.getMessageProxy();
		if (mp != null) {
		    if (msg != null && msg instanceof CachingMimeMessage) {
			if (mce.getMessageChangeType() == MessageChangedEvent.FLAGS_CHANGED)
			    getCache().cacheMessage((MimeMessage)msg, uid, uidValidity, SimpleFileCache.FLAGS);
			else if (mce.getMessageChangeType() == MessageChangedEvent.ENVELOPE_CHANGED)
			    getCache().cacheMessage((MimeMessage)msg, uid, uidValidity, SimpleFileCache.HEADERS);
		    }
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
	targetFolder.appendMessages(msgs);
    }

    /**
     * This appends the given message to the given FolderInfo.
     */
    public void appendMessages(MessageInfo[] msgs) throws MessagingException {
	if (isAvailable()) {
	    super.appendMessages(msgs);
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
		long uid = ((UIDFolder)getFolder()).getUID(m);
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
		    if (Pooka.getProperty(getFolderProperty() + "." + newFolderName + ".cacheMessages", "false").equalsIgnoreCase("true"))
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

    public void closed(ConnectionEvent e) {
	if (Pooka.isDebug()) {
	    System.out.println("Folder " + getFolderID() + " closed:  " + e);
	}
	
	if (getFolderDisplayUI() != null) {
	    if (getStatus() == CLOSED)
		getFolderDisplayUI().showStatusMessage(Pooka.getProperty("error.CachingFolder.disconnected", "Folder disconnected.  Only cached messages will be available."));
	}
	
	if (status == CONNECTED) {
	    setStatus(LOST_CONNECTION);
	}
	
	fireConnectionEvent(e);
    }
    
    public void disconnected(ConnectionEvent e) {
	if (Pooka.isDebug()) {
	    System.out.println("Folder " + getFolderID() + " disconnected.");
	    Thread.dumpStack();
	}
	
	if (getFolderDisplayUI() != null) {
	    if (getStatus() == CLOSED)
		getFolderDisplayUI().showStatusMessage(Pooka.getProperty("error.CachingFolder.disconnected", "Folder disconnected.  Only cached messages will be available."));
	}
	
	if (status == CONNECTED) {
	    setStatus(LOST_CONNECTION);
	}
	fireConnectionEvent(e);
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
}

