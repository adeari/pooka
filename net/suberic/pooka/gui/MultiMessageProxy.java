package net.suberic.pooka.gui;
import javax.mail.*;
import javax.swing.*;
import java.util.Hashtable;
import java.util.Vector;
import java.awt.event.MouseEvent;
import java.awt.print.*;
import net.suberic.pooka.FolderInfo;
import net.suberic.pooka.Pooka;
import net.suberic.util.gui.ConfigurablePopupMenu;
import net.suberic.util.thread.*;

/**
 * This class represents a group of Messages all selected.
 */

public class MultiMessageProxy extends MessageProxy{
    Vector messages;
    FolderWindow folderWindow;
    MessagePanel messagePanel;
    int[] rowNumbers;

    Hashtable commands;

    public Action[] defaultAction;

    /**
     * This creates a new MultiMessageProxy from the MessageProxys in 
     * the newMessages Vector.  These should be the MessageProxy objects
     * which correspond with rows newRowNumbers on FolderWindow
     * newFolderWindow.
     */
    public MultiMessageProxy(int[] newRowNumbers, Vector newMessages, FolderWindow newFolderWindow) {
	rowNumbers=newRowNumbers;
	messages=newMessages;
	folderWindow=newFolderWindow;
	folderInfo = newFolderWindow.getFolderInfo();
	
	ActionThread storeThread = folderWindow.getFolderInfo().getParentStore().getStoreThread();

	defaultActions = new Action[] {
	    new ActionWrapper(new OpenAction(), storeThread),
	    new ActionWrapper(new DeleteAction(), storeThread),
	    new ActionWrapper(new MoveAction(), storeThread),
	    new ActionWrapper(new PrintAction(), storeThread)
		};

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
     * This opens up new windows for all of the selected messages.
     */
    public void openWindow() {
	for (int i = 0; i < messages.size(); i++) {
	    folderWindow.getMessagePanel().openMessageWindow((MessageProxy)messages.elementAt(i));
	}
    }

    /**
     * Moves the Message into the target Folder.
     */
    public void moveMessage(FolderInfo targetFolder) {
        boolean success=false;
	Message[] allMessages = new Message[messages.size()];
	for (int i = 0; i < messages.size(); i++)
	    allMessages[i] = ((MessageProxy)messages.elementAt(i)).getMessage();

	// these should all be from the same folder, shouldn't they?
	FolderInfo fInfo = ((MessageProxy)messages.elementAt(0)).getFolderInfo();

	try {
	    fInfo.getFolder().copyMessages(allMessages, targetFolder.getFolder());
	    success=true;
	} catch (MessagingException me) {
	    if (folderInfo != null && folderInfo.getFolderWindow() != null)
		JOptionPane.showInternalMessageDialog(folderInfo.getFolderWindow().getDesktopPane(), Pooka.getProperty("error.Message.CopyErrorMessage", "Error:  could not copy messages to folder:  ") + targetFolder.toString() +"\n");
	    if (Pooka.isDebug())
		me.printStackTrace();
	}

	if (success == true) 
	    try {
		for (int j = 0; j < allMessages.length; j++)
		    allMessages[j].setFlag(Flags.Flag.DELETED, true);
		
		if ( Pooka.getProperty("Pooka.autoExpunge", "true").equals("true") )
		    fInfo.getFolder().expunge();
	    } catch (MessagingException me) {
		if (folderInfo != null && folderInfo.getFolderWindow() != null)
		    JOptionPane.showInternalMessageDialog(folderInfo.getFolderWindow().getDesktopPane(), Pooka.getProperty("error.Message.RemoveErrorMessage", "Error:  could not remove messages from folder:  ") + targetFolder.toString() +"\n" + me.getMessage());
		if (Pooka.isDebug())
		    me.printStackTrace();
	    }		
    }

    public void showPopupMenu(JComponent component, MouseEvent e) {
	ConfigurablePopupMenu popupMenu = new ConfigurablePopupMenu();
	popupMenu.configureComponent("MessageProxy.popupMenu", Pooka.getResources());	
	popupMenu.setActive(getActions());
	popupMenu.show(component, e.getX(), e.getY());
	    
    }

    /**
     * deletes all the messages in this proxy.
     */
    public void deleteMessages() {
	for (int i = 0; i < messages.size(); i++) {
	    ((MessageProxy)messages.elementAt(i)).deleteMessage(false);
	}

	if (Pooka.getProperty("Pooka.autoExpunge", "true").equals("true"))
	    try {
		folderInfo.getFolder().expunge();
	    } catch (MessagingException me) {
		if (folderInfo != null && folderInfo.getFolderWindow() != null)
		    JOptionPane.showInternalMessageDialog(folderInfo.getFolderWindow().getDesktopPane(), Pooka.getProperty("error.Message.DeleteErrorMessage", "Error:  could not delete message.") +"\n" + me.getMessage());
	    }
    }

        /**
     * This sends the message to the printer, first creating an appropriate
     * print dialog, etc.
     */

    public void printMessage() {
	PrinterJob job = PrinterJob.getPrinterJob ();
	Book book = new Book ();
	PageFormat pf = job.pageDialog (job.defaultPage ());
	for (int i = 0; i < messages.size(); i++) {
	    MessagePrinter printer = new MessagePrinter((MessageProxy)messages.elementAt(i), book.getNumberOfPages());
	    book.append (printer, pf);
	}
	job.setPageable (book);
	final PrinterJob externalJob = job;
	if (job.printDialog ()) {
	    Thread printThread = new Thread(new net.suberic.util.swing.RunnableAdapter() {
		public void run() {
		    try {
			externalJob.print ();
		    }
		    catch (PrinterException ex) {
			ex.printStackTrace ();
		    }
		}
	    });
	    printThread.start();
	    
	}
    }

    public Message getMessage() { return null; }

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
	    openWindow();
	}
    }

    public class DeleteAction extends AbstractAction {
	DeleteAction() {
	    super("message-delete");
	}

	public void actionPerformed(java.awt.event.ActionEvent e) {
	    deleteMessages();
	}
    }

    public class MoveAction extends net.suberic.util.DynamicAbstractAction {
	MoveAction() {
	    super("message-move");
	}

	public void actionPerformed(java.awt.event.ActionEvent e) {
	    moveMessage((FolderInfo)getValue("target"));
	}

    }

    public class PrintAction extends AbstractAction {
	PrintAction() {
	    super("file-print");
	}
	
	public void actionPerformed(java.awt.event.ActionEvent e) {
	    folderWindow.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
	    printMessage();
	    folderWindow.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
	}
    }
}

