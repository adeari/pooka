package net.suberic.pooka.gui;
import net.suberic.pooka.*;
import net.suberic.util.gui.*;
import net.suberic.util.swing.EntryTextArea;
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

/**
 * A window for entering new messages.
 */
public class NewMessageFrame extends MessageFrame implements NewMessageUI {

    public boolean firstShow = true;

    /**
     * Creates a NewMessageFrame from the given Message.
     */

    public NewMessageFrame(NewMessageProxy newMsgProxy) {
	super(newMsgProxy);

	configureMessageFrame();
    }

    public NewMessageFrame(NewMessageInternalFrame source) {
	this.setTitle(Pooka.getProperty("Pooka.messageWindow.messageTitle.newMessage", "New Message"));
	messageDisplay = source.getMessageDisplay();
	msg = source.getMessageProxy();
	toolbar = source.getToolbar();
	keyBindings = source.getKeyBindings();
	msg.setMessageUI(this);

	this.getContentPane().add("North", toolbar);
	this.getContentPane().add("Center", messageDisplay);
	
	toolbar.setActive(this.getActions());

	configureInterfaceStyle();

	this.setLocation(source.getLocationOnScreen());
    }

    /**
     * This configures the MessageFrame.  This means that here is 
     * where we create the headerPanel and editorPane and add them to the 
     * splitPane.
     */
    protected void configureMessageFrame() {

	try {
	    this.createDefaultActions();
	    
	    this.setTitle(Pooka.getProperty("Pooka.messageWindow.messageTitle.newMessage", "New Message"));
	    
	    messageDisplay = new NewMessageDisplayPanel((NewMessageProxy)msg);
	    messageDisplay.configureMessageDisplay();
	    
	    toolbar = new ConfigurableToolbar("NewMessageWindowToolbar", Pooka.getResources());
	    
	    this.getContentPane().add("North", toolbar);
	    this.getContentPane().add("Center", messageDisplay);
	    
	    toolbar.setActive(this.getActions());
	    
	    keyBindings = new ConfigurableKeyBinding(getMessageDisplay(), "NewMessageWindow.keyBindings", Pooka.getResources());
	    keyBindings.setActive(getActions());
	} catch (MessagingException me) {
	    showError(Pooka.getProperty("error.MessageFrame.errorLoadingMessage", "Error loading Message:  ") + "\n" + me.getMessage(), Pooka.getProperty("error.MessageFrame.errorLoadingMessage.title", "Error loading message."));
	    me.printStackTrace();
	}
	
	configureInterfaceStyle();
    }
    
  /**
   * Configures the InterfaceStyle for this component.
   */
  public void configureInterfaceStyle() {
    HashMap uiStyle = Pooka.getUIFactory().getPookaUIManager().getNewMessageWindowStyle(this);
    messageDisplay.configureInterfaceStyle(uiStyle);
  }

    /**
     * Closes the message window.  This checks to see if the underlying
     * message is modified, and if so, pops up a dialog to make sure that
     * you really want to close the window.
     *
     * Currently, saveDraft isn't implemented, so 'yes' acts as 'cancel'.
     */
    public void closeMessageUI() {
	
	if (isModified()) {
	    int saveDraft = showConfirmDialog(Pooka.getProperty("error.saveDraft.message", "This message has unsaved changes.  Would you like to save a draft copy?"), Pooka.getProperty("error.saveDraft.title", "Save Draft"), JOptionPane.YES_NO_CANCEL_OPTION);
	    switch (saveDraft) {
	    case JOptionPane.YES_OPTION:
		//this.saveDraft();
	    case JOptionPane.NO_OPTION:
		this.dispose();
	    default:
		return;
	    }
	} else {
	    this.dispose();
	}
    }

    /**
     * Reattaches the window to the MessagePanel, if there is one.
     */
    public void attachWindow() {
	if (Pooka.getMainPanel().getContentPanel() instanceof MessagePanel) {
            MessagePanel mp = (MessagePanel) Pooka.getMainPanel().getContentPanel();
            NewMessageInternalFrame nmif = new NewMessageInternalFrame(mp, this);
            nmif.openMessageUI();
	    this.setModified(false);
            this.dispose();
        }
    }

    /**
     * This returns the values in the MesssageWindow as a set of 
     * InternetHeaders.
     */
    public InternetHeaders getMessageHeaders() throws MessagingException {
	return getNewMessageDisplay().getMessageHeaders();
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

    /**
     * This notifies the NewMessageUI that the attachment at the 
     * provided index has been removed.  This does not actually remove
     * the attachment, but rather should be called by the MessageProxy
     * when an attachment has been removed.
     */
    public void attachmentRemoved(int index) {
	getNewMessageDisplay().attachmentRemoved(index);
    }

    /**
     * This notifies the NewMessageUI that an attachment has been added
     * at the provided index.  This does not actually add an attachment,
     * but rather should be called by the MessageProxy when an attachment
     * has been added.
     */
    public void attachmentAdded(int index) {
	getNewMessageDisplay().attachmentAdded(index);
    }

    /**
     * Pops up a JFileChooser and returns the results.
     *
     * Note:  i'd like to have this working so that you can attach multiple
     * files at once, but it seems that the JFileChooser really doesn't 
     * want to return an array with anything in it for getSelectedFiles().  
     * So for now, I'll leave the Pooka API as is, but only ever return a 
     * single entry in the File array.
     */
    public File[] getFiles(String title, String buttonText) {
	JFileChooser jfc = new JFileChooser();
	jfc.setDialogTitle(title);
	jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
	jfc.setMultiSelectionEnabled(false);
	int a = jfc.showDialog(this, buttonText);

	if (a == JFileChooser.APPROVE_OPTION)
	    return new File[] {jfc.getSelectedFile()};
	else
	    return null;
    }

    /**
     * As specified by interface net.suberic.pooka.UserProfileContainer.
     *
     * This implementation returns the DefaultProfile of the associated
     * MessageProxy if the MessageFrame is not editable.  If the 
     * MessageFrame is editable, it returns the currently selected 
     * UserProfile object.
     */

    public UserProfile getDefaultProfile() {
	if (isEditable())
	    return getSelectedProfile();
	else
	    return getMessageProxy().getDefaultProfile();
    }

    /**
     * This method returns the UserProfile currently selected in the 
     * drop-down menu.
     */

    public UserProfile getSelectedProfile() {
	return getNewMessageDisplay().getSelectedProfile();
    }
  
  /**
   * sets the currently selected Profile.
   */
  public void setSelectedProfile(UserProfile newProfile) {
    getNewMessageDisplay().setSelectedProfile(newProfile);
  }

    /**
     * Overrides JComponent.addNotify().
     *
     * We override addNotify() here to set the proper splitPane location.
     */

    public void addNotify() {
        super.addNotify();
	
	if (firstShow) {
	    messageDisplay.sizeToDefault();
	    resizeByWidth();
	    firstShow = false;
	    
	}
    }

    public boolean isEditable() {
	return true;
    }

    public boolean isModified() {
	return getNewMessageDisplay().isModified();
    }

    public void setModified(boolean mod) {
	getNewMessageDisplay().setModified(mod);
    }

    public NewMessageDisplayPanel getNewMessageDisplay() {
	return (NewMessageDisplayPanel) messageDisplay;
    }

    //------- Actions ----------//

    /**
     * performTextAction grabs the focused component on the 
     * MessageFrame and, if it is a JTextComponent, tries to get it 
     * to perform the appropriate ActionEvent.
     */
    public void performTextAction(String name, ActionEvent e) {
	getNewMessageDisplay().performTextAction(name, e);
    }

    public Action[] getActions() {
	Action[] returnValue = getDefaultActions();
	
	if (getMessageDisplay() != null && getMessageDisplay().getActions() != null) 
	    returnValue = TextAction.augmentList(getMessageDisplay().getActions(), returnValue);

	return returnValue;
    }

    public Action[] getDefaultActions() {
	return defaultActions;
    }

    private void createDefaultActions() {
	// The actions supported by the window itself.

	/*	defaultActions = new Action[] {
	    new CloseAction(),
	    new CutAction(),
	    new CopyAction(),
	    new PasteAction(),
	    new TestAction()
	    };*/

	defaultActions = new Action[] {
	    new CloseAction(),
	};
    }

    //-----------actions----------------


    class CloseAction extends AbstractAction {

	CloseAction() {
	    super("file-close");
	}
	
        public void actionPerformed(ActionEvent e) {
	    closeMessageUI();
	}
    }

    class CutAction extends AbstractAction {
	
	CutAction() {
	    super("cut-to-clipboard");
	}

	public void actionPerformed(ActionEvent e) {
	    performTextAction((String)getValue(Action.NAME), e);
	}
    }

    class CopyAction extends AbstractAction {
	
	CopyAction() {
	    super("copy-to-clipboard");
	}

	public void actionPerformed(ActionEvent e) {
	    performTextAction((String)getValue(Action.NAME), e);
	}
    }

    class PasteAction extends AbstractAction {
	
	PasteAction() {
	    super("paste-from-clipboard");
	}

	public void actionPerformed(ActionEvent e) {
	    performTextAction((String)getValue(Action.NAME), e);
	}
    }

    class TestAction extends AbstractAction {
	
	TestAction() {
	    super("test");
	}

	public void actionPerformed(ActionEvent e) {
	    System.out.println(net.suberic.pooka.MailUtilities.wrapText(getMessageText()));
	}
    }

}





