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

public class MessageWindow extends JInternalFrame implements UserProfileContainer, ItemListener {

    public static int HEADERS_DEFAULT = 0;
    public static int HEADERS_FULL = 1;

    MessagePanel parentContainer;

    MessageProxy msg;
    JSplitPane splitPane = null;
    AttachmentPane attachmentPanel = null;
    JComponent bodyPanel = null;
    int headerStyle = MessageWindow.HEADERS_DEFAULT;
    boolean editable = false;
    boolean showFullHeaders = false;
    JEditorPane editorPane = null;
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

	configureMessageWindow();
    }
    
    protected void configureMessageWindow() {

	this.getContentPane().setLayout(new BorderLayout());

	try {
	    this.setTitle(msg.getMessage().getSubject());
	} catch (MessagingException me) {
	    this.setTitle(Pooka.getProperty("Pooka.messageWindow.messageTitle.noSubject", "<no subject>"));
	}

	toolbar = new ConfigurableToolbar("MessageWindowToolbar", Pooka.getResources());
	
	toolbar.setActive(this.getActions());
	this.getContentPane().add("North", toolbar);

	editorPane = createMessagePanel(msg);

	if (!getMessageProxy().hasLoadedAttachments())
	    getMessageProxy().loadAttachmentInfo();

	if (getMessageProxy().getAttachments() != null && getMessageProxy().getAttachments().size() > 0) {
	    attachmentPanel = new AttachmentPane(msg);
	    attachmentScrollPane = new JScrollPane(attachmentPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

	    splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
	    
	    splitPane.setTopComponent(new JScrollPane(editorPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
	    splitPane.setBottomComponent(attachmentScrollPane);
	    splitPane.resetToPreferredSizes();
	    this.getContentPane().add("Center", splitPane);
	} else {
	    this.getContentPane().add("Center", new JScrollPane(editorPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
	}
	
	this.setSize(Integer.parseInt(Pooka.getProperty("MessageWindow.hsize", "500")), Integer.parseInt(Pooka.getProperty("MessageWindow.vsize", "500")));
	
	msg.setMessageWindow(this);

	this.addInternalFrameListener(new InternalFrameAdapter() {
		public void internalFrameClosed(InternalFrameEvent e) {
		    if (getMessageProxy().getMessageWindow() == MessageWindow.this)
			getMessageProxy().setMessageWindow(null);
		}
	    });
    }

    public void closeMessageWindow() {
	try {
	    this.setClosed(true);
	} catch (java.beans.PropertyVetoException e) {
	}
    }

    /**
     * as defined in java.awt.event.ItemListener
     *
     * This implementation calls a refreshCurrentUser() on the MainPanel.
     */

    public void itemStateChanged(ItemEvent ie) {
	getParentContainer().getMainPanel().refreshCurrentUser();
    }


    /**
     * This method creates the component that will display the message
     * itself.
     *
     * It returns a JEditorPane with the headers and the message body
     * together.
     */

    public JEditorPane createMessagePanel(MessageProxy aMsg) {
	editorPane = new JEditorPane();
	StringBuffer messageText = new StringBuffer();
	
	if (aMsg.getMessage() instanceof javax.mail.internet.MimeMessage) {
	    javax.mail.internet.MimeMessage mMsg = (javax.mail.internet.MimeMessage) aMsg.getMessage();

	    // first do the headers.

	    if (showFullHeaders()) {
	    }
	    else {
		StringTokenizer tokens = new StringTokenizer(Pooka.getProperty("MessageWindow.Header.DefaultHeaders", "From:To:CC:Date:Subject"), ":");
		String hdrLabel,currentHeader = null;
		String[] hdrValue = null;
		
		while (tokens.hasMoreTokens()) {
		    currentHeader=tokens.nextToken();
		    hdrLabel = Pooka.getProperty("MessageWindow.Header." + currentHeader + ".label", currentHeader);
		    try {
			hdrValue = mMsg.getHeader(Pooka.getProperty("MessageWindow.Header." + currentHeader + ".MIMEHeader", currentHeader));
		    } catch (MessagingException me) {
			hdrValue = null;
		    }
		    
		    if (hdrValue != null && hdrValue.length > 0) {
			messageText.append(hdrLabel + ":  ");
			for (int i = 0; i < hdrValue.length; i++) {
			    messageText.append(hdrValue[i]);
			    if (i != hdrValue.length -1) 
				messageText.append(", ");
			}
			messageText.append("\n");
		    }
		}
		String separator = Pooka.getProperty("MessageWindow.separator", "");
		if (separator.equals(""))
		    messageText.append("\n\n");
		else
		    messageText.append(separator);
	    }

	    // then do the content
	    String content = net.suberic.pooka.MailUtilities.getTextPart(mMsg);
	    if (content != null) {
		messageText.append(content);
		editorPane.setEditable(false);
		editorPane.setText(messageText.toString());
	    } 
	 
	    return editorPane;

	} else
	    return new JEditorPane();
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

    public UserProfile getDefaultProfile() {
	return getMessageProxy().getDefaultProfile();
    }

    public String getMessageText() {
	return getEditorPane().getText();
    }

    public String getMessageContentType() {
	return getEditorPane().getContentType();
    }

    public boolean showFullHeaders() {
	return showFullHeaders;
    }

    public JEditorPane getEditorPane() {
	return editorPane;
    }

    public MessageProxy getMessageProxy() {
	return msg;
    }

    public void setMessageProxy(MessageProxy newValue) {
	msg = newValue;
    }

    public AttachmentPane getAttachmentPanel() {
	return attachmentPanel;
    }

    public MessagePanel getParentContainer() {
	return parentContainer;
    }

    //------- Actions ----------//

    /**
     * performTextAction grabs the focused component on the MessageWindow
     * and, if it is a JTextComponent, tries to get it to perform the
     * appropriate ActionEvent.
     */
    public void performTextAction(String name, ActionEvent e) {
	Action[] textActions;

	Component focusedComponent = getFocusedComponent(this);

	// this is going to suck more.

	if (focusedComponent != null) {
	    if (focusedComponent instanceof JTextComponent) {
		JTextComponent fTextComp = (JTextComponent) focusedComponent;
		textActions = fTextComp.getActions();
		Action selectedAction = null;
		for (int i = 0; (selectedAction == null) && i < textActions.length; i++) {
		    if (textActions[i].getValue(Action.NAME).equals(name))
			selectedAction = textActions[i];
		}
		
		if (selectedAction != null) {
		    selectedAction.actionPerformed(e);
		}
	    }
	}
    }

    private Component getFocusedComponent(Container container) {
	Component[] componentList = container.getComponents();
	
	Component focusedComponent = null;
	
	// this is going to suck.
	
	for (int i = 0; (focusedComponent == null) && i < componentList.length; i++) {
	    if (componentList[i].hasFocus())
		focusedComponent = componentList[i];
	    else if (componentList[i] instanceof Container) 
		focusedComponent=getFocusedComponent((Container)componentList[i]);
	    
	}
	
	return focusedComponent;
	
    }	

    public Action[] getActions() {
	if (msg.getActions() != null) {
	    return TextAction.augmentList(msg.getActions(), getDefaultActions());
	} else 
	    return getDefaultActions();

	/*
	  if (getSelectedField() != null) 
	  return TextAction.augmentList(getSelectedField().getActions(), getDefaultActions());
	  else 
	*/
    }

    public Action[] getDefaultActions() {
	return defaultActions;
    }

    //-----------actions----------------

    // The actions supported by the window itself.

    public Action[] defaultActions = {
	new CloseAction(),
	new CopyAction()
    };

    class CloseAction extends AbstractAction {

	CloseAction() {
	    super("file-close");
	}
	
        public void actionPerformed(ActionEvent e) {
	    closeMessageWindow();
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

}





