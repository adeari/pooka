package net.suberic.pooka.gui;
import javax.mail.*;
import javax.mail.internet.MimeMessage;
import javax.mail.event.*;
import javax.swing.*;
import java.util.Hashtable;
import java.util.Vector;
import net.suberic.pooka.*;
import java.awt.event.ActionEvent;

public class MessageProxy {
    // the underlying message
    Message message;

    // the source FolderInfo
    FolderInfo folderInfo;

    // the information for the FolderTable
    Vector tableInfo;
    
    // the column Headers for the FolderInfo Vector; used for loading the
    // folderInfo.
    Vector columnHeaders;

    // if the message has been read
    boolean seen = false;

    // if the tableInfo has been loaded yet.
    boolean loaded = false;

    // if the attachments have been loaded yet.
    boolean attachmentsLoaded = false;

    // commands for the GUI
    Hashtable commands;

    // The Window associated with this MessageProxy.
    MessageWindow msgWindow;

    // The attachments
    Vector attachments;

    protected MessageProxy() {
    }

    /**
     * This creates a new MessageProxy from a set of Column Headers (for 
     * the tableInfo), a Message, and a link to a FolderInfo object.
     */
    public MessageProxy(Vector newColumnHeaders, Message newMessage, FolderInfo newFolderInfo) {
	folderInfo = newFolderInfo;
	message=newMessage;
	columnHeaders = newColumnHeaders;

	commands = new Hashtable();
	
        Action[] actions = getActions();
        if (actions != null) {
            for (int i = 0; i < actions.length; i++) {
                Action a = actions[i];
                commands.put(a.getValue(Action.NAME), a);
            }
        }
	
    }

    /**
     * This loads the tableInfo (the fields that will be displayed in the
     * FolderTable) using the columnHeaders property to know which fields
     * to load.
     */
    public synchronized void loadTableInfo() {
	if (!loaded) {
	    int columnCount = columnHeaders.size();
	    
	    tableInfo = new Vector();
	    
	    for(int j=0; j < columnCount; j++) {
		tableInfo.addElement(getMessageProperty((String)(columnHeaders.elementAt(j)), message));
	    }
	    
	    try {
		seen=message.isSet(Flags.Flag.SEEN);
	    } catch (MessagingException me) {
		System.out.println("Error:  " + me.getMessage());
	    }
	    
	    loaded=true;
	}
    }	

    /**
     * This loads the Attachment information into the attachments vector.
     */

    public void loadAttachmentInfo() {
	attachments = MailUtilities.getAttachments(getMessage());
    }

    /**
     * This gets a particular property (From, To, Date, Subject, or just
     * about any Email Header) from the Message.
     */
    public String getMessageProperty(String prop, Message msg) {
	if (Pooka.isDebug())
	    System.out.println("calling getMessageProperty with string " + prop);
	try {
	    if (prop.equals("From")) {
		Address[] fromAddr = msg.getFrom();
		if (fromAddr != null && fromAddr[0] != null) 
		    return ((javax.mail.internet.InternetAddress)fromAddr[0]).toString();
		else 
		    return null;
	    } else if (prop.equals("receivedDate")) {
		return msg.getReceivedDate().toString();
	    } else if (prop.equals("recipients")) {
		return msg.getRecipients(Message.RecipientType.TO).toString();
	    } else if (prop.equals("sentDate")) {
		return msg.getSentDate().toString();
	    } else if (prop.equals("Subject")) {
		return msg.getSubject();
	    } 
	    
	} catch (MessagingException me) {
	    System.out.println("MessagingException:  " + me.getMessage());

	    return "";
	}
	
	if (msg instanceof MimeMessage) {
	    try {
		String hdrVal = ((MimeMessage)msg).getHeader(prop, ",");
		if (hdrVal != null && hdrVal.length() > 0)
		    return hdrVal;
	    } catch (MessagingException me) {
	    }
	}
	return "";
	
    }

    /**
     * this opens a MessageWindow for this Message.
     */
    public void openWindow() {
	folderInfo.getFolderWindow().getMessagePanel().openMessageWindow(this);
	this.setSeen(true);
    }

    /**
     * Moves the Message into the target Folder.
     */
    public void moveMessage(Folder targetFolder) {
	try {
	    if (!targetFolder.isOpen()) {
		targetFolder.open(Folder.READ_WRITE);
	    } else if (! (targetFolder.getMode() != Folder.READ_WRITE)) {
		targetFolder.close(false);
		targetFolder.open(Folder.READ_WRITE);
	    }

	    folderInfo.getFolder().copyMessages(new Message[] {message}, targetFolder);
	    message.setFlag(Flags.Flag.DELETED, true);
	    if ( Pooka.getProperty("Pooka.autoExpunge", "true").equals("true") )
		folderInfo.getFolder().expunge();
	} catch (MessagingException me) {
	    if (folderInfo != null && folderInfo.getFolderWindow() != null)
		JOptionPane.showInternalMessageDialog(folderInfo.getFolderWindow().getDesktopPane(), Pooka.getProperty("error.Message.CopyErrorMessage", "Error:  could not copy messages to folder:  ") + targetFolder.toString() +"\n" + me.getMessage());
	}
    }

    /**
     * Deletes the Message from the current Folder.  If a Trash folder is
     * set, this method moves the message into the Trash folder.  If no
     * Trash folder is set, this marks the message as deleted.  In addition,
     * if the Pooka.autoExpunge property is set to true, it also expunges
     * the message from the mailbox.
     */
    public void deleteMessage() {
	Folder trashFolder = getMessagePanel().getMainPanel().getFolderPanel().getTrashFolder();
	if ((trashFolder != null) && (trashFolder != message.getFolder()))
	    moveMessage(trashFolder);
	else {
	    try {
		message.setFlag(Flags.Flag.DELETED, true);
		if ( Pooka.getProperty("Pooka.autoExpunge", "true").equals("true") )
		    folderInfo.getFolder().expunge();
	    } catch (MessagingException me) {
		if (folderInfo != null && folderInfo.getFolderWindow() != null)
		    JOptionPane.showInternalMessageDialog(folderInfo.getFolderWindow().getDesktopPane(), Pooka.getProperty("error.Message.DeleteErrorMessage", "Error:  could not delete message.") +"\n" + me.getMessage());
	    }   
	}
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
     * This creates the intro to a quoted message, i.e. 'On Jan 1, 2000,
     * allen@suberic.net wrote...'
     */
    public String createIntro(MimeMessage m, String introTemplate) {
	StringBuffer intro = new StringBuffer(introTemplate);
	int index = introTemplate.lastIndexOf('%', introTemplate.length());
	try {
	    while (index > -1) {
		try {
		    char nextChar = introTemplate.charAt(index + 1);
		    if (nextChar == Pooka.getProperty("Pooka.replyIntro.nameChar", "n").charAt(0)) {

			Address[] fromAddresses = m.getFrom();
			if (fromAddresses.length > 0 && fromAddresses[0] != null)
			    intro.replace(index, index +2, fromAddresses[0].toString());
		    } else if (nextChar == Pooka.getProperty("Pooka.replyIntro.dateChar", "d").charAt(0)) {
			intro.replace(index, index + 2, Pooka.getDateFormatter().format(m.getSentDate()));
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

	if (intro.charAt(intro.length()-1) != '\n')
	    intro.append('\n');

	return intro.toString();
    }
    
    /**
     * This populates a message which is a Reply to another Message.
     */
    protected void populateReply(MimeMessage mMsg) 
	throws MessagingException {
	String textPart = MailUtilities.getTextPart(message);
	UserProfile up = UserProfile.getDefaultProfile(this);

	String parsedText;
	String replyPrefix;
	String parsedIntro;

	if (up != null && up.getMailProperties() != null) {
	    
	    replyPrefix = up.getMailProperties().getProperty("replyPrefix", Pooka.getProperty("Pooka.replyPrefix", "> "));
	    parsedIntro = createIntro((MimeMessage)getMessage(), up.getMailProperties().getProperty("replyIntro", Pooka.getProperty("Pooka.replyIntro", "On %d, %n wrote:")));
	} else { 
	    replyPrefix = Pooka.getProperty("Pooka.replyPrefix", "> ");
	    parsedIntro = createIntro((MimeMessage)getMessage(), Pooka.getProperty("Pooka.replyIntro", "On %d, %n wrote:"));
	}
	parsedText = prefixMessage(textPart, replyPrefix, parsedIntro);
	mMsg.setText(parsedText);
	
    }
        
    public Message getMessage() {
	return message;
    }

    public Vector getTableInfo() {
	if (isLoaded()) {
	    return tableInfo;
	} else {
	    loadTableInfo();
	    return tableInfo;
	}
    }

    public FolderInfo getFolderInfo() {
	return folderInfo;
    }

    public void setTableInfo(Vector newValue) {
	tableInfo=newValue;
    }

    public boolean isSeen() {
	return seen;
    }

    public void setSeen(boolean newValue) {
	if (newValue != seen) {
	    seen=newValue;
	    try {
		message.setFlag(Flags.Flag.SEEN, newValue);
		getFolderInfo().fireMessageChangedEvent(new MessageChangedEvent(this, MessageChangedEvent.FLAGS_CHANGED, getMessage()));
	    } catch (MessagingException me) {
		JOptionPane.showInternalMessageDialog(getMessagePanel(), Pooka.getProperty("error.MessageWindow.setSeenFailed", "Failed to set Seen flag to ") + newValue + "\n" + me.getMessage());
	    }
	}
    }

    public MessagePanel getMessagePanel() {
	if (folderInfo != null && folderInfo.getFolderWindow() != null)
	    return folderInfo.getFolderWindow().getMessagePanel();
	else
	    return null;
    }

    public boolean isLoaded() {
	return loaded;
    }

    public boolean hasLoadedAttachments() {
	return attachmentsLoaded;
    }

    public MessageWindow getMessageWindow() {
	return msgWindow;
    }

    public void setMessageWindow(MessageWindow newValue) {
	msgWindow = newValue;
    }

    public Vector getAttachments() {
	return attachments;
    }


    public Action getAction(String name) {
	return (Action)commands.get(name);
    }

    public Action[] getActions() {
	return defaultActions;
    }

    public Action[] defaultActions = {
	new OpenAction(),
	new MoveAction(),
	new ReplyAction(),
	new ReplyAllAction(),
	new DeleteAction()
    };

    public class OpenAction extends AbstractAction {
	OpenAction() {
	    super("message-open");
	}

	public void actionPerformed(java.awt.event.ActionEvent e) {
	    openWindow();
	}
    }

    public class MoveAction extends net.suberic.util.DynamicAbstractAction {
	MoveAction() {
	    super("message-move");
	}

	public void actionPerformed(java.awt.event.ActionEvent e) {
	    moveMessage((Folder)getValue("target"));
	}

    }

    public class ReplyAction extends AbstractAction {

	ReplyAction() {
	    super("message-reply");
	}

	public void actionPerformed(ActionEvent e) {
	    try {
		javax.mail.internet.MimeMessage m = (javax.mail.internet.MimeMessage)message.reply(false);
		populateReply(m);
		getMessagePanel().createNewMessage(m);

	    } catch (MessagingException me) {
		JOptionPane.showInternalMessageDialog(getMessagePanel(), Pooka.getProperty("error.MessageWindow.replyFailed", "Failed to create new Message.") + "\n" + me.getMessage());
	    }
	}
    }

    public class ReplyAllAction extends AbstractAction {

	ReplyAllAction() {
	    super("message-reply-all");
	}

	public void actionPerformed(ActionEvent e) {
	    try {
		javax.mail.internet.MimeMessage m = (javax.mail.internet.MimeMessage)message.reply(true);

		populateReply(m);
		getMessagePanel().createNewMessage(m);

	    } catch (MessagingException me) {
		JOptionPane.showInternalMessageDialog(getMessagePanel(), Pooka.getProperty("error.MessageWindow.replyFailed", "Failed to create new Message.") + "\n" + me.getMessage());
	    }
	}
    }

    public class DeleteAction extends AbstractAction {
	DeleteAction() {
	    super("message-delete");
	}

	public void actionPerformed(ActionEvent e) {
	    deleteMessage();
	}
    }

}







