package net.suberic.pooka.gui;
import javax.mail.*;
import javax.mail.internet.*;
import javax.swing.*;
import javax.activation.*;
import java.util.Hashtable;
import java.util.Vector;
import net.suberic.pooka.Pooka;
import java.awt.event.ActionEvent;
import java.io.*;

public class NewMessageProxy extends MessageProxy {
    Hashtable commands;
    NewMessageWindow msgWindow;

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
		    if (attachments != null && attachments.size() > 0) {
			MimeBodyPart mbp = new MimeBodyPart();
			mbp.setContent(msgWindow.getMessageText(), msgWindow.getMessageContentType());
			MimeMultipart multipart = new MimeMultipart();
			multipart.addBodyPart(mbp);
			for (int i = 0; i < attachments.size(); i++) 
			    multipart.addBodyPart((BodyPart)attachments.elementAt(i));
			multipart.setSubType("mixed");
			getMessage().setContent(multipart);
			getMessage().saveChanges();
		    } else {
			getMessage().setContent(msgWindow.getMessageText(), msgWindow.getMessageContentType());
		    }
	       
		    ((MessagePanel)getMessageWindow().getDesktopPane()).getMainPanel().getMailQueue().sendMessage(getMessage(), urlName);
		}
		((NewMessageWindow)getMessageWindow()).setModified(false);
		getMessageWindow().closeMessageWindow();
	    } catch (MessagingException me) {
		if (me instanceof SendFailedException) {
		    JOptionPane.showInternalMessageDialog(getMessageWindow().getDesktopPane(), Pooka.getProperty("error.MessageWindow.sendFailed", "Failed to send Message.") + "\n" + me.getMessage());
		    me.printStackTrace(System.out);
		} else {
		    JOptionPane.showInternalMessageDialog(getMessageWindow().getDesktopPane(), Pooka.getProperty("error.MessageWindow.sendFailed", "Failed to send Message.") + "\n" + me.getMessage());
		    me.printStackTrace(System.out);
		}
	    }
	    
	}
    }
    
    /**
     * This attaches a file to a given message.  Really, all it does is
     * calls getFileToAttach(), and then sends that to attachFile().
     */
    public void attach() {
	File[] f = getFileToAttach();
	if (f != null) {
	    for (int i = 0; i < f.length; i++)
		attachFile(f[i]);
	} else
	    System.out.println("attached file was null.");
    }

    /**
     * This calls on the MessageWindow to bring up a FileDialog to choose
     * the file to attach to the message.  If no choice is made, this 
     * method returns null.
     */
    public File[] getFileToAttach() {
	return ((NewMessageWindow)getMessageWindow()).getFiles(Pooka.getProperty("MessageWindow.attachFileDialog.title", "Choose file to attach."), Pooka.getProperty("MessageWindow.attachFileDialog.buttonText", "Attach"));
    }

    /**
     * This actually attaches the File to the Message.  Any errors are 
     * sent to the MessageWindow to display.  
     *
     * This also sets the 'hasAttachment' property on the MessageWindow
     * to true.
     */
    public void attachFile(File f) {
	try {

	    // borrowing liberally from ICEMail here.

	    MimeBodyPart mbp = new MimeBodyPart();
	    
	    FileDataSource fds = new FileDataSource(f);

	    DataHandler dh = new DataHandler(fds);
	    
	    mbp.setFileName(f.getName());

	    if (Pooka.getMimeTypesMap().getContentType(f).startsWith("text"))
		mbp.setDisposition(Part.ATTACHMENT);
	    else
		mbp.setDisposition(Part.INLINE);
	    
	    mbp.setDescription(f.getName());
	    
	    mbp.setDataHandler( dh );
	    
	    if (attachments == null)
		attachments = new Vector();
	    
	    attachments.add(mbp);
	    
	    ((NewMessageWindow)getMessageWindow()).attachmentAdded(attachments.size() -1);
	} catch (Exception e) {
	    getMessageWindow().showError(Pooka.getProperty("error.MessageWindow.unableToAttachFile", "Unable to attach file."), Pooka.getProperty("error.MessageWindow.unableToAttachFile.title", "Unable to Attach File."), e);
	}
	
    }

    /**
     * This removes the given MimeBodyPart from the list of attachments.
     * I figure that you're likely only to be removing attachments from 
     * the attachment list itself, so you should be able to get the
     * correct underlying object.
     */
    public void detachFile(MimeBodyPart mbp) {
	if (attachments != null) {
	    int index = attachments.indexOf(mbp);
	    attachments.remove(mbp);
	    ((NewMessageWindow)getMessageWindow()).attachmentRemoved(index);
	}
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
	    attach();
	}
    }
    
}






