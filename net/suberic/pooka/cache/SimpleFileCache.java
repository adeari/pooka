package net.suberic.pooka.cache;
import javax.mail.internet.*;
import net.suberic.pooka.FolderInfo;
import net.suberic.pooka.MessageInfo;
import java.util.HashMap;
import java.util.Vector;
import java.io.*;
import javax.mail.*;
import javax.mail.event.MessageChangedEvent;
import javax.activation.DataHandler;

/**
 * A simple cache.
 *
 */
public class SimpleFileCache implements MessageCache {

    // FIXME:  why isn't anyhting synchronized?
    
    public static int CONTENT = 0;
    public static int HEADERS = 1;
    public static int FLAGS = 2;
    public static int FLAGS_AND_HEADERS = 3;

    public static int ADDED = 10;
    public static int REMOVED = 11;

    public static String DELIMETER = "_";
    public static String CONTENT_EXT = "msg";
    public static String HEADER_EXT = "hdr";
    public static String FLAG_EXT = "flag";
    
    protected long uidValidity;

    // the source FolderInfo.
    private CachingFolderInfo folderInfo;

    // the directory in which the cache is stored.
    private File cacheDir;

    // the UIDValidity
    private long newUidValidity;

    // the HashMaps which store the cached results.
    //private HashMap headerCache = new HashMap();
    //private HashMap flagCache = new HashMap();
    //private HashMap dataHandlerCache = new HashMap();

    // the currently cached uid's
    private Vector cachedMessages;

    // the currently cached Flags.
    private HashMap cachedFlags;

    // the place where we store changes to happen later...
    private ChangeCache changes = null;

    /**
     * Creates a new SimpleFileCache for the given FolderInfo, in the
     * directory provided.
     */

    public SimpleFileCache(CachingFolderInfo folder, String directoryName) throws IOException {
	folderInfo = folder;
	cacheDir = new File(directoryName);
	if ( ! cacheDir.exists() )
	    cacheDir.mkdirs();
	else if (! cacheDir.isDirectory())
	    throw new IOException("not a directory.");

	changes = new ChangeCache(cacheDir);

	loadCache();
    }

    public DataHandler getDataHandler(long uid, long newUidValidity, boolean saveToCache) throws MessagingException {
	if (newUidValidity != uidValidity) {
	    throw new StaleCacheException(uidValidity, newUidValidity);
	}

	DataHandler h = getHandlerFromCache(uid);
	if (h != null) {
	    return h;
	} else {
	    if (getFolderInfo().shouldBeOpen()) {
		MimeMessage m = getFolderInfo().getRealMessageById(uid);
		if (m != null) {
		    h = m.getDataHandler();
		    if (saveToCache)
			cacheMessage(m, uid, newUidValidity, CONTENT);
		    return h;
		} else
		    throw new MessagingException("No such message:  " + uid);
	    } else {
		throw new NotCachedException("Message is not cached, and folder is not available.");
	    }
	}
    }

    /**
     * Returns the datahandler for the given message uid.
     */
    public DataHandler getDataHandler(long uid, long uidValidity) throws MessagingException {
	return getDataHandler(uid, uidValidity, true);
    }
    
    /**
     * Adds the given Flags to the message with the given uid.
     *
     * This affects both the client cache as well as the message on the
     * server, if the server is available.
     */
    public void addFlag(long uid, long newUidValidity, Flags flag) throws MessagingException {
	if (newUidValidity != uidValidity) {
	    throw new StaleCacheException(uidValidity, newUidValidity);
	}
	Flags f = getFlags(uid, newUidValidity);
	System.out.println("original for " + uid + " contains deleted:  " + f.contains(Flags.Flag.DELETED));
	if (f != null) {
	    f.add(flag);
	} else {
	    f = flag;
	}

	System.out.println("new for " + uid + " contains deleted:  " + f.contains(Flags.Flag.DELETED));

	if (getFolderInfo().shouldBeOpen()) {
	    MimeMessage m = getFolderInfo().getRealMessageById(uid);
	    if (m != null)
		m.setFlags(flag, true);
	} else {
	    writeToChangeLog(uid, flag, ADDED);
	    getFolderInfo().messageChanged(new MessageChangedEvent(this, MessageChangedEvent.FLAGS_CHANGED, getFolderInfo().getMessageInfoByUid(uid).getMessage()));
	}
	
	System.out.println("saving new flags for " + uid);
	saveFlags(uid, uidValidity, f);
    }

    /**
     * Removes the given Flags from the message with the given uid.
     *
     * This affects both the client cache as well as the message on the
     * server, if the server is available.
     */
    public void removeFlag(long uid, long newUidValidity, Flags flag) throws MessagingException {
	if (newUidValidity != uidValidity) {
	    throw new StaleCacheException(uidValidity, newUidValidity);
	}
	Flags f = getFlags(uid, newUidValidity);
	if (f != null) {
	    f.remove(flag);

	    if (getFolderInfo().shouldBeOpen()) {
		MimeMessage m = getFolderInfo().getRealMessageById(uid);
		if (m != null)
		    m.setFlags(flag, false);
	    } else {
		writeToChangeLog(uid, flag, REMOVED);
	    }
	    
	    saveFlags(uid, uidValidity, f);
	}
    }

    /**
     * Returns the InternetHeaders object for the given uid.
     */    
    public InternetHeaders getHeaders(long uid, long newUidValidity, boolean saveToCache) throws MessagingException {
	if (newUidValidity != uidValidity) {
	    throw new StaleCacheException(uidValidity, newUidValidity);
	}
	InternetHeaders h = getHeadersFromCache(uid);
	if (h != null) {
	    return h;
	} else {
	    if (getFolderInfo().shouldBeOpen()) {
		MimeMessage m = getFolderInfo().getRealMessageById(uid);
		if (m != null) {
		    java.util.Enumeration enum = m.getAllHeaderLines();
		    h = new InternetHeaders();
		    while (enum.hasMoreElements()) {
			h.addHeaderLine((String) enum.nextElement());
		    }
		    if (saveToCache)
			cacheMessage(m, uid, newUidValidity, HEADERS);
		    return h;
		} else
		    throw new MessagingException("No such message:  " + uid);
	    } else {
		throw new NotCachedException("Message is not cached, and folder is not available.");
	    }
	}
    }

    public InternetHeaders getHeaders(long uid, long uidValidity) throws MessagingException {
	return getHeaders(uid, uidValidity, true);
    }

    /**
     * Returns the Flags object for the given uid.
     */
    public Flags getFlags(long uid, long newUidValidity, boolean saveToCache) throws MessagingException {
	if (newUidValidity != uidValidity) {
	    throw new StaleCacheException(uidValidity, newUidValidity);
	}
	Flags f = getFlagsFromCache(uid);
	
	if (f != null) {
	    return f;
	} else {
	    if (getFolderInfo().shouldBeOpen()) {
		MimeMessage m = getFolderInfo().getRealMessageById(uid);
		if (m != null) {
		    f = m.getFlags();
		    if (saveToCache)
			cacheMessage(m, uid, newUidValidity, FLAGS);
		    return f;
		} else
		    throw new MessagingException("No such message:  " + uid);
	    } else {
		throw new NotCachedException("Message is not cached, and folder is not available.");
	    }
	}

    }

    /**
     * Returns the Flags object for the given uid.
     */
    public Flags getFlags(long uid, long uidValidity) throws MessagingException {
	return getFlags(uid, uidValidity, true);
    }

    /**
     * Adds a message to the cache.  Note that status is only used to
     * determine whether or not the entire message is cached, or just
     * the headers and flags.
     *
     * This does not affect the server, nor does it affect message
     * count on the client.
     */  
    public boolean cacheMessage(MimeMessage m, long uid, long newUidValidity, int status) throws MessagingException {
	System.out.println("cache message(" + uid + ")");
	if (newUidValidity != uidValidity) {
	    throw new StaleCacheException(uidValidity, newUidValidity);
	}
	try {
	    if (status == CONTENT) {
		File outFile = new File(cacheDir, uid + DELIMETER + CONTENT_EXT);
		if (outFile.exists())
		    outFile.delete();
		
		FileOutputStream fos = new FileOutputStream(outFile);
		m.writeTo(fos);
		
		fos.flush();
		fos.close();
	    }
	    
	    if (status == CONTENT || status == FLAGS || status == FLAGS_AND_HEADERS) {
		Flags flags = m.getFlags();
		saveFlags(uid, uidValidity, flags);
	    }


	    if (status == CONTENT || status == HEADERS || status == FLAGS_AND_HEADERS) {
	    
		File outFile = new File(cacheDir, uid + DELIMETER + HEADER_EXT);
		if (outFile.exists())
		    outFile.delete();
		
		outFile.createNewFile();
		
		FileWriter fos = new FileWriter(outFile);
		java.util.Enumeration enum = m.getAllHeaderLines();
		BufferedWriter bos = new BufferedWriter(fos);
		
		int foo = 0;
		while (enum.hasMoreElements()) {
		    bos.write((String) enum.nextElement());
		    bos.newLine();
		}
		
		bos.newLine();
		bos.flush();
		bos.close();
	    }

	    if (! cachedMessages.contains(new Long(uid))) {
		cachedMessages.add(new Long(uid));
		writeMsgFile();
	    } 
	} catch (IOException ioe) {
	    throw new MessagingException(ioe.getMessage(), ioe);
	}
	
	return true;
    }

    /**
     * Removes a message from the cache only.  This has no effect on the
     * server.
     */
    public boolean invalidateCache(long uid, int status) {
	invalidateCache(new long[] { uid }, status);

	return true;
    }
    
    /**
     * Invalidates all of the messages in the uids array in the cache.
     */
    public boolean invalidateCache(long[] uids, int status) {
	for (int i = 0; i < uids.length; i++) {
	    FilenameFilter filter = new CacheFilenameFilter(uids[i], status);
	    File[] matchingFiles = cacheDir.listFiles(filter);
	    for (int j = 0; j < matchingFiles.length; j++)
		matchingFiles[j].delete();
	    if (status == CONTENT) {
		Long l = new Long(uids[i]);
		cachedMessages.remove(l);
		cachedFlags.remove(l);
		writeMsgFile();
	    } 
	}

	return true;
    }

    /**
     * Invalidates the entire cache.  Usually called when the uidValidity
     * is changed.
     */
    public void invalidateCache() {
	File[] matchingFiles = cacheDir.listFiles();
	if (matchingFiles != null)
	    for (int j = 0; j < matchingFiles.length; j++) {
		if (matchingFiles[j].isFile())
		    matchingFiles[j].delete();
	    }

	cachedMessages = new Vector();
	cachedFlags = new HashMap();

    }


    /**
     * Adds the messages to the given folder.  Returns the uids for the 
     * message.  Uses the status to determine how much of the message
     * is cached.
     *
     * This method changes both the client cache as well as the server, if
     * the server is available.
     */
    
    /*
    public void appendMessages(MessageInfo[] msgs, int status) throws MessagingException {
	if (newUidValidity != uidValidity) {
	    throw new StaleCacheException(uidValidity, newUidValidity);
	}
	if (getFolderInfo().shouldBeOpen()) {
	    getFolderInfo().appendMessages(msgs);
	} else {
	    throw new MessagingException("Error:  cannot append to an unavailable folder.");
	}
    }
    */

    /**
     * Removes all messages marked as 'DELETED'  from the given folder.  
     * Returns the uids of all the removed messages.
     *
     * Note that if any message fails to be removed, then the ones
     * that have succeeded should be returned in the long[].
     *
     * This method changes both the client cache as well as the server, if
     * the server is available.
     */
    public void expungeMessages() throws MessagingException {
	if (getFolderInfo().shouldBeOpen()) {
	    getFolderInfo().expunge();
	} else {
	    try {
		getChangeAdapter().expunge();
	    } catch (IOException ioe) {
		throw new MessagingException(ioe.getMessage(), ioe);
	    }
	}
    }

    /**
     * This returns the uid's of the message which exist in updatedUids, but
     * not in the current list of messsages.
     */ 
    public long[] getAddedMessages(long[] uids, long newUidValidity) throws StaleCacheException {
	if (newUidValidity != uidValidity) {
	    throw new StaleCacheException(uidValidity, newUidValidity);
	}
	long[] added = new long[uids.length];
	int addedCount = 0;
	
	for (int i = 0; i < uids.length; i++) {
	    if (! cachedMessages.contains(new Long(uids[i]))) {
		added[addedCount++]=uids[i];
	    }
	}

	long[] returnValue = new long[addedCount];
	if (addedCount > 0) 
	    System.arraycopy(added, 0, returnValue, 0, addedCount);

	return returnValue;
    }

    /**
     * This returns the uid's of the message which exist in the current
     * list of messages, but no longer exist in the updatedUids.
     */
    public long[] getRemovedMessages(long[] uids, long newUidValidity) throws StaleCacheException {
	if (newUidValidity != uidValidity) {
	    throw new StaleCacheException(uidValidity, newUidValidity);
	}
	Vector remainders = new Vector(cachedMessages);
	
	for (int i = 0; i < uids.length; i++) {
	    remainders.remove(new Long(uids[i]));
	}

	long[] returnValue = new long[remainders.size()];
	for (int i = 0; i < remainders.size(); i++)
	    returnValue[i] = ((Long) remainders.elementAt(i)).longValue();
	
	return returnValue;
    }

    /**
     * This returns the message id's of all the currently cached messages.
     * Note that only the headers and flags of the message need to be
     * cached for a message to be considered in the cache.
     */
    public long[] getMessageUids() {
	long[] returnValue = new long[cachedMessages.size()];
	for (int i = 0; i < cachedMessages.size(); i++) 
	    returnValue[i] = ((Long) cachedMessages.elementAt(i)).longValue();

	return returnValue;
    }

    /**
     * Gets a DataHandler from the cache.  Returns null if no handler is
     * available in the cache.
     */
    protected DataHandler getHandlerFromCache(long uid) {
	File f = new File(cacheDir, uid + DELIMETER + CONTENT_EXT);
	if (f.exists()) {
	    try {
		FileInputStream fis = new FileInputStream(f);
		MimeMessage mm = new MimeMessage(net.suberic.pooka.Pooka.getDefaultSession(), fis);
		javax.activation.DataSource source = new MimePartDataSource (mm);
		return new DataHandler(source);
	    } catch (Exception e) {
		return null;
	    } 
	    //return new DataHandler(new FileDataSource(f));
	} else
	    return null;
    }

    /**
     * Gets the InternetHeaders from the cache.  Returns null if no headers are
     * available in the cache.
     */
    protected InternetHeaders getHeadersFromCache(long uid) throws MessagingException {
	File f = new File(cacheDir, uid +DELIMETER + HEADER_EXT);
	if (f.exists())
	    try {
		return new InternetHeaders(new FileInputStream(f));
	    } catch (FileNotFoundException fnfe) {
		throw new MessagingException(fnfe.getMessage(), fnfe);
	    }
	else
	    return null;
    }

    /**
     * Gets the Flagss from the cache.  Returns null if no flagss are
     * available in the cache.
     */
    protected Flags getFlagsFromCache(long uid) {
	Flags returnValue = (Flags) cachedFlags.get(new Long(uid));
	if (returnValue != null) {
	    return returnValue;
	} else {
	    File f = new File(cacheDir, uid + DELIMETER + FLAG_EXT);
	    if (f.exists()) {
		try {
		    Flags newFlags = new Flags();
		    BufferedReader in = new BufferedReader(new FileReader(f));
		    for (String currentLine = in.readLine(); currentLine != null; currentLine = in.readLine()) {
			
			if (currentLine.equalsIgnoreCase("Deleted"))
			    newFlags.add(Flags.Flag.DELETED);
			else if (currentLine.equalsIgnoreCase("Answered"))
			    newFlags.add(Flags.Flag.ANSWERED);
			else if (currentLine.equalsIgnoreCase("Draft"))
			    newFlags.add(Flags.Flag.DRAFT);
			else if (currentLine.equalsIgnoreCase("Flagged"))
			    newFlags.add(Flags.Flag.FLAGGED);
			else if (currentLine.equalsIgnoreCase("Recent"))
			    newFlags.add(Flags.Flag.RECENT);
			else if (currentLine.equalsIgnoreCase("SEEN"))
			    newFlags.add(Flags.Flag.SEEN);
			else 
			    newFlags.add(new Flags(currentLine));
		    }
		
		    cachedFlags.put(new Long(uid), newFlags);
		    return newFlags;
		} catch (FileNotFoundException fnfe) {
		    return null;
		} catch (IOException ioe) {
		    return null;
		}
	    }
	    return null;
	}
    }

    /**
     * Saves the given flags to the cache.
     */
    protected void saveFlags(long uid, long newUidValidity, Flags f) throws MessagingException {
	Flags oldFlags = getFlagsFromCache(uid);
	System.out.println("caching flags for " + uid);
	if (oldFlags != null && ! oldFlags.equals(f)) {
	    System.out.println("new flags don't equal cached Flags.");
	    cachedFlags.put(new Long(uid), f);
	    try {
		File outFile = new File(cacheDir, uid + DELIMETER + FLAG_EXT);
		if (outFile.exists())
		    outFile.delete();
		
		FileWriter fw = new FileWriter(outFile);
		BufferedWriter bw = new BufferedWriter(fw);
		
		Flags.Flag[] systemFlags = f.getSystemFlags();
		for (int i = 0; i < systemFlags.length; i++) {
		    if (systemFlags[i] == Flags.Flag.ANSWERED) {
			bw.write("Answered");
			bw.newLine();
		    } else if (systemFlags[i] == Flags.Flag.DELETED) {
			bw.write("Deleted");
			bw.newLine();
		    } else if (systemFlags[i] == Flags.Flag.DRAFT) {
			bw.write("Draft");
			bw.newLine();
		    } else if (systemFlags[i] == Flags.Flag.FLAGGED) {
			bw.write("Flagged");
			bw.newLine();
		    } else if (systemFlags[i] == Flags.Flag.RECENT) {
			bw.write("Recent");
			bw.newLine();
		    } else if (systemFlags[i] == Flags.Flag.SEEN) {
			bw.write("Seen");
			bw.newLine();
		}
		}
		
		String[] userFlags = f.getUserFlags();
		for (int i = 0; i < userFlags.length; i++) {
		    bw.write(userFlags[i]);
		    bw.newLine();
		}
		
		bw.flush();
		bw.close();
	    } catch (IOException ioe) {
		throw new MessagingException (ioe.getMessage(), ioe);
	    }
	}
    }

    protected void writeToChangeLog(long uid, Flags flags, int status) throws MessagingException {
	System.out.println("writing to change log.");
	try {
	    if (status == REMOVED)
		getChangeAdapter().setFlags(uid, flags, false);
	    else
		getChangeAdapter().setFlags(uid, flags, true);
	} catch (IOException ioe) {
	    throw new MessagingException (ioe.getMessage(), ioe);
	}
	System.out.println("done writing to change log.");
    }

    /**
     * Initializes the cache from the file system.
     */
    public void loadCache() {
	cachedMessages = new Vector();
	cachedFlags = new HashMap();
	
	File msgListFile = new File(cacheDir, "messageList");
	if (msgListFile.exists()) {
	    try {
		BufferedReader in = new BufferedReader(new FileReader(msgListFile));
		for (String nextLine = in.readLine(); nextLine != null; nextLine = in.readLine()) {
		    Long l = new Long(nextLine);
		    cachedMessages.add(l);
		    // this has the side effect of loading the cached flags
		    // to the cachedFlags HashMap.
		    getFlagsFromCache(l.longValue());
		}
	    } catch (Exception e) { }
	}
	
	File validityFile = new File(cacheDir, "validity");
	if (validityFile.exists()) {
	    try {
		BufferedReader in = new BufferedReader(new FileReader(validityFile));
		uidValidity = Long.parseLong(in.readLine());
	    } catch (Exception e) {
	    }
	}
    }	    

    public void writeMsgFile() {
	try {
	    File msgListFile = new File(cacheDir, "messageList");
	    if (! msgListFile.exists()) {
		msgListFile.createNewFile();
	    }
	    BufferedWriter out = new BufferedWriter(new FileWriter(msgListFile));
	    for (int i = 0; i < cachedMessages.size(); i++) {
		out.write(((Long) cachedMessages.elementAt(i)).toString());
		out.newLine();
	    }
	    out.flush();
	    out.close();
	} catch (Exception e) {
	}
    }
    
    public CachingFolderInfo getFolderInfo() {
	return folderInfo;
    }

    private class CacheID {
	long id;
	long lastAccessed;
	long size;
	
	CacheID(long newId, long newLastAccessed, long newSize) {
	    id = newId;
	    lastAccessed = newLastAccessed;
	    size = newSize;
	}
    }

    private class CacheFilenameFilter implements FilenameFilter {
	long uid;
	int status;

	public CacheFilenameFilter(long newUid, int newStatus) {
	    uid = newUid;
	    status = newStatus;
	}
	
	public boolean accept(File dir, String name) {
	    if (status == CONTENT) {
		if (name.startsWith(uid + DELIMETER))
		    return true;
		else
		    return false;
	    } else {
		if (name.startsWith(uid + DELIMETER))
		    return true;
		else
		    return false;
		
	    }
	}
    }

    /**
     * This returns the number of messages in the cache.
     */
    public int getMessageCount() {
	return cachedMessages.size();
    }
    
    /**
     * This returns the number of unread messages in the cache.
     */
    public int getUnreadMessageCount() throws MessagingException {
	// sigh.
	int unreadCount = 0;
	for (int i = 0; i < cachedMessages.size(); i++) {
	    Flags f = getFlags(((Long) cachedMessages.elementAt(i)).longValue(), uidValidity, false);
	    if (f.contains(Flags.Flag.SEEN))
		unreadCount++;
	}

	return unreadCount;
    }

    public long getUIDValidity() {
	return uidValidity;
    }

    public void setUIDValidity(long newValidity) {
	try {
	    File f = new File(cacheDir, "validity");
	    if (f.exists())
		f.delete();

	    f.createNewFile();
	    
	    BufferedWriter out = new BufferedWriter(new FileWriter(f));
	    out.write(Long.toString(newValidity));
	    out.flush();
	    out.close();
	} catch (Exception e) {
	}

	uidValidity = newValidity;
    }

    public ChangeCache getChangeAdapter() {
	return changes;
    }
}


