package net.suberic.pooka.cache;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.DataHandler;

public interface MessageCache {

    public static int CACHE_ALL = 0;

    public static int CACHE_HEADERS = 1;

    public static int CACHE_FLAGS_AND_HEADERS = 2;

    public static int CACHE_ALL = 3;

    public DataHandler getDataHandler(long uid) throws MessagingException;

    public void addFlag(long uid, Flags flag) throws MessagingException;

    public void removeFlag(long uid, Flags flag) throws MessagingException;

    public InternetHeaders getHeaders(long uid) throws MessagingException;

    public boolean addMessage(MimeMessage m, long uid, int status);

    public boolean removeMessage(long uid);

    public boolean invalidateCache(long[] uids);

    public long[] getAddedMessages(long[] uids);

    public long[] getRemovedMessages(long[] uids);

}
