package net.suberic.pooka.gui.cache;
import net.suberic.pooka.FolderInfo;
import net.suberic.pooka.gui.MessageProxy;
import java.util.HashMap;
import java.io.*;
import javax.mail.*;


/**
 * A simple cache.
 *
 */
public class SimpleFileCache {
    
    // the source FolderInfo.
    private FolderInfo folderInfo;

    // the File in which the cache is stored.
    private File cacheFile;

    // the UIDValidity
    private long uidValidity;

    // the HashMap which stores the cached results.
    private HashMap cache;

    /**
     * Creates a new SimpleFileCache for the given FolderInfo, in the
     * directory provided.
     */

    public SimpleFileCache(FolderInfo folder, String directoryName) {
	folderInfo = folder;
	File directory = new File(directoryName);
	cacheFile = new File(directory, folderInfo.getFolderID() + ".cache");
	loadCache(cacheFile);
    }

    /**
     * Gets the MessageProxy associated with the given unique ID.
     *
     * This implementation just calls getMessageProxy(uid, true).
     */
    public synchronized MessageProxy getMessageProxy(long uid) throws MessagingException {
	return getMessageProxy(uid, true);
    }

    /**
     * Gets the MessageProxy associated with the given unique ID.
     * 
     * If addToCache is set to true, then if the MessageProxy requested is 
     * not in the cache already, it tries to create a new MessageProxy
     * from the FolderInfo, add that to the cache, and return it.
     * 
     * If addToCache is set to false, then, if the given MessageProxy
     * is not in the cache, then null is returned.
     */
    public synchronized MessageProxy getMessageProxy(long uid, boolean addToCache) 
	throws MessagingException {

	MessageProxy proxy = (MessageProxy) cache.get(new Long(uid));
	if (proxy == null && addToCache && folderInfo.isOpen()) {
	    Folder f = folderInfo.getFolder();
	    if (f instanceof UIDFolder) {
		UIDFolder uidFolder = (UIDFolder) f;
		Message m = uidFolder.getMessageByUID(uid);
		if (m != null) {
		    proxy = new MessageProxy(folderInfo.getColumnValues(), m, folderInfo);
		    addToCache(uid, proxy);
		}
		
	    }
	}

	return proxy;
    }

    /**
     * Gets the MessageProxies associated with the given unique IDs.
     *
     * If a particular uid is not available, null is returned in its spot.
     */
    public synchronized MessageProxy[] getMessageProxy(long[] uids) throws MessagingException {
	return getMessageProxy(uids, true);
    }

    /**
     * Gets the MessageProxies associated with the given unique IDs.
     *
     * If a particular uid is not available, null is returned in its spot.
     */
    public synchronized MessageProxy[] getMessageProxy(long[] uids, boolean addToCache) throws MessagingException {
	MessageProxy[] proxyArray = new MessageProxy[uids.length];
	for (int i = 0; i < uids.length; i++) {
	    proxyArray[i] = getMessageProxy(uids[i], addToCache);
	}

	return proxyArray;
    }

    /**
     * Removes the given MessageProxy from the cache.
     */
    public synchronized void removeFromCache(MessageProxy toBeRemoved) {
	cache.remove(new Long(toBeRemoved.getUID()));
    }

    /**
     * Removes the given MessageProxies from the cache.
     */
    public synchronized void removeFromCache(MessageProxy[] toBeRemoved){
	for (int i = 0; i < toBeRemoved.length; i++) {
	    removeFromCache(toBeRemoved[i]);
	}
    }

    /**
     * Adds the given MessageProxy to the cache.
     */
    public synchronized void addToCache(long uid, MessageProxy toBeAdded){
	cache.put(new Long(uid), toBeAdded);
    }

    /**
     * Adds the given MessageProxies to the cache.
     */
    public synchronized void addToCache(long[] uid, MessageProxy[] toBeAdded){
	for (int i = 0; i < uid.length; i++) {
	    addToCache(uid[i], toBeAdded[i]);
	}
    }

    /**
     * Flushes the cache.
     */
    public synchronized void flushCache(long newValidity){
	cache = new HashMap();
	uidValidity = newValidity;
    }

    /**
     * Says whether or not the MessageProxy with the given UID is cached.
     */
    public boolean isCached(long uid){
	return cache.containsKey(new Long(uid));
    }
    
    /**
     * Checks against the given validity UID to see if the information in
     * 
     */
    public boolean isValid(long validity) {
	return (validity == uidValidity);
    }

    /**
     * Loads the cache from disk.
     */
    public void loadCache(File cacheFile) {
	if (cacheFile.exists() && cacheFile.isDirectory()) {
	    File 

	}
    }

    /**
     * Loads a CacheID from a String.
     */
    private loadCacheID(String idLine) {
	try {
	    StringTokenizer tok = new StringTokenizer(idLine, ":");
	    long uid = Long.parseLong(tok.nextToken());
	    String lastAccessed = Long.parseLong(tok.nextToken());
    }

    /**
     * This synchronizes the cache with the content file.
     */
    protected void synchronizeCache() {
	
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


