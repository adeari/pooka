package net.suberic.pooka.gui;
import net.suberic.pooka.Pooka;
import net.suberic.pooka.UserProfile;
import javax.mail.*;
import javax.mail.internet.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.TextAction;
import java.util.*;

public class MessageWindow extends JInternalFrame {

    public static int HEADERS_DEFAULT = 0;
    public static int HEADERS_FULL = 1;

    MessageProxy msg;
    JSplitPane splitPane = null;
    JComponent headerPanel = null;
    JComponent bodyPanel = null;
    int headerStyle = MessageWindow.HEADERS_DEFAULT;
    boolean editable = false;
    boolean showFullHeaders = false;
    boolean modified = false;
    Hashtable inputTable = null;
    JEditorPane editorPane = null;

    /**
     * Creates a MessageWindow from the given Message.
     */

    public MessageWindow(MessageProxy newMsgProxy, boolean isEditable) {
	super(Pooka.getProperty("Pooka.messageWindow.messageTitle.newMessage", "New Message"), true, true, true, true);

	editable = isEditable;
	if (isEditable()) 
	    inputTable = new Hashtable();

	msg=newMsgProxy;
	if (editable) {
	    this.setModified(true);
	    this.setTitle(Pooka.getProperty("Pooka.messageWindow.messageTitle.newMessage", "New Message"));
	} else 
	    try {
		this.setTitle(msg.getMessage().getSubject());
	    } catch (MessagingException me) {
		this.setTitle(Pooka.getProperty("Pooka.messageWindow.messageTitle.noSubject", "<no subject>"));
	    }
	
	splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
	
	splitPane.setTopComponent(createHeaderPanel(msg));
	splitPane.setBottomComponent(createBodyPanel(msg));
	this.getContentPane().add(splitPane);
	
	this.setSize(Integer.parseInt(Pooka.getProperty("MessageWindow.hsize", "300")), Integer.parseInt(Pooka.getProperty("MessageWindow.vsize", "200")));
	
    }

    /**
     * Create a New Message Window


    public MessageWindow(Session thisSession) {
	super(Pooka.getProperty("Pooka.messageWindow.messageTitle.newMessage", "New Message"), true, true, true, true);
	
	editable=true;
	
	try {
	} catch (MessagingException me) {
	}

	splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
	
	splitPane.setTopComponent(createHeaderPanel(msg));
	splitPane.setBottomComponent(createBodyPanel(msg));
	this.getContentPane().add(splitPane);
    }
	
    */		 


    public void closeMessageWindow() {
	
	if (isModified()) {
	    int saveDraft = JOptionPane.showInternalConfirmDialog(this.getDesktopPane(), Pooka.getProperty("error.saveDraft.message", "This message has unsaved changes.  Would you like to save a draft copy?"), Pooka.getProperty("error.saveDraft.title", "Save Draft"), JOptionPane.YES_NO_CANCEL_OPTION);
	    switch (saveDraft) {
	    case JOptionPane.YES_OPTION:
		//this.saveDraft();
	    case JOptionPane.NO_OPTION:
		try {
		    this.setClosed(true);
		} catch (java.beans.PropertyVetoException e) {
		}
	    default:
		return;
	    }
	} else {
	    try {
		this.setClosed(true);
	    } catch (java.beans.PropertyVetoException e) {
	    }
	}
    }

    public Container createHeaderPanel(MessageProxy aMsg) {
	/*
	 * This is interesting.  We need to deal with several possibilities:
	 *
	 * 1)  Simple (non-editable) with just basic headers
	 * 2)  Simple with full headers
	 * 3)  Simple with attachments and both header sets
	 * 4)  Editable with just UserProfile
	 * 5)  Editable with normal headers
	 * 6)  Editable with full headers
	 */

	if (isEditable()) {
	    return createHeaderInputPanel(aMsg, inputTable);
	} else if (aMsg.getMessage() instanceof MimeMessage) {
	    MimeMessage mMsg = (MimeMessage)aMsg.getMessage();

	    boolean multiTest = false;
	    
	    try {
		multiTest = (mMsg.getContent() instanceof Multipart);
	    } catch (Exception e) {
	    }

	    if (multiTest) {
		JSplitPane hdrSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		hdrSplitPane.setTopComponent(createHeaderTextField(mMsg));
		hdrSplitPane.setBottomComponent(createAttachmentPanel(mMsg));
		return hdrSplitPane;
	    } else {
		return createHeaderTextField(mMsg);
	    }
	}

	//shouldn't happen.
	return null;
    }
	

    public JTextArea createHeaderTextField(MimeMessage mMsg) {
	JTextArea headerArea = new JTextArea();

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
		    headerArea.append(hdrLabel + ":  ");
		    for (int i = 0; i < hdrValue.length; i++) {
			headerArea.append(hdrValue[i]);
			if (i != hdrValue.length -1) 
			    headerArea.append(", ");
		    }
		    headerArea.append("\n");
		}
	    }
	}
	return headerArea;
    }
		    

    public Container createHeaderInputPanel(MessageProxy aMsg, Hashtable proptDict) {
	
	Box inputPanel = new Box(BoxLayout.Y_AXIS);

	Box inputRow = new Box(BoxLayout.X_AXIS);

	// Create UserProfile DropDown
	JLabel userProfileLabel = new JLabel(Pooka.getProperty("UserProfile.label","User:"), SwingConstants.RIGHT);
	userProfileLabel.setPreferredSize(new Dimension(75,userProfileLabel.getPreferredSize().height));
	JComboBox profileCombo = new JComboBox(UserProfile.getProfileList());
	inputRow.add(userProfileLabel);
	inputRow.add(profileCombo);
	
	profileCombo.setSelectedItem(UserProfile.getDefaultProfile(aMsg.getMessage()));
	proptDict.put("UserProfile", profileCombo);

	inputPanel.add(inputRow);
	
	// Create Address panel

	StringTokenizer tokens = new StringTokenizer(Pooka.getProperty("MessageWindow.Input.DefaultFields", "To:CC:BCC:Subject"), ":");
	String currentHeader = null;
	JLabel hdrLabel = null;
	JTextField inputField = null;

	while (tokens.hasMoreTokens()) {
	    inputRow = new Box(BoxLayout.X_AXIS);
	    currentHeader=tokens.nextToken();
	    hdrLabel = new JLabel(Pooka.getProperty("MessageWindow.Input.." + currentHeader + ".label", currentHeader) + ":", SwingConstants.RIGHT);
	    hdrLabel.setPreferredSize(new Dimension(75,hdrLabel.getPreferredSize().height));
	    inputRow.add(hdrLabel);

	    if (aMsg.getMessage() instanceof MimeMessage) {
		MimeMessage mMsg = (MimeMessage)aMsg.getMessage();
		try {
		    inputField = new JTextField(mMsg.getHeader(Pooka.getProperty("MessageWindow.Input." + currentHeader + ".MIMEHeader", "") , ","));
		} catch (MessagingException me) {
		    inputField = new JTextField();
		}
	    } else {
		inputField = new JTextField();
	    }
		inputRow.add(inputField);
	    
	    inputPanel.add(inputRow);

	    proptDict.put(Pooka.getProperty("MessageWindow.Input." + currentHeader + ".value", currentHeader), inputField);
	}

	return inputPanel;
    }

    /**
     * This returns the JComponent which shows the attachments.
     *
     * All it does now is returns a new AttachmentPane.
     */
    public JComponent createAttachmentPanel(MimeMessage mMsg) {
	return new AttachmentPane(mMsg);
    }

    public JComponent createBodyPanel(MessageProxy aMsg) {
	if (isEditable()) {
	    editorPane = new JEditorPane();
	    
	    // see if this message already has a text part, and if so,
	    // include it.

	    String origText = net.suberic.pooka.MailUtilities.getTextPart(aMsg.getMessage());
	    if (origText != null && origText.length() > 0) 
		editorPane.setText(origText);

	    //	    bodyInputPane.setContentType("text");
	    editorPane.setSize(200,300);
	    return new JScrollPane(editorPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	} else {
	    if (aMsg.getMessage() instanceof javax.mail.internet.MimeMessage) {
		javax.mail.internet.MimeMessage mMsg = (javax.mail.internet.MimeMessage) aMsg.getMessage();
		String content = net.suberic.pooka.MailUtilities.getTextPart(mMsg);
		if (content != null) {
		    JTextArea bodyPane = new JTextArea(content, 20, 40);
		    return new JScrollPane(bodyPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		} else { 
		    
		    /* nothing found.  return a blank TextArea. */
		    
		    return new JScrollPane(new JTextArea(), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		}
	    } else 
		return null;
	}
    }

    public void send() throws MessagingException {
	if (msg.getMessage() instanceof MimeMessage) {
	    MimeMessage mMsg = (MimeMessage)msg.getMessage();
	    String key;
	    URLName urlName = new URLName("smtp://localhost");

	    Enumeration keys = inputTable.keys();
	    while (keys.hasMoreElements()) {
		key = (String)(keys.nextElement());

		if (key.equals("UserProfile")) {
		    UserProfile up =  (UserProfile)(((JComboBox)(inputTable.get(key))).getSelectedItem());
		    up.populateMessage(mMsg);
		    urlName = new URLName(up.getMailProperties().getProperty("sendMailURL", "smtp://localhost/"));
		    System.out.println("set sendMailURL to " + urlName);
		} else {
		    String header = new String(Pooka.getProperty("MessageWindow.Header." + key + ".MIMEHeader", key));
		    String value = ((JTextField)(inputTable.get(key))).getText();
		    mMsg.setHeader(header, value);
		}
	    }
	
	    mMsg.setContent(getEditorPane().getText(), getEditorPane().getContentType());
	
	    ((MessagePanel)getDesktopPane()).getMainPanel().getMailQueue().sendMessage(mMsg, urlName);
	    this.setModified(false);
	    this.closeMessageWindow();
	}
    }


	
	
    public boolean isEditable() {
	return editable;
    }

    public boolean isModified() {
	return modified;
    }

    public void setModified(boolean mod) {
	if (isEditable())
	    modified=mod;
    }

    public boolean showFullHeaders() {
	return showFullHeaders;
    }

    public JEditorPane getEditorPane() {
	if (isEditable())
	    return editorPane;
	else
	    return null;
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
	new SendAction()
    };

    class CloseAction extends AbstractAction {

	CloseAction() {
	    super("file-close");
	}
	
        public void actionPerformed(ActionEvent e) {
	    closeMessageWindow();
	}
    }

    class SendAction extends AbstractAction {

	SendAction() {
	    super("message-send");
	}

	public void actionPerformed(ActionEvent e) {
	    try {
		send();
	    } catch (MessagingException me) {
		JOptionPane.showInternalMessageDialog(getDesktopPane(), Pooka.getProperty("error.MessageWindow.sendFailed", "Failed to send Message.") + "\n" + me.getMessage());
	    }
	}
    }

}





