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
import java.io.*;

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

    public static int FORWARD_AS_ATTACHMENT = 0;
    public static int FORWARD_QUOTED = 1;
    public static int FORWARD_AS_INLINE = 2;

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
	    return MailUtilities.getAddressString(fromAddr);
	} else if (prop.equalsIgnoreCase("receivedDate")) {
	    return msg.getReceivedDate();
	} else if (prop.equalsIgnoreCase("recipients")) {
	    return msg.getAllRecipients();
	} else if (prop.equalsIgnoreCase("to")) {
	    return MailUtilities.getAddressString(msg.getRecipients(Message.RecipientType.TO));
	} else if (prop.equalsIgnoreCase("cc")) {
	    return MailUtilities.getAddressString(msg.getRecipients(Message.RecipientType.CC));
	} else if (prop.equalsIgnoreCase("bcc")) {
	    return MailUtilities.getAddressString(msg.getRecipients(Message.RecipientType.BCC));
	} else if (prop.equalsIgnoreCase("Date")) {
	    return msg.getSentDate();
	} else if (prop.equalsIgnoreCase("Subject")) {
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
		if (getFolderInfo().shouldBeConnected()) {
		    getFolderInfo().openFolder(Folder.READ_WRITE);
		    loadAttachmentInfo();
		    return attachments.getTextAndTextInlines(attachmentSeparator, withHeaders, showFullHeaders, maxLength, truncationMessage);
		} else {
		    throw fce;
		}
	    } catch (java.io.IOException ioe) {
		throw new MessagingException(ioe.getMessage()); 
	    }
	} catch (java.io.IOException ioe) {
	    ioe.printStackTrace();
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
     * Gets the Content and inline text content for the Message.
     */
    public String getTextAndTextInlines(boolean withHeaders, boolean showFullHeaders) throws MessagingException {
	return getTextAndTextInlines(getAttachmentSeparator(), withHeaders, showFullHeaders, getMaxMessageDisplayLength(), getTruncationMessage());
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
		if (getFolderInfo().shouldBeConnected()) {
		    getFolderInfo().openFolder(Folder.READ_WRITE);
		    loadAttachmentInfo();
		    return attachments.getTextPart(withHeaders, showFullHeaders, maxLength, truncationMessage);
		} else {
		    throw fce;
		}
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
     * Gets the Html part of the Content of this Message.
     */
    public String getHtmlPart(boolean withHeaders, boolean showFullHeaders, int maxLength, String truncationMessage) throws MessagingException {
	try {
	    if (!hasLoadedAttachments()) 
		loadAttachmentInfo();
	    return attachments.getHtmlPart(withHeaders, showFullHeaders, maxLength, truncationMessage);
	} catch (FolderClosedException fce) {
	    try {
		if (getFolderInfo().shouldBeConnected()) {
		    getFolderInfo().openFolder(Folder.READ_WRITE);
		    loadAttachmentInfo();
		    return attachments.getHtmlPart(withHeaders, showFullHeaders, maxLength, truncationMessage);
		} else {
		    throw fce;
		}
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
		if (getFolderInfo().shouldBeConnected()) {
		    getFolderInfo().openFolder(Folder.READ_WRITE);
		    loadAttachmentInfo();
		    return attachments.getHtmlAndTextInlines(attachmentSeparator, withHeaders, showFullHeaders, maxLength, truncationMessage);
		} else {
		    throw fce;
		}
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
	return getHtmlAndTextInlines(attachmentSeparator, withHeaders, showFullHeaders, getMaxMessageDisplayLength(), getHtmlTruncationMessage());
    }

    /**
     * Gets the Content and inline text content for the Message.
     */
    public String getHtmlAndTextInlines(boolean withHeaders, boolean showFullHeaders) throws MessagingException {
	return getHtmlAndTextInlines(getHtmlAttachmentSeparator(), withHeaders, showFullHeaders, getMaxMessageDisplayLength(), getHtmlTruncationMessage());
    }

  /**
   * Moves the Message into the target Folder.
   */
  public void moveMessage(FolderInfo targetFolder, boolean expunge) throws MessagingException {
    try {
      folderInfo.copyMessages(new MessageInfo[] { this }, targetFolder);
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
   * Copies the Message into the target Folder.
   */
  public void copyMessage(FolderInfo targetFolder) throws MessagingException {
    try {
      folderInfo.copyMessages(new MessageInfo[] { this }, targetFolder);
    } catch (MessagingException me) {
      throw new MessagingException (Pooka.getProperty("error.Message.CopyErrorMessage", "Error:  could not copy messages to folder:  ") + targetFolder.toString() +"\n", me);
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
	    folderInfo.expunge();
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
			intro.replace(index, index +2, MailUtilities.getAddressString(fromAddresses));
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
    public NewMessageInfo populateReply(boolean replyAll, boolean withAttachments) 
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

	NewMessageInfo returnValue = new NewMessageInfo(newMsg);

	if (withAttachments) {
	    returnValue.attachments = new AttachmentBundle();
	    returnValue.attachments.addAll(attachments);
	    returnValue.attachmentsLoaded=true;
	}

	return returnValue;
    }

    /**
     * This populates a message which is a reply to the current
     * message.
     */
    public NewMessageInfo populateReply(boolean replyAll)
	throws MessagingException {
	return populateReply(replyAll, false);
    }

    /**
     * This populates a new message which is a forwarding of the
     * current message.
     */
    public NewMessageInfo populateForward(boolean withAttachments, int method) 
	throws MessagingException {
	MimeMessage mMsg = (MimeMessage) getMessage();
	MimeMessage newMsg = new MimeMessage(Pooka.getDefaultSession());

	String parsedText = "";

	if (method == FORWARD_QUOTED) {
	    String textPart = getTextPart(false, false, getMaxMessageDisplayLength(), getTruncationMessage());
	    
	    UserProfile up = getDefaultProfile();
	    
	    String forwardPrefix;
	    String parsedIntro;
	
	    if (up != null && up.getMailProperties() != null) {
		forwardPrefix = up.getMailProperties().getProperty("forwardPrefix", Pooka.getProperty("Pooka.forwardPrefix", "> "));
		parsedIntro = parseMsgString(mMsg, up.getMailProperties().getProperty("forwardIntro", Pooka.getProperty("Pooka.forwardIntro", "Forwarded message from %n:")), true);
	    } else { 
		forwardPrefix = Pooka.getProperty("Pooka.forwardPrefix", "> ");
		parsedIntro = parseMsgString(mMsg, Pooka.getProperty("Pooka.forwardIntro", "Forwarded message from %n:"), true);
	    }
	    parsedText = prefixMessage(textPart, forwardPrefix, parsedIntro);

	} else if (method == FORWARD_AS_INLINE) {

	    String textPart = getTextPart(true, false, getMaxMessageDisplayLength(), getTruncationMessage());
	    
	    parsedText = Pooka.getProperty("Pooka.forwardInlineIntro", "----------  Original Message  ----------\n") + textPart;
	    
	}

	newMsg.setText(parsedText);
	newMsg.setSubject(parseMsgString(mMsg, Pooka.getProperty("Pooka.forwardSubject", "Fwd:  %s"), false));
	
	NewMessageInfo returnValue = new NewMessageInfo(newMsg);

	// handle attachments.
	if (method == FORWARD_AS_ATTACHMENT) {

	    javax.mail.internet.MimeBodyPart mbp = new javax.mail.internet.MimeBodyPart();
	    mbp.setDataHandler(getRealMessage().getDataHandler());
	    returnValue.addAttachment(new MBPAttachment(mbp));
	    returnValue.attachmentsLoaded=true;

	    /*
	    try {
		final java.io.PipedOutputStream pos = new java.io.PipedOutputStream();
		final java.io.PipedInputStream pis = new java.io.PipedInputStream(pos);

		Thread t = new Thread(new Runnable() {
			public void run() {
			    try {
				getRealMessage().writeTo(pos);
				pos.flush();
				pos.close();
			    } catch (java.io.IOException ioe) {
			    } catch (MessagingException me) {
			    } 
			}
		    });
		t.start();
		returnValue.addAttachment(new MBPAttachment(new javax.mail.internet.MimeBodyPart(pis)));
	    } catch (java.io.IOException ioe) {
		MessagingException me = new MessagingException(Pooka.getProperty("error.errorCreatingAttachment", "Error attaching message"));
		me.setNextException(ioe);
		throw me;
	    }
	    */
	    returnValue.attachmentsLoaded=true;
	} else if (withAttachments) {
	    returnValue.attachments = new AttachmentBundle();
	    Vector fromAttachments = attachments.getAttachments();
	    if (fromAttachments != null) {
		for (int i = 0; i < fromAttachments.size(); i++) {
		    Attachment current = (Attachment) fromAttachments.elementAt(i);
		    Attachment newAttachment = null;
		    //try {
			javax.mail.internet.MimeBodyPart mbp = new javax.mail.internet.MimeBodyPart();
			mbp.setDataHandler(current.getDataHandler());
			newAttachment = new MBPAttachment(mbp);
			/* } catch (java.io.IOException ioe) {
			MessagingException me = new MessagingException(Pooka.getProperty("error.errorCreatingAttachment", "Error attaching message"));
			me.setNextException(ioe);
			throw me;
			}*/
		    returnValue.addAttachment(newAttachment);
		}
		returnValue.attachmentsLoaded=true;
	    }
	}

	return returnValue;
    }

    /**
     * This populates a new message which is a forwarding of the
     * current message.
     */
    public NewMessageInfo populateForward() 
	throws MessagingException {
	return populateForward(false, FORWARD_QUOTED);
    }

    /**
     *  Caches the current messages.
     */
    public void cacheMessage() throws MessagingException {
	FolderInfo fi = getFolderInfo();
	if (fi != null && fi instanceof net.suberic.pooka.cache.CachingFolderInfo) {
	    ((net.suberic.pooka.cache.CachingFolderInfo) fi).cacheMessage(this, net.suberic.pooka.cache.MessageCache.MESSAGE);
	    
	}
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
     * Saves the message to the given filename.
     */
    public void saveMessageAs(File saveFile) throws MessagingException{
	try {
	    FileOutputStream fos = new FileOutputStream(saveFile);
	    ((MimeMessage)getRealMessage()).writeTo(fos);
	    //MimeMessage tmpMM = new MimeMessage((MimeMessage)getRealMessage());
	    //tmpMM.writeTo(fos);
	} catch (IOException ioe) {
	    MessagingException me = new MessagingException(Pooka.getProperty("error.errorCreatingAttachment", "Error attaching message"));
	    me.setNextException(ioe);
	    throw me;

	}
    }

  /**
   * Adds the sender of the message to the current AddressBook, if any.
   */
  public void addAddress(AddressBook book, boolean useVcard) throws MessagingException {
    boolean found = false;
    if (useVcard) {
      Attachment vcard = null;

      // see if there's a Vcard attachment on here.
      Vector attachList = getAttachments();
      if (attachList != null) {
	for (int i = 0; i < attachList.size() && vcard==null; i++) {
	  Attachment current = (Attachment)attachList.get(i);
	  if (current.getMimeType().match("text/x-vcard")) {
	    vcard = current;
	  }
	}

	if (vcard != null) {
	  try {
	    String vcardText = (String) vcard.getContent();
	    BufferedReader reader = new BufferedReader(new StringReader(vcardText));
	    net.suberic.pooka.vcard.Vcard addressEntry = net.suberic.pooka.vcard.Vcard.parse(reader);
	    book.addAddress(addressEntry);
	    found = true;
	  } catch (Exception e) {
	    // if we get any exceptions parsing the Vcard, just fall back to
	    // using the fromAddress.  do print out a debugging message,
	    // though.
	    getMessageProxy().showError(Pooka.getProperty("error.parsingVcard", "Error parsing Vcard"), e);
	  }
	}
      }
    }

    if (!found) {
      Address[] fromAddresses = getMessage().getFrom();
      javax.mail.internet.InternetAddress addr = (javax.mail.internet.InternetAddress) fromAddresses[0];

      // let's not support multiple froms.
      AddressBookEntry entry = new net.suberic.pooka.vcard.Vcard(new java.util.Properties());
      entry.setPersonalName(addr.getPersonal());
      entry.setAddress(addr);
      book.addAddress(entry);
    }
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

    public String getHtmlTruncationMessage() {
	return Pooka.getProperty("Pooka.html.messageTruncation", "<br><br><b>------ Message truncated ------</b><br><br>");
    }

    public String getAttachmentSeparator() {
	return Pooka.getProperty("Pooka.attachmentSeparator", "\n\n");
    }

    public String getHtmlAttachmentSeparator() {
	return Pooka.getProperty("Pooka.html.attachmentSeparator", "<br><hr><br>");
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

}







