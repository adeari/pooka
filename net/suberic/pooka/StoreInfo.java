package net.suberic.pooka;

import javax.mail.*;
import javax.mail.event.*;
import javax.swing.event.EventListenerList;
import java.util.*;
import net.suberic.pooka.gui.*;
import net.suberic.util.ValueChangeListener;
import net.suberic.util.thread.ActionThread;
import net.suberic.util.VariableBundle;
import net.suberic.util.Item;

/**
 * This class does all of the work for a Store.  It keeps track of the
 * StoreNode for the Store, as well as keeping the children of the store
 * and the properties of the Store.
 */

public class StoreInfo implements ValueChangeListener, Item, NetworkConnectionListener {
  
  private Store store;
  
  // The is the store ID.
  private String storeID;
  
  // Information for the StoreNode
  private StoreNode storeNode;
  private Vector children;
  
  // the status indicators
  private boolean connected = false;
  private boolean authorized = false;
  private boolean available = false;
  
  // if this is a pop mailbox.
  private boolean popStore = false;
  
  private UserProfile defaultProfile;
  
  private NetworkConnection connection;

  // the connection information.
  private String user;
  private String password;
  private String server;
  private String protocol;
  private int port;
  private URLName url;
  
  // the Thread for connections to this Store.
  private ActionThread storeThread;
  
  // the Trash folder for this Store, if any.
  private FolderInfo trashFolder;

  // whether or not this store synchronizes with the subscribed folders
  // automatically
  private boolean useSubscribed = false;

  /**
   * Creates a new StoreInfo from a Store ID.
   */
  
  public StoreInfo(String sid) {
    setStoreID(sid);
    
    configureStore();
  }
  
  /**
   * This configures the store from the property information.
   */
  public void configureStore() {
    connected = false;
    authorized = false;
    available = false;
    
    protocol = Pooka.getProperty("Store." + storeID + ".protocol", "");
    
    if (protocol.equalsIgnoreCase("pop3")) {
      user = "";
      password = "";
      server = "localhost";
      protocol = "mbox";
      port = -1;
      popStore = true;
    } else {
      popStore = false;
      user = Pooka.getProperty("Store." + storeID + ".user", "");
      password = Pooka.getProperty("Store." + storeID + ".password", "");
      String portValue = Pooka.getProperty("Store." + storeID + ".port", "");
      port = -1;
      if (!portValue.equals("")) {
	try {
	  port = Integer.parseInt(portValue);
	} catch (Exception e) {
	}
      }
	    if (!password.equals(""))
	      password = net.suberic.util.gui.propedit.PasswordEditorPane.descrambleString(password);
	    server = Pooka.getProperty("Store." + storeID + ".server", "");
    }
    
    
    url = new URLName(protocol, server, port, "", user, password);
    
    try {
      Properties p = loadProperties();
      Session s = Session.getInstance(p, Pooka.defaultAuthenticator);
      if (Pooka.getProperty("Pooka.sessionDebug", "false").equalsIgnoreCase("true"))
	s.setDebug(true);

      store = s.getStore(url);
      available=true;
    } catch (NoSuchProviderException nspe) {
      Pooka.getUIFactory().showError(Pooka.getProperty("error.loadingStore", "Unable to load Store ") + getStoreID(), nspe);
      available=false;
    }
    
    // don't allow a StoreInfo to get created with an empty folderList.
    
    if (Pooka.getProperty("Store." + storeID + ".folderList", "").equals(""))
      Pooka.setProperty("Store." + storeID + ".folderList", "INBOX");

    // check to see if we're using the subscribed property.
    useSubscribed = Pooka.getProperty(getStoreProperty() + ".useSubscribed", "false").equalsIgnoreCase("true");

    Pooka.getResources().addValueChangeListener(this, getStoreProperty());
    Pooka.getResources().addValueChangeListener(this, getStoreProperty() + ".folderList");
    Pooka.getResources().addValueChangeListener(this, getStoreProperty() + ".defaultProfile");
    Pooka.getResources().addValueChangeListener(this, getStoreProperty() + ".protocol");
    Pooka.getResources().addValueChangeListener(this, getStoreProperty() + ".user");
    Pooka.getResources().addValueChangeListener(this, getStoreProperty() + ".password");
    Pooka.getResources().addValueChangeListener(this, getStoreProperty() + ".server");
    Pooka.getResources().addValueChangeListener(this, getStoreProperty() + ".port");
    Pooka.getResources().addValueChangeListener(this, getStoreProperty() + ".connection");
    
    if (available) {
      store.addConnectionListener(new ConnectionAdapter() { 
	  
	  public void disconnected(ConnectionEvent e) {
	    if (Pooka.isDebug())
	      System.out.println("Store " + getStoreID() + " disconnected.");
	    /*
	      if (connected == true) {
	      try {
	      if (!(store.isConnected()))
	      store.connect();
	      } catch (MessagingException me) {
	      System.out.println("Store " + getStoreID() + " disconnected and unable to reconnect:  " + me.getMessage());
	      }
	      }
	    */
	    
	    try {
	      disconnectStore();
	    } catch (MessagingException me) {
	      if (Pooka.isDebug())
		System.out.println("error disconnecting Store:  " + me.getMessage());
	    }
	    
	  }
	});
    }
    
    if (storeThread == null) {
      storeThread = new ActionThread(this.getStoreID() + " - ActionThread");
      storeThread.start();
    }
    
    defaultProfile = UserProfile.getProfile(Pooka.getProperty(getStoreProperty() + ".defaultProfile", ""));
    
    connection = Pooka.getConnectionManager().getConnection(Pooka.getProperty(getStoreProperty() + ".connection", ""));
    if (connection == null) {
      connection = Pooka.getConnectionManager().getDefaultConnection();
    }

    if (connection != null) {
      connection.addConnectionListener(this);
    }

    updateChildren();
    
    String trashFolderName = Pooka.getProperty(getStoreProperty() + ".trashFolder", "");
    if (trashFolderName.length() > 0) {
      trashFolder = getChild(trashFolderName);
      if (trashFolder != null)
	trashFolder.setTrashFolder(true);
    }
  }	
  
  /**
   * This loads in the default session properties for this Store's
   * Session.
   */
  public Properties loadProperties() {
    Properties p = new Properties(System.getProperties());
    
    p.setProperty("mail.imap.timeout", Pooka.getProperty(getStoreProperty() + ".timeout", Pooka.getProperty("Pooka.timeout", "-1")));
    p.setProperty("mail.imap.connectiontimeout", Pooka.getProperty(getStoreProperty() + ".connectionTimeout", Pooka.getProperty("Pooka.connectionTimeout", "-1")));

    if (Pooka.getProperty(getStoreProperty() + ".SSL", "false").equalsIgnoreCase("true")) {
      //p.setProperty("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
      p.setProperty("mail.imap.socketFactory.class", "net.suberic.pooka.ssl.PookaSSLSocketFactory");
      p.setProperty("mail.imap.socketFactory.fallback", Pooka.getProperty(getStoreProperty() + ".SSL.fallback", "false"));
      
      p.setProperty("mail.imap.socketFactory.port", Pooka.getProperty(getStoreProperty() + ".SSL.port", "993"));
    }
    
    // set the properties for the mbox backend of a pop mailbox
    if (Pooka.getProperty(getStoreProperty() + ".protocol", "mbox").equalsIgnoreCase("pop3")) {
      String mailHome = Pooka.getProperty(getStoreProperty() + ".mailDir", "");
      if (mailHome.equals("")) {
	mailHome = Pooka.getProperty("Pooka.defaultMailSubDir", "");
	if (mailHome.equals(""))
	  mailHome = System.getProperty("user.home") + java.io.File.separator + ".pooka";
	
	mailHome = mailHome + java.io.File.separator + storeID;
      }
      String inboxFileName = mailHome + java.io.File.separator + Pooka.getProperty("Pooka.inboxName", "INBOX");
      String userHomeName = mailHome + java.io.File.separator + Pooka.getProperty("Pooka.subFolderName", "folders");
      
      p.setProperty("mail.mbox.inbox", inboxFileName);
      p.setProperty("mail.mbox.userhome", userHomeName);
      
    }

    return p;
  }
    
  /**
   * This updates the children of the current store.  Generally called
   * when the folderList property is changed.
   */
  
  public void updateChildren() {
    
    Vector newChildren = new Vector();
    
    StringTokenizer tokens = new StringTokenizer(Pooka.getProperty(getStoreProperty() + ".folderList", "INBOX"), ":");
    
    if (Pooka.isDebug())
      System.out.println("Pooka.getProperty(" + getStoreProperty() + ".folderList = " + Pooka.getProperty(getStoreProperty() + ".folderList"));
    
    String newFolderName;
    
    for (int i = 0 ; tokens.hasMoreTokens() ; i++) {
      newFolderName = (String)tokens.nextToken();
      FolderInfo childFolder = getChild(newFolderName);
      if (childFolder == null) {
	if (popStore && newFolderName.equalsIgnoreCase("INBOX")) 
	  childFolder = new PopInboxFolderInfo(this, newFolderName);
	else if (Pooka.getProperty(getStoreProperty() + ".cachingEnabled", "false").equalsIgnoreCase("true") || Pooka.getProperty(getStoreProperty() + "." + newFolderName + ".cachingEnabled", "false").equalsIgnoreCase("true"))
	  childFolder = new net.suberic.pooka.cache.CachingFolderInfo(this, newFolderName);
	else if (Pooka.getProperty(getStoreProperty() + ".protocol", "mbox").equalsIgnoreCase("imap")) {
	  childFolder = new UIDFolderInfo(this, newFolderName);
	} else
	  childFolder = new FolderInfo(this, newFolderName);
      }
      
      newChildren.add(childFolder);
    }
    children = newChildren;
    if (Pooka.isDebug())
      System.out.println(getStoreID() + ":  in configureStore.  children.size() = " + children.size());
    
    if (storeNode != null)
      storeNode.loadChildren();
  }
  
    /**
     * This goes through the list of children of this store and
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
   * This handles the even that the StoreInfo is removed from Pooka.
   */
  public void remove() {

  }
  /**
   * This handles the changes if the source property is modified.
   *
   * As defined in net.suberic.util.ValueChangeListener.
   */
  
  public void valueChanged(String changedValue) {
    if (changedValue.equals(getStoreProperty() + ".folderList")) {
      updateChildren();
    } else if (changedValue.equals(getStoreProperty() + ".defaultProfile")) {
      defaultProfile = UserProfile.getProfile(Pooka.getProperty(changedValue, ""));
    } else if (changedValue.equals(getStoreProperty() + ".protocol") || changedValue.equals(getStoreProperty() + ".user") || changedValue.equals(getStoreProperty() + ".password") || changedValue.equals(getStoreProperty() + ".server") || changedValue.equals(getStoreProperty() + ".port")) {
      
      if (storeNode != null) {
	Enumeration enum = storeNode.children();
	Vector v = new Vector();
	while (enum.hasMoreElements())
	  v.add(enum.nextElement());
	
	storeNode.removeChildren(v);
      }
      
      children = null;
      
      /*
	String realChildren = Pooka.getProperty(getStoreProperty() + ".folderList", "");
	Pooka.setProperty(getStoreProperty() + ".folderList", "");
	Pooka.setProperty(getStoreProperty() + ".folderList", realChildren);
      */
      
      try {
	disconnectStore();
      } catch (Exception e) { }
      if (Pooka.isDebug())
	System.out.println("calling configureStore()");
      
      configureStore();
    } else if (changedValue.equals(getStoreProperty() + ".connection")) {
      connection.removeConnectionListener(this);

      connection = Pooka.getConnectionManager().getConnection(Pooka.getProperty(getStoreProperty() + ".connection", ""));
      if (connection == null) {
	connection = Pooka.getConnectionManager().getDefaultConnection();
      }
      
      if (connection != null) {
	connection.addConnectionListener(this);
      }
    }

  }
  

  /**
   * Called when the status of the NetworkConnection changes.
   */
  public void connectionStatusChanged(NetworkConnection connection, int newStatus) {
    // mbox folders still don't care.
    if (!protocol.equalsIgnoreCase("mbox")) {
      if (newStatus == NetworkConnection.CONNECTED) {
	// we've connected.
	// we probably don't care.
	
      } else if (newStatus == NetworkConnection.DISCONNECTED) {
	// we're being disconnected.  close all the connections.
	try {
	  disconnectStore();
	} catch (MessagingException me) {
	  if (Pooka.isDebug()) {
	    System.out.println("Caught exception disconnecting Store " + getStoreID() + ":  " + me);
	    me.printStackTrace();
	  }
	  // else ignore
	}
	
      } else {
	// we've been cut off.  note it.
	try {
	  disconnectStore();
	} catch (MessagingException me) {
	  if (Pooka.isDebug()) {
	    System.out.println("Caught exception disconnecting Store " + getStoreID() + ":  " + me);
	    me.printStackTrace();
	  }
	  // else ignore
	}
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
	Vector folderNames = Pooka.getResources().getPropertyAsVector(getStoreProperty() + ".folderList", "");
	
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
	
	Pooka.setProperty(getStoreProperty() + ".folderList", newValue.toString());
    }
    
    /**
     * This adds the given folderString to the folderList property.
     */
    void addToFolderList(String addFolderName) {
	String folderName;
	Vector folderNames = Pooka.getResources().getPropertyAsVector(getStoreProperty() + ".folderList", "");
	
	boolean found = false;

	for (int i = 0; i < folderNames.size(); i++) {
	    folderName = (String) folderNames.elementAt(i);

	    if (folderName.equals(addFolderName)) {
		found=true;
	    }
	    
	}
	
	if (!found) {
	    String currentValue = Pooka.getProperty(getStoreProperty() + ".folderList");
	    if (currentValue.equals(""))
		Pooka.setProperty(getStoreProperty() + ".folderList", addFolderName);
	    else
		Pooka.setProperty(getStoreProperty() + ".folderList", currentValue + ":" + addFolderName);
	}
			      
    }
    
  /**
   * This subscribes the Folder described by the given String to this
   * StoreInfo.
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
   * This method connects the Store, and sets the StoreInfo to know that
   * the Store should be connected.  You should use this method instead of
   * calling getStore().connect(), because if you use this method, then
   * the StoreInfo will try to keep the Store connected, and will try to
   * reconnect the Store if it gets disconnected before 
   * disconnectStore is called.
   *
   * This method also calls updateChildren() to load the children of 
   * the Store, if the children vector has not been loaded yet.
   */
  public void connectStore() throws MessagingException {
    if (Pooka.isDebug())
      System.out.println("trying to connect store " + getStoreID());

    if (store.isConnected()) {
      if (Pooka.isDebug())
	System.out.println("store " + getStoreID() + " is already connected.");

      connected=true;
      return;
    } else { 
      try {
	// don't test for connections for mbox providers.
	if (!protocol.equalsIgnoreCase("mbox")) {
	  NetworkConnection currentConnection = getConnection();
	  if (Pooka.isDebug())
	    System.out.println("connect store " + getStoreID() + ":  checking connection.");

	  if (currentConnection != null) {
	    if (currentConnection.getStatus() == NetworkConnection.DISCONNECTED) {
	      if (Pooka.isDebug())
		System.out.println("connect store " + getStoreID() + ":  connection not up.  trying to connect it..");

	      currentConnection.connect(true, true);
	    }
	    
	    if (connection.getStatus() != NetworkConnection.CONNECTED) {
	      throw new MessagingException(Pooka.getProperty("error.connectionDown", "Connection down for Store:  ") + getItemID());
	    } else {
	      if (Pooka.isDebug())
		System.out.println("connect store " + getStoreID() + ":  successfully opened connection.");
	      
	    }
	  }
	}

	// Execute the precommand if there is one
	String preCommand = Pooka.getProperty(getStoreProperty() + ".precommand", "");
	if (preCommand.length() > 0) {
	  if (Pooka.isDebug())
	    System.out.println("connect store " + getStoreID() + ":  executing precommand.");

	  try {
	    Process p = Runtime.getRuntime().exec(preCommand);
	    p.waitFor();
	  } catch (Exception ex) {
	    System.out.println("Could not run precommand:");
	    ex.printStackTrace();
	  }
	}

	if (Pooka.isDebug())
	  System.out.println("connect store " + getStoreID() + ":  doing store.connect()");
	store.connect();
      } catch (MessagingException me) {
	Exception e = me.getNextException();
	if (e != null && e instanceof java.io.InterruptedIOException) 
	  store.connect();
	else
	  throw me;
      }

      if (Pooka.isDebug())
	System.out.println("connect store " + getStoreID() + ":  connection succeeded; connected = true.");
      connected=true;
      
      if (useSubscribed && protocol.equalsIgnoreCase("imap")) {
	synchSubscribed();
      }
      
      if (Pooka.getProperty("Pooka.openFoldersOnConnect", "true").equalsIgnoreCase("true")) {
	for (int i = 0; i < children.size(); i++) {
	  doOpenFolders((FolderInfo) children.elementAt(i));
	}
      }
    }
  }

  private void doOpenFolders(FolderInfo fi) {
    if (Pooka.getProperty("Pooka.openFoldersInBackGround", "false").equalsIgnoreCase("true")) {

      final FolderInfo current = fi;
      getStoreThread().addToQueue(new javax.swing.AbstractAction() {
	  public void actionPerformed(java.awt.event.ActionEvent e) {
	    current.openAllFolders(Folder.READ_WRITE);
	  }
	}, new java.awt.event.ActionEvent(this, 0, "open-all"), ActionThread.PRIORITY_LOW);
    }
    else {
      fi.openAllFolders(Folder.READ_WRITE);
    }
  }
  
  /**
   * This method disconnects the Store.  If you connect to the Store using 
   * connectStore() (which you should), then you should use this method
   * instead of calling getStore.disconnect().  If you don't, then the
   * StoreInfo will try to reconnect the store.
   */
  public void disconnectStore() throws MessagingException {
    MessagingException storeException = null;
    if (!(store.isConnected())) {
      connected=false;
      return;
    } else {
      try {
	store.close();
      } catch (MessagingException me) {
	storeException = me;
      } finally {
	connected=false;
	try {
	  closeAllFolders(false, false);
	} catch (MessagingException folderMe) {
	  if (storeException != null)
	    throw folderMe;
	  else {
	    storeException.setNextException(folderMe);
	    throw storeException;
	  }
	}
      }
    }
  }

  /**
   * Closes all of the Store's children.
   */
  public void closeAllFolders(boolean expunge, boolean shuttingDown) throws MessagingException {
    synchronized(getStoreThread().getRunLock()) {
      if (Pooka.isDebug())
	System.out.println("closing all folders of store " + getStoreID());
      Vector folders = getChildren();
      if (folders != null) {
	for (int i = 0; i < folders.size(); i++) {
	  ((FolderInfo) folders.elementAt(i)).closeAllFolders(expunge, shuttingDown);
	}
      }
    }
  }

    /**
     * Gets all of the children folders of this StoreInfo which are both
     * Open and can contain Messages.
     */
    public Vector getAllFolders() {
	Vector returnValue = new Vector();
	Vector subFolders = getChildren();
	for (int i = 0; i < subFolders.size(); i++) {
	    returnValue.addAll(((FolderInfo) subFolders.elementAt(i)).getAllFolders());
	}
	return returnValue;
    }

  /**
   * Synchronizes the locally stored subscribed folders list to the subscribed
   * folder information from the IMAP server.
   */
  public void synchSubscribed() throws MessagingException {
    Folder[] subscribedFolders = store.getDefaultFolder().list();
    
    StringBuffer newSubscribed = new StringBuffer();

    for (int i = 0; subscribedFolders != null && i < subscribedFolders.length; i++) {
      // sometimes listSubscribed() doesn't work.
      if (subscribedFolders[i].isSubscribed()) {
	String folderName = subscribedFolders[i].getName();
	newSubscribed.append(folderName).append(':');
      }
    }

    if (newSubscribed.length() > 0)
      newSubscribed.deleteCharAt(newSubscribed.length() -1);
    
    // this will update our children vector.
    Pooka.setProperty(getStoreProperty() + ".folderList", newSubscribed.toString());

    for (int i = 0; children != null && i < children.size(); i++) {
      FolderInfo fi = (FolderInfo) children.elementAt(i);
      fi.synchSubscribed();
    }
  }
    // Accessor methods.

    public Store getStore() {
	return store;
    }

    private void setStore(Store newValue) {
	store=newValue;
    }

    /**
     * This returns the StoreID.
     */
    public String getStoreID() {
	return storeID;
    }

  /**
   * This returns the ItemID, which in this case is the StoreID.
   */
  public String getItemID() {
    return getStoreID();
  }
  
    /**
     * This sets the storeID.
     */
    private void setStoreID(String newValue) {
	storeID=newValue;
    }

  /**
   * This returns the property which defines this StoreInfo, such as
   * "Store.myStore".
   */
  public String getStoreProperty() {
    return "Store." + getStoreID();
  }
  
  /**
   * This returns the item property, which in this case is the same as 
   * the storeProperty.
   */
  public String getItemProperty() {
    return getStoreProperty();
  }

    public Vector getChildren() {
	return children;
    }

    public StoreNode getStoreNode() {
	return storeNode;
    }

    public void setStoreNode(StoreNode newValue) {
	storeNode = newValue;
    }

    public boolean isConnected() {
	return connected;
    }

    public boolean isAvailable() {
	return available;
    }

    public boolean isAuthorized() {
	return authorized;
    }

    public UserProfile getDefaultProfile() {
	return defaultProfile;
    }

  public NetworkConnection getConnection() {
    return connection;
  }

    public ActionThread getStoreThread() {
	return storeThread;
    }

    public void setStoreThread(ActionThread newValue) {
	storeThread=newValue;
    }

    public FolderInfo getTrashFolder() {
	return trashFolder;
    }

    /**
     * This returns whether or not this Store is set up to use the 
     * TrashFolder.  If StoreProperty.useTrashFolder is set, return that as
     * a boolean.  Otherwise, return Pooka.useTrashFolder as a boolean.
     */
    public boolean useTrashFolder() {
	if (getTrashFolder() == null)
	    return false;

	String prop = Pooka.getProperty(getStoreProperty() + ".useTrashFolder", "");
	if (!prop.equals(""))
	    return (! prop.equalsIgnoreCase("false"));
	else
	    return (! Pooka.getProperty("Pooka.useTrashFolder", "true").equalsIgnoreCase("true"));
	
    }


    public void setTrashFolder(FolderInfo newValue) {
	trashFolder = newValue;
    }

}
