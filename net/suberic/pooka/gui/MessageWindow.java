package net.suberic.pooka.gui;
import net.suberic.pooka.*;
import net.suberic.util.gui.ConfigurableToolbar;
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

public abstract class MessageWindow extends JInternalFrame implements UserProfileContainer {

    MessagePanel parentContainer;

    MessageProxy msg;
    JSplitPane splitPane = null;
    AttachmentPane attachmentPanel = null;
    JTextPane editorPane = null;
    ConfigurableToolbar toolbar;
    boolean hasAttachment = false;

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

	msg.setMessageWindow(this);
	
	this.addInternalFrameListener(new InternalFrameAdapter() {
		public void internalFrameClosed(InternalFrameEvent e) {
		    if (getMessageProxy().getMessageWindow() == MessageWindow.this)
			getMessageProxy().setMessageWindow(null);
		}
	    });
	
    }
    
    /**
     * this method is expected to do all the implementation-specific
     * duties, like setting the editorPane, etc.
     */

    protected abstract void configureMessageWindow();

    public void closeMessageWindow() {
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
	return JOptionPane.showInternalConfirmDialog(this.getDesktopPane(), messageText, title, type);
    }

    /**
     * This shows an Error Message window.  We include this so that
     * the MessageProxy can call the method without caring abou the
     * actual implementation of the Dialog.
     */
    public void showError(String errorMessage, String title) {
	JOptionPane.showInternalMessageDialog(this.getDesktopPane(), errorMessage, title, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * This shows an Error Message window.  We include this so that
     * the MessageProxy can call the method without caring abou the
     * actual implementation of the Dialog.
     */
    public void showError(String errorMessage, String title, Exception e) {
	showError(errorMessage + e.getMessage(), title);
    }

    /**
     * As specified by interface net.suberic.pooka.UserProfileContainer.
     *
     * This implementation returns the DefaultProfile of the associated
     * MessageProxy if the MessageWindow is not editable.  If the 
     * MessageWindow is editable, it returns the currently selected 
     * UserProfile object.
     */

    /**
     * This method sets the size of the MessageWindow to the default size.
     * In this case, the default size is taken from the MessageWindow.vsize
     * property for the vertical size.  For the horizontal size, if
     * MessageWindow.hsizeByCharLength is set to true, this method uses the
     * MessageWindow.charLength property along with the configured font
     * to determine the size of the window.  If this is not set, or set
     * to false, then the MessageWindow.hsize property is used instead.
     */
    public Dimension getDefaultMessageSize(Font currentFont) {
	int hsize = 500;
	int vsize = 500;
	
	try {
	    vsize = Integer.parseInt(Pooka.getProperty("MessageWindow.vsize", "500"));
	} catch (NumberFormatException nfe) {
	    vsize=500;
	}
	
	try {
	    if (Pooka.getProperty("MessageWindow.hsizeByCharLength", "false").equalsIgnoreCase("true")) {
		int charLength = Integer.parseInt(Pooka.getProperty("MessageWindow.charLength", "80"));
		if (currentFont != null) {
		    FontMetrics fm = this.getFontMetrics(currentFont);

		    int[] firstWidths = fm.getWidths();
		    int accumulator = 0;
		    for (int i = 0; i < 256; i++)
			accumulator+=firstWidths[i];
		    hsize = accumulator / 256 * 80;
		}
	    } else {
		hsize = Integer.parseInt(Pooka.getProperty("MessageWindow.hsize", "500"));
	    }
	} catch (NumberFormatException nfe) {
	    hsize=500;
	}
	
	return new Dimension(hsize, vsize);

    }


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
	    closeMessageWindow();
	}
    }

}





