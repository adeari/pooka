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

public class ReadMessageInternalFrame extends MessageInternalFrame {

    public boolean firstShow = true;

    /**
     * Creates a ReadMessageInternalFrame from the given Message.
     */

    public ReadMessageInternalFrame(MessagePanel newParentContainer, MessageProxy newMsgProxy) {
	super(newParentContainer, newMsgProxy);

	this.addFocusListener(new FocusAdapter() {
	    public void focusGained(FocusEvent e) {
	      if (getMessageDisplay() != null)
		getMessageDisplay().requestFocus();
	    }
	  });

	this.addInternalFrameListener(new InternalFrameAdapter() {
	    public void internalFrameClosed(InternalFrameEvent e) {
	      if (getMessageProxy().getMessageUI() == ReadMessageInternalFrame.this)
		getMessageProxy().setMessageUI(null);
	    }
	  });

    }

    public ReadMessageInternalFrame(MessagePanel newParentContainer, ReadMessageFrame source) {
	parentContainer = newParentContainer;
    	messageDisplay = source.getMessageDisplay();
	msg = source.getMessageProxy();
	toolbar = source.getToolbar();
	keyBindings = source.getKeyBindings();
	msg.setMessageUI(this);

	try {
	    this.setTitle((String)msg.getMessageInfo().getMessageProperty("Subject"));
	} catch (MessagingException me) {
	    this.setTitle(Pooka.getProperty("Pooka.messageFrame.messageTitle.noSubject", "<no subject>"));
	}
	
	this.getContentPane().add("North", toolbar);
	this.getContentPane().add("Center", messageDisplay);
	
	toolbar.setActive(this.getActions());

	Point loc = source.getLocationOnScreen();
	SwingUtilities.convertPointFromScreen(loc, parentContainer);
	this.setLocation(loc);

	this.addFocusListener(new FocusAdapter() {
	    public void focusGained(FocusEvent e) {
	      if (getMessageDisplay() != null)
		getMessageDisplay().requestFocus();
	    }
	  });

	this.addInternalFrameListener(new InternalFrameAdapter() {
	    public void internalFrameClosed(InternalFrameEvent e) {
	      if (getMessageProxy().getMessageUI() == ReadMessageInternalFrame.this)
		getMessageProxy().setMessageUI(null);
	    }
	  });

	configureInterfaceStyle();
    }

    /**
     * Configures the MessageInteralFrame.
     */
    public void configureMessageInternalFrame() throws MessagingException {
	try {
	    this.setTitle((String)msg.getMessageInfo().getMessageProperty("Subject"));
	} catch (MessagingException me) {
	    this.setTitle(Pooka.getProperty("Pooka.messageInternalFrame.messageTitle.noSubject", "<no subject>"));
	}
	
	messageDisplay = new ReadMessageDisplayPanel(msg);
	messageDisplay.configureMessageDisplay();
	
	toolbar = new ConfigurableToolbar("MessageWindowToolbar", Pooka.getResources());
	
	this.getContentPane().add("North", toolbar);
	this.getContentPane().add("Center", messageDisplay);
	
	toolbar.setActive(this.getActions());
	
	keyBindings = new ConfigurableKeyBinding(this, "ReadMessageWindow.keyBindings", Pooka.getResources());
	keyBindings.setActive(getActions());

	configureInterfaceStyle();
	
    }

 /**
   * Gets the UIConfig object from the UpdatableUIManager which is appropriate
   * for this UI.
   */
  public net.suberic.util.swing.UIConfig getUIConfig(net.suberic.util.swing.UpdatableUIManager uuim) {
    MessageProxy mp = getMessageProxy();
    if (mp == null)
      return null;

    MessageInfo mi = mp.getMessageInfo();
    if (mi == null)
      return null;

    FolderInfo fi = mi.getFolderInfo();
    if (fi != null) {
      String id = Pooka.getProperty(fi.getFolderProperty() + ".uiConfig", "");
      if (id != null && ! id.equals("")) {
	return uuim.getUIConfig(id);
      } 
    } 

    return null;
  }

    public void detachWindow() {
	ReadMessageFrame rmf = new ReadMessageFrame(this);

	rmf.show();
	try {
	    this.setClosed(true);
	} catch (java.beans.PropertyVetoException pve) {
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
	    resizeByWidth();
	    getMessageDisplay().sizeToDefault();
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
	super.registerKeyboardAction(anAction, aCommand, aKeyStroke, aCondition);

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
	super.unregisterKeyboardAction(aKeyStroke);

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





