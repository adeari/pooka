package net.suberic.pooka.cache;
import javax.mail.internet.*;

public class WrappedMimePartDataSource extends MimePartDataSource {
    MessageCache cache;
    long uid;

    public WrappedMimePartDataSource(MimePart mm, MessageCache newCache, long newUid) {
	super(mm);
	cache=newCache;
	uid = newUid;
    }

    public java.io.InputStream getInputStream() throws java.io.IOException {
	try {

	    return super.getInputStream();

	} catch (java.io.IOException ioe) {
	    cache.invalidateCache(uid, MessageCache.CONTENT);
	    throw ioe;
	}
    }

}
