package net.suberic.pooka;
import javax.mail.*;
import javax.mail.event.MessageCountEvent;
import javax.mail.internet.MimeMessage;
import javax.mail.event.MessageChangedEvent;
import net.suberic.pooka.*;
import java.util.Vector;
import java.util.StringTokenizer;
import java.util.HashMap;
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
    public synchronized void loadAllMessages() {
	if (folderTableModel == null) {
	    Vector messageProxies = new Vector();
	    
	    FetchProfile fp = createColumnInformation();
	    fp.add(UIDFolder.FetchProfileItem.UID);
	    
	    if (loaderThread == null) 
		loaderThread = createLoaderThread();
	    
	    try {
		if (!(getFolder().isOpen())) {
		    openFolder(Folder.READ_WRITE);
		}
		
		Message[] msgs = folder.getMessages();
		folder.fetch(msgs, fp);
		MessageInfo mi;
		
		for (int i = 0; i < msgs.length; i++) {
		    long uid = ((UIDFolder)folder).getUID(msgs[i]);
		    mi = new UIDMessageInfo(uid, uidValidity, this);
		    
		    messageProxies.add(new MessageProxy(getColumnValues() , mi));
		    messageToInfoTable.put(msgs[i], mi);
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
	    
	    folderTableModel = ftm;
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
	    
	    if (isOpen()) {
                Folder current = getFolder();
                if (current != null && current.isOpen()) {
                    current.getNewMessageCount();
                    current.getUnreadMessageCount();
                }
	    } else if (isAvailable() && (status == PASSIVE || status == LOST_CONNECTION)) {
		s = getParentStore();
		if (! s.isConnected())
		    s.connectStore();
		
		openFolder(Folder.READ_WRITE);

		if (isAvailable() && preferred_state == PASSIVE)
		    closeFolder(false);
	    } 
	    

	} catch ( MessagingException me ) {
	}
	
	resetMessageCounts();
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

	long newValidity = ((UIDFolder)folder).getUIDValidity();
	if (uidValidity != newValidity) {
	    unloadAllMessages();
	    loadAllMessages();
	    if (getFolderDisplayUI() != null)
		getFolderDisplayUI().resetFolderTableModel(folderTableModel);
	} else {
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
	    
	    updateFlags(uids, messages, cacheUidValidity);
	    
	}
    }

    /**
     * Gets the added UIDs.
     */
    protected long getAddedMessages(long[] newUids, long uidValidity) {
	long[] added = new long[newUids.length];
	int addedCount = 0;
	Set currentUids = uidToInfoTable.keySet();

	for (int i = 0; i < uids.length; i++) {
	    if (! currentUids.contains(new Long(uids[i]))) {
		added[addedCount++]=uids[i];
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
    protected long getRemovedMessages(long[] newUids, long uidValidity {
	Vector remainders = new Vector(uidToInfoTable.keySet());
	
	for (int i = 0; i < uids.length; i++) {
	    remainders.remove(new Long(uids[i]));
	}
	
	long[] returnValue = new long[remainders.size()];
	for (int i = 0; i < remainders.size(); i++)
	    returnValue[i] = ((Long) remainders.elementAt(i)).longValue();
	
	return returnValue;
    }

    protected void updateFlags(long[] uids, Message[] messages, long uidValidity) throws MessagingException {
	for (int i = 0; i < messages.length; i++) {
	    getCache().cacheMessage((MimeMessage)messages[i], uids[i], uidValidity, SimpleFileCache.FLAGS);
	}
	
    }

    
    protected void runMessagesAdded(MessageCountEvent mce) {
	if (folderTableModel != null) {
	    Message[] addedMessages = mce.getMessages();
	    FetchProfile fp = new FetchProfile();
	    fp.add(FetchProfile.Item.ENVELOPE);
	    fp.add(FetchProfile.Item.FLAGS);
	    fp.add(UIDFolder.FetchProfileItem.UID);
	    getFolder().fetch(addedMessages, fp);
	    MessageInfo mp;
	    Vector addedProxies = new Vector();
	    for (int i = 0; i < addedMessages.length; i++) {
		mp = new MessageInfo(addedMessages[i], FolderInfo.this);
		addedProxies.add(new MessageProxy(getColumnValues(), mp));
		messageToInfoTable.put(addedMessages[i], mp);
		uidToInfoTable.put(new Long(((UIDFolder)getFolder()).getUID(addedMessages[i])), mp);
	    }
	    addedProxies.removeAll(applyFilters(addedProxies));
	    if (addedProxies.size() > 0) {
		getFolderTableModel().addRows(addedProxies);
		setNewMessages(true);
		resetMessageCounts();
		fireMessageCountEvent(mce);
	    }
	}
		
    }

    protected void runMessagesRemoved(MessageCountEvent mce) {
	if (folderTableModel != null) {
	    Message[] removedMessages = mce.getMessages();
	    if (Pooka.isDebug())
		System.out.println("removedMessages was of size " + removedMessages.length);
	    MessageInfo mi;
	    Vector removedProxies=new Vector();
	    for (int i = 0; i < removedMessages.length; i++) {
		if (Pooka.isDebug())
		    System.out.println("checking for existence of message.");
		mi = getMessageInfo(removedMessages[i]);
		if (mi.getMessageProxy() != null)
		    mi.getMessageProxy().close();
		
		if (mi != null) {
		    if (Pooka.isDebug())
			System.out.println("message exists--removing");
		    removedProxies.add(mi.getMessageProxy());
		    messageToInfoTable.remove(mi);
		    uidToInfoTable.remove(new Long(((UIDFolder)getFolder()).getUID(removedMessages[i])));
		}
	    }
	    getFolderTableModel().removeRows(removedProxies);
	}
	resetMessageCounts();
	fireMessageCountEvent(mce)
    }

    /**
     * This updates the children of the current folder.  Generally called
     * when the folderList property is changed.
     */
    
    public void updateChildren() {
	// FIXME
	Vector newChildren = new Vector();

	String childList = Pooka.getProperty(getFolderProperty() + ".folderList", "");
	if (childList != "") {
	    StringTokenizer tokens = new StringTokenizer(childList, ":");
	    
	    String newFolderName;
	
	    for (int i = 0 ; tokens.hasMoreTokens() ; i++) {
		newFolderName = (String)tokens.nextToken();
		FolderInfo childFolder = getChild(newFolderName);
		if (childFolder == null) {
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
    public void closeFolder(boolean expunge) throws MessagingException {

	if (getFolderTracker() != null) {
	    getFolderTracker().removeFolder(this);
	    setFolderTracker(null);
	}

	if (isLoaded() && isAvailable()) {
	    setStatus(CLOSED);
	    try {
		getFolder().close(expunge);
	    } catch (java.lang.IllegalStateException ise) {
		throw new MessagingException(ise.getMessage(), ise);
	    }
	}

    }

    /**
     * This returns the MessageCache associated with this FolderInfo,
     * if any.
     */
    public MessageCache getCache() {
	return cache;
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

