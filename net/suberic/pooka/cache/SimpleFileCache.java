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
	DataHandler h = (DataHandler) getFromCache(uid, CONTENT);
	if (h != null) {
	    return h;
	} else {
	    if (getFolderInfo().isAvailable()) {
		MimeMessage m = getMessageById(uid);
		if (m != null) {
		    h = m.getDataHandler();
		    if (saveToCache)
			addMessage(m, uid, CACHE_ALL);
		    return h;
		} else
		    throw new MessagingException("No such message:  " + uid);
	    } else {
		throw new NotCachedException("Message is not cached, and folder is not available.");
	    }
	}
    }

    public DataHandler getDataHandler(long uid) throws MessagingException {
	return getDataHandler(uid, true);
    }

    public void addFlag(long uid, Flags flag) throws MessagingException {
	
    }

    public void removeFlag(long uid, Flags flag) throws MessagingException {

    }

    public InternetHeaders getHeaders(long uid, boolean saveToCache) throws MessagingException {
	InternetHeaders h = (InternetHeaders) getFromCache(uid, HEADERS);
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
			addMessage(m, uid, CACHE_HEADERS_AND_FLAGS);
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

    public Flags getFlags(long uid, boolean saveToCache) throws MessagingException {
	Flags f = (Flags) getFromCache(uid, FLAGS);
	if (f != null) {
	    return f;
	} else {
	    if (getFolderInfo().isAvailable()) {
		MimeMessage m = getMessageById(uid);
		if (f != null) {
		    f = m.getFlags();
		    if (saveToCache)
			addMessage(m, uid, CACHE_HEADERS_AND_FLAGS);
		    return f;
		} else
		    throw new MessagingException("No such message:  " + uid);
	    } else {
		throw new NotCachedException("Message is not cached, and folder is not available.");
	    }
	}

    }

    public Flags getFlags(long uid) throws MessagingException {
	return getFlags(uid, true);
    }

    public boolean addMessage(MimeMessage m, long uid) throws MessagingException {
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

    public boolean removeMessage(long uid) {
	invalidateCache(new long[] { uid });
	cachedMessages.remove(uid);

	return true;
    }

    public boolean invalidateCache(long[] uids) {
	if (uids != null) {

	    for (int i = 0 ; i < uids.length; i++)
		invalidateCache(uids[i]);

	    return true;
	} else
	    return false;
    }

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

    public long[] getAddedMessages(long[] uids) {

    }

    public long[] getRemovedMessages(long[] uids) {

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


