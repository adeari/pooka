package net.suberic.pooka;
import net.suberic.pooka.*;
import net.suberic.pooka.gui.MessageProxy;
import net.suberic.util.thread.*;
import javax.mail.*;
import javax.mail.internet.MimeMessage;
import javax.mail.event.*;
import javax.swing.*;
import java.util.Hashtable;
import java.util.Vector;

public class MessageInfo {
    // the wrapped Message 
    Message message;

    // the source FolderInfo
    FolderInfo folderInfo;

    // if the tableInfo has been loaded yet.
    boolean loaded = false;

    // if the message has been read
    boolean seen = false;

    // if the attachments have been loaded yet.
    boolean attachmentsLoaded = false;

    // the MessageProxy associated with this MessageInfo
    MessageProxy messageProxy;

    // the attachments on the message.
    AttachmentBundle attachments;

    protected MessageInfo() {
    }

    /**
     * This creates a new MessageInfo from the given FolderInfo and Message.
     */
    public MessageInfo(Message newMessage, FolderInfo newFolderInfo) {
	folderInfo = newFolderInfo;
	message = newMessage;

	try {
	    seen = flagIsSet("FLAG.SEEN");
	} catch (MessagingException me) { }
    }

    /**
     * This loads the Attachment information into the attachments vector.
     */

    public void loadAttachmentInfo() throws MessagingException {
	attachments = MailUtilities.parseAttachments(getMessage());
	
	attachmentsLoaded = true;
    }

    /**
     * This gets a Flag property from the Message.
     */

    public boolean flagIsSet(String flagName) throws MessagingException {

	if (flagName.equals("FLAG.ANSWERED") )
	    return getMessage().isSet(Flags.Flag.ANSWERED);
	else if (flagName.equals("FLAG.DELETED"))
	    return getMessage().isSet(Flags.Flag.DELETED);
	else if (flagName.equals("FLAG.DRAFT"))
	    return getMessage().isSet(Flags.Flag.DRAFT);
	else if (flagName.equals("FLAG.FLAGGED"))
	    return getMessage().isSet(Flags.Flag.FLAGGED);
	else if (flagName.equals("FLAG.RECENT"))
	    return getMessage().isSet(Flags.Flag.RECENT);
	else if (flagName.equals("FLAG.SEEN"))
	    return getMessage().isSet(Flags.Flag.SEEN);
	
	return false;
    }

    /**
     * This gets the Flags object for the wrapped Message.
     */
    public Flags getFlags() throws MessagingException {
	return getMessage().getFlags();
    }

    /**
     * Refreshes the flags object.
     */
    public void refreshFlags() throws MessagingException {
	// this is a no-op for this implementation
    }
    
    /**
     * Refreshes the Headers object.
     */
    public void refreshHeaders() throws MessagingException {
	// this is a no-op for this implementation
    }

    /**
     * This gets a particular property (From, To, Date, Subject, or just
     * about any Email Header) from the Message.
     */
    public Object getMessageProperty(String prop) throws MessagingException {
	Message msg = getMessage();
	if (prop.equals("From")) {
	    Address[] fromAddr = msg.getFrom();
	    if (fromAddr != null && fromAddr[0] != null) 
		return ((javax.mail.internet.InternetAddress)fromAddr[0]).toString();
	    else 
		return null;
	} else if (prop.equals("receivedDate")) {
	    return msg.getReceivedDate();
	} else if (prop.equals("recipients")) {
	    return msg.getRecipients(Message.RecipientType.TO).toString();
	} else if (prop.equals("Date")) {
	    return msg.getSentDate();
	} else if (prop.equals("Subject")) {
	    return msg.getSubject();
	} 
	
	if (msg instanceof MimeMessage) {
	    String hdrVal = ((MimeMessage)msg).getHeader(prop, ",");
	    if (hdrVal != null && hdrVal.length() > 0)
		return hdrVal;
	}
	return "";
	
    }

    /**
     * Gets the Content and inline text content for the Message.
     */
    public String getTextAndTextInlines(String attachmentSeparator, boolean withHeaders, boolean showFullHeaders, int maxLength, String truncationMessage) throws MessagingException {
	try {
	    if (!hasLoadedAttachments()) 
		loadAttachmentInfo();
	    return attachments.getTextAndTextInlines(attachmentSeparator, withHeaders, showFullHeaders, maxLength, truncationMessage);
	} catch (FolderClosedException fce) {
	    try {
		getFolderInfo().openFolder(Folder.READ_WRITE);
		loadAttachmentInfo();
		return attachments.getTextAndTextInlines(attachmentSeparator, withHeaders, showFullHeaders, maxLength, truncationMessage);
	    } catch (java.io.IOException ioe) {
		throw new MessagingException(ioe.getMessage()); 
	    }
	} catch (java.io.IOException ioe) {
	    throw new MessagingException(ioe.getMessage()); 
	}
    }

    /**
     * Gets the Content and inline text content for the Message.
     */
    public String getTextAndTextInlines(String attachmentSeparator, boolean withHeaders, boolean showFullHeaders) throws MessagingException {
	return getTextAndTextInlines(attachmentSeparator, withHeaders, showFullHeaders, getMaxMessageDisplayLength(), getTruncationMessage());
    }

    /**
     * Gets the Text part of the Content of this Message.
     */
    public String getTextPart(boolean withHeaders, boolean showFullHeaders, int maxLength, String truncationMessage) throws MessagingException {
	try {
	    if (!hasLoadedAttachments()) 
		loadAttachmentInfo();
	    return attachments.getTextPart(withHeaders, showFullHeaders, maxLength, truncationMessage);
	} catch (FolderClosedException fce) {
	    try {
		getFolderInfo().openFolder(Folder.READ_WRITE);
		loadAttachmentInfo();
		return attachments.getTextPart(withHeaders, showFullHeaders, maxLength, truncationMessage);
	    } catch (java.io.IOException ioe) {
		throw new MessagingException(ioe.getMessage()); 
	    }
	} catch (java.io.IOException ioe) {
	    throw new MessagingException(ioe.getMessage()); 
	}
    }

    /**
     * Gets the Text part of the Content of this Message.
     */
    public String getTextPart(boolean withHeaders, boolean showFullHeaders) throws MessagingException {
	return getTextPart(withHeaders, showFullHeaders, getMaxMessageDisplayLength(), getTruncationMessage());
    }

    /**
     * Moves the Message into the target Folder.
     */
    public void moveMessage(FolderInfo targetFolder, boolean expunge) throws MessagingException {
	Message m = getRealMessage();
	try {
	    folderInfo.getFolder().copyMessages(new Message[] {m}, targetFolder.getFolder());
	} catch (MessagingException me) {
	    throw new MessagingException (Pooka.getProperty("error.Message.CopyErrorMessage", "Error:  could not copy messages to folder:  ") + targetFolder.toString() +"\n", me);
	}
	
	try {
	    remove(expunge);
	} catch (MessagingException me) {
	    throw new MessagingException(Pooka.getProperty("error.Message.RemoveErrorMessage", "Error:  could not remove messages from folder:  ") + targetFolder.toString() +"\n", me);
	}
    }

    /**
     * A convenience method which sets autoExpunge by the value of 
     * Pooka.autoExpunge, and then calls moveMessage(targetFolder, autoExpunge)
     * with that value.
     */
    public void moveMessage(FolderInfo targetFolder) throws MessagingException {
	moveMessage(targetFolder, Pooka.getProperty("Pooka.autoExpunge", "true").equals("true"));
    }

    /**
     * Deletes the Message from the current Folder.  If a Trash folder is
     * set, this method moves the message into the Trash folder.  If no
     * Trash folder is set, this marks the message as deleted.  In addition,
     * if the autoExpunge variable is set to true, it also expunges
     * the message from the mailbox.
     */
    public void deleteMessage(boolean autoExpunge) throws MessagingException {
	FolderInfo trashFolder = getFolderInfo().getTrashFolder();
	if ((getFolderInfo().useTrashFolder()) && (trashFolder != null) && (trashFolder != getFolderInfo())) {
	    try {
		moveMessage(trashFolder, autoExpunge);
	    } catch (MessagingException me) {
		throw new MessagingException(Pooka.getProperty("error.Messsage.DeleteNoTrashFolder", "No trash folder available."),  me);
	    }
	} else {
	    
	    // actually remove the message, if we haven't already moved it.
	    
	    try {
		remove(autoExpunge);
	    } catch (MessagingException me) {
		throw new MessagingException(Pooka.getProperty("error.Message.DeleteErrorMessage", "Error:  could not delete message.") +"\n", me);
	    }   
	}

	if (getMessageProxy() != null)
	    getMessageProxy().close();
    }

    /**
     * A convenience method which sets autoExpunge by the value of 
     * Pooka.autoExpunge, and then calls deleteMessage(boolean autoExpunge)
     * with that value.
     */
    public void deleteMessage() throws MessagingException {
	deleteMessage(Pooka.getProperty("Pooka.autoExpunge", "true").equals("true"));
    }

    /**
     * This actually marks the message as deleted, and, if autoexpunge is
     * set to true, expunges the folder.
     *
     * This should not be called directly; rather, deleteMessage() should
     * be used in order to ensure that the delete is done properly (using
     * trash folders, for instance).  If, however, the deleteMessage() 
     * throws an Exception, it may be necessary to follow up with a call
     * to remove().
     */
    public void remove(boolean autoExpunge) throws MessagingException {
	Message m = getRealMessage();
	m.setFlag(Flags.Flag.DELETED, true);
	if ( autoExpunge )
	    folderInfo.getFolder().expunge();
    }
    
    /**
     * This puts the reply prefix 'prefix' in front of each line in the
     * body of the Message.
     */
    public String prefixMessage(String originalMessage, String prefix, String intro) {
	StringBuffer newValue = new StringBuffer(originalMessage);
	
	int currentCR = originalMessage.lastIndexOf('\n', originalMessage.length());
	while (currentCR != -1) {
	    newValue.insert(currentCR+1, prefix);
	    currentCR=originalMessage.lastIndexOf('\n', currentCR-1);
	}
	newValue.insert(0, prefix);
	newValue.insert(0, intro);
	
	return newValue.toString();
    }

    /**
     * This parses a message line using the current Message as a model.
     * The introTemplate will be of the form 'On %d, %n wrote', or 
     * something similar.  This method uses the Pooka.parsedString
     * characters to decide which strings to substitute for which
     * characters.
     */
    public String parseMsgString(MimeMessage m, String introTemplate, boolean addLF) {
	StringBuffer intro = new StringBuffer(introTemplate);
	int index = introTemplate.lastIndexOf('%', introTemplate.length());
	try {
	    while (index > -1) {
		try {
		    char nextChar = introTemplate.charAt(index + 1);
		    if (nextChar == Pooka.getProperty("Pooka.parsedString.nameChar", "n").charAt(0)) {

			Address[] fromAddresses = m.getFrom();
			if (fromAddresses.length > 0 && fromAddresses[0] != null)
			    intro.replace(index, index +2, fromAddresses[0].toString());
		    } else if (nextChar == Pooka.getProperty("Pooka.parsedString.dateChar", "d").charAt(0)) {
			intro.replace(index, index + 2, Pooka.getDateFormatter().fullDateFormat.format(m.getSentDate()));
		    } else if (nextChar == Pooka.getProperty("Pooka.parsedString.subjChar", "s").charAt(0)) {
			intro.replace(index, index + 2, m.getSubject());
		    } else if (nextChar == '%') {
			intro.replace(index, index+1, "%");
		    }
		    index = introTemplate.lastIndexOf('%', index -1);
		} catch (StringIndexOutOfBoundsException e) {
		    index = introTemplate.lastIndexOf('%', index -1);
		}
	    }
	} catch (MessagingException me) {
	    return null;
	}

	if (addLF)
	    if (intro.charAt(intro.length()-1) != '\n')
		intro.append('\n');

	return intro.toString();
    }
    
    /**
     * This populates a message which is a reply to the current
     * message.
     */
    public Message populateReply(boolean replyAll) 
	throws MessagingException {
	MimeMessage newMsg = (MimeMessage) getRealMessage().reply(replyAll);

	MimeMessage mMsg = (MimeMessage) getMessage();

	String textPart = getTextPart(false, false, getMaxMessageDisplayLength(), getTruncationMessage());
	UserProfile up = getDefaultProfile();

	String parsedText;
	String replyPrefix;
	String parsedIntro;

	if (up != null && up.getMailProperties() != null) {
	    
	    replyPrefix = up.getMailProperties().getProperty("replyPrefix", Pooka.getProperty("Pooka.replyPrefix", "> "));
	    parsedIntro = parseMsgString(mMsg, up.getMailProperties().getProperty("replyIntro", Pooka.getProperty("Pooka.replyIntro", "On %d, %n wrote:")), true);
	} else { 
	    replyPrefix = Pooka.getProperty("Pooka.replyPrefix", "> ");
	    parsedIntro = parseMsgString(mMsg, Pooka.getProperty("Pooka.replyIntro", "On %d, %n wrote:"), true);
	}
	parsedText = prefixMessage(textPart, replyPrefix, parsedIntro);
	newMsg.setText(parsedText);
	
	if (replyAll && Pooka.getProperty("Pooka.excludeSelfInReply", "true").equalsIgnoreCase("true")) {
	    getDefaultProfile().removeFromAddress(newMsg); 
	}

	return newMsg;
    }

    /**
     * This populates a new message which is a forwarding of the
     * current message.
     */
    public Message populateForward() 
	throws MessagingException {
	MimeMessage mMsg = (MimeMessage) getMessage();
	MimeMessage newMsg = new MimeMessage(Pooka.getDefaultSession());

	String textPart = getTextPart(false, false, getMaxMessageDisplayLength(), getTruncationMessage());
	UserProfile up = getDefaultProfile();

	String parsedText = null;
	String forwardPrefix;
	String parsedIntro;
	String forwardStyle = Pooka.getProperty("Pooka.forwardStle", "prefixed");

	if (up != null && up.getMailProperties() != null) {
	    if (forwardStyle.equals("prefixed")) {
		forwardPrefix = up.getMailProperties().getProperty("forwardPrefix", Pooka.getProperty("Pooka.forwardPrefix", "> "));
		parsedIntro = parseMsgString(mMsg, up.getMailProperties().getProperty("forwardIntro", Pooka.getProperty("Pooka.forwardIntro", "Forwarded message from %n:")), true);
	    } else { 
		forwardPrefix = Pooka.getProperty("Pooka.forwardPrefix", "> ");
		parsedIntro = parseMsgString(mMsg, Pooka.getProperty("Pooka.forwardIntro", "Forwarded message from %n:"), true);
	    }
	    parsedText = prefixMessage(textPart, forwardPrefix, parsedIntro);
	}

	    newMsg.setText(parsedText);
	    newMsg.setSubject(parseMsgString(mMsg, Pooka.getProperty("Pooka.forwardSubject", "Fwd:  %s"), false));

	    return newMsg;
    }

    /**
     * As specified by interface net.suberic.pooka.UserProfileContainer.
     *
     * If the MessageProxy's folderInfo is set, this returns the 
     * DefaultProfile of that folderInfo.  If the folderInfo isn't set
     * (should that happen?), this returns null.
     */

    public UserProfile getDefaultProfile() {
	if (getFolderInfo() != null) {
	    return getFolderInfo().getDefaultProfile();
	} else 
	    return null;
    }

    /**
     * Returns the Message that this MessageInfo is wrapping.
     */
    public Message getMessage() {
	return message;
    }

    /**
     * Returns the real, modifiable message that this MessageInfo is
     * wrapping.
     */
    public Message getRealMessage() {
	return message;
    }

    
    /**
     * Refreshes the message using the underlying UID.
     */
    /*
    public Message refreshMessage() {
	Folder sourceFolder = getFolderInfo().getFolder();
	if (sourceFolder != null && sourceFolder instanceof UIDFolder) {
	    UIDFolder uidFolder = (UIDFolder)sourceFolder;
	    try {
		if (uidFolder.getUIDValidity() == uidValidity) {
		    message = uidFolder.getMessageByUID(uid);
		    return message;
		}
	    } catch (MessagingException me) {

	    }
	}
	return null;
    }
    */

    public FolderInfo getFolderInfo() {
	return folderInfo;
    }

    public boolean isSeen() {
	return seen;
    }

    /**
     * Sets the seen parameter to the newValue.  This basically calls
     * setFlag(Flags.Flag.SEEN, newValue) on the wrapped Message.
     */
    public void setSeen(boolean newValue) throws MessagingException {
	if (newValue != seen) {
	    seen=newValue;
	    Message m = getRealMessage();
	    m.setFlag(Flags.Flag.SEEN, newValue);
	    getFolderInfo().fireMessageChangedEvent(new MessageChangedEvent(this, MessageChangedEvent.FLAGS_CHANGED, getMessage()));
	}
    }

    public boolean isLoaded() {
	return loaded;
    }

    /**
     * This sets the loaded value for the MessageProxy to false.   This 
     * should be called only if the TableInfo of the Message has been 
     * changed and needs to be reloaded.
     */
    public void unloadTableInfo() {
	loaded=false;
    }

    public boolean hasLoadedAttachments() {
	return attachmentsLoaded;
    }

    public boolean hasAttachments() throws MessagingException {
	if (!hasLoadedAttachments())
	    loadAttachmentInfo();

	if (getAttachments() != null && getAttachments().size() > 0)
	    return true;
	else
	    return false;
    }

    /**
     * Returns the attachments for this MessageInfo.  If the attachments
     * have not yet been loaded, attempts to load the attachments.
     */
    public Vector getAttachments() throws MessagingException {
	if (hasLoadedAttachments())
	    return attachments.getAttachments(getMaxMessageDisplayLength());
	else {
	    loadAttachmentInfo();
	    return attachments.getAttachments(getMaxMessageDisplayLength());
	}
	    
    }

    public MessageProxy getMessageProxy() {
	return messageProxy;
    }

    public void setMessageProxy(MessageProxy newMp) {
	messageProxy = newMp;
    }

    public int getMaxMessageDisplayLength() {
	int displayLength = 10000;
	try {
	    displayLength = Integer.parseInt(Pooka.getProperty("Pooka.attachmentDisplayMaxLength", "100000"));
	} catch (NumberFormatException nfe) {
	}
	return displayLength;
    }

    public String getTruncationMessage() {
	return Pooka.getProperty("Pooka.messageTruncation", "------ Message truncated ------");
    }

    /**
     * Returns whether or not this message has an HTML version available.
     */
    public boolean containsHtml() throws MessagingException {
	if (!hasLoadedAttachments())
	    loadAttachmentInfo();

	return attachments.containsHtml();
    }

    /**
     * Returns true if the main content of this message exists only as
     * HTML.
     */
    public boolean isHtml() throws MessagingException {
	if (!hasLoadedAttachments())
	    loadAttachmentInfo();

	return attachments.isHtml();
    }

    /**
     * Gets the Html part of the Content of this Message.
     */
    public String getHtmlPart(boolean withHeaders, boolean showFullHeaders, int maxLength, String truncationMessage) throws MessagingException {
	try {
	    if (!hasLoadedAttachments()) 
		loadAttachmentInfo();
	    return attachments.getHtmlPart(withHeaders, showFullHeaders, maxLength, truncationMessage);
	} catch (FolderClosedException fce) {
	    try {
		getFolderInfo().openFolder(Folder.READ_WRITE);
		loadAttachmentInfo();
		return attachments.getHtmlPart(withHeaders, showFullHeaders, maxLength, truncationMessage);
	    } catch (java.io.IOException ioe) {
		throw new MessagingException(ioe.getMessage()); 
	    }
	} catch (java.io.IOException ioe) {
	    throw new MessagingException(ioe.getMessage()); 
	}
    }

    /**
     * Gets the Html part of the Content of this Message.
     */
    public String getHtmlPart(boolean withHeaders, boolean showFullHeaders) throws MessagingException {
	return getHtmlPart(withHeaders, showFullHeaders, getMaxMessageDisplayLength(), getTruncationMessage());
    }

    /**
     * Gets the Content and inline text content for the Message.
     */
    public String getHtmlAndTextInlines(String attachmentSeparator, boolean withHeaders, boolean showFullHeaders, int maxLength, String truncationMessage) throws MessagingException {
	try {
	    if (!hasLoadedAttachments()) 
		loadAttachmentInfo();
	    return attachments.getHtmlAndTextInlines(attachmentSeparator, withHeaders, showFullHeaders, maxLength, truncationMessage);
	} catch (FolderClosedException fce) {
	    try {
		getFolderInfo().openFolder(Folder.READ_WRITE);
		loadAttachmentInfo();
		return attachments.getHtmlAndTextInlines(attachmentSeparator, withHeaders, showFullHeaders, maxLength, truncationMessage);
	    } catch (java.io.IOException ioe) {
		throw new MessagingException(ioe.getMessage()); 
	    }
	} catch (java.io.IOException ioe) {
	    throw new MessagingException(ioe.getMessage()); 
	}
    }

    /**
     * Gets the Content and inline text content for the Message.
     */
    public String getHtmlAndTextInlines(String attachmentSeparator, boolean withHeaders, boolean showFullHeaders) throws MessagingException {
	return getHtmlAndTextInlines(attachmentSeparator, withHeaders, showFullHeaders, getMaxMessageDisplayLength(), getTruncationMessage());
    }

}







