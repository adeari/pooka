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

public class MessageWindow extends JInternalFrame implements UserProfileContainer {

    public static int HEADERS_DEFAULT = 0;
    public static int HEADERS_FULL = 1;

    MessagePanel parentContainer;

    MessageProxy msg;
    JSplitPane splitPane = null;
    JTabbedPane tabbedPane = null;
    Container headerPanel = null;
    AttachmentPane attachmentPanel = null;
    JComponent bodyPanel = null;
    int headerStyle = MessageWindow.HEADERS_DEFAULT;
    boolean editable = false;
    boolean showFullHeaders = false;
    boolean modified = false;
    Hashtable inputTable = null;
    JEditorPane editorPane = null;
    ConfigurableToolbar toolbar;
    boolean hasAttachment = false;

    //<sigh>
    JScrollPane attachmentScrollPane, headerScrollPane;

    /**
     * Creates a MessageWindow from the given Message.
     */

    public MessageWindow(MessagePanel newParentContainer, MessageProxy newMsgProxy, boolean isEditable) {
	super(Pooka.getProperty("Pooka.messageWindow.messageTitle.newMessage", "New Message"), true, true, true, true);

	parentContainer = newParentContainer;
	editable = isEditable;
	if (isEditable()) 
	    inputTable = new Hashtable();
	this.getContentPane().setLayout(new BorderLayout());

	msg=newMsgProxy;
	if (editable) {
	    this.setModified(true);
	    this.setTitle(Pooka.getProperty("Pooka.messageWindow.messageTitle.newMessage", "New Message"));
	    toolbar = new ConfigurableToolbar("NewMessageWindowToolbar", Pooka.getResources());
	} else {
	    try {
		this.setTitle(msg.getMessage().getSubject());
	    } catch (MessagingException me) {
		this.setTitle(Pooka.getProperty("Pooka.messageWindow.messageTitle.noSubject", "<no subject>"));
	    }
	    toolbar = new ConfigurableToolbar("MessageWindowToolbar", Pooka.getResources());
	}
	
	toolbar.setActive(this.getActions());
	this.getContentPane().add("North", toolbar);

	splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
	tabbedPane = new JTabbedPane();
	
	headerPanel = createHeaderPanel(msg);
	bodyPanel = createBodyPanel(msg);

	headerPanel.setSize(Integer.parseInt(Pooka.getProperty("MessageWindow.hsize", "300")), Integer.parseInt(Pooka.getProperty("MessageWindow.headerPanel.vsize", "200")));
	//	headerPanel.setPreferredSize(new Dimension(Integer.parseInt(Pooka.getProperty("MessageWindow.hsize", "300")), Integer.parseInt(Pooka.getProperty("MessageWindow.headerPanel.vsize", "200"))));
	headerScrollPane = new JScrollPane(headerPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	headerScrollPane.setSize(Integer.parseInt(Pooka.getProperty("MessageWindow.hsize", "300")), Integer.parseInt(Pooka.getProperty("MessageWindow.headerPanel.vsize", "200")));
	headerScrollPane.setPreferredSize(new Dimension(Integer.parseInt(Pooka.getProperty("MessageWindow.hsize", "300")), Integer.parseInt(Pooka.getProperty("MessageWindow.headerPanel.vsize", "200"))));
	tabbedPane.setSize(Integer.parseInt(Pooka.getProperty("MessageWindow.hsize", "300")), Integer.parseInt(Pooka.getProperty("MessageWindow.headerPanel.vsize", "200")));
	tabbedPane.setPreferredSize(new Dimension(Integer.parseInt(Pooka.getProperty("MessageWindow.hsize", "300")), Integer.parseInt(Pooka.getProperty("MessageWindow.headerPanel.vsize", "200"))));
	tabbedPane.add(Pooka.getProperty("MessageWindow.HeaderTab", "Headers"), headerScrollPane);

	if (!getMessageProxy().hasLoadedAttachments())
	    getMessageProxy().loadAttachmentInfo();

	if (getMessageProxy().getAttachments() != null && getMessageProxy().getAttachments().size() > 0)
	    addAttachmentPane();
	
	splitPane.setTopComponent(tabbedPane);
	splitPane.setBottomComponent(bodyPanel);
	this.getContentPane().add("Center", splitPane);
	
	this.setSize(Integer.parseInt(Pooka.getProperty("MessageWindow.hsize", "500")), Integer.parseInt(Pooka.getProperty("MessageWindow.vsize", "500")));
	
	newMsgProxy.setMessageWindow(this);

	this.addInternalFrameListener(new InternalFrameAdapter() {
		public void internalFrameClosed(InternalFrameEvent e) {
		    if (getMessageProxy().getMessageWindow() == MessageWindow.this)
			getMessageProxy().setMessageWindow(null);
		}
	    });
	
    }

    public void closeMessageWindow() {
	
	if (isModified()) {
	    int saveDraft = showConfirmDialog(Pooka.getProperty("error.saveDraft.message", "This message has unsaved changes.  Would you like to save a draft copy?"), Pooka.getProperty("error.saveDraft.title", "Save Draft"), JOptionPane.YES_NO_CANCEL_OPTION);
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
	if (isEditable()) {
	    return createHeaderInputPanel(aMsg, inputTable);
	} else {
	    return createHeaderTextField(aMsg);
	}
    }
	

    public JTextArea createHeaderTextField(MessageProxy aMsg) {
	MimeMessage mMsg = (MimeMessage)aMsg.getMessage();
	
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
	
	UserProfile selectedProfile = getParentContainer().getMainPanel().getCurrentUser();
	if (selectedProfile != null)
	    System.out.println("MessageWindow:  got default Profile " + selectedProfile.getName());
	else
	    System.out.println("MessageWindow:  got default Profile null.");

	profileCombo.setSelectedItem(selectedProfile);
	//	profileCombo.setSelectedItem(getParentContainer().getMainPanel().getDefaultProfile());
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

    public JComponent createBodyPanel(MessageProxy aMsg) {
	editorPane = new JEditorPane();
	
	if (isEditable()) {
	    
	    // see if this message already has a text part, and if so,
	    // include it.
	    
	    String origText = net.suberic.pooka.MailUtilities.getTextPart(aMsg.getMessage());
	    if (origText != null && origText.length() > 0) 
		editorPane.setText(origText);
	    
	    //	    bodyInputPane.setContentType("text");
	    editorPane.setSize(Integer.parseInt(Pooka.getProperty("MessageWindow.hsize", "500")), Integer.parseInt(Pooka.getProperty("MessageWindow.editorPane.vsize", "300")));	    
	    editorPane.setPreferredSize(new Dimension(Integer.parseInt(Pooka.getProperty("MessageWindow.hsize", "500")), Integer.parseInt(Pooka.getProperty("MessageWindow.editorPane.vsize", "300"))));	    
	    return new JScrollPane(editorPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	} else {
	    if (aMsg.getMessage() instanceof javax.mail.internet.MimeMessage) {
		javax.mail.internet.MimeMessage mMsg = (javax.mail.internet.MimeMessage) aMsg.getMessage();
		String content = net.suberic.pooka.MailUtilities.getTextPart(mMsg);
		if (content != null) {
		    editorPane.setEditable(false);
		    editorPane.setText(content);
		    editorPane.setSize(Integer.parseInt(Pooka.getProperty("MessageWindow.hsize", "500")), Integer.parseInt(Pooka.getProperty("MessageWindow.editorPane.vsize", "300")));	    
		    editorPane.setPreferredSize(new Dimension(Integer.parseInt(Pooka.getProperty("MessageWindow.hsize", "500")), Integer.parseInt(Pooka.getProperty("MessageWindow.editorPane.vsize", "300"))));	    
		    return new JScrollPane(editorPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		} else { 
		    
		    /* nothing found.  return a blank TextArea. */
		    
		    return new JScrollPane(new JTextArea(), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		}
	    } else 
		return null;
	}
    }

    /**
     * This will populate a Message with the values entered in the 
     * MessageWindow.
     */

    public URLName populateMessageHeaders(Message m) throws MessagingException {
	if (m instanceof MimeMessage) {
	    MimeMessage mMsg = (MimeMessage)m;
	    String key;
	    URLName urlName = null;
	    
	    Enumeration keys = inputTable.keys();
	    while (keys.hasMoreElements()) {
		key = (String)(keys.nextElement());

		if (key.equals("UserProfile")) {
		    UserProfile up = (UserProfile)(((JComboBox)(inputTable.get(key))).getSelectedItem());
		    up.populateMessage(mMsg);
		    urlName = new URLName(up.getMailProperties().getProperty("sendMailURL", "smtp://localhost/"));
		} else {
		    String header = new String(Pooka.getProperty("MessageWindow.Header." + key + ".MIMEHeader", key));
		    String value = ((JTextField)(inputTable.get(key))).getText();
		    mMsg.setHeader(header, value);
		}
	    }
	    return urlName;
	}
	return null;
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
	System.out.println("filechooser returned " + a + ".  approve-option = " + JFileChooser.APPROVE_OPTION + ".  The file selected (singular) was " + jfc.getSelectedFile() + ".  The length of the file array was " + jfc.getSelectedFiles().length);

	if (a == JFileChooser.APPROVE_OPTION)
	    return new File[] {jfc.getSelectedFile()};
	else
	    return null;
    }
    /**
     * This notifies the MessageWindow that an attachment has been added
     * at the provided index.  This does not actually add an attachment,
     * but rather should be called by the MessageProxy when an attachment
     * has been added.
     *
     * If an AttachmentPane does not currently exist for this MessageWindow,
     * this method will call addAttachmentPane() to create one.
     */
    public void attachmentAdded(int index) {
	if (getAttachmentPanel() == null)
	    addAttachmentPane();
	else
	    getAttachmentPanel().getTableModel().fireTableRowsInserted(index, index);
    }

    /**
     * This notifies the MessageWindow that the attachment at the 
     * provided index has been removed.  This does not actually remove
     * the attachment, but rather should be called by the MessageProxy
     * when an attachment has been removed.
     *
     * If this removes the last attachment, the entire AttachmentPane
     * is removed from the MessageWindow.
     */
    public void attachmentRemoved(int index) {
	Vector attach = getMessageProxy().getAttachments();
	if (attach == null || attach.size() == 0) {
	    removeAttachmentPane();
	} else {
	    getAttachmentPanel().getTableModel().fireTableRowsDeleted(index, index);
	}
    }

    /**
     * This creates the JComponent which shows the attachments, and then
     * adds it to the JTabbedPane.
     *
     */
    public void addAttachmentPane() {
	attachmentPanel = new AttachmentPane(getMessageProxy());
	attachmentScrollPane = new JScrollPane(attachmentPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	attachmentScrollPane.setSize(Integer.parseInt(Pooka.getProperty("MessageWindow.hsize", "300")), Integer.parseInt(Pooka.getProperty("MessageWindow.headerPanel.vsize", "200")));
	attachmentScrollPane.setPreferredSize(new Dimension(Integer.parseInt(Pooka.getProperty("MessageWindow.hsize", "300")), Integer.parseInt(Pooka.getProperty("MessageWindow.headerPanel.vsize", "200"))));
	tabbedPane.add(Pooka.getProperty("MessageWindow.AttachmentTab", "Attachments"), attachmentScrollPane);
    }

    /**
     * This removes the AttachmentPane from the JTabbedPane.
     */

    public void removeAttachmentPane() {
	if (attachmentPanel != null) {
	    tabbedPane.setSelectedComponent(headerScrollPane);
	    tabbedPane.remove(attachmentScrollPane);
	}
	attachmentPanel = null;
	attachmentScrollPane=null;
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
	return (UserProfile)(((JComboBox)(inputTable.get("UserProfile"))).getSelectedItem());
    }

    public String getMessageText() {
	return getEditorPane().getText();
    }

    public String getMessageContentType() {
	return getEditorPane().getContentType();
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
	new CutAction(),
	new CopyAction(),
	new PasteAction()
    };

    class CloseAction extends AbstractAction {

	CloseAction() {
	    super("file-close");
	}
	
        public void actionPerformed(ActionEvent e) {
	    closeMessageWindow();
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
}





