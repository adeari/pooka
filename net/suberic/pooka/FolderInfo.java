package net.suberic.pooka;

import javax.mail.*;
import javax.mail.event.*;
import javax.swing.event.EventListenerList;
import java.util.*;
import net.suberic.pooka.gui.*;
import net.suberic.pooka.thread.LoadMessageThread;
import net.suberic.pooka.event.*;
import net.suberic.util.ValueChangeListener;

/**
 * This class does all of the work for a Folder.  If a FolderTableModel,
 * FolderWindow, Message/Row-to-MessageProxy map, or FolderTreeNode exist
 * for a Folder, the FolderInfo object has a reference to it.
 */

public class FolderInfo implements MessageCountListener, ValueChangeListener, UserProfileContainer {

    private Folder folder;

    // The is the folder ID: storeName.parentFolderName.folderName
    private String folderID;

    // This is just the simple folderName, such as "INBOX"
    private String folderName;

    private EventListenerList messageCountListeners = new EventListenerList();
    private EventListenerList messageChangedListeners = new EventListenerList();
    
    // Information for the FolderNode
    private FolderNode folderNode;
    private Vector children;

    // Information for the FolderTable.
    private FolderTableModel folderTableModel;
    private Hashtable messageToProxyTable = new Hashtable();
    private Vector columnValues;
    private Vector columnNames;

    // GUI information.
    private LoadMessageThread loaderThread;
    private FolderWindow folderWindow;

    private boolean loaded = false;
    private boolean available = true;
    private boolean open = false;
    private boolean unread = false;

    private FolderInfo parentFolder = null;
    private StoreInfo parentStore = null;
    private UserProfile defaultProfile = null;

    /**
     * Creates a new FolderInfo from a parent FolderInfo and a Folder 
     * name.
     */
    
    public FolderInfo(FolderInfo parent, String fname) {
	parentFolder = parent;
	setFolderID(parent.getFolderID() + "." + fname);
	folderName = fname;

	if (parent.isAvailable() && parent.isLoaded())
	    loadFolder();

	updateChildren();
    }


    /**
     * Creates a new FolderInfo from a parent StoreInfo and a Folder 
     * name.
     */
    
    public FolderInfo(StoreInfo parent, String fname) {
	parentStore = parent;
	setFolderID(parent.getStoreID() + "." + fname);
	folderName = fname;

	if (parent.isConnected())
	    loadFolder();

	updateChildren();
    }
    
    /**
     * This actually loads up the Folder object itself.  This is used so 
     * that we can have a FolderInfo even if we're not connected to the
     * parent Store.
     */
    public void loadFolder() {
	if (isLoaded())
	    return;

	Folder[] tmpFolder;
	Folder tmpParentFolder;
	
	try {
	    if (parentStore != null) {
		try {
		    if (!parentStore.isConnected())
			parentStore.connectStore();
		    Store store = parentStore.getStore();
		    tmpParentFolder = store.getDefaultFolder();
		    tmpFolder = tmpParentFolder.list(folderName);
		} catch (MessagingException me) {
		    tmpFolder =null;
		}
	    } else {
		if (!parentFolder.isLoaded())
		    parentFolder.loadFolder();
		if (!parentFolder.isLoaded()) {
		    tmpFolder = null;
		} else {
		    tmpParentFolder = parentFolder.getFolder();
		    tmpFolder = tmpParentFolder.list(folderName);
		}
	    }
	    if (tmpFolder != null && tmpFolder.length > 0) {
		folder = tmpFolder[0];
		available = true;
	    } else {
		available = false;
		open = false;
		folder = null;
	    }
	    loaded = true;
	} catch (MessagingException me) {
	    loaded = false;
	    open = false;
	    folder = null;
	} 
	
	if (folder != null) {
	    initializeFolderInfo();
	}

    }

    /**
     * this is called by the constructors if a proper Folder object 
     * is returned.
     */
    private void initializeFolderInfo() {
	folder.addMessageCountListener(this);
	Pooka.getResources().addValueChangeListener(this, getFolderProperty());
	Pooka.getResources().addValueChangeListener(this, getFolderProperty() + ".folderList");
	Pooka.getResources().addValueChangeListener(this, getFolderProperty() + ".defaultProfile");

	folder.addConnectionListener(new ConnectionAdapter() { 
		public void closed(ConnectionEvent e) {
		    if (Pooka.isDebug())
			System.out.println("Folder " + getFolderID() + " closed.");
		    if (open == true) {
			try {
			    Store store = getFolder().getStore();
			    if (!(store.isConnected()))
				store.connect();
			    openFolder(Folder.READ_WRITE);
			} catch (MessagingException me) {
			    System.out.println("Folder " + getFolderID() + " closed and unable to reopen:  " + me.getMessage());
			}
		    }
		}
		
		public void disconnected(ConnectionEvent e) {
		    if (Pooka.isDebug())
			System.out.println("Folder " + getFolderID() + " disconnected.");
		    if (open == true) {
			try {
			    Store store = getFolder().getStore();
			    if (!(store.isConnected()))
				store.connect();
			    openFolder(Folder.READ_WRITE);
			} catch (MessagingException me) {
			    System.out.println("Folder " + getFolderID() + " disconnected and unable to reconnect:  " + me.getMessage());
			}
		    }
		}
	    });

	String defProfile = Pooka.getProperty(getFolderProperty() + ".defaultProfile", "");
	if (!defProfile.equals(""))
	    defaultProfile = UserProfile.getProfile(defProfile);

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
	    if (!(getFolder().isOpen())) {
		openFolder(Folder.READ_WRITE);
	    }
	    Message[] msgs = folder.getMessages();
	    MessageProxy mp;

	    for (int i = 0; i < msgs.length; i++) {
		mp = new MessageProxy(getColumnValues(), msgs[i], this);

		messageProxies.add(mp);
		messageToProxyTable.put(msgs[i], mp);
	    }

	} catch (MessagingException me) {
	    System.out.println("aigh!  messaging exception while loading!  implement Pooka.showError()!");
	}

	FolderTableModel ftm = new FolderTableModel(messageProxies, getColumnNames());

	setFolderTableModel(ftm);


	loaderThread.loadMessages(messageProxies);
	
	loaderThread.start();

	return ftm;
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
	    int numUnread = getFolder().getUnreadMessageCount();
	    int countUnread = 0;
	    int i;
	    if (numUnread > 0) {
		Message[] messages = getFolder().getMessages();
		for (i = messages.length - 1; ( i >= 0 && countUnread < numUnread) ; i--) {
		    if (!(messages[i].isSet(Flags.Flag.SEEN))) 
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
     * This goes through the list of children of this folder and
     * returns the FolderInfo for the given childName, if one exists.
     * If none exists, or if the children Vector has not been loaded
     * yet, or if this is a leaf node, then this method returns null.
     */
    public FolderInfo getChild(String childName) {
	if (children != null) {
	    for (int i = 0; i < children.size(); i++)
		if (((FolderInfo)children.elementAt(i)).getFolderName().equals(childName))
		    return (FolderInfo)children.elementAt(i);
	}
	
	// no match.
	return null;
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

	resetUnread();

	if (Pooka.isDebug())
	    System.out.println("firing message changed event.");
	// Guaranteed to return a non-null array
	Object[] listeners = messageChangedListeners.getListenerList();
	// Process the listeners last to first, notifying
	// those that are interested in this event
	for (int i = listeners.length-2; i>=0; i-=2) {
	    if (Pooka.isDebug())
		System.out.println("listeners[" + i + "] is " + listeners[i] );
	    if (listeners[i]==MessageChangedListener.class) {
		if (Pooka.isDebug())
		    System.out.println("check.  running messageChanged on listener.");
		((MessageChangedListener)listeners[i+1]).messageChanged(mce);
	    }              
	}
    }  

    /**
     * This handles the changes if the source property is modified.
     *
     * As defined in net.suberic.util.ValueChangeListener.
     */

    public void valueChanged(String changedValue) {
	if (changedValue.equals(getFolderProperty() + ".folderList")) {
	    updateChildren();
	    if (folderNode != null) {
		((javax.swing.tree.DefaultTreeModel)(((FolderPanel)folderNode.getParentContainer()).getFolderTree().getModel())).nodeStructureChanged(folderNode);
	    }
	} else if (changedValue.equals(getFolderProperty() + ".defaultProfile")) {
	    if (Pooka.getProperty(changedValue, "").equals(""))
		defaultProfile = null;
	    else 
		defaultProfile = UserProfile.getProfile(Pooka.getProperty(changedValue, ""));
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

    public void fireMessageCountEvent(MessageCountEvent mce) {

	// from the EventListenerList javadoc, including comments.

	// Guaranteed to return a non-null array
	Object[] listeners = messageCountListeners.getListenerList();
	// Process the listeners last to first, notifying
	// those that are interested in this event

	if (mce.getType() == MessageCountEvent.ADDED) {
	    for (int i = listeners.length-2; i>=0; i-=2) {
		if (listeners[i]==MessageCountListener.class) {
		    ((MessageCountListener)listeners[i+1]).messagesAdded(mce);
		}              
	    }
	} else if (mce.getType() == MessageCountEvent.REMOVED) {
	    for (int i = listeners.length-2; i>=0; i-=2) {
		if (listeners[i]==MessageCountListener.class) {
		    ((MessageCountListener)listeners[i+1]).messagesRemoved(mce);
		}              
	    }

	}
    }
	
    public void addMessageChangedListener(MessageChangedListener newListener) {
	messageChangedListeners.add(MessageChangedListener.class, newListener);
    }

    public void removeMessageChangedListener(MessageChangedListener oldListener) {
	messageChangedListeners.remove(MessageChangedListener.class, oldListener);
    }

    // as defined in javax.mail.event.MessageCountListener

    public void messagesAdded(MessageCountEvent e) {
	if (Pooka.isDebug())
	    System.out.println("Messages added.");
	if (folderTableModel != null) {
	    Message[] addedMessages = e.getMessages();
	    MessageProxy mp;
	    Vector addedProxies = new Vector();
	    for (int i = 0; i < addedMessages.length; i++) {
		mp = new MessageProxy(getColumnValues(), addedMessages[i], this);
		addedProxies.add(mp);
		messageToProxyTable.put(addedMessages[i], mp);
	    }
	    getFolderTableModel().addRows(addedProxies);
	}
	resetUnread();
	fireMessageCountEvent(e);
    }

    public void messagesRemoved(MessageCountEvent e) {
	if (Pooka.isDebug())
	    System.out.println("Messages Removed.");
	if (folderTableModel != null) {
	    Message[] removedMessages = e.getMessages();
	    if (Pooka.isDebug())
		System.out.println("removedMessages was of size " + removedMessages.length);
	    MessageProxy mp;
	    Vector removedProxies=new Vector();
	    for (int i = 0; i < removedMessages.length; i++) {
		if (Pooka.isDebug())
		    System.out.println("checking for existence of message.");
		mp = getMessageProxy(removedMessages[i]);
		if (mp != null) {
		    if (Pooka.isDebug())
			System.out.println("message exists--removing");
		    removedProxies.add(mp);
		    messageToProxyTable.remove(mp);
		}
	    }
	    getFolderTableModel().removeRows(removedProxies);
	}
	resetUnread();
	fireMessageCountEvent(e);
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
	if (! isLoaded())
	    loadFolder();
	
	if (isLoaded() && isAvailable()) {
	    if (folder.isOpen()) {
		if (folder.getMode() == mode)
		    return;
		else { 
		    closeFolder(false);
		    openFolder(mode);
		}
	    } else {
		folder.open(mode);
		open=true;
		resetUnread();
		if (getFolderNode() != null)
		    getFolderNode().getParentContainer().repaint();
	    }
	}
    }

    /**
     * This method calls openFolder() on this FolderInfo, and then, if
     * this FolderInfo has any children, calls openFolder() on them,
     * also.  
     * 
     * This is usually called by StoreInfo.connectStore() if 
     * Pooka.openFoldersOnConnect is set to true.
     */

    public void openAllFolders(int mode) {
	try {
	    openFolder(mode);
	} catch (MessagingException me) {
	}

	if (children != null)
	    for (int i = 0; i < children.size(); i++) 
		try {
		    ((FolderInfo)children.elementAt(i)).openFolder(mode);
		} catch (MessagingException me) {
		}
	
    }
    

    /**
     * This method closes the Folder.  If you open the Folder using 
     * openFolder (which you should), then you should use this method
     * instead of calling getFolder.close().  If you don't, then the
     * FolderInfo will try to reopen the folder.
     */
    public void closeFolder(boolean expunge) throws MessagingException {
	if (isLoaded()) {
	    if (!(folder.isOpen()))
		return;
	    else {
		open=false;
		folder.close(expunge);
	    }
	}
    }
    
    // Accessor methods.

    public Folder getFolder() {
	return folder;
    }

    private void setFolder(Folder newValue) {
	folder=newValue;
    }

    /**
     * This returns the FolderID, such as "myStore.INBOX".
     */
    public String getFolderID() {
	return folderID;
    }

    /**
     * This sets the folderID.
     */
    private void setFolderID(String newValue) {
	folderID=newValue;
    }

    /**
     * This returns the simple folderName, such as "INBOX".
     */
    public String getFolderName() {
	return folderName;
    }

    /**
     * This returns the property which defines this FolderNode, such as
     * "Store.myStore.INBOX".
     */
    public String getFolderProperty() {
	return "Store." + getFolderID();
    }

    public Vector getChildren() {
	return children;
    }

    public FolderNode getFolderNode() {
	return folderNode;
    }

    public void setFolderNode(FolderNode newValue) {
	folderNode = newValue;
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

    public boolean isOpen() {
	return open;
    }

    public boolean isAvailable() {
	return available;
    }

    public boolean isLoaded() {
	return loaded;
    }

    public boolean hasUnread() {
	return unread;
    }

    private void resetUnread() {
	try {
	    if (Pooka.isDebug())
		System.out.println("running resetUnread.  unread message count is " + getFolder().getUnreadMessageCount());

	    unread = (getFolder().getUnreadMessageCount() > 0);
	} catch (MessagingException me) {
	    unread = false;
	}
    }

    /**
     * This returns the parentFolder.  If this FolderInfo is a direct
     * child of a StoreInfo, this method will return null.
     */
    public FolderInfo getParentFolder() {
	return parentFolder;
    }

    /**
     * This method actually returns the parent StoreInfo.  If this 
     * particular FolderInfo is a child of another FolderInfo, this
     * method will call getParentStore() on that FolderInfo.
     */
    public StoreInfo getParentStore() {
	if (parentStore == null)
	    return parentFolder.getParentStore();
	else
	    return parentStore;
    }

    public UserProfile getDefaultProfile() {
	if (defaultProfile != null) {
	    return defaultProfile;
	}
	else if (parentFolder != null) {
	    return parentFolder.getDefaultProfile();
	}
	else if (parentStore != null) {
	    return parentStore.getDefaultProfile();
	}
	else {
	    return null;
	}
    }
}
