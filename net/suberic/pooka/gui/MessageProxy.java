package net.suberic.pooka.gui;
import net.suberic.pooka.*;
import net.suberic.util.gui.ConfigurablePopupMenu;
import net.suberic.util.thread.*;
import javax.mail.*;
import javax.mail.internet.MimeMessage;
import javax.mail.event.*;
import javax.swing.*;
import java.util.Hashtable;
import java.util.Vector;
import java.awt.event.*;
import java.awt.print.*;

public class MessageProxy {
    // the underlying message
    Message message;

    // the source FolderInfo
    FolderInfo folderInfo;

    // the information for the FolderTable
    Vector tableInfo;
    
    // the column Headers for the FolderInfo Vector; used for loading the
    // tableInfo.
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

    public Action[] defaultActions;

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
	
	ActionThread storeThread = folderInfo.getParentStore().getStoreThread();
	
	defaultActions = new Action[] {
	    new ActionWrapper(new OpenAction(), storeThread),
	    new ActionWrapper(new MoveAction(), storeThread),
	    new ActionWrapper(new ReplyAction(), storeThread),
	    new ActionWrapper(new ReplyAllAction(), storeThread),
	    new ActionWrapper(new ForwardAction(), storeThread),
	    new ActionWrapper(new DeleteAction(), storeThread),
	    new ActionWrapper(new PrintAction(), storeThread)
		};
	
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
		String propertyName = (String)columnHeaders.elementAt(j);
		
		if (propertyName.startsWith("FLAG")) 
		    tableInfo.addElement(getMessageFlag((String)(columnHeaders.elementAt(j)), message));
		else if (propertyName.equals("attachments"))
		    try {
			tableInfo.addElement(new BooleanIcon(message.getContentType().substring(0, 15).equalsIgnoreCase("multipart/mixed"), Pooka.getProperty("FolderTable.Attachments.icon", "")));
		    } catch (MessagingException me) {
			tableInfo.addElement(new BooleanIcon(false, ""));
		    }
		else
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
	attachments = MailUtilities.getAttachments(getMessage(), false);
    }

    /**
     * This gets a Flag property from the Message.
     */

    public BooleanIcon getMessageFlag(String flagName, Message msg) {
	try {
	    if (flagName.equals("FLAG.ANSWERED") )
		return new BooleanIcon(msg.isSet(Flags.Flag.ANSWERED), Pooka.getProperty("FolderTable.Answered.icon", ""));
	    else if (flagName.equals("FLAG.DELETED"))
		return new BooleanIcon(msg.isSet(Flags.Flag.DELETED),Pooka.getProperty("FolderTable.Deleted.icon", ""));
	    else if (flagName.equals("FLAG.DRAFT"))
		return new BooleanIcon(msg.isSet(Flags.Flag.DRAFT), Pooka.getProperty("FolderTable.Draft.icon", ""));
	    else if (flagName.equals("FLAG.FLAGGED"))
		return new BooleanIcon(msg.isSet(Flags.Flag.FLAGGED), Pooka.getProperty("FolderTable.Flagged.icon", ""));
	    else if (flagName.equals("FLAG.RECENT"))
		return new BooleanIcon(msg.isSet(Flags.Flag.RECENT), Pooka.getProperty("FolderTable.Recent.icon", ""));
	    else if (flagName.equals("FLAG.NEW")) 
		return new MultiValueIcon(msg.isSet(Flags.Flag.SEEN), msg.isSet(Flags.Flag.RECENT), Pooka.getProperty("FolderTable.New.recentAndUnseenIcon", ""), Pooka.getProperty("FolderTable.New.justUnseenIcon", ""));
	    else if (flagName.equals("FLAG.SEEN"))
		return new BooleanIcon(msg.isSet(Flags.Flag.SEEN), Pooka.getProperty("FolderTable.Seen.icon", ""));
	    else
		return new BooleanIcon(false, "");
	} catch (MessagingException me) {
	    return new BooleanIcon(false, "");
	}
    }

    /**
     * This gets a particular property (From, To, Date, Subject, or just
     * about any Email Header) from the Message.
     */
    public Object getMessageProperty(String prop, Message msg) {
	try {
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
    public void moveMessage(FolderInfo targetFolder) {
        boolean success=false;
	try {
	    folderInfo.getFolder().copyMessages(new Message[] {message}, targetFolder.getFolder());
	    success=true;
	} catch (MessagingException me) {
	    if (folderInfo != null && folderInfo.getFolderWindow() != null)
		JOptionPane.showInternalMessageDialog(folderInfo.getFolderWindow().getDesktopPane(), Pooka.getProperty("error.Message.CopyErrorMessage", "Error:  could not copy messages to folder:  ") + targetFolder.toString() +"\n" + me.getMessage());
	    if (Pooka.isDebug())
		me.printStackTrace();
	}

	if (success == true) 
	    try {
		message.setFlag(Flags.Flag.DELETED, true);
		
		if ( Pooka.getProperty("Pooka.autoExpunge", "true").equals("true") )
		    folderInfo.getFolder().expunge();
	    } catch (MessagingException me) {
		if (folderInfo != null && folderInfo.getFolderWindow() != null)
		    JOptionPane.showInternalMessageDialog(folderInfo.getFolderWindow().getDesktopPane(), Pooka.getProperty("error.Message.RemoveErrorMessage", "Error:  could not remove messages from folder:  ") + targetFolder.toString() +"\n" + me.getMessage());
		if (Pooka.isDebug())
		    me.printStackTrace();
	    }		
    }

    /**
     * Deletes the Message from the current Folder.  If a Trash folder is
     * set, this method moves the message into the Trash folder.  If no
     * Trash folder is set, this marks the message as deleted.  In addition,
     * if the autoExpunge variable is set to true, it also expunges
     * the message from the mailbox.
     */
    public void deleteMessage(boolean autoExpunge) {

	FolderInfo trashFolder = getMessagePanel().getMainPanel().getFolderPanel().getTrashFolder();
	if ((trashFolder != null) && (trashFolder != getFolderInfo()))
	    moveMessage(trashFolder);
	else {
	    try {
		message.setFlag(Flags.Flag.DELETED, true);
		if ( autoExpunge )
		    folderInfo.getFolder().expunge();
	    } catch (MessagingException me) {
		if (folderInfo != null && folderInfo.getFolderWindow() != null)
		    JOptionPane.showInternalMessageDialog(folderInfo.getFolderWindow().getDesktopPane(), Pooka.getProperty("error.Message.DeleteErrorMessage", "Error:  could not delete message.") +"\n" + me.getMessage());
	    }   
	}
	
	if (getMessageWindow() != null)
	    getMessageWindow().closeMessageWindow();
    }

    /**
     * A convenience method which sets autoExpunge by the value of 
     * Pooka.autoExpunge, and then calls deleteMessage(boolean autoExpunge)
     * with that value.
     */
    public void deleteMessage() {
	deleteMessage(Pooka.getProperty("Pooka.autoExpunge", "true").equals("true"));
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
    public String parseMsgString(String introTemplate) {
	MimeMessage m = (MimeMessage)message;
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

	if (intro.charAt(intro.length()-1) != '\n')
	    intro.append('\n');

	return intro.toString();
    }
    
    /**
     * This populates a message which is a reply to the current
     * message.
     */
    protected void populateReply(MimeMessage mMsg) 
	throws MessagingException {
	String textPart = MailUtilities.getTextPart(message, false);
	UserProfile up = getDefaultProfile();

	String parsedText;
	String replyPrefix;
	String parsedIntro;

	if (up != null && up.getMailProperties() != null) {
	    
	    replyPrefix = up.getMailProperties().getProperty("replyPrefix", Pooka.getProperty("Pooka.replyPrefix", "> "));
	    parsedIntro = parseMsgString(up.getMailProperties().getProperty("replyIntro", Pooka.getProperty("Pooka.replyIntro", "On %d, %n wrote:")));
	} else { 
	    replyPrefix = Pooka.getProperty("Pooka.replyPrefix", "> ");
	    parsedIntro = parseMsgString(Pooka.getProperty("Pooka.replyIntro", "On %d, %n wrote:"));
	}
	parsedText = prefixMessage(textPart, replyPrefix, parsedIntro);
	mMsg.setText(parsedText);
	
    }

    /**
     * This populates a new message which is a forwarding of the
     * current message.
     */
    protected void populateForward(MimeMessage mMsg) 
	throws MessagingException {
	String textPart = MailUtilities.getTextPart(message, false);
	UserProfile up = getDefaultProfile();

	String parsedText = null;
	String forwardPrefix;
	String parsedIntro;
	String forwardStyle = Pooka.getProperty("Pooka.forwardStle", "prefixed");

	if (up != null && up.getMailProperties() != null) {
	    if (forwardStyle.equals("prefixed")) {
		forwardPrefix = up.getMailProperties().getProperty("forwardPrefix", Pooka.getProperty("Pooka.forwardPrefix", "> "));
		parsedIntro = parseMsgString(up.getMailProperties().getProperty("forwardIntro", Pooka.getProperty("Pooka.forwardIntro", "Forwarded message from %n:")));
	    } else { 
		forwardPrefix = Pooka.getProperty("Pooka.forwardPrefix", "> ");
		parsedIntro = parseMsgString(Pooka.getProperty("Pooka.forwardIntro", "Forwarded message from %n:"));
	    }
	    parsedText = prefixMessage(textPart, forwardPrefix, parsedIntro);
	}

	    mMsg.setText(parsedText);
	    mMsg.setSubject(parseMsgString(Pooka.getProperty("Pooka.forwardSubject", "Fwd:  %s")));
    }

    /**
     * This sends the message to the printer, first creating an appropriate
     * print dialog, etc.
     */

    public void printMessage() {
	PrinterJob job = PrinterJob.getPrinterJob ();
	Book book = new Book ();
	MessagePrinter printer = new MessagePrinter(this);
	PageFormat pf = job.pageDialog (job.defaultPage ());
	int count = printer.getPageCount(pf);
	book.append (printer, pf, count);
	job.setPageable (book);
	final PrinterJob externalJob = job;

	if (job.printDialog ()) {
	    Thread t = new Thread(new net.suberic.util.swing.RunnableAdapter() {
		    public void run() {
			try {
			    externalJob.print ();
			}
			catch (PrinterException ex) {
			    ex.printStackTrace ();
			}
		    }
	    });
	    t.start();
	    
	}
    }

    /**
     * This creates and shows a PopupMenu for this component.  
     */
    public void showPopupMenu(JComponent component, MouseEvent e) {
	ConfigurablePopupMenu popupMenu = new ConfigurablePopupMenu();
	popupMenu.configureComponent("MessageProxy.popupMenu", Pooka.getResources());	
	popupMenu.setActive(getActions());
	popupMenu.show(component, e.getX(), e.getY());
	    
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

    public class OpenAction extends AbstractAction {
	OpenAction() {
	    super("message-open");
	}

	public void actionPerformed(java.awt.event.ActionEvent e) {
	    folderInfo.getFolderWindow().setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
	    openWindow();
	    folderInfo.getFolderWindow().setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
	}
    }

    public class MoveAction extends net.suberic.util.DynamicAbstractAction {
	MoveAction() {
	    super("message-move");
	}

	public void actionPerformed(java.awt.event.ActionEvent e) {
	    if (getMessageWindow() != null)
		getMessageWindow().setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
	    folderInfo.getFolderWindow().setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
	    moveMessage((FolderInfo)getValue("target"));
	    folderInfo.getFolderWindow().setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
	    if (getMessageWindow() != null)
		getMessageWindow().setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
	}

    }

    public class ReplyAction extends AbstractAction {

	ReplyAction() {
	    super("message-reply");
	}

	public void actionPerformed(ActionEvent e) {
	    if (getMessageWindow() != null)
		getMessageWindow().setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
	    folderInfo.getFolderWindow().setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
	    try {
		javax.mail.internet.MimeMessage m = (javax.mail.internet.MimeMessage)message.reply(false);
		populateReply(m);
		getMessagePanel().createNewMessage(m);

	    } catch (MessagingException me) {
		JOptionPane.showInternalMessageDialog(getMessagePanel(), Pooka.getProperty("error.MessageWindow.replyFailed", "Failed to create new Message.") + "\n" + me.getMessage());
	    }
	    folderInfo.getFolderWindow().setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
	    if (getMessageWindow() != null)
		getMessageWindow().setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
	}
    }

    public class ReplyAllAction extends AbstractAction {

	ReplyAllAction() {
	    super("message-reply-all");
	}

	public void actionPerformed(ActionEvent e) {
	    if (getMessageWindow() != null)
		getMessageWindow().setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
	    folderInfo.getFolderWindow().setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
	    try {
		javax.mail.internet.MimeMessage m = (javax.mail.internet.MimeMessage)message.reply(true);

		populateReply(m);
		getMessagePanel().createNewMessage(m);

	    } catch (MessagingException me) {
		JOptionPane.showInternalMessageDialog(getMessagePanel(), Pooka.getProperty("error.MessageWindow.replyFailed", "Failed to create new Message.") + "\n" + me.getMessage());
	    }
	    folderInfo.getFolderWindow().setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
	    if (getMessageWindow() != null)
		getMessageWindow().setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
	}
    }

    public class ForwardAction extends AbstractAction {

	ForwardAction() {
	    super("message-forward");
	}

	public void actionPerformed(ActionEvent e) {
	    if (getMessageWindow() != null)
		getMessageWindow().setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
	    folderInfo.getFolderWindow().setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
	    try {
		javax.mail.internet.MimeMessage m = new MimeMessage(getMessagePanel().getMainPanel().getSession());

		populateForward(m);
		getMessagePanel().createNewMessage(m);

	    } catch (MessagingException me) {
		JOptionPane.showInternalMessageDialog(getMessagePanel(), Pooka.getProperty("error.MessageWindow.replyFailed", "Failed to create new Message.") + "\n" + me.getMessage());
	    }
	    folderInfo.getFolderWindow().setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
	    if (getMessageWindow() != null)
		getMessageWindow().setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
	}
    }

    public class DeleteAction extends AbstractAction {
	DeleteAction() {
	    super("message-delete");
	}

	public void actionPerformed(ActionEvent e) {
	    if (getMessageWindow() != null)
		getMessageWindow().setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
	    folderInfo.getFolderWindow().setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
	    deleteMessage();
	    folderInfo.getFolderWindow().setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
	}
    }

    public class PrintAction extends AbstractAction {
	PrintAction() {
	    super("file-print");
	}

	public void actionPerformed(ActionEvent e) {
	    if (getMessageWindow() != null)
		getMessageWindow().setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
	    folderInfo.getFolderWindow().setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
	    printMessage();
	    folderInfo.getFolderWindow().setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
	    if (getMessageWindow() != null)
		getMessageWindow().setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
	}
    }

}







