package net.suberic.pooka.cache;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.DataHandler;
import net.suberic.pooka.MessageInfo;

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
    public DataHandler getDataHandler(long uid, long uidValidity) throws MessagingException;

    /**
     * Adds the given Flags to the message with the given uid.
     *
     * This affects both the client cache as well as the message on the
     * server, if the server is available.
     */
    public void addFlag(long uid, long uidValidity, Flags flag) throws MessagingException;

    /**
     * Removes the given Flags from the message with the given uid.
     *
     * This affects both the client cache as well as the message on the
     * server, if the server is available.
     */
    public void removeFlag(long uid, long uidValidity, Flags flag) throws MessagingException;

    /**
     * Returns the InternetHeaders object for the given uid.
     */
    public InternetHeaders getHeaders(long uid, long uidValidity) throws MessagingException;

    /**
     * Returns the Flags object for the given uid.
     */
    public Flags getFlags(long uid, long uidValidity) throws MessagingException;

    /**
     * Adds a message to the cache.  Note that status is only used to
     * determine whether or not the entire message is cached, or just
     * the headers and flags.
     *
     * This does not affect the server, nor does it affect message
     * count on the client.
     */
    public boolean cacheMessage(MimeMessage m, long uid, long uidValidity, int status) throws MessagingException;

    /**
     * Removes a message from the cache only.  This has no effect on the
     * server.
     */
    public boolean invalidateCache(long uid, int status);

    /**
     *  Invalidates all of the messages in the uids array in the cache.
     */
    public boolean invalidateCache(long[] uids, int status);

    /**
     * Adds the messages to the given folder.  Returns the uids for the 
     * message.  Uses the status to determine how much of the message
     * is cached.
     *
     * This method changes both the client cache as well as the server, if
     * the server is available.
     */
    //public void appendMessages(MessageInfo[] msgs, int status) throws MessagingException;

    /**
     * Removes all messages marked as 'DELETED'  from the given folder.  
     *
     * Note that if any message fails to be removed, then the ones
     * that have succeeded should be returned in the long[].
     *
     * This method changes both the client cache as well as the server, if
     * the server is available.
     */
    public void expungeMessages() throws MessagingException;

    /**
     * This returns the uid's of the message which exist in updatedUids, but
     * not in the current list of messsages.
     */ 
    public long[] getAddedMessages(long[] updatedUids, long uidValidity) throws StaleCacheException;

    /**
     * This returns the uid's of the message which exist in the current
     * list of messages, but no longer exist in the updatedUids.
     */
    public long[] getRemovedMessages(long[] updatedUids, long uidValidity) throws StaleCacheException;

    /**
     * This returns the message id's of all the currently cached messages.
     * Note that only the headers and flags of the message need to be
     * cached for a message to be considered in the cache.
     */
    public long[] getMessageUids();

    /**
     * This returns the number of messages in the cache.
     */
    public int getMessageCount() throws MessagingException;

    /**
     * This returns the number of unread messages in the cache.
     */
    public int getUnreadMessageCount() throws MessagingException;

}
