package net.suberic.pooka;

import javax.mail.*;
import javax.mail.event.*;
import javax.swing.event.EventListenerList;
import java.util.*;
import javax.swing.AbstractAction;
import javax.swing.Action;
import java.awt.event.ActionEvent;
import net.suberic.pooka.gui.*;
import net.suberic.pooka.thread.*;
import net.suberic.pooka.event.*;
import net.suberic.util.ValueChangeListener;
import net.suberic.util.thread.ActionThread;

/**
 * This class emulates a FolderInfo for things such as Search Results.
 */

public class VirtualFolderInfo extends FolderInfo {
    FolderInfo[] parents;
    MessageInfo[] originalMessages;
    ActionThread folderThread;

    /**
     * Creates a new VirtualFolderInfo from the list of messageInfos,
     * which in turn should belong to the FolderInfos in parents.
     */
    public VirtualFolderInfo(MessageInfo[] newMessages, FolderInfo[] newParents) {
	parents = newParents;
	originalMessages = newMessages;
	setStatus(CONNECTED);
	initializeFolderInfo();
	resetDefaultActions();
    }

    /**
     * Loads the FolderInfo.
     */
    public void loadFolder() {
	return;
    }

    /**
     * Initialized the FolderInfo.  Basically adds itself as a listener
     * to all the parent FolderInfos.
     */
    private void initializeFolderInfo() {
	for (int i = 0; i < parents.length; i++) {
	    parents[i].addMessageCountListener(this);
	    parents[i].addMessageChangedListener(this);
	}

	folderThread = new ActionThread(Pooka.getProperty("thread.searchThread", "Search Thread "));

	folderThread.start();
	loadAllMessages();
    }

    /**
     * Disposes of this VirtualFolderInfo.
     */
    public void dispose() {
	for (int i = 0; i < parents.length; i++) {
	    parents[i].removeMessageCountListener(this);
	    parents[i].removeMessageChangedListener(this);
	}

	folderThread.setStop(true);
    }

    /**
     * Loads all the Messages into a new FolderTableModel.
     */
    public synchronized void loadAllMessages() {
	if (folderTableModel == null) {
	    Vector messageProxies = new Vector();
	    createColumnInformation();

	    for (int i = 0 ; i < originalMessages.length; i++) {
		if (Pooka.isDebug())
		    System.out.println("originalMessages[" + i + "] = " + originalMessages[i]);
		messageProxies.add(originalMessages[i].getMessageProxy());
		messageToInfoTable.put(originalMessages[i].getMessage(), originalMessages[i]);
	    }

	    if (Pooka.isDebug()) {
		System.out.println("originalMessages.length = " + originalMessages.length + "; messageProxies.size() = " + messageProxies.size() + "; getColumnNames() = " + getColumnNames() + "; getColumnSizes() = " + getColumnSizes());
		for (int i = 0 ; i < getColumnNames().size() ; i++) {
		    System.out.println("column name " + i + " = " + getColumnNames().elementAt(i));
		}
	    }
	    FolderTableModel ftm = new FolderTableModel(messageProxies, getColumnNames(), getColumnSizes());
	    setFolderTableModel(ftm);

	    resetMessageCounts();
	}
    }

    /**
     * Gets the row number of the first unread message.  Returns 0.
     */
    
    public int getFirstUnreadMessage() {
	return -1;
    }

    /**
     * This sets the given Flag for all the MessageInfos given.
     */
    public void setFlags(MessageInfo[] msgs, Flags flag, boolean value) throws MessagingException {
	// these should only belong to FolderInfos in the parents array.
	HashMap map = new HashMap();
	for (int i = 0; i < parents.length; i++) {
	    map.put(parents[i], new Vector());
	}
	
	for (int i = 0; i < msgs.length; i++) {
	    Vector v = (Vector) map.get(msgs[i].getFolderInfo());
	    if (v != null)
		v.add(msgs[i]);
	}

	for (int i = 0; i < parents.length; i++) {
	    Vector v = (Vector) map.get(parents[i]);
	    if (v.size() > 0) {
		MessageInfo[] folderMsgs = new MessageInfo[v.size()];
		for (int j = 0; j < v.size(); j++) {
		    folderMsgs[j] = (MessageInfo) v.elementAt(j);
		}

		parents[i].setFlags(folderMsgs, flag, value);
	    }
		
	}
    }

    /**
     * This copies the given messages to the given FolderInfo.
     */
    public void copyMessages(MessageInfo[] msgs, FolderInfo targetFolder) throws MessagingException {
	// these should only belong to FolderInfos in the parents array.
	HashMap map = new HashMap();
	for (int i = 0; i < parents.length; i++) {
	    map.put(parents[i], new Vector());
	}
	
	for (int i = 0; i < msgs.length; i++) {
	    Vector v = (Vector) map.get(msgs[i].getFolderInfo());
	    if (v != null)
		v.add(msgs[i]);
	}

	for (int i = 0; i < parents.length; i++) {
	    Vector v = (Vector) map.get(parents[i]);
	    if (v.size() > 0) {
		MessageInfo[] folderMsgs = new MessageInfo[v.size()];
		for (int j = 0; j < v.size(); j++) {
		    folderMsgs[j] = (MessageInfo) v.elementAt(j);
		}

		parents[i].copyMessages(folderMsgs, targetFolder);
	    }
		
	}
    }

    /**
     * This appends the given message to the given FolderInfo.
     */
    public void appendMessages(MessageInfo[] msgs) throws MessagingException {
	throw new MessagingException (Pooka.getProperty("error.search.appendToSearchFolder", "Cannot append to a Virtual Folder."));
    }

    /**
     * This expunges the deleted messages from the Folder.
     */
    public void expunge() throws MessagingException {
	for (int i = 0; i < parents.length; i++)
	    parents[i].expunge();
    }

    
    /**
     * The parent FolderInfos should be updating the MessageProxies, so 
     * we can just pass this on.
     */
    protected void runMessageChanged(MessageChangedEvent mce) {
	fireMessageChangedEvent(mce);
	resetMessageCounts();
    }

    /**
     * Searches for messages in this folder which match the given
     * SearchTerm.
     *
     */
    public MessageInfo[] search(javax.mail.search.SearchTerm term) 
    throws MessagingException {
	Vector matches = new Vector();
	for (int i = 0; i < getFolderTableModel().getRowCount(); i++) {
	    MessageInfo currentInfo = (MessageInfo) ((MessageProxy) getFolderTableModel().getValueAt(i, 0)).getMessageInfo();
	    if (term.match(currentInfo.getMessage()))
		matches.add(currentInfo);
	}
	MessageInfo returnValue[] = new MessageInfo[matches.size()];
	for (int i = 0; i < matches.size(); i++) {
	    returnValue[i] = (MessageInfo) matches.elementAt(i);
	}
	return returnValue;
    }

    protected void removeFromListeners(FolderDisplayUI display) {
	if (display != null) {
	    removeMessageChangedListener(display);
	    removeMessageCountListener(display);
	}
    }

    protected void addToListeners(FolderDisplayUI display) {
	if (display != null) {
	    addMessageChangedListener(display);
	    addMessageCountListener(display);
	}
    }

    
    /**
     * This forces an update of both the total and unread message counts.
     */
    public void resetMessageCounts() {
	if (Pooka.isDebug()) {
	    System.out.println("running resetMessageCounts.");
	}
	
	int tmpUnreadCount = 0;
	int tmpMessageCount = 0;
	Enumeration msgs = messageToInfoTable.keys();
	while (msgs.hasMoreElements()) {
	    MessageInfo mi = (MessageInfo) messageToInfoTable.get(msgs.nextElement());
	    tmpMessageCount++;
	    if (! mi.isSeen())
		tmpUnreadCount++;
	}
	unreadCount = tmpUnreadCount;
	messageCount = tmpMessageCount;
    }

    public ActionThread getFolderThread() {
	return folderThread;
    }

    public StoreInfo getParentStore() {

	return null;

    }

    public String getFolderID() {
	return Pooka.getProperty("title.searchResults", "Search results");
    }
}
