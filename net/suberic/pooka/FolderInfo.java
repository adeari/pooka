package net.suberic.pooka;

import javax.mail.*;
import javax.mail.event.*;
import javax.swing.event.EventListenerList;
import java.util.*;
import net.suberic.pooka.gui.*;
import net.suberic.pooka.thread.LoadMessageThread;
import net.suberic.pooka.event.*;

/**
 * This class does all of the work for a Folder.  If a FolderTableModel,
 * FolderWindow, Message/Row-to-MessageProxy map, or FolderTreeNode exist
 * for a Folder, the FolderInfo object has a reference to it.
 */

public class FolderInfo {
    private Folder folder;
    private String folderID;
    private EventListenerList messageCountListeners = new EventListenerList();
    private EventListenerList messageChangedListeners = new EventListenerList();
    private FolderTableModel folderTableModel;
    private Hashtable messageToProxyTable = new Hashtable();
    private Vector columnValues;
    private Vector columnNames;
    private LoadMessageThread loaderThread;
    private FolderWindow folderWindow;

    /**
     * Creates a new FolderInfo from a Folder and a Folder ID (like 
     * Store.defaultStore.folderList.folderName).
     */

    public FolderInfo(Folder f, String fid) {
	folder=f;
	folderID=fid;
    }

    /**
     * Loads all Messages into a new FolderTableModel, sets this 
     * FolderTableModel as the current FolderTableModel, and then returns
     * said FolderTableModel.  This is the basic way to populate a new
     * FolderTableModel.
     */
    public FolderTableModel loadAllMessages() {
	Vector messageProxies = new Vector();

	if (columnValues == null) {
	    Enumeration tokens = Pooka.getResources().getPropertyAsEnumeration("FolderTable", "");
	    Vector colvals = new Vector();
	    Vector colnames = new Vector();

	    String tmp;
	
	    while (tokens.hasMoreElements()) {
		tmp = (String)tokens.nextElement();
		colvals.addElement(Pooka.getProperty("FolderTable." + tmp + ".value", tmp));
		colnames.addElement(Pooka.getProperty("FolderTable." + tmp + ".label", tmp));
	    }	    
	    setColumnNames(colnames);
	    setColumnValues(colvals);
	}
	    
	if (loaderThread == null) 
	    loaderThread = createLoaderThread();

	try {
	    if (!(getFolder().isOpen()))
		getFolder().open(Folder.READ_WRITE);
	    Message[] msgs = folder.getMessages();
	    MessageProxy mp;

	    for (int i = 0; i < msgs.length; i++) {
		mp = new MessageProxy(getColumnValues(), msgs[i], this);
		System.out.println("Created MessageProxy " + i);

		messageProxies.add(mp);
		mp.loadTableInfo();
	    }

		System.out.println("total size of messageProxies is " + messageProxies.size());

		//	    loaderThread.loadMessages(messageProxies);

		//	    loaderThread.start();

	} catch (MessagingException me) {
	    System.out.println("aigh!  messaging exception while loading!  implement Pooka.showError()!");
	}

	FolderTableModel ftm = new FolderTableModel(messageProxies, getColumnNames());

	setFolderTableModel(ftm);

	return ftm;
    }
    
    /**
     * Creates the column values from the FolderTable property.
     */
    public Vector createColumnValues() {

	return columnValues;
    }
    
    /**
     * creates the loaded thread.
     */
    public LoadMessageThread createLoaderThread() {
	LoadMessageThread lmt = new LoadMessageThread(this);
	return lmt;
    }
    
    
    /**
     * This handles the MessageLoadedEvent.
     *
     * As defined in interface net.suberic.pooka.event.MessageLoadedListener.
     */

    public void fireMessageChangedEvent(MessageChangedEvent mce) {
	// from the EventListenerList javadoc, including comments.

	System.out.println("firing message changed event.");
	// Guaranteed to return a non-null array
	Object[] listeners = messageChangedListeners.getListenerList();
	// Process the listeners last to first, notifying
	// those that are interested in this event
	for (int i = listeners.length-2; i>=0; i-=2) {
	    System.out.println("listeners[" + i + "] is " + listeners[i] );
	    if (listeners[i]==MessageChangedListener.class) {
		System.out.println("check.  running messageChanged on listener.");
		((MessageChangedListener)listeners[i+1]).messageChanged(mce);
	    }              
	}
    }  

    // semi-accessor methods.

    public MessageProxy getMessageProxy(int rowNumber) {
	return getFolderTableModel().getMessageProxy(rowNumber);
    }

    public MessageProxy getMessageProxy(Message m) {
	return (MessageProxy)messageToProxyTable.get(m);
    }

    public void addMessageCountListener(MessageCountListener newListener) {
	messageCountListeners.add(MessageCountListener.class, newListener);
    }
	
    public void removeMessageCountListener(MessageCountListener oldListener) {
	messageCountListeners.remove(MessageCountListener.class, oldListener);
    }
	
    public void addMessageChangedListener(MessageChangedListener newListener) {
	messageChangedListeners.add(MessageChangedListener.class, newListener);
    }

    public void removeMessageChangedListener(MessageChangedListener oldListener) {
	messageChangedListeners.remove(MessageChangedListener.class, oldListener);
    }

    // Accessor methods.

    public Folder getFolder() {
	return folder;
    }

    private void setFolder(Folder newValue) {
	folder=newValue;
    }

    public String getFolderID() {
	return folderID;
    }

    private void setFolderID(String newValue) {
	folderID=newValue;
    }

    public FolderTableModel getFolderTableModel() {
	if (folderTableModel == null) 
	    return loadAllMessages();
	else 
	    return folderTableModel;
    }

    public void setFolderTableModel(FolderTableModel newValue) {
	folderTableModel = newValue;
    }

    public Vector getColumnValues() {
	return columnValues;
    }

    public void setColumnValues(Vector newValue) {
	columnValues = newValue;
    }

    public Vector getColumnNames() {
	return columnNames;
    }

    public void setColumnNames(Vector newValue) {
	columnNames = newValue;
    }

    public FolderWindow getFolderWindow() {
	return folderWindow;
    }

    public void setFolderWindow(FolderWindow newValue) {
	folderWindow = newValue;
    }
}
