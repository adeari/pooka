package net.suberic.pooka.gui;
import javax.mail.*;
import javax.swing.*;
import java.util.Hashtable;
import java.util.Vector;
import java.awt.event.MouseEvent;
import net.suberic.pooka.FolderInfo;
import net.suberic.pooka.Pooka;
import net.suberic.util.gui.ConfigurablePopupMenu;

/**
 * This class represents a group of Messages all selected.
 */

public class MultiMessageProxy extends MessageProxy{
    Vector messages;
    FolderWindow folderWindow;
    MessagePanel messagePanel;
    int[] rowNumbers;

    Hashtable commands;

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
	    ((MessageProxy)messages.elementAt(i)).deleteMessage();
	}
    }

    public Message getMessage() { return null; }

    public Action getAction(String name) {
	return (Action)commands.get(name);
    }

    public Action[] getActions() {
	return defaultActions;
    }

    public Action[] defaultActions = {
	new OpenAction(),
	new DeleteAction(),
	new MoveAction()
    };

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

}
