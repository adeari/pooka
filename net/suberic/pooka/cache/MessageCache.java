package net.suberic.pooka.cache;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.DataHandler;

public interface MessageCache {

    // tag defining the entire message
    public static int MESSAGE = 0;

    // tag defining the headers
    public static int HEADERS = 1;

    // tag defining the flags
    public static int FLAGS = 2;

    /**
     * Returns the datahandler for the given message uid.
     */
    public DataHandler getDataHandler(long uid) throws MessagingException;

    /**
     * Adds the given Flags to the message with the given uid.
     *
     * This affects both the client cache as well as the message on the
     * server, if the server is available.
     */
    public void addFlag(long uid, Flags flag) throws MessagingException;

    /**
     * Removes the given Flags from the message with the given uid.
     *
     * This affects both the client cache as well as the message on the
     * server, if the server is available.
     */
    public void removeFlag(long uid, Flags flag) throws MessagingException;

    /**
     * Returns the InternetHeaders object for the given uid.
     */
    public InternetHeaders getHeaders(long uid) throws MessagingException;

    /**
     * Returns the Flags object for the given uid.
     */
    public Flags getFlags(long uid) throws MessagingException;

    /**
     * Adds a message to the cache.  Note that status is only used to
     * determine whether or not the entire message is cached, or just
     * the headers and flags.
     *
     * This does not affect the server, nor does it affect message
     * count on the client.
     */
    public boolean cacheMessage(MimeMessage m, long uid, int status);

    /**
     * Removes a message from the cache only.  This has no effect on the
     * server.
     */
    public boolean invalidateCache(long uid);

    /**
     *  Invalidates all of the messages in the uids array in the cache.
     */
    public boolean invalidateCache(long[] uids);

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
    public long[] appendMessages(MimeMessage[] msgs, int status);

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
    public long[] expungeMessages();

    /**
     * This returns the uid's of the message which exist in updatedUids, but
     * not in the current list of messsages.
     */ 
    public long[] getAddedMessages(long[] updatedUids);

    /**
     * This returns the uid's of the message which exist in the current
     * list of messages, but no longer exist in the updatedUids.
     */
    public long[] getRemovedMessages(long[] updatedUids);

    /**
     * This returns the message id's of all the currently cached messages.
     * Note that only the headers and flags of the message need to be
     * cached for a message to be considered in the cache.
     */
    public long[] getMessageUids();
}
