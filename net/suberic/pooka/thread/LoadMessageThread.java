package net.suberic.pooka.thread;

import net.suberic.pooka.*;
import net.suberic.pooka.event.*;
import net.suberic.pooka.gui.LoadMessageTracker;
import net.suberic.pooka.gui.MessageProxy;
import net.suberic.pooka.gui.FolderInternalFrame;
import javax.mail.*;
import javax.mail.internet.*;
import java.util.*;

/**
 * This class does the actual loading of the header information from 
 * the messages in the folder.  It also is set up to communicate with
 * a JProgessBar to show how far the loading has gotten.
 *
 * More specifically, this thread takes an array of Messages and a 
 * Vector of Strings which are the column values which are to be put
 * into the table.  It then loads the values into a Vector of Vectors,
 * each of which contains the information for the Table for a group
 * of Messages.  It then throws a ChangeEvent to the listening
 * FolderTableModel.  The FolderTableModel can then get the information
 * using the getNewMessages() function.
 */

public class LoadMessageThread extends Thread {
    private FolderInfo folderInfo;
    private Vector loadedMessageInfo = new Vector();
    private Vector columnValues;
    private Vector loadQueue = new Vector();
    private Vector messageLoadedListeners = new Vector();
    private int updateCheckMilliseconds = 60000;
    private int updateMessagesCount = 10;
    private int loadedMessageCount = 0;
    
    /**
     * This creates a new LoadMessageThread from a FolderInfo object.
     */
    
    public LoadMessageThread(FolderInfo newFolderInfo) {
	super("Load Message Thread - " + newFolderInfo.getFolderID());
	folderInfo = newFolderInfo;
	this.setPriority(1);
    }

    public void run() {
	int uptime = 0;
	while (true) {
	    loadWaitingMessages();
	    
	    try {
		if (updateCheckMilliseconds < 1) {
		    while (updateCheckMilliseconds < 1)
			sleep(60000);
		} else { 
		    sleep(updateCheckMilliseconds);
		}
	    } catch (InterruptedException ie) {
	    }
	}
    }

    public void loadWaitingMessages() {

	Vector messages = retrieveLoadQueue();
	int numMessages = messages.size();
	MessageProxy mp;

	int updateCounter = 0;

	if (numMessages > 0) {
	    MessageLoadedListener display = getFolderInfo().getFolderDisplayUI();
	    if (display != null)
		this.addMessageLoadedListener(display);

	    fireMessageLoadedEvent(MessageLoadedEvent.LOADING_STARTING, getLoadedMessageCount(), messages.size());
	    
	    for(int i=numMessages-1; i >= 0; i--) {
		mp=(MessageProxy)messages.elementAt(i);
		mp.loadTableInfo();
		
		if (++updateCounter >= getUpdateMessagesCount()) {
		    fireMessageLoadedEvent(MessageLoadedEvent.MESSAGES_LOADED, getLoadedMessageCount(), messages.size());
		    updateCounter = 0;		   
		}
		loadedMessageCount++;
	    }
	    
	    if (updateCounter > 0)
		fireMessageLoadedEvent(MessageLoadedEvent.MESSAGES_LOADED, getLoadedMessageCount(), messages.size());
	    
	    fireMessageLoadedEvent(MessageLoadedEvent.LOADING_COMPLETE, getLoadedMessageCount(), messages.size());
	    
	    if (display != null)
		removeMessageLoadedListener(display);
	}
    }
    
    /**
     * Fires a new MessageLoadedEvent to each registered MessageLoadedListener.
     */

    public void fireMessageLoadedEvent(int type, int numMessages, int max) {
	for (int i = 0; i < messageLoadedListeners.size(); i ++) {
	    ((MessageLoadedListener)messageLoadedListeners.elementAt(i)).handleMessageLoaded(new MessageLoadedEvent(this, type, numMessages, max));
	}
    }

    /**
     * Adds a MessageLoadedListener to the messageLoadedListener list.
     */
    public void addMessageLoadedListener(MessageLoadedListener newListener) {
	if (messageLoadedListeners.indexOf(newListener) == -1)
	    messageLoadedListeners.add(newListener);
    }

    /**
     * Removes a MessageLoadedListener from the messageLoadedListener list,
     * if it's in the list.
     */

    public void removeMessageLoadedListener(MessageLoadedListener remListener) {
	if (messageLoadedListeners.indexOf(remListener) > -1)
	    messageLoadedListeners.remove(remListener);
    }

    /**
     * Adds the MessageProxy(s) to the loadQueue.
     */
    public synchronized void loadMessages(MessageProxy mp) {
	loadQueue.add(mp);
    }

    /**
     * Adds the MessageProxy(s) to the loadQueue.
     */
    public synchronized void loadMessages(MessageProxy[] mp) {
	if (mp != null && mp.length > 0) {
	    for (int i = 0; i < mp.length; i++) 
		loadQueue.add(mp[i]);
	}
    }

    /**
     * Adds the MessageProxy(s) to the loadQueue.
     */
    public synchronized void loadMessages(Vector mp) {
	if (mp != null && mp.size() > 0) 
	    for (int i = 0; i < mp.size(); i++)
		loadQueue.add(mp.elementAt(i));
    }

    /**
     * retrieves all the messages from the loadQueue, and resets that
     * Vector to 0 (an empty Vector).
     */
    public synchronized Vector retrieveLoadQueue() {
	Vector returnValue = loadQueue;
	loadQueue = new Vector();
	return returnValue;
    }

    public int getUpdateMessagesCount() {
	return updateMessagesCount;
    }

    public void setUpdateMessagesCount(int newValue) {
	updateMessagesCount = newValue;
    }

    public Vector getColumnValues() {
	return columnValues;
    }

    public void setColumnValues(Vector newValue) {
	columnValues=newValue;
    }

    public FolderInfo getFolderInfo() {
	return folderInfo;
    }

    public int getLoadedMessageCount() {
	return loadedMessageCount;
    }
}






