package net.suberic.pooka.cache;
import javax.mail.*;
import javax.mail.internet.MimeMessage;
import net.suberic.pooka.*;

/**
 * A FolderInfo which caches its messages in a MessageCache.
 */

public class CachingFolderInfo extends FolderInfo {
    protected MessageCache cache = null;
    protected int status;

    // folder is currently open and available.
    public static int CONNECTED = 0;

    // folder is available, but only 
    public static int DISCONNECTED = 1;
    public static int UNAVAILABLE = 2;

    // folder does not exist on server
    public static int INVALID = 3;
    
    public CachingFolderInfo(StoreInfo parent, String fname) {
	super(parent, fname);
	
	cache = new SimpleFileCache(this, Pooka.getProperty(getFolderProperty() + ".cacheDir", ""));

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
		    fp.add(value);
		}

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

	    long[] uids = cache.getMessageUids();
	    MessageInfo mi;
	    
	    for (int i = 0; i < msgs.length; i++) {
		mi = new MessageInfo(new CachingMimeMessage(uids[i],this), this);
		
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

	if (isAvailable()) {
	    Store s = getParentStore().getStore();
	    try {
		//Folder f = s.getFolder("nfdsaf238sa");
		//f.exists();
		Folder current = getFolder();
		if (current != null && current.isOpen()) {
		    current.getNewMessageCount();
		    current.getUnreadMessageCount();
		}
	    } catch ( MessagingException me ) {
		try {
		    if ( ! s.isConnected() )
			s.connect();
		} catch ( MessagingException me2 ) {
		}
	    }
	    
	    resetMessageCounts();
	}
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
     * This returns the MessageCache associated with this FolderInfo,
     * if any.
     */
    public MessageCache getCache() {
	return cache;
    }
    
    public javax.mail.internet.MimeMessage getMessageById(long uid) throws MessagingException {
	if (folder != null && folder instanceof UIDFolder) {
	    javax.mail.internet.MimeMessage m = (javax.mail.internet.MimeMessage) ((UIDFolder) folder).getMessageByUID(uid);
	    return m;
	}
	return null;
    }
}

