package net.suberic.pooka;

import javax.mail.*;
import javax.mail.event.*;
import javax.mail.search.*;
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

public class FolderInfo implements MessageCountListener, ValueChangeListener, UserProfileContainer, MessageChangedListener, ConnectionListener {

  // folder is currently open and available.
  public static int CONNECTED = 0;
  
  // folder is disconnected, but should be open; try to reopen at first
  // opportunity
  public static int LOST_CONNECTION = 5;
  
  // folder is available, but only should be accessed during the checkFolder
  // phase.
  
  public static int PASSIVE = 10;
  
  // folder is running in disconnected mode; only act on the cached 
  // messages.
  public static int DISCONNECTED = 15;
  
  // Folder doesn't seem to exist on server, but exists in cache.
  public static int CACHE_ONLY = 18;
  
  // folder is just simply closed.
  public static int CLOSED = 20;
  
  // folder is not yet loaded.
  public static int NOT_LOADED = 25;
  
  // folder does not exist on server or in cache.
  public static int INVALID = 30;
  
  // shows the current status of the FolderInfo.
  protected int status = NOT_LOADED;
  
  // shows the type of this folder.
  protected int type = 0;
  
  // shows the preferred state of the FolderInfo.  should be CONNECTED,
  // PASSIVE, DISCONNECTED, or CLOSED.
  protected int preferredStatus = CONNECTED;
  
  // the resource for the folder disconnected message
  protected static String disconnectedMessage = "error.Folder.disconnected";
  
  // the Folder wrapped by this FolderInfo.
  private Folder folder;
  
  // The is the folder ID: storeName.parentFolderName.folderName
  private String folderID;
  
  // This is just the simple folderName, such as "INBOX"
  private String mFolderName;
  
  private EventListenerList eventListeners = new EventListenerList();
  
  // Information for the FolderNode
  protected FolderNode folderNode;
  protected Vector children;
  
  // Information for the FolderTable.
  protected FolderTableModel folderTableModel;
  protected Hashtable messageToInfoTable = new Hashtable();
  private Vector columnValues;
  private Vector columnNames;
  private Vector columnSizes;
  
  // GUI information.
  private FolderDisplayUI folderDisplayUI;
  private Action[] defaultActions;
  
  //filters
  protected BackendMessageFilter[] backendFilters = null;
  protected MessageFilter[] displayFilters = null;

  protected LoadMessageThread loaderThread;
  private FolderTracker folderTracker = null;
  
  protected boolean loading = false;
  protected int unreadCount = 0;
  protected int messageCount = 0;
  private boolean newMessages = false;
  
  private FolderInfo parentFolder = null;
  private StoreInfo parentStore = null;
  private UserProfile defaultProfile = null;
  
  private boolean sentFolder = false;
  private boolean trashFolder = false;
  
  private boolean notifyNewMessagesMain = true;
  private boolean notifyNewMessagesNode = true;
  private boolean tracksUnreadMessages = true;
  
  protected FetchProfile fetchProfile = null;

  protected OutgoingMailServer mailServer = null;

  /**
   * For subclasses.
   */
  protected FolderInfo() {
  }
  
  /**
   * Creates a new FolderInfo from a parent FolderInfo and a Folder 
   * name.
   */
  
  public FolderInfo(FolderInfo parent, String fname) {
    parentFolder = parent;
    setFolderID(parent.getFolderID() + "." + fname);
    mFolderName = fname;
    
    try {
      if (parent.isLoaded())
	loadFolder();
    } catch (MessagingException me) {
      // if we get an exception loading the folder while creating the folder
      // object, just ignore it.
      if (Pooka.isDebug()) {
	System.out.println(Thread.currentThread() + "loading folder " + getFolderID() + ":  caught messaging exception from parentStore getting folder: " + me);
	me.printStackTrace();
	
      }
    }
    
    updateChildren();
    
    createFilters();
    
    resetDefaultActions();
    
    if (!Pooka.getProperty(getFolderProperty() + ".notifyNewMessagesMain", "").equals(""))
      setNotifyNewMessagesMain(Pooka.getProperty(getFolderProperty() + ".notifyNewMessagesMain", "true").equalsIgnoreCase("true"));
    
    if (!Pooka.getProperty(getFolderProperty() + ".notifyNewMessagesNode", "").equals(""))
      setNotifyNewMessagesNode(Pooka.getProperty(getFolderProperty() + ".notifyNewMessagesNode", "true").equalsIgnoreCase("true"));
  }
  
  
  /**
   * Creates a new FolderInfo from a parent StoreInfo and a Folder 
   * name.
   */
  
  public FolderInfo(StoreInfo parent, String fname) {
    parentStore = parent;
    setFolderID(parent.getStoreID() + "." + fname);
    mFolderName = fname;
    
    try {
      if (parent.isConnected())
	loadFolder();
    } catch (MessagingException me) {
      // if we get an exception loading the folder while creating the folder
      // object, just ignore it.
      if (Pooka.isDebug()) {
	System.out.println(Thread.currentThread() + "loading folder " + getFolderID() + ":  caught messaging exception from parentStore getting folder: " + me);
	me.printStackTrace();
	
      }
    }
    
    updateChildren();
    
    createFilters();
    
    resetDefaultActions();
    
    if (!Pooka.getProperty(getFolderProperty() + ".notifyNewMessagesMain", "").equals(""))
      setNotifyNewMessagesMain(Pooka.getProperty(getFolderProperty() + ".notifyNewMessagesMain", "true").equalsIgnoreCase("true"));
    
    if (!Pooka.getProperty(getFolderProperty() + ".notifyNewMessagesNode", "").equals(""))
      setNotifyNewMessagesNode(Pooka.getProperty(getFolderProperty() + ".notifyNewMessagesNode", "true").equalsIgnoreCase("true"));
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
  public void loadFolder() throws MessagingException {
    boolean parentIsConnected = false;

    if (isLoaded() || (loading && children == null)) 
      return;
    
    Folder[] tmpFolder;
    Folder tmpParentFolder;
    
    try {
      loading = true;
      if (parentStore != null) {
	//try {
	if (Pooka.isDebug())
	  System.out.println(Thread.currentThread() + "loading folder " + getFolderID() + ":  checking parent store connection.");
	
	if (! parentStore.isAvailable())
	  throw new MessagingException();
	
	if (!parentStore.isConnected())
	  parentStore.connectStore();
	Store store = parentStore.getStore();

	tmpParentFolder = store.getDefaultFolder();
	if (Pooka.isDebug())
	  System.out.println("got " + tmpParentFolder + " as Default Folder for store.");
	tmpFolder = tmpParentFolder.list(mFolderName);
	if (Pooka.isDebug())
	  System.out.println("got " + tmpFolder + " as Folder for folder " + getFolderID() + ".");
	/*
	  } catch (MessagingException me) {
	  if (Pooka.isDebug()) {
	  System.out.println(Thread.currentThread() + "loading folder " + getFolderID() + ":  caught messaging exception from parentStore getting folder: " + me);
	  me.printStackTrace();
	  }
	  tmpFolder =null;
	  }
	*/
	
      } else {
	if (!parentFolder.isLoaded())
	  parentFolder.loadFolder();
	if (!parentFolder.isLoaded()) {
	  tmpFolder = null;
	} else {
	  tmpParentFolder = parentFolder.getFolder();
	  if (tmpParentFolder != null) {
	    parentIsConnected = true;
	    tmpFolder = tmpParentFolder.list(mFolderName);
	  } else {
	    tmpFolder = null;
	  }
	}
      }
      if (tmpFolder != null && tmpFolder.length > 0) {
	setFolder(tmpFolder[0]);
	if (! getFolder().isSubscribed())
	  getFolder().setSubscribed(true);

	type = getFolder().getType();
	setStatus(CLOSED);
      } else {
	if (parentIsConnected)
	  setStatus(INVALID);
	setFolder(null);
      }
      /*
	} catch (MessagingException me) {
	if (Pooka.isDebug()) {
	System.out.println(Thread.currentThread() + "loading folder " + getFolderID() + ":  caught messaging exception; setting loaded to false:  " + me.getMessage() );
	me.printStackTrace();
	}
	setStatus(NOT_LOADED);
	setFolder(null);
      */
    } finally {
      loading = false;
    }
    
    initializeFolderInfo();
    
  }

    /**
     * This adds a listener to the Folder.
     */
    protected void addFolderListeners() {
	if (folder != null) {
	    folder.addMessageChangedListener(this);
	    folder.addMessageCountListener(this);
	    folder.addConnectionListener(this);
	}
    }

    /**
     * this is called by loadFolders if a proper Folder object 
     * is returned.
     */
    protected void initializeFolderInfo() {
	addFolderListeners();

	Pooka.getResources().addValueChangeListener(this, getFolderProperty());
	Pooka.getResources().addValueChangeListener(this, getFolderProperty() + ".folderList");
	Pooka.getResources().addValueChangeListener(this, getFolderProperty() + ".defaultProfile");
	Pooka.getResources().addValueChangeListener(this, getFolderProperty() + ".displayFilters");
	Pooka.getResources().addValueChangeListener(this, getFolderProperty() + ".backendFilters");

	String defProfile = Pooka.getProperty(getFolderProperty() + ".defaultProfile", "");
	if (!defProfile.equals(""))
	  defaultProfile = UserProfile.getProfile(defProfile);
	
	// if we got to this point, we should assume that the open worked.
	
	if (getFolderTracker() == null) {
	  FolderTracker tracker = Pooka.getFolderTracker();
	  tracker.addFolder(this);
	  this.setFolderTracker(tracker);
	}
    }

    public void closed(ConnectionEvent e) {
      synchronized(this) {
	if (Pooka.isDebug()) {
	  System.out.println("Folder " + getFolderID() + " closed:  " + e);
	}
	
	
	if (getFolderDisplayUI() != null) {
	  if (status != CLOSED)
	    getFolderDisplayUI().showStatusMessage(Pooka.getProperty(disconnectedMessage, "Lost connection to folder..."));
	}
	
	
	if (status == CONNECTED) {
	  setStatus(LOST_CONNECTION);
	}
	
      }
      fireConnectionEvent(e);
    }
    
    public void disconnected(ConnectionEvent e) {
      synchronized(this) {
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
      }
      fireConnectionEvent(e);
    }


   /**
     * Invoked when a Store/Folder/Transport is opened.
     * 
     * As specified in javax.mail.event.ConnectionListener.
     */
    public void opened (ConnectionEvent e) {
	fireConnectionEvent(e);
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
	if (folder.isOpen()) {
	  if (folder.getMode() == mode)
	    return;
	  else { 
	    folder.close(false);
	    openFolder(mode);
	    }
	} else {
	  folder.open(mode);
	  updateFolderOpenStatus(true);
	  resetMessageCounts();
	}
      } else if (status == INVALID) {
	throw new MessagingException(Pooka.getProperty("error.folderInvalid", "Error:  folder is invalid.  ") + getFolderID());
	}
      
    }
  
  /**
   * Actually records that the folde has been opened or closed.  
   * This is separated out so that subclasses can override it more
   * easily.
   */
  protected void updateFolderOpenStatus(boolean isNowOpen) {
    if (isNowOpen) {
      setStatus(CONNECTED);
    } else {
      setStatus(CLOSED);
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
      
      if (children != null) {
	for (int i = 0; i < children.size(); i++) {
	  doOpenFolders((FolderInfo) children.elementAt(i), mode);
	}
      }
    }
  
  private void doOpenFolders(FolderInfo fi, int mode) {
    if (Pooka.getProperty("Pooka.openFoldersInBackground", "false").equalsIgnoreCase("true")) {
      final FolderInfo current = fi;
      final int finalMode = mode;
      getFolderThread().addToQueue(new javax.swing.AbstractAction() {
	  public void actionPerformed(java.awt.event.ActionEvent e) {
	    current.openAllFolders(finalMode);
	  }
	}, new java.awt.event.ActionEvent(this, 0, "open-all"), ActionThread.PRIORITY_LOW);
    } else {
      fi.openAllFolders(mode);
    }


  }
  
  /**
   * This method closes the Folder.  If you open the Folder using 
   * openFolder (which you should), then you should use this method
   * instead of calling getFolder.close().  If you don't, then the
   * FolderInfo will try to reopen the folder.
   */
  public void closeFolder(boolean expunge, boolean closeDisplay) throws MessagingException {
    
    if (closeDisplay) {
      unloadAllMessages();
      
      if (getFolderDisplayUI() != null)
	getFolderDisplayUI().closeFolderDisplay();
      
      setFolderDisplayUI(null);
    }
    
    /*
      if (getFolderTracker() != null) {
      getFolderTracker().removeFolder(this);
      setFolderTracker(null);
      }
    */
    
    if (isLoaded() && isValid()) {
      setStatus(CLOSED);
      try {
	folder.close(expunge);
      } catch (java.lang.IllegalStateException ise) {
	throw new MessagingException(ise.getMessage(), ise);
      }
    }
    
  }
  
  public void closeFolder(boolean expunge) throws MessagingException {
    closeFolder(expunge, true);
  }
  
  /**
   * This closes the current Folder as well as all subfolders.
   */
  public void closeAllFolders(boolean expunge, boolean shuttingDown) throws MessagingException {
    if (shuttingDown && loaderThread != null) {
      loaderThread.stopThread();
    }
    
    synchronized(getFolderThread().getRunLock()) {
      MessagingException otherException = null;
      Vector folders = getChildren();
      if (folders != null) {
	for (int i = 0; i < folders.size(); i++) {
	  try {
	    ((FolderInfo) folders.elementAt(i)).closeAllFolders(expunge, shuttingDown);
	  } catch (MessagingException me) {
	    if (otherException == null)
	      otherException = me;
	  } catch (Exception e) {
	    MessagingException newMe = new MessagingException (e.getMessage(), e);
	    if (otherException == null)
	      otherException = newMe;
	  }
	}
      }  
      
      closeFolder(expunge, false);
      
      if (otherException != null)
	throw otherException;
    }
  }

    /**
     * Gets all of the children folders of this FolderInfo which are
     * both Open and can contain Messages.  The return value should include
     * the current FolderInfo, if it is Open and can contain Messages.
     */
    public Vector getAllFolders() {
	Vector returnValue = new Vector();
	if (children != null) {
	    for (int i = 0 ; i < children.size(); i++) 
		returnValue.addAll(((FolderInfo) children.elementAt(i)).getAllFolders());
	}
	
	if (isSortaOpen() && (getType() & Folder.HOLDS_MESSAGES) != 0)
	    returnValue.add(this);

	return returnValue;
    }

  /**
   * Synchronizes the locally stored subscribed folders list to the subscribed
   * folder information from the IMAP server.
   */
  public void synchSubscribed() throws MessagingException {
    // at this point we should get folder objects.

    if (! isLoaded())
      loadFolder();

    if ((getType() & Folder.HOLDS_FOLDERS) != 0) {

      Folder[] subscribedFolders = folder.list();
      
      StringBuffer newSubscribed = new StringBuffer();

      for (int i = 0; subscribedFolders != null && i < subscribedFolders.length; i++) {
	// sometimes listSubscribed() doesn't work.
	if (subscribedFolders[i].isSubscribed() || subscribedFolders[i].getName().equalsIgnoreCase("INBOX")) {
	  String folderName = subscribedFolders[i].getName();
	  newSubscribed.append(folderName).append(':');
	}
      }

      if (newSubscribed.length() > 0)
	newSubscribed.deleteCharAt(newSubscribed.length() -1);
      
      // this will update our children vector.
      Pooka.setProperty(getFolderProperty() + ".folderList", newSubscribed.toString());
      
      for (int i = 0; children != null && i < children.size(); i++) {
	FolderInfo fi = (FolderInfo) children.elementAt(i);
	fi.synchSubscribed();
      }
    }
  }

  /**
   * Loads the column names and sizes.
   */
  protected FetchProfile createColumnInformation() {
    String tableType;
    
    if (isSentFolder())
      tableType="SentFolderTable";
    //else if (this instanceof VirtualFolderInfo)
    //    tableType="SearchResultsTable";
    else if (isOutboxFolder()) 
      tableType="SentFolderTable";
    else
      tableType="FolderTable";
    
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
	String type = Pooka.getProperty(tableType + "." + tmp + ".type", "");
	if (type.equalsIgnoreCase("Multi")) {
	  SearchTermIconManager stm = new SearchTermIconManager(tableType + "." + tmp);
	  colvals.addElement(stm);
	  Vector toFetch = Pooka.getResources().getPropertyAsVector(tableType + "." + tmp + ".profileItems", "");
	  if (toFetch != null) {
	    for (int z = 0; z < toFetch.size(); z++) {
	      String profileDef = (String) toFetch.elementAt(z);
	      if (profileDef.equalsIgnoreCase("Flags"))
		fp.add(FetchProfile.Item.FLAGS);
	      else if (profileDef.equalsIgnoreCase("Envelope"))
		fp.add(FetchProfile.Item.ENVELOPE);
	      else if (profileDef.equalsIgnoreCase("Content_Info"))
		fp.add(FetchProfile.Item.CONTENT_INFO);
	      else
		fp.add(profileDef);
	    }
	  }
	} else if (type.equalsIgnoreCase("RowCounter")) {
	  colvals.addElement(RowCounter.getInstance());
	} else {
	  String value = Pooka.getProperty(tableType + "." + tmp + ".value", tmp);
	  colvals.addElement(value);
	  String fpValue = Pooka.getProperty(tableType + "." + tmp + ".profileItems", value);
	  fp.add(fpValue);
 	}
	
	colnames.addElement(Pooka.getProperty(tableType + "." + tmp + ".label", tmp));
	colsizes.addElement(Pooka.getProperty(tableType + "." + tmp + ".size", tmp));
      }	    
      setColumnNames(colnames);
      setColumnValues(colvals);
      setColumnSizes(colsizes);
    }

    // if we've already loaded the filters, then add those in, too.
    if (filterHeaders != null) {
      for (int i = 0; i < filterHeaders.size(); i++) {
	fp.add((String) filterHeaders.get(i));
      }
    }

    return fp;
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
      
      fetchProfile = createColumnInformation();
      if (loaderThread == null) 
	loaderThread = createLoaderThread();
      
      if (! isLoaded())
	loadFolder();
      
      if (! isConnected() ) {
	openFolder(Folder.READ_WRITE);
      }

      int fetchBatchSize = 50;
      try {
	fetchBatchSize = Integer.parseInt(Pooka.getProperty("Pooka.fetchBatchSize", "50"));
      } catch (NumberFormatException nfe) {
      }
      
      Message[] msgs = folder.getMessages();

      Message[] toFetch = msgs;

      // go ahead and fetch the first set of messages; the rest will be
      // taken care of by the loaderThread.
      if (msgs.length > fetchBatchSize) {
	toFetch = new Message[fetchBatchSize];
	System.arraycopy(msgs, msgs.length - fetchBatchSize, toFetch, 0, fetchBatchSize);
      }

      folder.fetch(toFetch, fetchProfile);
      
      int firstFetched = Math.max(msgs.length - fetchBatchSize, 0);

      MessageInfo mi;
      
      for (int i = 0; i < msgs.length; i++) {
	mi = new MessageInfo(msgs[i], this);
	
	if ( i >= firstFetched)
	  mi.setFetched(true);

	messageProxies.add(new MessageProxy(getColumnValues() , mi));
	messageToInfoTable.put(msgs[i], mi);
      }
      
      FolderTableModel ftm = new FolderTableModel(messageProxies, getColumnNames(), getColumnSizes(), getColumnValues());
      
      setFolderTableModel(ftm);
      
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
    }
  }

  /**
   * Loads the FolderTableInfo objects for the given messages.
   */
  public void loadMessageTableInfos(Vector messages) {
    int numMessages = messages.size();
    MessageProxy mp;
    
    int updateCounter = 0;
    
    if (numMessages > 0) {

      int fetchBatchSize = 25;
      int loadBatchSize = 25;
      try {
	fetchBatchSize = Integer.parseInt(Pooka.getProperty("Pooka.fetchBatchSize", "50"));
      } catch (NumberFormatException nfe) {
      }
      
      FetchProfile fetchProfile = getFetchProfile();

      int i = numMessages - 1;
      while ( i >= 0 ) {
	for (int batchCount = 0; i >=0 && batchCount < loadBatchSize; batchCount++) {
	  mp=(MessageProxy)messages.elementAt(i);
	  
	  if (! mp.getMessageInfo().hasBeenFetched()) {
	    try {
	      int fetchCount = 0;
	      Vector fetchVector = new Vector();
	      for (int j = i; fetchCount < fetchBatchSize && j >= 0; j--) {
		MessageInfo fetchInfo = ((MessageProxy) messages.elementAt(j)).getMessageInfo();
		if (! fetchInfo.hasBeenFetched()) {
		  fetchVector.add(fetchInfo);
		  fetchInfo.setFetched(true);
		}
	      }
	      
	      MessageInfo[] toFetch = new MessageInfo[fetchVector.size()];
	      toFetch = (MessageInfo[]) fetchVector.toArray(toFetch);
	      this.fetch(toFetch, fetchProfile);
	    } catch(MessagingException me) {
	      System.out.println("caught error while fetching for folder " + getFolderID() + ":  " + me);
	      me.printStackTrace();
	    }
	      
	  }
	  try {
	    if (! mp.isLoaded())
	      mp.loadTableInfo();
	    if (mp.needsRefresh())
	      mp.refreshMessage();
	    else if (! mp.matchedFilters()) {
	      mp.matchFilters();
	    }
	  } catch (Exception e) {
	    e.printStackTrace();
	  }
	  i--;
	}
	
      }
    }
  }

  /**
   * Fetches the information for the given messages using the given
   * FetchProfile.
   */
  public void fetch(MessageInfo[] messages, FetchProfile profile) throws MessagingException  {
    Message[] realMsgs = new Message[messages.length];
    for (int i = 0; i < realMsgs.length; i++) {
      realMsgs[i] = messages[i].getMessage();
    }
    getFolder().fetch(realMsgs, profile);

    for (int i = 0 ; i < messages.length; i++) {
      messages[i].setFetched(true);
    }
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
   * Unloads all of the tableInfos of the MessageInfo objects.  This
   * should be used either when the message information is stale, or when
   * the display rules have changed.
   */
  public void unloadTableInfos() {
    if (folderTableModel != null) {
      Vector allProxies = folderTableModel.getAllProxies();
      for (int i = 0; i < allProxies.size(); i++) {
	MessageProxy mp = (MessageProxy) allProxies.elementAt(i);
	mp.unloadTableInfo();
      }
      
      if (loaderThread != null)
	loaderThread.loadMessages(allProxies);
      
    }
  }
  
    /**
     * Unloads the matching filters.
     */
  public void unloadMatchingFilters() {
    if (folderTableModel != null) {
      Vector allProxies = folderTableModel.getAllProxies();
      for (int i = 0; i < allProxies.size(); i++) {
	MessageProxy mp = (MessageProxy) allProxies.elementAt(i);
	mp.clearMatchedFilters();
      }
      
      if (loaderThread != null)
	loaderThread.loadMessages(allProxies);
      
    }
  }
  
  /**
   * Refreshes the headers for the given MessageInfo.
   */
  public void refreshHeaders(MessageInfo mi) throws MessagingException {
    // no-op for default; only really used by UIDFolderInfos.
  }

  /**
   * Refreshes the flags for the given MessageInfo.
   */
  public void refreshFlags(MessageInfo mi) throws MessagingException {
    // no-op for default; only really used by UIDFolderInfos.
  }


  /**
   * This just checks to see if we can get a NewMessageCount from the
   * folder.  As a brute force method, it also accesses the folder
   * at every check.  It's nasty, but it _should_ keep the Folder open..
   */
  public void checkFolder() throws javax.mail.MessagingException {
    if (Pooka.isDebug())
      System.out.println("checking folder " + getFolderName());
    
    // i'm taking this almost directly from ICEMail; i don't know how
    // to keep the stores/folders open, either.  :)
    
    if (isConnected()) {
      //try {
      //Store s = getParentStore().getStore();
      Folder current = getFolder();
      if (current != null && current.isOpen()) {
	current.getNewMessageCount();
	current.getUnreadMessageCount();
      }
      //} catch ( MessagingException me ) {
      //  if ( ! s.isConnected() )
      //    s.connect();
      //}
      
      resetMessageCounts();
    }
  }
  
  /**
   * Gets the row number of the first unread message.  Returns -1 if
   * there are no unread messages, or if the FolderTableModel is not
   * set or empty.
   */
  
  public int getFirstUnreadMessage() {
    if (Pooka.isDebug())
      System.out.println("getting first unread message");
    
    if (! tracksUnreadMessages()) 
      return -1;
    
    if (getFolderTableModel() == null)
      return -1;
    
    try {
      int countUnread = 0;
      int i;
      if (unreadCount > 0) {
	
	// one part brute, one part force, one part ignorance.
	
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
   *
   * Should be called on the folder thread.
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
    if (Pooka.isDebug())
      System.out.println("folder " + getFolderID() + " getting child " + childName);

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
      
      if (Pooka.isDebug())
	System.out.println("getting direct child " + folderName);
      
	for (int i = 0; i < children.size(); i++)
	  if (((FolderInfo)children.elementAt(i)).getFolderName().equals(folderName))
	    childFolder = (FolderInfo)children.elementAt(i);
    } else {
      if (Pooka.isDebug())
	System.out.println("children of " + getFolderID() + " is null.");
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
   * creates the loader thread.
   */
  public LoadMessageThread createLoaderThread() {
    LoadMessageThread lmt = new LoadMessageThread(this);
    return lmt;
  }
  
  /**
   * gets the 'real' message for the given MessageInfo.
   */
  public Message getRealMessage(MessageInfo mi) throws MessagingException {
    return mi.getMessage();
  }
  
  /**
   * This sets the given Flag for all the MessageInfos given.
   */
  public void setFlags(MessageInfo[] msgs, Flags flag, boolean value) throws MessagingException {
    Message[] m = new Message[msgs.length];
    for (int i = 0; i < msgs.length; i++) {
      m[i] = msgs[i].getRealMessage();
    }
    
    getFolder().setFlags(m, flag, value);
  }

  /**
   * This copies the given messages to the given FolderInfo.
   */
  public void copyMessages(MessageInfo[] msgs, FolderInfo targetFolder) throws MessagingException {
    if (! targetFolder.isAvailable()) 
      targetFolder.loadFolder();
    
    Folder target = targetFolder.getFolder();
    if (target != null) {
      Message[] m = new Message[msgs.length];
      for (int i = 0; i < msgs.length; i++) {
	m[i] = msgs[i].getRealMessage();
      }
      
      getFolder().copyMessages(m, target);
    } else {
      targetFolder.appendMessages(msgs);
    }
  }

  /**
   * This appends the given message to the given FolderInfo.
   */
  public void appendMessages(MessageInfo[] msgs) throws MessagingException {
    if (! isSortaOpen())
      openFolder(Folder.READ_WRITE);
    Message[] m = new Message[msgs.length];
    for (int i = 0; i < msgs.length; i++) {
      m[i] = msgs[i].getRealMessage();
    }
    
    getFolder().appendMessages(m);
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
      
      if (! (mce instanceof net.suberic.pooka.event.MessageTableInfoChangedEvent)) {
	resetMessageCounts();
      }
      
      if (Pooka.isDebug())
	System.out.println("firing message changed event.");
      // Guaranteed to return a non-null array
      Object[] listeners = eventListeners.getListenerList();
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

      if (Pooka.isDebug())
	System.out.println("done handing event " + mce);
    }  
  
    public void addConnectionListener(ConnectionListener newListener) {
	eventListeners.add(ConnectionListener.class, newListener);
    }

    public void removeConnectionListener(ConnectionListener oldListener) {
	eventListeners.remove(ConnectionListener.class, oldListener);
    }


    /**
     * This handles the distributions of any Connection events.
     *
     * As defined in interface net.suberic.pooka.event.MessageLoadedListener.
     */

    public void fireConnectionEvent(ConnectionEvent e) {
      // from the EventListenerList javadoc, including comments.
      
      if (Pooka.isDebug())
	System.out.println("firing connection event.");
      // Guaranteed to return a non-null array
      Object[] listeners = eventListeners.getListenerList();
      // Process the listeners last to first, notifying
      // those that are interested in this event
      for (int i = listeners.length-2; i>=0; i-=2) {
	if (Pooka.isDebug())
	  System.out.println("listeners[" + i + "] is " + listeners[i] );
	if (listeners[i]==ConnectionListener.class) {
	  if (Pooka.isDebug())
	    System.out.println("check.  it's a connection listener.");
	  ConnectionListener listener = (ConnectionListener) listeners[i+1];
	  if (e.getType() == ConnectionEvent.CLOSED)
	    listener.closed(e);
	  else if (e.getType() == ConnectionEvent.DISCONNECTED)
	    listener.disconnected(e);
	  else if (e.getType() == ConnectionEvent.OPENED)
	    listener.opened(e);
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
      final Runnable runMe = new  Runnable() {
	  public void run() {
	    ((javax.swing.tree.DefaultTreeModel)(((FolderPanel)folderNode.getParentContainer()).getFolderTree().getModel())).nodeStructureChanged(folderNode);
	  }
	};
      // if we don't do the update synchronously on the folder thread,
      // then subscribing to subfolders breaks.
      if (Thread.currentThread() != getFolderThread()) {
	getFolderThread().addToQueue(new javax.swing.AbstractAction() {
	    public void actionPerformed(java.awt.event.ActionEvent e) {
	      updateChildren();
	      if (folderNode != null) {
		javax.swing.SwingUtilities.invokeLater(runMe);
	      }
	    }
	  } , new java.awt.event.ActionEvent(this, 0, "open-all"));
      } else {
	updateChildren();
	if (folderNode != null) {
	  javax.swing.SwingUtilities.invokeLater(runMe);
	}
      }
    } else if (changedValue.equals(getFolderProperty() + ".defaultProfile")) {
      if (Pooka.getProperty(changedValue, "").equals(""))
	defaultProfile = null;
      else 
	defaultProfile = UserProfile.getProfile(Pooka.getProperty(changedValue, ""));
    } else if (changedValue.equals(getFolderProperty() + ".backendFilters")) { 
      createFilters();
      
    } else if (changedValue.equals(getFolderProperty() + ".displayFilters")) {
      createFilters();
      unloadMatchingFilters();
    }
  }

  /**
   * This creates a folder if it doesn't exist already.  If it does exist,
   * but is not of the right type, or if there is a problem in creating the
   * folder, throws an error.
   */
  public void createSubFolder(String subFolderName, int type) throws MessagingException {
    if ( ! isLoaded()) {
      loadFolder();
    }

    if (folder != null) {
      Folder subFolder = folder.getFolder(subFolderName);
      
      if (subFolder == null) {
	throw new MessagingException("Store returned null for subfolder " + subFolderName + " of folder " + getFolderName());
      }

      if (! subFolder.exists())
	subFolder.create(type);

      subscribeFolder(subFolderName);
    } else {
      throw new MessagingException("Failed to open folder " + getFolderName() + " to create subfolder " + subFolderName);
    }
  }

  /**
   * This subscribes to the FolderInfo indicated by the given String.
   * If this defines a subfolder, then that subfolder is added to this
   * FolderInfo, if it doesn't already exist.
   */
  public void subscribeFolder(String folderName) {
    if (Pooka.isDebug())
      System.out.println("Folder " + getFolderID() + " subscribing subfolder " + folderName);

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
    
    if (Pooka.isDebug()) {
      System.out.println("Folder " + getFolderID() + " subscribing folder " + childFolderName + ", plus subfolder " + subFolderName);
    }
    
    this.addToFolderList(childFolderName);
    
    FolderInfo childFolder = getChild(childFolderName);

    if (Pooka.isDebug()) {
      System.out.println("got child folder " + childFolder + " from childFolderName " + childFolderName);
    }

    if (childFolder != null && subFolderName != null) {
      childFolder.subscribeFolder(subFolderName);	
    }

    try {
      if (childFolder != null && childFolder.isLoaded() == false)
	childFolder.loadFolder();
    } catch (MessagingException me) {
      // if we get an exception loading a child folder while subscribing a
      // folder object, just ignore it.
      if (Pooka.isDebug()) {
	System.out.println(Thread.currentThread() + "loading folder " + getFolderID() + ":  caught messaging exception from parentStore getting folder: " + me);
	me.printStackTrace();
	
      }
    }

    updateChildren();
  }
  
  /**
   * This adds the given folderString to the folderList of this
   * FolderInfo.
   */
  void addToFolderList(String addFolderName) {
    Vector folderNames = Pooka.getResources().getPropertyAsVector(getFolderProperty() + ".folderList", "");
    
    boolean found = false;
    
    for (int i = 0; i < folderNames.size(); i++) {
      String folderName = (String) folderNames.elementAt(i);
      
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
    if (getFolderDisplayUI() != null)
      getFolderDisplayUI().closeFolderDisplay();
    
    Pooka.getResources().removeProperty(getFolderProperty() + ".folderList");
    
    if (parentFolder != null)
      parentFolder.removeFromFolderList(getFolderName());
    else if (parentStore != null)
      parentStore.removeFromFolderList(getFolderName());
    
    try {
      if (folder != null)
	folder.setSubscribed(false);
    } catch (MessagingException me) {
      Pooka.getUIFactory().showError(Pooka.getProperty("error.folder.unsubscribe", "Error unsubscribing on server from folder ") + getFolderID(), me);
    }
  }

  /**
   * This deletes the underlying Folder.
   */
  public void delete() throws MessagingException {
    
    if (! isLoaded())
      loadFolder();

    Folder f = getFolder();
    if (f == null)
      throw new MessagingException("No folder.");

    unsubscribe();

    f.close(true);
    f.delete(true);
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
	if (getFolderDisplayUI() != null) 
	  getFolderDisplayUI().showError(m);
	else
	  System.out.println(m);
      }
    }
  }

  /**
   * This resets the defaultActions.  Useful when this goes to and from
   * being a trashFolder, since only trash folders have emptyTrash
   * actions.
   */
  public void resetDefaultActions() {
    if (isTrashFolder()) {
      defaultActions = new Action[] {
	new net.suberic.util.thread.ActionWrapper(new UpdateCountAction(), getFolderThread()),
	new net.suberic.util.thread.ActionWrapper(new EmptyTrashAction(), getFolderThread()),
	new EditPropertiesAction()
      };
    } else if (isOutboxFolder()) {
      defaultActions = new Action[] {
	new net.suberic.util.thread.ActionWrapper(new UpdateCountAction(), getFolderThread()),
	new net.suberic.util.thread.ActionWrapper(new SendAllAction(), getFolderThread()),
	new EditPropertiesAction()
      };
      
    } else {
      defaultActions = new Action[] {
	new net.suberic.util.thread.ActionWrapper(new UpdateCountAction(), getFolderThread()),
	new EditPropertiesAction()
      };
    }
  }

  // semi-accessor methods.
  
  public MessageProxy getMessageProxy(int rowNumber) {
    return getFolderTableModel().getMessageProxy(rowNumber);
  }
  
  public MessageInfo getMessageInfo(Message m) {
    return (MessageInfo)messageToInfoTable.get(m);
  }
  
  public void addMessageCountListener(MessageCountListener newListener) {
    eventListeners.add(MessageCountListener.class, newListener);
  }
  
  public void removeMessageCountListener(MessageCountListener oldListener) {
    eventListeners.remove(MessageCountListener.class, oldListener);
  }
  
  public void fireMessageCountEvent(MessageCountEvent mce) {
    
    if (Pooka.isDebug())
      System.out.println("firing MessageCountEvent on " + getFolderID());
    
    // from the EventListenerList javadoc, including comments.
    
    // Guaranteed to return a non-null array
    Object[] listeners = eventListeners.getListenerList();
    // Process the listeners last to first, notifying
    // those that are interested in this event
    
    if (mce.getType() == MessageCountEvent.ADDED) {
      for (int i = listeners.length-2; i>=0; i-=2) {
	if (Pooka.isDebug())
	  System.out.println("listeners[" + i + "] is " + listeners[i] );
	if (listeners[i]==MessageCountListener.class) {
	  if (Pooka.isDebug())
	    System.out.println("check.  running messagesAdded on listener.");
	  
	  ((MessageCountListener)listeners[i+1]).messagesAdded(mce);
	}              
      }
    } else if (mce.getType() == MessageCountEvent.REMOVED) {
      for (int i = listeners.length-2; i>=0; i-=2) {
	if (Pooka.isDebug())
	  System.out.println("listeners[" + i + "] is " + listeners[i] );
	if (listeners[i]==MessageCountListener.class) {
	  if (Pooka.isDebug())
	    System.out.println("check.  running messagesRemoved on listener " + listeners[i+1] );
	  
	  ((MessageCountListener)listeners[i+1]).messagesRemoved(mce);
	}              
      }
      
    }
  }
  
  public void addMessageChangedListener(MessageChangedListener newListener) {
    eventListeners.add(MessageChangedListener.class, newListener);
  }
  
  public void removeMessageChangedListener(MessageChangedListener oldListener) {
    eventListeners.remove(MessageChangedListener.class, oldListener);
  }
  
  // as defined in javax.mail.event.MessageCountListener
  
  public void messagesAdded(MessageCountEvent e) {
    if (Pooka.isDebug())
      System.out.println("Messages added.");
    
    if (Thread.currentThread() == getFolderThread() )
      runMessagesAdded(e);
    else 
	    getFolderThread().addToQueue(new net.suberic.util.thread.ActionWrapper(new javax.swing.AbstractAction() {
		
		public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
		  runMessagesAdded((MessageCountEvent)actionEvent.getSource());
		}
	      }, getFolderThread()), new java.awt.event.ActionEvent(e, 1, "message-count-changed"));
	
  }
  
  protected void runMessagesAdded(MessageCountEvent mce) {
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
	
	// notify the message loaded thread.
	MessageProxy[] addedArray = (MessageProxy[]) addedProxies.toArray(new MessageProxy[0]);
	loaderThread.loadMessages(addedArray);
	
	fireMessageCountEvent(mce);
      }
    }
    
  }
  
  /**
   * As defined in javax.mail.MessageCountListener.
   *
   * This runs when we get a notification that messages have been
   * removed from the mail server.
   *
   * This implementation just moves the handling of the event to the
   * FolderThread, where it runs runMessagesRemoved().
   */
  public void messagesRemoved(MessageCountEvent e) {
    if (Pooka.isDebug())
      System.out.println("Messages Removed.");
    
    if (Thread.currentThread() == getFolderThread() )
      runMessagesRemoved(e);
    else 
      getFolderThread().addToQueue(new net.suberic.util.thread.ActionWrapper(new javax.swing.AbstractAction() {
	  public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
	    runMessagesRemoved((MessageCountEvent)actionEvent.getSource());
	  }
	}, getFolderThread()), new java.awt.event.ActionEvent(e, 1, "messages-removed"));
  }
  
  /**
   * This does the real work when messages are removed.  This can be
   * overridden by subclasses.
   */
  protected void runMessagesRemoved(MessageCountEvent mce) {
    if (Pooka.isDebug())
      System.out.println("running MessagesRemoved on " + getFolderID());
    
    if (folderTableModel != null) {
      Message[] removedMessages = mce.getMessages();
      if (Pooka.isDebug())
	System.out.println("removedMessages was of size " + removedMessages.length);
      MessageInfo mi;
      Vector removedProxies=new Vector();
      
      if (Pooka.isDebug()) {
	System.out.println("message in info table:");
	Enumeration keys = messageToInfoTable.keys();
	while (keys.hasMoreElements())
	  System.out.println(keys.nextElement());
      }
      
      for (int i = 0; i < removedMessages.length; i++) {
	if (Pooka.isDebug())
	  System.out.println("checking for existence of message " + removedMessages[i]);
	mi = getMessageInfo(removedMessages[i]);
	if (mi != null) {
	  if (mi.getMessageProxy() != null)
	    mi.getMessageProxy().close();
	  
	  if (Pooka.isDebug())
	    System.out.println("message exists--removing");
	  removedProxies.add(mi.getMessageProxy());
	  messageToInfoTable.remove(removedMessages[i]);
	}
      }
      if (getFolderDisplayUI() != null) {
	if (removedProxies.size() > 0) 
	  getFolderDisplayUI().removeRows(removedProxies);
	resetMessageCounts();
	fireMessageCountEvent(mce);
      } else {
	resetMessageCounts();
	fireMessageCountEvent(mce);
	if (removedProxies.size() > 0)
	  getFolderTableModel().removeRows(removedProxies);
      }
    } else {
      resetMessageCounts();
      fireMessageCountEvent(mce);
    }
  }
  
    /**
     * This updates the TableInfo on the changed messages.
     * 
     * As defined by java.mail.MessageChangedListener.
     */
    public void messageChanged(MessageChangedEvent e) {
	// blech.  we really have to do this on the action thread.
	
	if (Thread.currentThread() == getFolderThread() )
	    runMessageChanged(e);
	else 
	    getFolderThread().addToQueue(new net.suberic.util.thread.ActionWrapper(new javax.swing.AbstractAction() {
		public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
		    runMessageChanged((MessageChangedEvent)actionEvent.getSource());
		}
	    }, getFolderThread()), new java.awt.event.ActionEvent(e, 1, "message-changed"));
    }


    protected void runMessageChanged(MessageChangedEvent mce) {
	
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

  /**
   * This puts up the gui for the Search.
   */
  public void showSearchFolder() {
    Pooka.getUIFactory().showSearchForm(new FolderInfo[] { this });
  }
  
  /**
   * This is a static calls which searches the given FolderInfo objects,
   * collects the results into a VirtualFolderInfo, and then displays
   * the results of the search in the UI.
   */
  public static void searchFolders(Vector folderList, javax.mail.search.SearchTerm term) {
    final javax.mail.search.SearchTerm searchTerm = term;
    final Vector selectedFolders = folderList;

    Pooka.getSearchThread().addToQueue(new net.suberic.util.thread.ActionWrapper(new javax.swing.AbstractAction() {
	public void actionPerformed(ActionEvent e) {
	  Vector matchingValues = new Vector();
	  if (Pooka.isDebug()) 
	    System.out.println("init:  matchingValues.size() = " + matchingValues.size());

	  net.suberic.util.swing.ProgressDialog dialog = Pooka.getUIFactory().createProgressDialog(0,100,0,"Search","Searching");
	  dialog.show();
	  boolean cancelled = dialog.isCancelled();

	  for (int i = 0; ! cancelled && i < selectedFolders.size(); i++) {
	    if (Pooka.isDebug())
	      System.out.println("trying selected folder number " + i);
	    try {
	      net.suberic.pooka.MessageInfo[] matches = ((FolderInfo) selectedFolders.elementAt(i)).search(searchTerm);
	      if (Pooka.isDebug())
		System.out.println("matches.length = " + matches.length);
	      for (int j = 0; j < matches.length; j++) {
		matchingValues.add(matches[j]);
		if (Pooka.isDebug())
		  System.out.println("adding " + matches[j] + " to matchingValues.");
	      }
	      
	    } catch (MessagingException me) {
	      System.out.println("caught exception " + me);
	    }
	    cancelled = dialog.isCancelled();
	  }
	  
	  if (Pooka.isDebug())
	    System.out.println("got " + matchingValues.size() + " matches.");
	  
	  if (! cancelled) {
	    FolderInfo[] parentFolders = new FolderInfo[selectedFolders.size()];
	    for (int i = 0; i < selectedFolders.size(); i++) {
	      parentFolders[i] = (FolderInfo) selectedFolders.elementAt(i);
	    }
	    
	    MessageInfo[] matchingMessages = new MessageInfo[matchingValues.size()];
	    for (int i = 0; i < matchingValues.size(); i++) {
	      if (Pooka.isDebug())
		System.out.println("matchingValues.elementAt(" + i + ") = " + matchingValues.elementAt(i));
	      matchingMessages[i] = (MessageInfo) matchingValues.elementAt(i);
	    }
	    
	    final VirtualFolderInfo vfi = new VirtualFolderInfo(matchingMessages, parentFolders);
	    
	    Runnable runMe = new Runnable() {
		public void run() {
		  FolderDisplayUI fdui = Pooka.getUIFactory().createFolderDisplayUI(vfi);
		  fdui.openFolderDisplay();
		}
	      };
	    
	    javax.swing.SwingUtilities.invokeLater(runMe);
	  }

	  dialog.dispose();
	}
      }, Pooka.getSearchThread()), new java.awt.event.ActionEvent(FolderInfo.class, 1, "search"));
    
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
	if (folderTableModel == null)
	    loadAllMessages();

	Message[] matchingMessages = folder.search(term);
	MessageInfo returnValue[] = new MessageInfo[matchingMessages.length];
	for (int i = 0; i < matchingMessages.length; i++) {
	    if (Pooka.isDebug())
		System.out.println("match " + i + " = " + matchingMessages[i]);
	    MessageInfo info = getMessageInfo(matchingMessages[i]);
	    if (Pooka.isDebug())
		System.out.println("messageInfo " + i + " = " + info);
	    returnValue[i] = info;
	}
	if (Pooka.isDebug())
	    System.out.println("got " + returnValue.length + " results.");
	return returnValue;
    }

  /**
   * The resource for the default display filters.
   */
  protected String getDefaultDisplayFiltersResource() {
    if (isSentFolder())
      return "FolderInfo.sentFolderDefaultDisplayFilters";
    else
      return "FolderInfo.defaultDisplayFilters";
  }
  
  List filterHeaders = null;

  /**
   * This takes the FolderProperty.backendFilters and 
   * FolderProperty.displayFilters properties and uses them to populate
   * the backendMessageFilters and displayMessageFilters arrays.
   */
  public void createFilters() {
    BackendMessageFilter[] tmpBackendFilters = null;
    MessageFilter[] tmpDisplayFilters = null;
    Vector backendFilterNames=Pooka.getResources().getPropertyAsVector(getFolderProperty() + ".backendFilters", "");
    
    if (backendFilterNames != null && backendFilterNames.size() > 0) {
      
      tmpBackendFilters = new BackendMessageFilter[backendFilterNames.size()];
      for (int i = 0; i < backendFilterNames.size(); i++) {
	System.out.println("creating filter from " + getFolderProperty() + ".backendFitlers." + (String) backendFilterNames.elementAt(i));
	tmpBackendFilters[i] = new BackendMessageFilter(getFolderProperty() + ".backendFilters." + (String) backendFilterNames.elementAt(i));
      }
      
      backendFilters = tmpBackendFilters;
    }
    
    Vector foundFilters = new Vector();
    Vector defaultFilterNames = Pooka.getResources().getPropertyAsVector(getDefaultDisplayFiltersResource(), "");
    
    for (int i = 0; i < defaultFilterNames.size(); i++) {
      foundFilters.add(new MessageFilter("FolderInfo.defaultDisplayFilters." + (String) defaultFilterNames.elementAt(i)));
    }
    
    Vector displayFilterNames=Pooka.getResources().getPropertyAsVector(getFolderProperty() + ".displayFilters", "");
    for (int i = 0; i < displayFilterNames.size(); i++) {
      foundFilters.add(new MessageFilter(getFolderProperty() + ".displayFilters." + (String) displayFilterNames.elementAt(i)));
    }
    
    tmpDisplayFilters = new MessageFilter[foundFilters.size()];
    for (int i = 0; i < foundFilters.size(); i++)
      tmpDisplayFilters[i] = (MessageFilter) foundFilters.elementAt(i);
    
    displayFilters = tmpDisplayFilters;

    filterHeaders = new LinkedList();
    // update the fetch profile with the headers from the display filters.
    for (int i = 0; i < tmpDisplayFilters.length; i++) {
      javax.mail.search.SearchTerm filterTerm = tmpDisplayFilters[i].getSearchTerm();
      if (filterTerm != null) {
	List headers = getHeaders(filterTerm);
	filterHeaders.addAll(headers);
      }
    }

    if (fetchProfile != null) {
      for (int i = 0; i < filterHeaders.size(); i++) {
	fetchProfile.add((String) filterHeaders.get(i));
      }
    }
  }

  /**
   * Gets all of the header strings for the given search term.
   */
  private List getHeaders(SearchTerm term) {
    List returnValue = new LinkedList();
    if (term instanceof HeaderTerm) {
      String headerName = ((HeaderTerm) term).getHeaderName();
      returnValue.add(headerName);
    } else if (term instanceof AndTerm) {
      SearchTerm[] terms = ((AndTerm)term).getTerms();
      for (int i = 0; i < terms.length; i++) {
	returnValue.addAll(getHeaders(terms[i]));
      }
    } else if (term instanceof OrTerm) {
      SearchTerm[] terms = ((OrTerm)term).getTerms();
      for (int i = 0; i < terms.length; i++) {
	returnValue.addAll(getHeaders(terms[i]));
      }
    } else if (term instanceof NotTerm) {
      SearchTerm otherTerm = ((NotTerm)term).getTerm();
      returnValue.addAll(getHeaders(otherTerm));
    } else if (term instanceof FromTerm || term instanceof FromStringTerm) {
      returnValue.add("From");
    } else if (term instanceof RecipientTerm || term instanceof RecipientStringTerm) {
      Message.RecipientType type;
      if (term instanceof RecipientTerm)
	type = ((RecipientTerm) term).getRecipientType();
      else
	type = ((RecipientStringTerm) term).getRecipientType();
      if (type == Message.RecipientType.TO)
	returnValue.add("To");
      else if (type == Message.RecipientType.CC)
	returnValue.add("Cc");
      else if (type == Message.RecipientType.BCC)
	returnValue.add("Bcc");
    }

    return returnValue;
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
    if (backendFilters != null) 
      for (int i = 0; i < backendFilters.length; i++) {
	if (backendFilters[i] != null) {
	  Vector justRemoved = backendFilters[i].filterMessages(notRemovedYet);
	  removed.addAll(justRemoved);
	  notRemovedYet.removeAll(justRemoved);
	}
      }
    
    return removed;
  }
  
  // Accessor methods.
  
  public Action[] getActions() {
    return defaultActions;
  }
  
  public Folder getFolder() {
    return folder;
  }
  
  protected void setFolder(Folder newValue) {
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
    return mFolderName;
  }
  
  /**
   * This returns the folder display name, usually the FolderName plus
   * the store id.
   */
  public String getFolderDisplayName() {
    return mFolderName + " - " + getParentStore().getStoreID();
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
  
  public FolderDisplayUI getFolderDisplayUI() {
    return folderDisplayUI;
  }
  
  protected void removeFromListeners(FolderDisplayUI display) {
    if (display != null) {
      removeMessageChangedListener(display);
      removeMessageCountListener(display);
      //getFolder().removeConnectionListener(display);
    }
  }
  
  protected void addToListeners(FolderDisplayUI display) {
    if (display != null) {
      addMessageChangedListener(display);
      addMessageCountListener(display);
      //getFolder().addConnectionListener(display);
    }
  }
  
  /**
   * This sets the given FolderDisplayUI to be the UI for this
   * FolderInfo.
   * 
   * It automatically registers that FolderDisplayUI to be a listener
   * to MessageCount, MessageChanged, and Connection events.
   */
  public void setFolderDisplayUI(FolderDisplayUI newValue) {
    removeFromListeners(folderDisplayUI);
    folderDisplayUI = newValue;
    addToListeners(folderDisplayUI);
  }
  
  public int getType() {
    return type;
  }
  
  public boolean isConnected() {
    return (status == CONNECTED);
  }
  
  public boolean shouldBeConnected() {
    return (status < PASSIVE);
  }

  public boolean isSortaOpen() {
    return (status < CLOSED);
  }

  public boolean isAvailable() {
    return (status < NOT_LOADED);
  }

  public boolean isLoaded() {
    return (folder != null);
  }

  public boolean isValid() {
    return (status != INVALID);
  }

  public boolean hasUnread() {
    return (tracksUnreadMessages() && unreadCount > 0);
  }

  public int getUnreadCount() {
    if (!tracksUnreadMessages())
      return 0;
    else
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
    setNotifyNewMessagesMain(! newValue);
    setNotifyNewMessagesNode(! newValue);
    resetDefaultActions();
    if (getFolderNode() != null)
      getFolderNode().popupMenu = null;
  }
  
  
  public boolean isSentFolder() {
    return sentFolder;
  }
  
  
  public void setSentFolder(boolean newValue) {
    sentFolder = newValue;
    setNotifyNewMessagesMain(! newValue);
    setNotifyNewMessagesNode(! newValue);
    setTracksUnreadMessages (! newValue);
    createFilters();
  }

  /**
   * Returns whether or not this is an Outbox for an OutgoingMailServer.
   */
  public boolean isOutboxFolder() {
    return (mailServer != null);
  }

  /**
   * Sets this as an Outbox for the given OutgoingMailServer.  If this
   * is getting removed as an outbox, set the server to null.
   */
  public void setOutboxFolder(OutgoingMailServer newServer) {
    mailServer = newServer;
    setNotifyNewMessagesMain(newServer==null);
    setNotifyNewMessagesNode(newServer==null);
    resetDefaultActions();
  }

  public boolean notifyNewMessagesMain() {
    return notifyNewMessagesMain;
  }
  
  public void setNotifyNewMessagesMain(boolean newValue) {
    notifyNewMessagesMain = newValue;
  }
  
  public boolean notifyNewMessagesNode() {
    return notifyNewMessagesNode;
  }
  
  public void setTracksUnreadMessages(boolean newValue) {
    tracksUnreadMessages = newValue;
  }
  
  public boolean tracksUnreadMessages() {
    return tracksUnreadMessages;
  }
  
  public void setNotifyNewMessagesNode(boolean newValue) {
    notifyNewMessagesNode = newValue;
  }
  
  public MessageFilter[] getDisplayFilters() {
    return displayFilters;
  }

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
      
      if (tracksUnreadMessages())
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
  
  /**
   * sets the status.
   */
  public void setStatus(int newStatus) {
    synchronized(this) {
      status = newStatus;
    }
  }
  
    /**
     * gets the status.
     */
    public int getStatus() {
      return status;
    }

    public ActionThread getFolderThread() {
	return getParentStore().getStoreThread();
    }

    public FolderInfo getTrashFolder() {
	return getParentStore().getTrashFolder();
    }

  public FetchProfile getFetchProfile() {
    return fetchProfile;
  }

    class EditPropertiesAction extends AbstractAction {
	
	EditPropertiesAction() {
	    super("file-edit");
	} 

	public void actionPerformed(ActionEvent e) {
	    Pooka.getUIFactory().showEditorWindow(getFolderProperty(), getFolderProperty(), "Folder.editableFields");
	}
    }

    class UpdateCountAction extends AbstractAction {
	
	UpdateCountAction() {
	    super("folder-update");
	}
	
      public void actionPerformed(ActionEvent e) {
	// should always be on the Folder thread.
	try {
	  checkFolder();
	} catch (MessagingException me) {
	  final MessagingException me2 = me;

	  javax.swing.SwingUtilities.invokeLater(new Runnable() {
	      public void run() {
		if (getFolderDisplayUI() != null)
		  getFolderDisplayUI().showError(Pooka.getProperty("error.updatingFolder", "Error updating Folder ") + getFolderID(), me2);
		else
		  Pooka.getUIFactory().showError(Pooka.getProperty("error.updatingFolder", "Error updating Folder ") + getFolderID(), me2);
		  
	      }
	    });
	  
	}
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


    class SendAllAction extends AbstractAction {
      
      SendAllAction() {
	super("folder-send");
      }
      
      public void actionPerformed(ActionEvent e) {
	if (isOutboxFolder())
	  mailServer.sendAll();
      }
    }

}
