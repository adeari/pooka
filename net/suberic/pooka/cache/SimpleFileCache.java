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
    private long[] cachedMessages;

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

    public DataHandler getDataHandler(long uid) throws MessagingException;

    public void addFlag(long uid, Flags flag) throws MessagingException;

    public void removeFlag(long uid, Flags flag) throws MessagingException;

    public InternetHeaders getHeaders(long uid) throws MessagingException;

    public boolean addMessage(MimeMessage m, long uid, int status);

    public boolean removeMessage(long uid);

    public boolean invalidateCache(long[] uids);

    public long[] getAddedMessages(long[] uids);

    public long[] getRemovedMessages(long[] uids);

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


