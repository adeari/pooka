package net.suberic.pooka.gui;
import javax.mail.*;
import javax.swing.*;
import java.util.Hashtable;
import net.suberic.pooka.Pooka;
import java.awt.event.ActionEvent;

public class NewMessageProxy extends MessageProxy {
    Hashtable commands;

    public NewMessageProxy(Message newMessage) {
	message=newMessage;

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
	// shouldn't have to open window.
    }

    public void moveMessage(Folder targetFolder) {
	// shouldn't have to.  might want to implement this to move a message
	// to drafts, though.
    }
        
    public Message getMessage() {
	return message;
    }

    public Action getAction(String name) {
	return (Action)commands.get(name);
    }

    public Action[] getActions() {
	return null;
    }

}






