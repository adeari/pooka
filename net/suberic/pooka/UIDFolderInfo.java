package net.suberic.pooka;
import javax.mail.*;
import javax.mail.event.MessageCountEvent;
import javax.mail.internet.MimeMessage;
import javax.mail.event.MessageChangedEvent;
import javax.mail.event.ConnectionEvent;
import net.suberic.pooka.*;
import java.util.Vector;
import java.util.StringTokenizer;
import java.util.HashMap;
import java.util.Set;
import net.suberic.pooka.gui.MessageProxy;
import net.suberic.pooka.gui.FolderTableModel;

/**
 * A FolderInfo which keeps track of its messages' UID's.  This allows
 * it to recover if the connection to the server is lost.
 */

public class UIDFolderInfo extends FolderInfo {
    protected HashMap uidToInfoTable = new HashMap();
    protected long uidValidity;

    public UIDFolderInfo(StoreInfo parent, String fname) {
	super(parent, fname);
	
    }

    public UIDFolderInfo(FolderInfo parent, String fname) {
	super(parent, fname);
    }

    /**
     * Loads all Messages into a new FolderTableModel, sets this 
     * FolderTableModel as the current FolderTableModel, and then returns
     * said FolderTableModel.  This is the basic way to populate a new
     * FolderTableModel.
     */
    public synchronized void loadAllMessages() throws MessagingException {
	if (folderTableModel == null) {
	    Vector messageProxies = new Vector();
	    
	    FetchProfile fp = createColumnInformation();
	    fp.add(UIDFolder.FetchProfileItem.UID);
	    
	    if (loaderThread == null) 
		loaderThread = createLoaderThread();
	    
	    try {
		if (!isConnected()) {
		    openFolder(Folder.READ_WRITE);
		}
		
		Message[] msgs = getFolder().getMessages();
		getFolder().fetch(msgs, fp);
		MessageInfo mi;
		
		for (int i = 0; i < msgs.length; i++) {
		    long uid = ((UIDFolder)getFolder()).getUID(msgs[i]);
		    UIDMimeMessage newMessage = new UIDMimeMessage(this, uid);
		    mi = new MessageInfo(newMessage, this);
		    
		    messageProxies.add(new MessageProxy(getColumnValues() , mi));
		    messageToInfoTable.put(newMessage, mi);
		    uidToInfoTable.put(new Long(uid), mi);
		}
	    } catch (MessagingException me) {
		System.out.println("aigh!  messaging exception while loading!  implement Pooka.showError()!");
	    }
	    
	    FolderTableModel ftm = new FolderTableModel(messageProxies, getColumnNames(), getColumnSizes());
	    
	    setFolderTableModel(ftm);
	    
	    loaderThread.loadMessages(messageProxies);
	    
	    if (!loaderThread.isAlive())
		loaderThread.start();
	    
	}
    }
    
    /**
     * This just checks to see if we can get a NewMessageCount from the
     * folder.  As a brute force method, it also accesses the folder
     * at every check, catching and throwing away any Exceptions that happen.  
     * It's nasty, but it _should_ keep the Folder open..
     */
    public void checkFolder() {
	if (Pooka.isDebug())
	    System.out.println("checking folder " + getFolderName());

	// i'm taking this almost directly from ICEMail; i don't know how
	// to keep the stores/folders open, either.  :)

	StoreInfo s = null;
	try {
	    
	    if (isConnected()) {
                Folder current = getFolder();
                if (current != null && current.isOpen()) {
                    current.getNewMessageCount();
                    current.getUnreadMessageCount();
		    resetMessageCounts();
                }
	    } else if (isAvailable() && (status == PASSIVE || status == LOST_CONNECTION)) {
		s = getParentStore();
		if (! s.isConnected())
		    s.connectStore();
		
		openFolder(Folder.READ_WRITE);

		resetMessageCounts();

		if (isAvailable() && preferredStatus == PASSIVE)
		    closeFolder(false);
	    } 
	    

	} catch ( MessagingException me ) {
	}
    }

    protected void updateFolderOpenStatus(boolean isNowOpen) {
	if (isNowOpen) {
	    status = CONNECTED;
	    try {
		uidValidity = ((UIDFolder) getFolder()).getUIDValidity();
		if (getFolderTableModel() != null)
		    synchronizeCache();
	    } catch (Exception e) { }
	    
	} else
	    status = CLOSED;
    }

    /**
     * This synchronizes the cache with the new information from the 
     * Folder.
     */
    public void synchronizeCache() throws MessagingException {
	if (Pooka.isDebug())
	    System.out.println("synchronizing cache.");

	if (getFolderDisplayUI() != null)
	    getFolderDisplayUI().showStatusMessage(Pooka.getProperty("message.UIDFolder.synchronizing", "Re-synchronizing with folder..."));

	long newValidity = ((UIDFolder)getFolder()).getUIDValidity();
	if (uidValidity != newValidity) {
	    if (getFolderDisplayUI() != null)
		getFolderDisplayUI().showStatusMessage(Pooka.getProperty("error.UIDFolder.validityMismatch", "Error:  validity not correct.  reloading..."));
	    
	    unloadAllMessages();
	    loadAllMessages();
	    if (getFolderDisplayUI() != null)
		getFolderDisplayUI().resetFolderTableModel(folderTableModel);

	    if (getFolderDisplayUI() != null)
		getFolderDisplayUI().clearStatusMessage();

	} else {
	    if (getFolderDisplayUI() != null)
		getFolderDisplayUI().showStatusMessage(Pooka.getProperty("message.UIDFolder.synchronizing.loading", "Loading messages from folder..."));
	    FetchProfile fp = new FetchProfile();
	    fp.add(FetchProfile.Item.ENVELOPE);
	    fp.add(FetchProfile.Item.FLAGS);
	    fp.add(UIDFolder.FetchProfileItem.UID);
	    Message[] messages = getFolder().getMessages();
	    getFolder().fetch(messages, fp);
	    
	    if (getFolderDisplayUI() != null)
		getFolderDisplayUI().showStatusMessage(Pooka.getProperty("message.UIDFolder.synchronizing", "Comparing new messages to current list..."));

	    long[] uids = new long[messages.length];
	    
	    for (int i = 0; i < messages.length; i++) {
		uids[i] = ((UIDFolder)getFolder()).getUID(messages[i]);
	    }
	    
	    if (Pooka.isDebug())
		System.out.println("synchronizing--uids.length = " + uids.length);
	    
	    long[] addedUids = getAddedMessages(uids, uidValidity);
	    if (Pooka.isDebug())
		System.out.println("synchronizing--addedUids.length = " + addedUids.length);
	    
	    if (addedUids.length > 0) {
		Message[] addedMsgs = ((UIDFolder)getFolder()).getMessagesByUID(addedUids);
		MessageCountEvent mce = new MessageCountEvent(getFolder(), MessageCountEvent.ADDED, false, addedMsgs);
		messagesAdded(mce);
	    }    
	    
	    long[] removedUids = getRemovedMessages(uids, uidValidity);
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
	    
	    updateFlags(uids, messages, uidValidity);

	    if (getFolderDisplayUI() != null)
		getFolderDisplayUI().clearStatusMessage();
	}
    }

    /**
     * Gets the added UIDs.
     */
    protected long[] getAddedMessages(long[] newUids, long uidValidity) {
	long[] added = new long[newUids.length];
	int addedCount = 0;
	Set currentUids = uidToInfoTable.keySet();

	for (int i = 0; i < newUids.length; i++) {
	    if (! currentUids.contains(new Long(newUids[i]))) {
		added[addedCount++]=newUids[i];
	    }
	}

	long[] returnValue = new long[addedCount];
	if (addedCount > 0) 
	    System.arraycopy(added, 0, returnValue, 0, addedCount);
	
	return returnValue;

    }

    /**
     * Gets the removed UIDs.
     */
    protected long[] getRemovedMessages(long[] newUids, long uidValidity) {
	Vector remainders = new Vector(uidToInfoTable.keySet());
	
	for (int i = 0; i < newUids.length; i++) {
	    remainders.remove(new Long(newUids[i]));
	}
	
	long[] returnValue = new long[remainders.size()];
	for (int i = 0; i < remainders.size(); i++)
	    returnValue[i] = ((Long) remainders.elementAt(i)).longValue();
	
	return returnValue;
    }

    protected void updateFlags(long[] uids, Message[] messages, long uidValidity) throws MessagingException {
	for (int i = 0; i < messages.length; i++) {
	    MessageChangedEvent mce = new MessageChangedEvent(getFolder(), MessageChangedEvent.FLAGS_CHANGED, messages[i]);
	    fireMessageChangedEvent(mce);
	}
	
    }

    
    protected void runMessagesAdded(MessageCountEvent mce)  {
	if (folderTableModel != null) {
	    try {
		Message[] addedMessages = mce.getMessages();
		FetchProfile fp = new FetchProfile();
		fp.add(FetchProfile.Item.ENVELOPE);
		fp.add(FetchProfile.Item.FLAGS);
		fp.add(UIDFolder.FetchProfileItem.UID);
		getFolder().fetch(addedMessages, fp);
		MessageInfo mp;
		Vector addedProxies = new Vector();
		for (int i = 0; i < addedMessages.length; i++) {
		    UIDMimeMessage newMsg = getUIDMimeMessage(addedMessages[i]);
		    long uid = newMsg.getUID();
		    mp = new MessageInfo(newMsg, this);
		    addedProxies.add(new MessageProxy(getColumnValues(), mp));
		    messageToInfoTable.put(newMsg, mp);
		    uidToInfoTable.put(new Long(uid), mp);
		}
		addedProxies.removeAll(applyFilters(addedProxies));
		if (addedProxies.size() > 0) {
		    getFolderTableModel().addRows(addedProxies);
		setNewMessages(true);
		resetMessageCounts();
		
		// change the Message objects in the MessageCountEvent to 
		// our UIDMimeMessages.
		Message[] newMsgs = new Message[addedProxies.size()];
		for (int i = 0; i < addedProxies.size(); i++) {
		    newMsgs[i] = ((MessageProxy)addedProxies.elementAt(i)).getMessageInfo().getMessage();
		}
		MessageCountEvent newMce = new MessageCountEvent(getFolder(), mce.getType(), mce.isRemoved(), newMsgs);
		fireMessageCountEvent(newMce);
		}
	    } catch (MessagingException me) {
		if (getFolderDisplayUI() != null)
		    getFolderDisplayUI().showError(Pooka.getProperty("error.handlingMessages", "Error handling messages."), Pooka.getProperty("error.handlingMessages.title", "Error handling messages."), me);
	    }
	}
		
    }

    protected void runMessagesRemoved(MessageCountEvent mce) {
	if (Pooka.isDebug())
	    System.out.println("running MessagesRemoved on " + getFolderID());

	MessageCountEvent newMce = null;
	if (folderTableModel != null) {
	    Message[] removedMessages = mce.getMessages();
	    Message[] uidRemovedMessages = new Message[removedMessages.length];

	    if (Pooka.isDebug())
		System.out.println("removedMessages was of size " + removedMessages.length);

	    MessageInfo mi;
	    Vector removedProxies=new Vector();
	    for (int i = 0; i < removedMessages.length; i++) {
		if (Pooka.isDebug())
		    System.out.println("checking for existence of message.");
		
		try {
		    UIDMimeMessage removedMsg = getUIDMimeMessage(removedMessages[i]);
		    if (removedMsg != null)
			uidRemovedMessages[i] = removedMsg;
		    else
			uidRemovedMessages[i] = removedMessages[i];

		    mi = getMessageInfo(removedMsg);
		    if (mi.getMessageProxy() != null)
			mi.getMessageProxy().close();
		    
		    if (mi != null) {
			if (Pooka.isDebug())
			    System.out.println("message exists--removing");
			removedProxies.add(mi.getMessageProxy());
		    messageToInfoTable.remove(mi);
		    uidToInfoTable.remove(new Long(removedMsg.getUID()));
		    }
		} catch (MessagingException me) {
		}
	    }
	    newMce = new MessageCountEvent(getFolder(), mce.getType(), mce.isRemoved(), uidRemovedMessages);
	    if (getFolderDisplayUI() != null)
		getFolderDisplayUI().removeRows(removedProxies);
	    resetMessageCounts();
	    fireMessageCountEvent(newMce);
	} else {
	    resetMessageCounts();
	    fireMessageCountEvent(mce);
	}
    }

    protected void runMessageChanged(MessageChangedEvent mce) {
	// if the message is getting deleted, then we don't
	// really need to update the table info.  for that 
	// matter, it's likely that we'll get MessagingExceptions
	// if we do, anyway.
	try {
	    if (!mce.getMessage().isSet(Flags.Flag.DELETED) || ! Pooka.getProperty("Pooka.autoExpunge", "true").equalsIgnoreCase("true")) {
		Message msg = mce.getMessage();
		UIDMimeMessage changedMsg = getUIDMimeMessage(msg);
		long uid = changedMsg.getUID();
		
		MessageInfo mi = getMessageInfoByUid(uid);
		if (mi != null) {
		    MessageProxy mp = mi.getMessageProxy();
		    if (mp != null) {
			mp.unloadTableInfo();
			mp.loadTableInfo();
		    }
		}
	    }
	} catch (MessagingException me) {
	    // if we catch a MessagingException, it just means
	    // that the message has already been expunged.
	}
	
	// now let's go ahead and get the UIDMimeMessage for the event so
	// that we can fire that instead.

	try {
	    Message msg = mce.getMessage();
	    UIDMimeMessage changedMsg = getUIDMimeMessage(msg);
	    if (changedMsg != null) {
		MessageChangedEvent newMce = new MessageChangedEvent(mce.getSource(), mce.getMessageChangeType(), changedMsg);
		fireMessageChangedEvent(newMce);
	    } else 
		fireMessageChangedEvent(mce);
	} catch (MessagingException me) {
	    // if we catch a MessagingException, then we can just fire the
	    // original mce.
	    fireMessageChangedEvent(mce);
	    
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
		    childFolder = new UIDFolderInfo(this, newFolderName);
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
     * Unloads all messages.  This should be run if ever the current message
     * information becomes out of date, as can happen when the connection
     * to the folder goes down.
     *
     * Note that for this implementation, we just keep everything; we only
     * need to worry when we do the cache synchronization.
     */
    public void unloadAllMessages() {
	//folderTableModel = null;
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
	    setStatus(CLOSED);
	    try {
		getFolder().close(expunge);
	    } catch (java.lang.IllegalStateException ise) {
		throw new MessagingException(ise.getMessage(), ise);
	    }
	}

    }

    public void closed(ConnectionEvent e) {
	if (Pooka.isDebug()) {
	    System.out.println("Folder " + getFolderID() + " closed:  " + e);
	}
	
	if (getFolderDisplayUI() != null) {
	    if (status != CLOSED)
		getFolderDisplayUI().showStatusMessage(Pooka.getProperty("error.UIDFolder.disconnected", "Lost connection to folder..."));
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
	    if (status != CLOSED)
		getFolderDisplayUI().showStatusMessage(Pooka.getProperty("error.UIDFolder.disconnected", "Lost connection to folder..."));
	}
	
	if (status == CONNECTED) {
	    setStatus(LOST_CONNECTION);
	}
	fireConnectionEvent(e);
    }

    public UIDMimeMessage getUIDMimeMessage(Message m) throws MessagingException {
	if (m instanceof UIDMimeMessage)
	    return (UIDMimeMessage) m;

	// it's not a UIDMimeMessage, so it must be a 'real' message.
	long uid = ((UIDFolder)getFolder()).getUID(m);
	MessageInfo mi = getMessageInfoByUid(uid);
	if (mi != null)
	    return (UIDMimeMessage) mi.getMessage();
	
	// doesn't already exist.  just create a new one.
	return new UIDMimeMessage(this, uid);
    }

    /**
     * gets the 'real' message for the given MessageInfo.
     */
    public Message getRealMessage(MessageInfo mi) throws MessagingException {
	return ((UIDMimeMessage)mi.getMessage()).getMessage();
    }

    /**
     * gets the MessageInfo for the given Message.
     */
    public MessageInfo getMessageInfo(Message m) {
	if (m instanceof UIDMimeMessage)
	    return (MessageInfo) messageToInfoTable.get(m);
	else {
	    try {
		long uid = ((UIDFolder)getFolder()).getUID(m);
		return getMessageInfoByUid(uid);
	    } catch (MessagingException me) {
		return null;
	    }
	}
    }

    /**
     * Returns the MessageInfo associated with the given uid.
     */
    public MessageInfo getMessageInfoByUid(long uid) {
	return (MessageInfo) uidToInfoTable.get(new Long(uid));
    }

    /**
     * Returns the "real" message from the underlying folder that matches up
     * to the given UID.
     */
    public javax.mail.internet.MimeMessage getRealMessageById(long uid) throws MessagingException {
	Folder f = getFolder();
	if (f != null && f instanceof UIDFolder) {
	    javax.mail.internet.MimeMessage m = null;
	    try {
		m = (javax.mail.internet.MimeMessage) ((UIDFolder) f).getMessageByUID(uid);
		return m;
	    } catch (IllegalStateException ise) {
		return null;
	    }
	} else {
	    return null;
	}
    }

    public long getUIDValidity() {
	return uidValidity;
    }
}

