package net.suberic.pooka.gui;
import javax.mail.*;
import javax.swing.*;
import java.util.Hashtable;
import java.util.Vector;

public class MultiMessageProxy extends MessageProxy{
    Vector messages;
    FolderWindow folderWindow;
    MessagePanel messagePanel;
    int[] rowNumbers;

    Hashtable commands;

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

    public void openWindow() {
	for (int i = 0; i < messages.size(); i++) {
	    folderWindow.getMessagePanel().openMessageWindow((MessageProxy)messages.elementAt(i));
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
	new OpenAction()
    };

    public class OpenAction extends AbstractAction {
	OpenAction() {
	    super("message-open");
	}

	public void actionPerformed(java.awt.event.ActionEvent e) {
	    openWindow();
	}
    }

}
