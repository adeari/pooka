package net.suberic.pooka;
import javax.mail.*;

/**
 * A version of MessageInfo which stores a UID, and therefore can reload
 * itself if the Folder disconnects.
 */
public class UIDMessageInfo extends MessageInfo {
    
    long uid;
    long uidValidity;

    /**
     * Creates a new UIDMessageInfo from the given information.
     */
    public UIDMessageInfo(Message newMessage, FolderInfo newFolderInfo, long newUid, long newUidValidity) {
	super(newMessage, newFolderInfo);

	uid = newUid;
	uidValidity = newUidValidity;
    }

    /**
     * This loads the Attachment information into the attachments vector.
     *
     * If the load fails with a Exception, then this implementation will
     * try reloading the MessageInfo from the UID, and try again.
     */
    public void loadAttachmentInfo() throws MessagingException {

    }
   /**
     * This gets a Flag property from the Message.
     *
     * If the load fails with a Exception, then this implementation will
     * try reloading the MessageInfo from the UID, and try again.
     */

    public boolean flagIsSet(String flagName) throws MessagingException {
    }

}
