package net.suberic.pooka.gui;
import net.suberic.pooka.*;
import net.suberic.util.gui.*;
import net.suberic.util.swing.*;
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
import javax.swing.plaf.metal.MetalTheme;

/**
 * An InternalFrame which can display messages.
 * 
 * This class should be used in conjunction with a MessagePanel.
 */
public abstract class MessageInternalFrame extends JInternalFrame implements MessageUI, ThemeSupporter, ThemeListener {

    protected MessagePanel parentContainer;

    protected MessageProxy msg;
    protected MessageDisplayPanel messageDisplay;

    protected ConfigurableToolbar toolbar;
    protected ConfigurableKeyBinding keyBindings;
    protected boolean addedToDesktop = false;

  protected javax.swing.plaf.metal.MetalTheme currentTheme = null;
    /**
     * Creates a MessageInternalFrame from the given Message.
     */

    public MessageInternalFrame(MessagePanel newParentContainer, MessageProxy newMsgProxy) {
	super(Pooka.getProperty("Pooka.messageInternalFrame.messageTitle.newMessage", "New Message"), true, true, true, true);

	parentContainer = newParentContainer;
	msg=newMsgProxy;

	this.getContentPane().setLayout(new BorderLayout());

	msg.setMessageUI(this);
	
    }

    /**
     * Creates a MessageInternalFrame from the given Message.
     */

    protected MessageInternalFrame() {
	super(Pooka.getProperty("Pooka.messageInternalFrame.messageTitle.newMessage", "New Message"), true, true, true, true);
	this.getContentPane().setLayout(new BorderLayout());
	
    }
    
    /**
     * this method is expected to do all the implementation-specific
     * duties.
     */

    protected abstract void configureMessageInternalFrame() throws MessagingException;

  /**
   * Configures the InterfaceStyle for this component.
   */
  public void configureInterfaceStyle() {
    try {
      Pooka.getUIFactory().getPookaThemeManager().updateUI(this, this);
      getMessageDisplay().setDefaultFont(getMessageDisplay().getEditorPane());
      getMessageDisplay().sizeToDefault();
      MessageInternalFrame.this.setSize(MessageInternalFrame.this.getPreferredSize());
      
    } catch (Exception e) {
    }
  }

  /**
   * Gets the currently configured Theme.
   */
  public MetalTheme getCurrentTheme() {
    return currentTheme;
  }

  /**
   * Sets the Theme that this component is currently using.
   */
  public void setCurrentTheme(MetalTheme newTheme) {
    if (currentTheme != null && currentTheme instanceof ConfigurableMetalTheme) {
      ((ConfigurableMetalTheme)currentTheme).removeThemeListener(this);
    }
    currentTheme = newTheme;
    
    if (currentTheme != null && currentTheme instanceof ConfigurableMetalTheme) {
      ((ConfigurableMetalTheme)currentTheme).addThemeListener(this);
    }
  }

  /**
   * Called when the specifics of a Theme change.
   */
  public void themeChanged(ConfigurableMetalTheme theme) {
    // we should really only be getting messages from our own current themes,
    // but, hey, it never hurts to check.
    if (currentTheme != null && currentTheme == theme) {
      SwingUtilities.invokeLater(new Runnable() {
	  public void run() {
	    try {
	      Pooka.getUIFactory().getPookaThemeManager().updateUI(MessageInternalFrame.this, MessageInternalFrame.this, true);
	      getMessageDisplay().setDefaultFont(getMessageDisplay().getEditorPane());
	      getMessageDisplay().sizeToDefault();
	      MessageInternalFrame.this.setSize(MessageInternalFrame.this.getPreferredSize());

	    } catch (Exception e) {
	    }

	  }
	});
    }
  }

    /**
     * This opens the MessageInternalFrame by calling 
     * getParentContainer().openMessageInternalFrame(getMessageProxy());
     */
    public void openMessageUI() {
	getParentContainer().openMessageWindow(getMessageProxy(), !addedToDesktop);
	addedToDesktop = true;
    }

    /**
     * This closes the MessageInternalFrame.
     */
    public void closeMessageUI() {
	try {
	    this.setClosed(true);
	} catch (java.beans.PropertyVetoException e) {
	}
    }

    /**
     * This detaches this window from the MessagePanel, instead making it
     * a top-level MessageFrame.
     */
    public abstract void  detachWindow();


    /**
     * This shows an Confirm Dialog window.  We include this so that
     * the MessageProxy can call the method without caring abou the
     * actual implementation of the Dialog.
    */    
    public int showConfirmDialog(String messageText, String title, int type) {
	return JOptionPane.showInternalConfirmDialog((JDesktopPane)Pooka.getMainPanel().getContentPanel(), messageText, title, type);
    }

    /**
     * This shows an Confirm Dialog window.  We include this so that
     * the MessageProxy can call the method without caring abou the
     * actual implementation of the Dialog.
     */    
  public int showConfirmDialog(String messageText, String title, int optionType, int iconType) {
    return JOptionPane.showInternalConfirmDialog((JDesktopPane)Pooka.getMainPanel().getContentPanel(), messageText, title, optionType);
    }

    /**
     * This shows an Error Message window.  We include this so that
     * the MessageProxy can call the method without caring abou the
     * actual implementation of the Dialog.
     */
    public void showError(String errorMessage, String title) {
	Pooka.getUIFactory().showError(errorMessage, title);
    }

    /**
     * This shows an Error Message window.  We include this so that
     * the MessageProxy can call the method without caring abou the
     * actual implementation of the Dialog.
     */
    public void showError(String errorMessage) {
	showError(errorMessage, Pooka.getProperty("Error", "Error"));
    }

    /**
     * This shows an Error Message window.  We include this so that
     * the MessageProxy can call the method without caring abou the
     * actual implementation of the Dialog.
     */
    public void showError(String errorMessage, Exception e) {
	showError(errorMessage, Pooka.getProperty("Error", "Error"), e);
    }

    /**
     * This shows an Error Message window.  We include this so that
     * the MessageProxy can call the method without caring about the
     * actual implementation of the Dialog.
     */
    public void showError(String errorMessage, String title, Exception e) {
	showError(errorMessage + e.getMessage(), title);
	e.printStackTrace();
    }

    /**
     * This shows a Message window.  We include this so that
     * the MessageProxy can call the method without caring about the
     * actual implementation of the Dialog.
     */
  public void showMessageDialog(String message, String title) {
    JOptionPane.showInternalMessageDialog((JDesktopPane)Pooka.getMainPanel().getContentPanel(), message, title, JOptionPane.PLAIN_MESSAGE);
  }

    /**
     * This shows an Input window.  We include this so that the 
     * MessageProxy can call the method without caring about the actual
     * implementation of the dialog.
     */
    public String showInputDialog(String inputMessage, String title) {
	return Pooka.getUIFactory().showInputDialog(inputMessage, title);
    }

    /**
     * This shows an Input window.  We include this so that the 
     * MessageProxy can call the method without caring about the actual
     * implementation of the dialog.
     */
    public String showInputDialog(Object[] inputPanes, String title) {
	return JOptionPane.showInternalInputDialog((MessagePanel)Pooka.getMainPanel().getContentPanel(), inputPanes, title, JOptionPane.QUESTION_MESSAGE);
    }

    /**
     * A convenience method to set the PreferredSize and Size of the
     * component to that of the current preferred width.
     */
    public void resizeByWidth() {
	/*
	int width = (int)messageDisplay.getPreferredSize().getWidth();
	this.setPreferredSize(new Dimension(width, width));
	*/
	this.setSize(this.getPreferredSize());
    }

    /**
     * As specified by interface net.suberic.pooka.gui.MessageUI.
     * 
     * This implementation sets the cursor to either Cursor.WAIT_CURSOR
     * if busy, or Cursor.DEFAULT_CURSOR if not busy.
     */
    public void setBusy(boolean newValue) {
	if (newValue)
	    this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	else
	    this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    /**
     * As specified by interface net.suberic.pooka.UserProfileContainer.
     *
     * This implementation returns the DefaultProfile of the associated
     * MessageProxy if the MessageInternalFrame is not editable.  If the 
     * MessageInternalFrame is editable, it returns the currently selected 
     * UserProfile object.
     */

    public UserProfile getDefaultProfile() {
	return getMessageProxy().getDefaultProfile();
    }

    public MessageDisplayPanel getMessageDisplay() {
	return messageDisplay;
    }

    public MessageProxy getMessageProxy() {
	return msg;
    }

    public void setMessageProxy(MessageProxy newValue) {
	msg = newValue;
    }

    public String getMessageText() {
	return getMessageDisplay().getMessageText();
    }

    public String getMessageContentType() {
	return getMessageDisplay().getMessageContentType();
    }

    public AttachmentPane getAttachmentPanel() {
	return getMessageDisplay().getAttachmentPanel();
    }

    public MessagePanel getParentContainer() {
	return parentContainer;
    }

    public ConfigurableToolbar getToolbar() {
	return toolbar;
    }

    public ConfigurableKeyBinding getKeyBindings() {
	return keyBindings;
    }

    //------- Actions ----------//

    public Action[] getActions() {
	return defaultActions;
    }

    public Action[] getDefaultActions() {
	return defaultActions;
    }

    //-----------actions----------------

    // The actions supported by the window itself.

    public Action[] defaultActions = {
	new CloseAction(),
	new DetachAction()
    };

    class CloseAction extends AbstractAction {

	CloseAction() {
	    super("file-close");
	}
	
        public void actionPerformed(ActionEvent e) {
	    closeMessageUI();
	}
    }

    class DetachAction extends AbstractAction {
	DetachAction() {
	    super("window-detach");
	}

	public void actionPerformed(ActionEvent e) {
	    detachWindow();
	}
    }
}





