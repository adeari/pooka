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
 * This class does all of the work for a Folder.  If a FolderTableModel,
 * FolderWindow, Message/Row-to-MessageInfo map, or FolderTreeNode exist
 * for a Folder, the FolderInfo object has a reference to it.
 */

public class FolderInfo implements MessageCountListener, ValueChangeListener, UserProfileContainer, MessageChangedListener {

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
    private Hashtable messageToInfoTable = new Hashtable();
    private Vector columnValues;
    private Vector columnNames;
    private Vector columnSizes;

    // GUI information.
    private FolderWindow folderWindow;
    private Action[] defaultActions;

    //filters
    private MessageFilter[] filters = null;

    private LoadMessageThread loaderThread;
    private FolderTracker folderTracker = null;

    private boolean loaded = false;
    private boolean loading = false;
    private boolean available = true;
    private boolean open = false;
    private int unreadCount = 0;
    private int messageCount = 0;
    private boolean newMessages = false;

    private FolderInfo parentFolder = null;
    private StoreInfo parentStore = null;
    private UserProfile defaultProfile = null;

    private boolean sentFolder = false;
    private boolean trashFolder = false;
    
    protected boolean uidFolder = false;

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

	createFilters();

	resetDefaultActions();

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

	createFilters();

	resetDefaultActions();
    }
    
    /**
     * This resets the defaultActions.  Useful when this goes to and from
     * being a trashFolder, since only trash folders have emptyTrash
     * actions.
     */
    public void resetDefaultActions() {
	if (isTrashFolder())
	    defaultActions = new Action[] {
		new net.suberic.util.thread.ActionWrapper(new UpdateCountAction(), getFolderThread()),
		new net.suberic.util.thread.ActionWrapper(new EmptyTrashAction(), getFolderThread())
		    
		    };
	else
	    defaultActions = new Action[] {
		new net.suberic.util.thread.ActionWrapper(new UpdateCountAction(), getFolderThread())
		    };
    }


    /**
     * This takes the FolderProperty.filters property and uses it to populate
     * the messageFilters array.
     */
    public void createFilters() {
	Vector filterNames=Pooka.getResources().getPropertyAsVector(getFolderProperty() + ".filters", "");
	if (filterNames != null && filterNames.size() > 0) {
	    filters = new MessageFilter[filterNames.size()];
	    for (int i = 0; i < filterNames.size(); i++) {
		filters[i] = new MessageFilter(getFolderProperty() + ".filters." + (String) filterNames.elementAt(i));
	    }
	}
    }

    /**
     * This applies each MessageFilter in filters array on the given 
     * MessageInfo objects.
     *
     * @return a Vector containing the removed MessageInfo objects.
     */
    public Vector applyFilters(Vector messages) {
	Vector notRemovedYet = new Vector(messages);
	Vector removed = new Vector();
	if (filters != null) 
	    for (int i = 0; i < filters.length; i++) {
		if (filters[i] != null) {
		    Vector justRemoved = filters[i].filterMessages(notRemovedYet);
		    removed.addAll(justRemoved);
		    notRemovedYet.removeAll(justRemoved);
		}
	}
	
	return removed;
    }

    /**
     * This actually loads up the Folder object itself.  This is used so 
     * that we can have a FolderInfo even if we're not connected to the
     * parent Store.
     */
    public void loadFolder() {
	if (isLoaded() || (loading && children == null)) 
	    return;

	Folder[] tmpFolder;
	Folder tmpParentFolder;
	
	try {
	    loading = true;
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
		setFolder(tmpFolder[0]);
		available = true;
		folder.addMessageChangedListener(this);
	    } else {
		available = false;
		open = false;
		setFolder(null);
	    }
	    loaded = true;
	} catch (MessagingException me) {
	    loaded = false;
	    open = false;
	    setFolder(null);
	} finally {
	    loading = false;
	}
	
	if (folder != null) {
	    initializeFolderInfo();
	}

    }

    /**
     * this is called by loadFolders if a proper Folder object 
     * is returned.
     */
    private void initializeFolderInfo() {
	folder.addMessageCountListener(this);
	Pooka.getResources().addValueChangeListener(this, getFolderProperty());
	Pooka.getResources().addValueChangeListener(this, getFolderProperty() + ".folderList");
	Pooka.getResources().addValueChangeListener(this, getFolderProperty() + ".defaultProfile");
	
	folder.addConnectionListener(new ConnectionAdapter() { 
		public void closed(ConnectionEvent e) {
		    if (Pooka.isDebug()) {
			System.out.println("Folder " + getFolderID() + " closed:  " + e);
		    }
		    
		    /*
		      if (open == true) {
			try {
			    Store store = getFolder().getStore();
			    if (!(store.isConnected()))
				store.connect();
			    openFolder(Folder.READ_WRITE);
			    //refreshAllMessages();
			} catch (MessagingException me) {
			    System.out.println("Folder " + getFolderID() + " closed and unable to reopen:  " + me.getMessage());
			    try {
				closeFolder(false);
			    } catch (Exception ex) {
			    System.out.println("Failure marking FolderInfo " + getFolderID() + " as closed.");
			    }
			}
		    }
		    */

		    try {
			closeFolder(false);
		    } catch (Exception ex) {
			if (Pooka.isDebug())
			    System.out.println("Failure marking FolderInfo " + getFolderID() + " as closed:  " + ex.getMessage());
		    }
		    
		}
		
		public void disconnected(ConnectionEvent e) {
		    if (Pooka.isDebug()) {
			System.out.println("Folder " + getFolderID() + " disconnected.");
			Thread.dumpStack();
		    }
		    
		    /*
		    if (open == true) {
			try {
			    Store store = getFolder().getStore();
			    if (!(store.isConnected()))
				store.connect();
			    openFolder(Folder.READ_WRITE);
			    //refreshAllMessages();
			} catch (MessagingException me) {
			    System.out.println("Folder " + getFolderID() + " disconnected and unable to reconnect:  " + me.getMessage());
			    try {
				closeFolder(false);
			    } catch (Exception ex) {
			    System.out.println("Failure marking FolderInfo " + getFolderID() + " as closed.");

			    }
			}
		    }
		    */
		    
		    try {
			closeFolder(false);
		    } catch (MessagingException me) {
			if (Pooka.isDebug())
			    System.out.println("Failure marking FolderInfo " + getFolderID() + " as closed:  " + me.getMessage());
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
	String tableType;

	if (isSentFolder())
	    tableType="SentFolderTable";
	else
	    tableType="FolderTable";

	Vector messageProxies = new Vector();

	FetchProfile fp = new FetchProfile();
	fp.add(FetchProfile.Item.FLAGS);
	if (columnValues == null) {
	    Enumeration tokens = Pooka.getResources().getPropertyAsEnumeration(tableType, "");
	    Vector colvals = new Vector();
	    Vector colnames = new Vector();
	    Vector colsizes = new Vector();
	    
	    String tmp;
	
	    while (tokens.hasMoreElements()) {
		tmp = (String)tokens.nextElement();
		String value = Pooka.getProperty(tableType + "." + tmp + ".value", tmp);
		colvals.addElement(value);
		fp.add(value);
		colnames.addElement(Pooka.getProperty(tableType + "." + tmp + ".label", tmp));
		colsizes.addElement(Pooka.getProperty(tableType + "." + tmp + ".size", tmp));
	    }	    
	    setColumnNames(colnames);
	    setColumnValues(colvals);
	    setColumnSizes(colsizes);
	}
	    
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
		mi = new MessageInfo(msgs[i], this);

		messageProxies.add(new MessageProxy(getColumnValues() , mi));
		messageToInfoTable.put(msgs[i], mi);
	    }

	} catch (MessagingException me) {
	    System.out.println("aigh!  messaging exception while loading!  implement Pooka.showError()!");
	}

	FolderTableModel ftm = new FolderTableModel(messageProxies, getColumnNames(), getColumnSizes());

	setFolderTableModel(ftm);


	loaderThread.loadMessages(messageProxies);
	
	if (!loaderThread.isAlive())
	    loaderThread.start();

	return ftm;
    }
    
    /**
     * Unloads all messages.  This should be run if ever the current message
     * information becomes out of date, as can happen when the connection
     * to the folder goes down.
     */
    public void unloadAllMessages() {
	folderTableModel = null;
    }
    
    /**
     * Refreshes all the MessageInfo objects by the UID, if any.
     */
    /*
    public void refreshAllMessages() {
	if (folder instanceof UIDFolder) {
	    UIDFolder uidFolder = (UIDFolder) folder;
	    Hashtable newMessageToInfoTable = new Hashtable();
	    Enumeration keys = messageToInfoTable.keys(); 
	    while (keys.hasMoreElements()) {
		MessageInfo proxy = (MessageInfo) messageToInfoTable.get(keys.nextElement());
		Message m = proxy.refreshMessage();
		if (m != null)
		    newMessageToInfoTable.put(m, proxy);
	    }
	}	
    }
    */

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
	    if (unreadCount > 0) {
		Message[] messages = getFolder().getMessages();
		for (i = messages.length - 1; ( i >= 0 && countUnread < unreadCount) ; i--) {
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
	FolderInfo childFolder = null;
	String folderName  = null, subFolderName = null;

	if (children != null) {
	    int divider = childName.indexOf('/');
	    if (divider > 0) {
		folderName = childName.substring(0, divider);
		if (divider < childName.length() - 1)
		    subFolderName = childName.substring(divider + 1);
	    } else 
		folderName = childName;
	    
	    for (int i = 0; i < children.size(); i++)
		if (((FolderInfo)children.elementAt(i)).getFolderName().equals(folderName))
		    childFolder = (FolderInfo)children.elementAt(i);
	}
	
	if (childFolder != null && subFolderName != null)
	    return childFolder.getChild(subFolderName);
	else
	    return childFolder;
    }

    /**
     * This goes through the list of children of this store and
     * returns the FolderInfo that matches this folderID.
     * If none exists, or if the children Vector has not been loaded
     * yet, or if this is a leaf node, then this method returns null.
     */
    public FolderInfo getFolderById(String folderID) {
	FolderInfo childFolder = null;

	if (getFolderID().equals(folderID))
	    return this;

	if (children != null) {
	    for (int i = 0; i < children.size(); i++) {
		FolderInfo possibleMatch = ((FolderInfo)children.elementAt(i)).getFolderById(folderID);
		if (possibleMatch != null) {
		    return possibleMatch;
		}
	    }
	}

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
     * This sets the given Flag for all the MessageInfos given.
     */
    public void setFlags(MessageInfo[] msgs, Flags flag, boolean value) throws MessagingException {
	Message[] m = new Message[msgs.length];
	for (int i = 0; i < msgs.length; i++) {
	    m[i] = msgs[i].getMessage();
	}

	getFolder().setFlags(m, flag, value);
    }

    /**
     * This copies the given messages to the given FolderInfo.
     */
    public void copyMessages(MessageInfo[] msgs, FolderInfo targetFolder) throws MessagingException {
	Message[] m = new Message[msgs.length];
	for (int i = 0; i < msgs.length; i++) {
	    m[i] = msgs[i].getMessage();
	}

	getFolder().copyMessages(m, targetFolder.getFolder());
    }
    
    /**
     * This expunges the deleted messages from the Folder.
     */
    public void expunge() throws MessagingException {
	getFolder().expunge();
    }

    /**
     * This handles the MessageLoadedEvent.
     *
     * As defined in interface net.suberic.pooka.event.MessageLoadedListener.
     */

    public void fireMessageChangedEvent(MessageChangedEvent mce) {
	// from the EventListenerList javadoc, including comments.

	resetMessageCounts();

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

    /**
     * This subscribes to the FolderInfo indicated by the given String.
     * If this defines a subfolder, then that subfolder is added to this
     * FolderInfo, if it doesn't already exist.
     */
    public void subscribeFolder(String folderName) {
	String subFolderName = null;
	String childFolderName = null;
	int firstSlash = folderName.indexOf('/');
	while (firstSlash == 0) {
	    folderName = folderName.substring(1);
	    firstSlash = folderName.indexOf('/');
	}

	if (firstSlash > 0) {
	    childFolderName = folderName.substring(0, firstSlash);
	    if (firstSlash < folderName.length() -1)
		subFolderName = folderName.substring(firstSlash +1);
	    
	} else
	    childFolderName = folderName;

	this.addToFolderList(childFolderName);

	FolderInfo childFolder = getChild(childFolderName);

	if (childFolder != null && subFolderName != null)
	    childFolder.subscribeFolder(subFolderName);	
    }

    /**
     * This adds the given folderString to the folderList of this
     * FolderInfo.
     */
    void addToFolderList(String addFolderName) {
	Vector folderNames = Pooka.getResources().getPropertyAsVector(getFolderProperty() + ".folderList", "");
	
	boolean found = false;

	for (int i = 0; i < folderNames.size(); i++) {
	    folderName = (String) folderNames.elementAt(i);

	    if (folderName.equals(addFolderName)) {
		found=true;
	    }
	    
	}
	
	if (!found) {
	    String currentValue = Pooka.getProperty(getFolderProperty() + ".folderList", "");
	    if (currentValue.equals(""))
		Pooka.setProperty(getFolderProperty() + ".folderList", addFolderName);
	    else
		Pooka.setProperty(getFolderProperty() + ".folderList", currentValue + ":" + addFolderName);
	}
			      
    }

    /**
     * This unsubscribes this FolderInfo and all of its children, if 
     * applicable.
     *
     * This implementation just removes the defining properties from
     * the Pooka resources.
     */
    public void unsubscribe() {
	
	if (children != null && children.size() > 0) {
	    for (int i = 0; i < children.size(); i++) 
		((FolderInfo)children.elementAt(i)).unsubscribe();
	}

	Pooka.getResources().removeValueChangeListener(this);
	if (getFolderWindow() != null)
	    getFolderWindow().closeFolderWindow();

	Pooka.getResources().removeProperty(getFolderProperty() + ".folderList");

	if (parentFolder != null)
	    parentFolder.removeFromFolderList(getFolderName());
	else if (parentStore != null)
	    parentStore.removeFromFolderList(getFolderName());
	
    }

    /**
     * This returns whether or not this Folder is set up to use the 
     * TrashFolder for the Store.  If this is a Trash Folder itself, 
     * then return false.  If FolderProperty.useTrashFolder is set, 
     * return that.  else go up the tree, until, in the end, 
     * Pooka.useTrashFolder is returned.
     */
    public boolean useTrashFolder() {
	if (isTrashFolder())
	    return false;

	String prop = Pooka.getProperty(getFolderProperty() + ".useTrashFolder", "");
	if (!prop.equals(""))
	    return (! prop.equalsIgnoreCase("false"));
	
	if (getParentFolder() != null)
	    return getParentFolder().useTrashFolder();
	else if (getParentStore() != null)
	    return getParentStore().useTrashFolder();
	else
	    return (! Pooka.getProperty("Pooka.useTrashFolder", "true").equalsIgnoreCase("true"));
    }

    /**
     * This removes all the messages in the folder, if the folder is a
     * TrashFolder.
     */
    public void emptyTrash() {
	if (isTrashFolder()) {
	    try {
		Message[] allMessages = getFolder().getMessages();
		getFolder().setFlags(allMessages, new Flags(Flags.Flag.DELETED), true);
		getFolder().expunge();
	    } catch (MessagingException me) {
		String m = Pooka.getProperty("error.trashFolder.EmptyTrashError", "Error emptying Trash:") +"\n" + me.getMessage();
		if (getFolderWindow() != null) 
		    getFolderWindow().showError(m);
		else
		    System.out.println(m);
	    }
	}
    }

    /**
     * Remove the given String from the folderList property.  
     *
     * Note that because this is also a ValueChangeListener to the
     * folderList property, this will also result in the FolderInfo being
     * removed from the children Vector.
     */
    void removeFromFolderList(String removeFolderName) {
	Vector folderNames = Pooka.getResources().getPropertyAsVector(getFolderProperty() + ".folderList", "");
	
	boolean first = true;
	StringBuffer newValue = new StringBuffer();
	String folderName;

	for (int i = 0; i < folderNames.size(); i++) {
	    folderName = (String) folderNames.elementAt(i);

	    if (! folderName.equals(removeFolderName)) {
		if (!first)
		    newValue.append(":");
		
		newValue.append(folderName);
		first = false;
	    }
	    
	}
	
	Pooka.setProperty(getFolderProperty() + ".folderList", newValue.toString());
    }

    // semi-accessor methods.

    public MessageProxy getMessageProxy(int rowNumber) {
	return getFolderTableModel().getMessageProxy(rowNumber);
    }

    public MessageInfo getMessageInfo(Message m) {
	return (MessageInfo)messageToInfoTable.get(m);
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

	getFolderThread().addToQueue(new net.suberic.util.thread.ActionWrapper(new javax.swing.AbstractAction() {
	    public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
		MessageCountEvent mce = (MessageCountEvent)actionEvent.getSource();
		if (folderTableModel != null) {
		    Message[] addedMessages = mce.getMessages();
		    MessageInfo mp;
		    Vector addedProxies = new Vector();
		    for (int i = 0; i < addedMessages.length; i++) {
			mp = new MessageInfo(addedMessages[i], FolderInfo.this);
			addedProxies.add(new MessageProxy(getColumnValues(), mp));
			messageToInfoTable.put(addedMessages[i], mp);
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
	    }, getFolderThread()), new java.awt.event.ActionEvent(e, 1, "message-count-changed"));
				     }
	
    public void messagesRemoved(MessageCountEvent e) {
	if (Pooka.isDebug())
	    System.out.println("Messages Removed.");
	
	getFolderThread().addToQueue(new net.suberic.util.thread.ActionWrapper(new javax.swing.AbstractAction() {
	    public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
		MessageCountEvent mce = (MessageCountEvent)actionEvent.getSource();		
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
			}
		    }
		    getFolderTableModel().removeRows(removedProxies);
		}
		resetMessageCounts();
		fireMessageCountEvent(mce);
	    }
	}, getFolderThread()), new java.awt.event.ActionEvent(e, 1, "message-changed"));
    }
    
    /**
     * This updates the TableInfo on the changed messages.
     * 
     * As defined by java.mail.MessageChangedListener.
     */

    public void messageChanged(MessageChangedEvent e) {
	// blech.  we really have to do this on the action thread.
	getFolderThread().addToQueue(new net.suberic.util.thread.ActionWrapper(new javax.swing.AbstractAction() {
		public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
		    MessageChangedEvent mce = (MessageChangedEvent)actionEvent.getSource();
		    // if the message is getting deleted, then we don't
		    // really need to update the table info.  for that 
		    // matter, it's likely that we'll get MessagingExceptions
		    // if we do, anyway.
		    try {
			if (!mce.getMessage().isSet(Flags.Flag.DELETED) || ! Pooka.getProperty("Pooka.autoExpunge", "true").equalsIgnoreCase("true")) {
			    MessageInfo mi = getMessageInfo(mce.getMessage());
			    MessageProxy mp = mi.getMessageProxy();
			    if (mp != null) {
				mp.unloadTableInfo();
				mp.loadTableInfo();
				if (mce.getMessageChangeType() == MessageChangedEvent.FLAGS_CHANGED)
				    mi.refreshFlags();
				else if (mce.getMessageChangeType() == MessageChangedEvent.ENVELOPE_CHANGED)
				    mi.refreshHeaders();
			    }
			}
		    } catch (MessagingException me) {
			// if we catch a MessagingException, it just means
			// that the message has already been expunged.
		    }
		    
		    fireMessageChangedEvent(mce);
		}
	    }, getFolderThread()), new java.awt.event.ActionEvent(e, 1, "message-changed"));
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
	
	if (!getParentStore().isConnected())
	    getParentStore().connectStore();

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
		resetMessageCounts();
		if (getFolderNode() != null)
		    getFolderNode().getParentContainer().repaint();
	    }
	}

	// if we got to this point, we should assume that the open worked.

	if (getFolderTracker() == null) {
	    FolderTracker tracker = Pooka.getFolderTracker();
	    tracker.addFolder(this);
	    this.setFolderTracker(tracker);
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
     * Searches for messages in this folder which match the given
     * SearchTerm.
     *
     * Basically wraps the call to Folder.search(), and then wraps the
     * returned Message objects as MessageInfos.
     */
    public MessageInfo[] search(javax.mail.search.SearchTerm term) 
    throws MessagingException {
	Message[] returnValue = folder.search(term);
	System.out.println("got " + returnValue.length + " results.");
	return null;

    }

    /**
     * This method closes the Folder.  If you open the Folder using 
     * openFolder (which you should), then you should use this method
     * instead of calling getFolder.close().  If you don't, then the
     * FolderInfo will try to reopen the folder.
     */
    public void closeFolder(boolean expunge) throws MessagingException {

	unloadAllMessages();

	if (getFolderWindow() != null)
	    getFolderWindow().setEnabled(false);

	setFolderWindow(null);

	if (getFolderTracker() != null) {
	    getFolderTracker().removeFolder(this);
	    setFolderTracker(null);
	}

	if (isLoaded()) {
	    open=false;
	    folder.close(expunge);
	}

    }

    /**
     * This closes the curernt Folder as well as all subfolders.
     */
    public void closeAllFolders(boolean expunge) throws MessagingException {
	MessagingException otherException = null;
	Vector folders = getChildren();
	if (folders != null) {
	    for (int i = 0; i < folders.size(); i++) {
		try {
		    ((FolderInfo) folders.elementAt(i)).closeAllFolders(expunge);
		} catch (MessagingException me) {
		    if (otherException == null)
			otherException = me;
		}
	    }
	}  
	
	closeFolder(expunge);

	if (otherException != null)
	    throw otherException;
    }
    
    // Accessor methods.

    public Action[] getActions() {
	return defaultActions;
    }

    public Folder getFolder() {
	return folder;
    }

    private void setFolder(Folder newValue) {
	folder=newValue;
	uidFolder = (folder instanceof UIDFolder);
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

    /**
     * Returns whether or not this FolderInfo wraps a UIDFolder or not.
     *
     * Note that this will return false until the Folder has been loaded.
     */
    public boolean isUIDFolder() {
	return uidFolder;
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

    public Vector getColumnSizes() {
	return columnSizes;
    }

    public void setColumnSizes(Vector newValue) {
	columnSizes = newValue;
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
	return (unreadCount > 0);
    }

    public int getUnreadCount() {
	return unreadCount;
    }
    
    public int getMessageCount() {
	return messageCount;
    }

    public boolean hasNewMessages() {
	return newMessages;
    }

    public void setNewMessages(boolean newValue) {
	newMessages = newValue;
    }

    public FolderTracker getFolderTracker() {
	return folderTracker;
    }

    public void setFolderTracker(FolderTracker newTracker) {
	folderTracker = newTracker;
    }

    public boolean isTrashFolder() {
	return trashFolder;
    }

    /**
     * This sets the trashFolder value.  it also resets the defaultAction
     * list and erases the FolderNode's popupMenu, if there is one.
     */
    public void setTrashFolder(boolean newValue) {
	trashFolder = newValue;
	resetDefaultActions();
	if (getFolderNode() != null)
	    getFolderNode().popupMenu = null;
    }

    public boolean isSentFolder() {
	return sentFolder;
    }

    public void setSentFolder(boolean newValue) {
	sentFolder = newValue;
    }

    /**
     * This forces an update of both the total and unread message counts.
     */
    public void resetMessageCounts() {
	try {
	    if (Pooka.isDebug())
		System.out.println("running resetMessageCounts.  unread message count is " + getFolder().getUnreadMessageCount());

	    unreadCount = getFolder().getUnreadMessageCount();
	    messageCount = getFolder().getMessageCount();
	} catch (MessagingException me) {
	    // if we lose the connection to the folder, we'll leave the old
	    // messageCount and set the unreadCount to zero.
	    unreadCount = 0;
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

    public ActionThread getFolderThread() {
	return getParentStore().getStoreThread();
    }

    public FolderInfo getTrashFolder() {
	return getParentStore().getTrashFolder();
    }

    class UpdateCountAction extends AbstractAction {
	
	UpdateCountAction() {
	    super("folder-update");
	}
	
        public void actionPerformed(ActionEvent e) {
	    resetMessageCounts();
	}
    }

    class EmptyTrashAction extends AbstractAction {
	
	EmptyTrashAction() {
	    super("folder-empty");
	}
	
        public void actionPerformed(ActionEvent e) {
	    emptyTrash();
	}
    }

}
