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

    /**
     * This sends the Message associated with this MessageProxy.
     *
     * If this MessageProxy has a MessageWindow associated with it, it 
     * will try to load the information from it, and then send the message.
     * Otherwise, it will just try sending the message as-is.
     *
     * If the Message.sendImmediately property is set, then the method will 
     * also pop up and error window if there are any problems sending the 
     * queued messages.  
     *
     * If there is a MessageWindow associated with this Proxy, and either 
     * there are no errors sending the message, or the Message is just added 
     * to the Queue and not sent yet, the Window will also be closed.
     *
     */
    public void send() {
	if (getMessageWindow() != null) { 
	    try {
		URLName urlName;
		
		urlName = msgWindow.populateMessageHeaders(getMessage());
		if (urlName != null) {
		    getMessage().setContent(msgWindow.getMessageText(), msgWindow.getMessageContentType());
		    
		    ((MessagePanel)getMessageWindow().getDesktopPane()).getMainPanel().getMailQueue().sendMessage(getMessage(), urlName);
		}
		getMessageWindow().setModified(false);
		getMessageWindow().closeMessageWindow();
	    } catch (MessagingException me) {
		if (me instanceof SendFailedException) {
		    JOptionPane.showInternalMessageDialog(getMessageWindow().getDesktopPane(), Pooka.getProperty("error.MessageWindow.sendFailed", "Failed to send Message.") + "\n" + me.getMessage());
		} else {
		    JOptionPane.showInternalMessageDialog(getMessageWindow().getDesktopPane(), Pooka.getProperty("error.MessageWindow.sendFailed", "Failed to send Message.") + "\n" + me.getMessage());
		}
	    }
	    
	}
    }
    
    /**
     * not implemented yet.
     */
    public void attachFile() {
    }

    public Message getMessage() {
	return message;
    }

    public Action[] defaultActions = {
	new SendAction(),
	new AttachAction()
	    };
    
    public Action getAction(String name) {
	return (Action)commands.get(name);
    }

    public Action[] getActions() {
	return defaultActions;
    }

    class SendAction extends AbstractAction {
	SendAction() {
	    super("message-send");
	}

	public void actionPerformed(ActionEvent e) {
	    send();
	}
    }

    class AttachAction extends AbstractAction {
	AttachAction() {
	    super("message-attach-file");
	}

	public void actionPerformed(ActionEvent e) {
	    attachFile();
	}
    }
    
}






