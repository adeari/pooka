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
    // the underlying MessageInfo
    MessageInfo messageInfo;

    // the information for the FolderTable
    Vector tableInfo;
    
    // the column Headers for the FolderInfo Vector; used for loading the
    // tableInfo.
    Vector columnHeaders;

    // if the tableInfo has been loaded yet.
    boolean loaded = false;

    // commands for the GUI
    Hashtable commands;

    // The Window associated with this MessageProxy.
    MessageWindow msgWindow;

    public Action[] defaultActions;

    /**
     * This class should make it easy for us to sort subjects correctly.
     * It stores both the subject String itself and a sortingString which
     * is taken to lowercase and also has all of the starting 're:' characters
     * removed.
     */
    public class SubjectLine implements Comparable {
	String subject;
	String sortingSubject;

	/**
	 * Constructor.
	 */
	public SubjectLine(String newSubject) {
	    subject = newSubject;
	    if (subject != null)
		sortingSubject = subject.toLowerCase();
	    else
		sortingSubject = new String("");

	    int cutoffPoint = 0;
	    while(sortingSubject.startsWith("re:", cutoffPoint)) 
		for(cutoffPoint = cutoffPoint + 3; cutoffPoint < sortingSubject.length() && Character.isWhitespace(sortingSubject.charAt(cutoffPoint)); cutoffPoint++) { }
	    if (cutoffPoint != 0)
		sortingSubject = sortingSubject.substring(cutoffPoint);
	}
	
	/**
	 * Compare function.
	 */
	public int compareTo(Object o) {
	    // proper SubjectLines are always greater than null.
	    if (o == null)
		return 1;

	    if (o instanceof SubjectLine) {
		return sortingSubject.compareTo(((SubjectLine)o).sortingSubject);
	    } else
		return sortingSubject.compareToIgnoreCase(o.toString());
	}

	/**
	 * toString() just returns the original subject.
	 */
	public String toString() {
	    return subject;
	}
    }

    protected MessageProxy() {
    }

    /**
     * This creates a new MessageProxy from a set of Column Headers (for 
     * the tableInfo), a Message, and a link to a FolderInfo object.
     */
    public MessageProxy(Vector newColumnHeaders, MessageInfo newMessage) {
	messageInfo = newMessage;

	columnHeaders = newColumnHeaders;

	commands = new Hashtable();
	
	ActionThread folderThread = messageInfo.getFolderInfo().getFolderThread();
	
	defaultActions = new Action[] {
	    new ActionWrapper(new OpenAction(), folderThread),
	    new ActionWrapper(new MoveAction(), folderThread),
	    new ActionWrapper(new ReplyAction(), folderThread),
	    new ActionWrapper(new ReplyAllAction(), folderThread),
	    new ActionWrapper(new ForwardAction(), folderThread),
	    new ActionWrapper(new DeleteAction(), folderThread),
	    new ActionWrapper(new PrintAction(), folderThread)
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
	    try {
		int columnCount = columnHeaders.size();
		
		tableInfo = new Vector();
		
		for(int j=0; j < columnCount; j++) {
		    String propertyName = (String)columnHeaders.elementAt(j);
		    
		    if (propertyName.startsWith("FLAG")) 
			tableInfo.addElement(getMessageFlag((String)(columnHeaders.elementAt(j))));
		    else if (propertyName.equals("attachments"))
			tableInfo.addElement(new BooleanIcon(getMessageInfo().hasAttachments(), Pooka.getProperty("FolderTable.Attachments.icon", "")));
		    else
			tableInfo.addElement(getMessageInfo().getMessageProperty((String)(columnHeaders.elementAt(j))));
		}
		
		getMessageInfo().isSeen();
	    loaded=true;
	    } catch (MessagingException me) {
	    }
	}
    }	

    /**
     * This loads the Attachment information into the attachments vector.
     */

    public void loadAttachmentInfo() {
	messageInfo.loadAttachmentInfo();
    }

    /**
     * This gets a Flag property from the Message.
     */

    public BooleanIcon getMessageFlag(String flagName) {
	try {
	    if (flagName.equals("FLAG.ANSWERED") )
		return new BooleanIcon(getMessageInfo().flagIsSet(flagName), Pooka.getProperty("FolderTable.Answered.icon", ""));
	    else if (flagName.equals("FLAG.DELETED"))
		return new BooleanIcon(getMessageInfo().flagIsSet(flagName),Pooka.getProperty("FolderTable.Deleted.icon", ""));
	    else if (flagName.equals("FLAG.DRAFT"))
		return new BooleanIcon(getMessageInfo().flagIsSet(flagName), Pooka.getProperty("FolderTable.Draft.icon", ""));
	    else if (flagName.equals("FLAG.FLAGGED"))
		return new BooleanIcon(getMessageInfo().flagIsSet(flagName), Pooka.getProperty("FolderTable.Flagged.icon", ""));
	    else if (flagName.equals("FLAG.RECENT"))
		return new BooleanIcon(getMessageInfo().flagIsSet(flagName), Pooka.getProperty("FolderTable.Recent.icon", ""));
	    else if (flagName.equals("FLAG.NEW")) 
		return new MultiValueIcon(getMessageInfo().flagIsSet("FLAG.SEEN"), getMessageInfo().flagIsSet("FLAG.RECENT"), Pooka.getProperty("FolderTable.New.recentAndUnseenIcon", ""), Pooka.getProperty("FolderTable.New.justUnseenIcon", ""));
	    else if (flagName.equals("FLAG.SEEN"))
		return new BooleanIcon(getMessageInfo().flagIsSet(flagName), Pooka.getProperty("FolderTable.Seen.icon", ""));
	    else
		return new BooleanIcon(false, "");
	} catch (MessagingException me) {
	    return new BooleanIcon(false, "");
	}
    }

    /**
     * this opens a MessageWindow for this Message.
     */
    public void openWindow() {
	try {
	    getMessageInfo().getFolderInfo().getFolderWindow().getMessagePanel().openMessageWindow(this);
	    getMessageInfo().setSeen(true);
	} catch (MessagingException me) {
	    showError(Pooka.getProperty("error.Message.openWindow", "Error opening window:  "), me);
	}
    }

    /**
     * Moves the Message into the target Folder.
     */
    public void moveMessage(FolderInfo targetFolder) {
	try {
	    messageInfo.moveMessage(targetFolder);
	} catch (MessagingException me) {
	    showError( Pooka.getProperty("error.Message.CopyErrorMessage", "Error:  could not copy messages to folder:  ") + targetFolder.toString() +"\n", me);
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
	try {
	    getMessageInfo().deleteMessage(autoExpunge);
	    this.close();
	} catch (MessagingException me) {
	    if (me instanceof NoTrashFolderException) {
		if (JOptionPane.showInternalConfirmDialog(getMessageInfo().getFolderInfo().getFolderWindow(), Pooka.getProperty("error.Messsage.DeleteNoTrashFolder", "The Trash Folder configured is not available.\nDelete messages anyway?"), Pooka.getProperty("error.Messsage.DeleteNoTrashFolder.title", "Trash Folder Unavailable"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION)
		    try {
			getMessageInfo().remove(autoExpunge);
			this.close();
		    } catch (MessagingException mex) {
			showError(Pooka.getProperty("error.Message.DeleteErrorMessage", "Error:  could not delete message.") +"\n", mex);
		    }
	    } else {
		showError(Pooka.getProperty("error.Message.DeleteErrorMessage", "Error:  could not delete message.") +"\n", me);
	    }
	}
    }

    public void showError(String message, Exception ex) {
	if (getMessageInfo().getFolderInfo() != null && getMessageInfo().getFolderInfo().getFolderWindow() != null)
	    JOptionPane.showInternalMessageDialog(getMessageInfo().getFolderInfo().getFolderWindow().getDesktopPane(), message + ex.getMessage());
    }

    /**
     * Closes this MessageProxy. 
     *
     * For this implementation, the only result is that the MessageWindow,
     * if any, is closed.
     */
    public void close() {
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
     * This sends the message to the printer, first creating an appropriate
     * print dialog, etc.
     */

    public void printMessage() {
	PrinterJob job = PrinterJob.getPrinterJob ();
	Book book = new Book ();
	MessagePrinter printer = new MessagePrinter(getMessageInfo());
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
     * If the MessageProxy's getMessageInfo().getFolderInfo() is set, this returns the 
     * DefaultProfile of that getMessageInfo().getFolderInfo().  If the getMessageInfo().getFolderInfo() isn't set
     * (should that happen?), this returns null.
     */

    public UserProfile getDefaultProfile() {
	return getMessageInfo().getDefaultProfile();
    }

    /**
     * This returns the tableInfo for this MessageProxy.
     */
    public Vector getTableInfo() {
	if (isLoaded()) {
	    return tableInfo;
	} else {
	    loadTableInfo();
	    return tableInfo;
	}
    }

    public FolderInfo getFolderInfo() {
	return getMessageInfo().getFolderInfo();
    }

    public void setTableInfo(Vector newValue) {
	tableInfo=newValue;
    }

    public boolean isSeen() {
	return getMessageInfo().isSeen();
    }

    public void setSeen(boolean newValue) {
	if (newValue != getMessageInfo().isSeen()) {
	    try {
		getMessageInfo().setSeen(newValue);
	    } catch (MessagingException me) {
		showError( Pooka.getProperty("error.MessageWindow.setSeenFailed", "Failed to set Seen flag to ") + newValue + "\n", me);
	    }
	}
    }

    public MessagePanel getMessagePanel() {
	if (getMessageInfo().getFolderInfo() != null && getMessageInfo().getFolderInfo().getFolderWindow() != null)
	    return getMessageInfo().getFolderInfo().getFolderWindow().getMessagePanel();
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

    public MessageWindow getMessageWindow() {
	return msgWindow;
    }

    public void setMessageWindow(MessageWindow newValue) {
	msgWindow = newValue;
    }

    public MessageInfo getMessageInfo() {
	return messageInfo;
    }

    public Action getAction(String name) {
	return (Action)commands.get(name);
    }

    public Action[] getActions() {
	return defaultActions;
    }

    public class OpenAction extends AbstractAction {
	OpenAction() {
	    super("file-open");
	}

	public void actionPerformed(java.awt.event.ActionEvent e) {
	    getMessageInfo().getFolderInfo().getFolderWindow().setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
	    openWindow();
	    getMessageInfo().getFolderInfo().getFolderWindow().setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
	}
    }

    public class MoveAction extends net.suberic.util.DynamicAbstractAction {
	MoveAction() {
	    super("message-move");
	}

	public void actionPerformed(java.awt.event.ActionEvent e) {
	    if (getMessageWindow() != null)
		getMessageWindow().setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
	    getMessageInfo().getFolderInfo().getFolderWindow().setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
	    moveMessage((FolderInfo)getValue("target"));
	    getMessageInfo().getFolderInfo().getFolderWindow().setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
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
	    getMessageInfo().getFolderInfo().getFolderWindow().setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
	    try {
		javax.mail.internet.MimeMessage m = (javax.mail.internet.MimeMessage)getMessageInfo().getMessage().reply(false);
		getMessageInfo().populateReply(m);
		getMessagePanel().createNewMessage(m);

	    } catch (MessagingException me) {
		showError(Pooka.getProperty("error.MessageWindow.replyFailed", "Failed to create new Message.") + "\n", me);
	    }
	    getMessageInfo().getFolderInfo().getFolderWindow().setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
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
	    getMessageInfo().getFolderInfo().getFolderWindow().setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
	    try {
		javax.mail.internet.MimeMessage m = (javax.mail.internet.MimeMessage)getMessageInfo().getMessage().reply(true);

		getMessageInfo().populateReply(m);
		getMessagePanel().createNewMessage(m);

	    } catch (MessagingException me) {
		showError(Pooka.getProperty("error.MessageWindow.replyFailed", "Failed to create new Message.") + "\n", me);
	    }
	    getMessageInfo().getFolderInfo().getFolderWindow().setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
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
	    getMessageInfo().getFolderInfo().getFolderWindow().setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
	    try {
		javax.mail.internet.MimeMessage m = new MimeMessage(getMessagePanel().getMainPanel().getSession());

		getMessageInfo().populateForward(m);
		getMessagePanel().createNewMessage(m);

	    } catch (MessagingException me) {
		JOptionPane.showInternalMessageDialog(getMessagePanel(), Pooka.getProperty("error.MessageWindow.replyFailed", "Failed to create new Message.") + "\n" + me.getMessage());
	    }
	    getMessageInfo().getFolderInfo().getFolderWindow().setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
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
	    getMessageInfo().getFolderInfo().getFolderWindow().setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
	    deleteMessage();
	    getMessageInfo().getFolderInfo().getFolderWindow().setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
	}
    }

    public class PrintAction extends AbstractAction {
	PrintAction() {
	    super("file-print");
	}

	public void actionPerformed(ActionEvent e) {
	    if (getMessageWindow() != null)
		getMessageWindow().setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
	    getMessageInfo().getFolderInfo().getFolderWindow().setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
	    printMessage();
	    getMessageInfo().getFolderInfo().getFolderWindow().setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
	    if (getMessageWindow() != null)
		getMessageWindow().setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
	}
    }

}







