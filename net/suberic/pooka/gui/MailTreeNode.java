package net.suberic.pooka.gui;
import javax.swing.tree.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Hashtable;

public class MailTreeNode extends DefaultMutableTreeNode {
    public Action[] defaultActions = null;

    public Hashtable commands;

    public JComponent parentContainer;

    MailTreeNode(Object userObj, JComponent parent) {
	super(userObj);

	parentContainer = parent;

    }

    protected void setCommands() {
	commands = new Hashtable();
	
	Action[] actions = getActions();
	if (actions != null) {
	    for (int i = 0; i < actions.length; i++) {
		Action a = actions[i];
		commands.put(a.getValue(Action.NAME), a);
	    }
	}
	
    }


    public Action[] getActions() {
	return getDefaultActions();
    }

    public Action getAction(String name) {
	return (Action)commands.get(name);
    }

    public Action[] getDefaultActions() {
	return defaultActions;
    }

    public JComponent getParentContainer() {
	return parentContainer;
    }
}

