package net.suberic.pooka.gui.cache;
import net.suberic.pooka.FolderInfo;
import java.util.HashMap;
import java.io.*;
import javax.mail.*;
import javax.mail.internet.*;


/**
 * A simple cache.
 *
 */
public class SimpleFileCache implements MessageCache {
    
    public static int CONTENT = 0;
    public static int HEADERS = 1;
    public static int FLAGS = 2;

    // the source FolderInfo.
    private FolderInfo folderInfo;

    // the directory in which the cache is stored.
    private File cacheDir;

    // the UIDValidity
    private long uidValidity;

    // the HashMaps which store the cached results.
    private HashMap headerCache = new HashMap();
    private HashMap flagCache = new HashMap();
    private HashMap dataHandlerCache = new HashMap();

    // the currently cached uid's
    private Vector cachedMessages;

    /**
     * Creates a new SimpleFileCache for the given FolderInfo, in the
     * directory provided.
     */

    public SimpleFileCache(FolderInfo folder, String directoryName) throws IOException {
	folderInfo = folder;
	cacheDir = new File(directoryName);
	if ( ! cacheDir.exists() )
	    cacheDir.mkdirs();
	else if (! cacheDir.isDirectory())
	    throw new IOException("not a directory.");
	
	loadCache();
    }

    public DataHandler getDataHandler(long uid, boolean saveToCache) throws MessagingException {
	DataHandler h = getHandlerFromCache(uid);
	if (h != null) {
	    return h;
	} else {
	    if (getFolderInfo().isAvailable()) {
		MimeMessage m = getMessageById(uid);
		if (m != null) {
		    h = m.getDataHandler();
		    if (saveToCache)
			cacheMessage(m, uid, CACHE_ALL);
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
    public DataHandler getDataHandler(long uid) throws MessagingException {
	return getDataHandler(uid, true);
    }
    
    /**
     * Adds the given Flags to the message with the given uid.
     *
     * This affects both the client cache as well as the message on the
     * server, if the server is available.
     */
    public void addFlag(long uid, Flags flag) throws MessagingException {

    }

    /**
     * Removes the given Flags from the message with the given uid.
     *
     * This affects both the client cache as well as the message on the
     * server, if the server is available.
     */
    public void removeFlag(long uid, Flags flag) throws MessagingException {

    }

    /**
     * Returns the InternetHeaders object for the given uid.
     */    
    public InternetHeaders getHeaders(long uid, boolean saveToCache) throws MessagingException {
	InternetHeaders h = getHeadersFromCache(uid);
	if (h != null) {
	    return h;
	} else {
	    if (getFolderInfo().isAvailable()) {
		MimeMessage m = getMessageById(uid);
		if (m != null) {
		    java.util.Enumeration enum = m.getAllHeaderLines();
		    h = new InternetHeaders();
		    while (enum.hasMoreElements()) {
			h.addHeaderLine((String) enum.nextElement());
		    }
		    if (saveToCache)
			cacheMessage(m, uid, CACHE_HEADERS_AND_FLAGS);
		    return h;
		} else
		    throw new MessagingException("No such message:  " + uid);
	    } else {
		throw new NotCachedException("Message is not cached, and folder is not available.");
	    }
	}
    }

    public InternetHeaders getHeaders(long uid) throws MessagingException {
	return getHeaders(uid, true);
    }

    /**
     * Returns the Flags object for the given uid.
     */
    public Flags getFlags(long uid, boolean saveToCache) throws MessagingException {
	Flags f = getFlagsFromCache(uid);
	if (f != null) {
	    return f;
	} else {
	    if (getFolderInfo().isAvailable()) {
		MimeMessage m = getMessageById(uid);
		if (f != null) {
		    f = m.getFlags();
		    if (saveToCache)
			cacheMessage(m, uid, CACHE_HEADERS_AND_FLAGS);
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
    public Flags getFlags(long uid) throws MessagingException {
	return getFlags(uid, true);
    }

    /**
     * Adds a message to the cache.  Note that status is only used to
     * determine whether or not the entire message is cached, or just
     * the headers and flags.
     *
     * This does not affect the server, nor does it affect message
     * count on the client.
     */  
    public boolean cacheMessage(MimeMessage m, long uid, int status) throws MessagingException {
	File outFile = new File(cacheDir, uid + "_msg.gz");
	if (outFile.exists())
	    outFile.delete();
	
	FileOutputStream fos = new FileOutputStream(outFile);
	m.writeTo(fos);

	fos.flush();
	fos.close();
	
	if (! cachedMessages.contains(uid))
	    cachedMessages.add(uid);
	    
    }

    /**
     * Removes a message from the cache only.  This has no effect on the
     * server.
     */
    public boolean invalidateCache(long uid) {
	invalidateCache(new long[] { uid });
	cachedMessages.remove(uid);

	return true;
    }
    
    /**
     * Invalidates all of the messages in the uids array in the cache.
     */
    public boolean invalidateCache(long[] uids) {
	FilenameFilter filter = new FilenameFilter() {
		public boolean accept(File dir, String name) {
		    if (name.startsWith(uid + "_"))
			return true;
		    else
			return false;
		}
	    };
	
	File[] matchingFiles = cacheDir.listFiles(filter);
	for (int i = 0; i < matchingFiles.length; i++)
	    matchingFiles[i].delete();
    }
    
    /**
     * Adds the messages to the given folder.  Returns the uids for the 
     * message.  Uses the status to determine how much of the message
     * is cached.
     *
     * Note that if any message fails to be appended, then the ones that
     * have succeeded should be returned in the long[].  
     *
     * This method changes both the client cache as well as the server, if
     * the server is available.
     */
    public long[] appendMessages(MimeMessage[] msgs, int status) {
    }
    
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
    public long[] expungeMessages() {
    }

    /**
     * This returns the uid's of the message which exist in updatedUids, but
     * not in the current list of messsages.
     */ 
    public long[] getAddedMessages(long[] uids) {
	
    }

    /**
     * This returns the uid's of the message which exist in the current
     * list of messages, but no longer exist in the updatedUids.
     */
    public long[] getRemovedMessages(long[] uids) {

    }

    /**
     * This returns the message id's of all the currently cached messages.
     * Note that only the headers and flags of the message need to be
     * cached for a message to be considered in the cache.
     */
    public long[] getMessageUids() {

    }

    /**
     * Gets a DataHandler from the cache.  Returns null if no handler is
     * available in the cache.
     */
    protected DataHandler getHandlerFromCache(long uid) {
	File f = new File(cacheDir, uid + "_handler");
	if (f.exists())
	    return new DataHandler(new FileDataSource(f));
	else
	    return null;
    }

    /**
     * Gets the InternetHeaders from the cache.  Returns null if no headers are
     * available in the cache.
     */
    protected InternetHeader getHeadersFromCache(long uid) {
	File f = new File(cacheDir, uid + "_headers");
	if (f.exists())
	    return new InternetHeaders(new FileInputStream(f));
	else
	    return null;
    }

    /**
     * Gets the Flagss from the cache.  Returns null if no flagss are
     * available in the cache.
     */
    protected Flags getFlagsFromCache(long uid) {
	File f = new File(cacheDir, uid + "_flags");
	if (f.exists()) {
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

	    return newFlags;
	}
	    
	else
	    return null;
    }

    /**
     * Initializes the cache from the file system.
     */
    public void loadCache() {

    }

    private class CacheID {
	long id;
	long lastAccessed;
	long size;
	
	CacheId(long newId, long newLastAccessed, long newSize) {
	    id = newId;
	    lastAccessed = newLastAccessed;
	    size = newSize;
	}
    }
	
}


