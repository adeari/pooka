package net.suberic.pooka.gui;
import net.suberic.pooka.*;
import net.suberic.util.gui.*;
import javax.mail.*;
import javax.mail.internet.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.TextAction;
import java.util.*;
import javax.swing.text.JTextComponent;
import javax.swing.event.*;
import java.io.File;

public class ReadMessageFrame extends MessageFrame {

    public boolean firstShow = true;

    /**
     * Creates a ReadMessageFrame from the given Message.
     */

    public ReadMessageFrame(MessageProxy newMsgProxy) {
	super(newMsgProxy);

	configureMessageFrame();
    }
    
    protected void configureMessageFrame() {
	try {
	    try {
		this.setTitle((String)msg.getMessageInfo().getMessageProperty("Subject"));
	    } catch (MessagingException me) {
		this.setTitle(Pooka.getProperty("Pooka.messageFrame.messageTitle.noSubject", "<no subject>"));
	    }
	    
	    messageDisplay = new ReadMessageDisplayPanel(msg);
	    messageDisplay.configureMessageDisplay();
	    
	    toolbar = new ConfigurableToolbar("MessageWindowToolbar", Pooka.getResources());
	    
	    this.getContentPane().add("North", toolbar);
	    this.getContentPane().add("Center", messageDisplay);

	    toolbar.setActive(this.getActions());

	    keyBindings = new ConfigurableKeyBinding(getMessageDisplay(), "ReadMessageWindow.keyBindings", Pooka.getResources());
	    keyBindings.setActive(getActions());

	} catch (MessagingException me) {
	    showError(Pooka.getProperty("error.MessageFrame.errorLoadingMessage", "Error loading Message:  ") + "\n" + me.getMessage(), Pooka.getProperty("error.MessageFrame.errorLoadingMessage.title", "Error loading message."));
	    me.printStackTrace();
	}
	
    }

    /**
     * Overrides JComponent.addNotify().
     *
     * We override addNotify() here to call resizeByWidth() to set
     * the correct width, and, if there is a splitPane with an attachment
     * panel, to set the correct divider location on the split pane.
     */
    public void addNotify() {
	super.addNotify();
	if (firstShow) {
	    ((ReadMessageDisplayPanel)messageDisplay).sizeToDefault();
	    resizeByWidth();
	    firstShow = false;
	}
    }

    /**
     * This registers the Keyboard action not only for the FolderWindow
     * itself, but also for pretty much all of its children, also.  This
     * is to work around something which I think is a bug in jdk 1.2.
     * (this is not really necessary in jdk 1.3.)
     *
     * Overrides JComponent.registerKeyboardAction(ActionListener anAction,
     *            String aCommand, KeyStroke aKeyStroke, int aCondition)
     */

    public void registerKeyboardAction(ActionListener anAction,
       	       String aCommand, KeyStroke aKeyStroke, int aCondition) {

	if (messageDisplay != null)
	    messageDisplay.registerKeyboardAction(anAction, aCommand, aKeyStroke, aCondition);
	toolbar.registerKeyboardAction(anAction, aCommand, aKeyStroke, aCondition);

    }
    
    /**
     * This unregisters the Keyboard action not only for the FolderWindow
     * itself, but also for pretty much all of its children, also.  This
     * is to work around something which I think is a bug in jdk 1.2.
     * (this is not really necessary in jdk 1.3.)
     *
     * Overrides JComponent.unregisterKeyboardAction(KeyStroke aKeyStroke)
     */

    public void unregisterKeyboardAction(KeyStroke aKeyStroke) {
	if (messageDisplay != null)
	    messageDisplay.unregisterKeyboardAction(aKeyStroke);
	toolbar.unregisterKeyboardAction(aKeyStroke);
    }

    //------- Actions ----------//

    public Action[] getActions() {
	
	Action[] actionList;

	if (messageDisplay.getActions() != null) {
	    actionList = TextAction.augmentList(messageDisplay.getActions(), getDefaultActions());
	} else 
	    actionList = getDefaultActions();

	return actionList;
    }

}





