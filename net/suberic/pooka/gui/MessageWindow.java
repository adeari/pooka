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

public abstract class MessageWindow extends JInternalFrame implements MessageUI {

    protected MessagePanel parentContainer;

    protected MessageProxy msg;
    protected JSplitPane splitPane = null;
    protected AttachmentPane attachmentPanel = null;
    protected JTextPane editorPane = null;
    protected JScrollPane editorScrollPane = null;
    protected ConfigurableToolbar toolbar;
    protected ConfigurableKeyBinding keyBindings;
    protected boolean hasAttachment = false;
    protected boolean addedToDesktop = false;

    //<sigh>
    JScrollPane attachmentScrollPane;

    /**
     * Creates a MessageWindow from the given Message.
     */

    public MessageWindow(MessagePanel newParentContainer, MessageProxy newMsgProxy) {
	super(Pooka.getProperty("Pooka.messageWindow.messageTitle.newMessage", "New Message"), true, true, true, true);

	parentContainer = newParentContainer;
	msg=newMsgProxy;

	this.getContentPane().setLayout(new BorderLayout());

	msg.setMessageUI(this);
	
	this.addInternalFrameListener(new InternalFrameAdapter() {
		public void internalFrameClosed(InternalFrameEvent e) {
		    if (getMessageProxy().getMessageUI() == MessageWindow.this)
			getMessageProxy().setMessageUI(null);
		}
	    });
	
    }
    
    /**
     * this method is expected to do all the implementation-specific
     * duties, like setting the editorPane, etc.
     */

    protected abstract void configureMessageWindow();

    /**
     * This opens the MessageWindow by calling 
     * getParentContainer().openMessageWindow(getMessageProxy());
     */
    public void openMessageUI() {
	
	getParentContainer().openMessageWindow(getMessageProxy(), !addedToDesktop);
	addedToDesktop = true;
    }

    /**
     * This closes the MessageWindow.
     */
    public void closeMessageUI() {
	try {
	    this.setClosed(true);
	} catch (java.beans.PropertyVetoException e) {
	}
    }


    /**
     * This shows an Confirm Dialog window.  We include this so that
     * the MessageProxy can call the method without caring abou the
     * actual implementation of the Dialog.
     */    
    public int showConfirmDialog(String messageText, String title, int type) {
	return JOptionPane.showInternalConfirmDialog(Pooka.getMainPanel().getMessagePanel(), messageText, title, type);
    }

    /**
     * This shows an Confirm Dialog window.  We include this so that
     * the MessageProxy can call the method without caring abou the
     * actual implementation of the Dialog.
     */    
    public int showConfirmDialog(String messageText, String title, int optionType, int iconType) {
	return JOptionPane.showInternalConfirmDialog(Pooka.getMainPanel().getMessagePanel(), messageText, title, optionType, iconType);
    }

    /**
     * This shows an Error Message window.  We include this so that
     * the MessageProxy can call the method without caring abou the
     * actual implementation of the Dialog.
     */
    public void showError(String errorMessage, String title) {
	JOptionPane.showInternalMessageDialog(Pooka.getMainPanel().getMessagePanel(), errorMessage, title, JOptionPane.ERROR_MESSAGE);
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
     * the MessageProxy can call the method without caring about the
     * actual implementation of the Dialog.
     */
    public void showError(String errorMessage, String title, Exception e) {
	showError(errorMessage + e.getMessage(), title);
	e.printStackTrace();
    }

    /**
     * This shows an Input window.  We include this so that the 
     * MessageProxy can call the method without caring about the actual
     * implementation of the dialog.
     */
    public String showInputDialog(String inputMessage, String title) {
	return JOptionPane.showInternalInputDialog(Pooka.getMainPanel().getMessagePanel(), inputMessage, title, JOptionPane.QUESTION_MESSAGE);
    }

    /**
     * This shows an Input window.  We include this so that the 
     * MessageProxy can call the method without caring about the actual
     * implementation of the dialog.
     */
    public String showInputDialog(Object[] inputPanes, String title) {
	return JOptionPane.showInternalInputDialog(Pooka.getMainPanel().getMessagePanel(), inputPanes, title, JOptionPane.QUESTION_MESSAGE);
    }

    /**
     * This sets the default font for the editorPane to a font determined
     * by the MessageWindow.editorPane.font (.name and .size) properties.
     * 
     * I believe that if the font cannot be found or instantiated, 
     * nothing should happen, but i'm not sure.  :)
     */
    public void setDefaultFont(JEditorPane jep) {
	String fontName = Pooka.getProperty("MessageWindow.editorPane.font.name", "monospaced");
	int fontSize = Integer.parseInt(Pooka.getProperty("MessageWindow.editorPane.font.size", "10"));

	Font f = new Font(fontName, Font.PLAIN, fontSize);
	if (f != null)
	    jep.setFont(f);
    }

    /**
     * This calculates the default size for the EditorPane.
     * 
     * Here, we use the MessageWindow.editorPane.* properties to determine
     * the size.  Specifically, we check for the hsizeByCharLength
     * property.  If this is set to true, then we dynamically determine
     * the appropriate width using the current font of the editorPane 
     * along with the charLength property.  

     * If hsizeByCharLength is set to false, or if for whatever reason we 
     * find that we're unable to determine an appropriate size, then we just 
     * use the vsize and hsize properties.
     */
    public Dimension getDefaultEditorPaneSize() {
	int hsize = 500;
	int vsize = 500;
	
	try {
	    vsize = Integer.parseInt(Pooka.getProperty("MessageWindow.editorPane.vsize", "500"));
	} catch (NumberFormatException nfe) {
	    vsize=500;
	}
	
	try {
	    if (Pooka.getProperty("MessageWindow.editorPane.hsizeByCharLength", "false").equalsIgnoreCase("true")) {
		int charLength = Integer.parseInt(Pooka.getProperty("MessageWindow.editorPane.charLength", "80"));
		Font currentFont = editorPane.getFont();
		if (currentFont != null) {
		    FontMetrics fm = this.getFontMetrics(currentFont);
		    hsize = (int)(charLength * fm.getStringBounds("Remember when you were young?  You shone like the sun.  Shine on you crazy diamo", editorPane.getGraphics()).getWidth() / 80);
		}
	    } else {
		hsize = Integer.parseInt(Pooka.getProperty("MessageWindow.editorPane.hsize", "500"));
	    }
	} catch (NumberFormatException nfe) {
	    hsize=500;
	}

	Dimension retval = new Dimension(hsize, vsize);
	return retval;
    }

    /**
     * A convenience method to set the PreferredSize and Size of the
     * component to that of the current preferred width.
     */
    public void resizeByWidth() {
	int width = (int)editorScrollPane.getPreferredSize().getWidth();
	this.setPreferredSize(new Dimension(width, width));
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
     * MessageProxy if the MessageWindow is not editable.  If the 
     * MessageWindow is editable, it returns the currently selected 
     * UserProfile object.
     */

    public UserProfile getDefaultProfile() {
	return getMessageProxy().getDefaultProfile();
    }

    public JTextPane getEditorPane() {
	return editorPane;
    }

    public MessageProxy getMessageProxy() {
	return msg;
    }

    public void setMessageProxy(MessageProxy newValue) {
	msg = newValue;
    }

    public String getMessageText() {
	return getEditorPane().getText();
    }

    public String getMessageContentType() {
	return getEditorPane().getContentType();
    }

    public AttachmentPane getAttachmentPanel() {
	return attachmentPanel;
    }

    public MessagePanel getParentContainer() {
	return parentContainer;
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
	new CloseAction()
    };

    class CloseAction extends AbstractAction {

	CloseAction() {
	    super("file-close");
	}
	
        public void actionPerformed(ActionEvent e) {
	    closeMessageUI();
	}
    }

}





