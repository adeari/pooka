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

public class NewMessageWindow extends MessageWindow implements ItemListener {

    JTabbedPane tabbedPane = null;
    Container headerPanel = null;
    boolean modified = false;
    Hashtable inputTable;

    JScrollPane headerScrollPane;

    /**
     * Creates a NewMessageWindow from the given Message.
     */

    public NewMessageWindow(MessagePanel newParentContainer, MessageProxy newMsgProxy) {
	super(newParentContainer, newMsgProxy);

	configureMessageWindow();
    }

    /**
     * This configures the MessageWindow.  This means that here is where we
     * create the headerPanel and editorPane and add them to the splitPane.
     */
    protected void configureMessageWindow() {

	this.createDefaultActions();

	this.getContentPane().setLayout(new BorderLayout());
	
	this.setTitle(Pooka.getProperty("Pooka.messageWindow.messageTitle.newMessage", "New Message"));

	splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
	tabbedPane = new JTabbedPane();

	inputTable = new Hashtable();
	
	headerPanel = createHeaderInputPanel(msg, inputTable);
	editorPane = createMessagePanel(msg);

	msg.setMessageWindow(this);

	headerScrollPane = new JScrollPane(headerPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	tabbedPane.add(Pooka.getProperty("MessageWindow.HeaderTab", "Headers"), headerScrollPane);

	if (!getMessageProxy().hasLoadedAttachments())
	    getMessageProxy().loadAttachmentInfo();

	if (getMessageProxy().getAttachments() != null && getMessageProxy().getAttachments().size() > 0)
	    addAttachmentPane();
	
	editorScrollPane = new JScrollPane(editorPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

	splitPane.setTopComponent(tabbedPane);
	splitPane.setBottomComponent(editorScrollPane);

	this.getContentPane().add("Center", splitPane);
	
	toolbar = new ConfigurableToolbar("NewMessageWindowToolbar", Pooka.getResources());
	
	toolbar.setActive(this.getActions());
	this.getContentPane().add("North", toolbar);

	keyBindings = new ConfigurableKeyBinding(this, "NewMessageWindow.keyBindings", Pooka.getResources());
	keyBindings.setActive(getActions());

	this.addInternalFrameListener(new InternalFrameAdapter() {
		public void internalFrameClosed(InternalFrameEvent e) {
		    if (getMessageProxy().getMessageWindow() == NewMessageWindow.this)
			getMessageProxy().setMessageWindow(null);
		}
	    });

	editorPane.addMouseListener(new MouseAdapter() {
		
		public void mousePressed(MouseEvent e) {
		    if (SwingUtilities.isRightMouseButton(e)) {
			showPopupMenu(editorPane, e);
		    }
		}
	    });

	this.sizeWindow();
    }

    private void sizeWindow() {
	editorScrollPane.setPreferredSize(getDefaultEditorPaneSize());
	splitPane.resetToPreferredSizes();
	this.resizeByWidth();
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

    /**
     * as defined in java.awt.event.ItemListener
     *
     * This implementation calls a refreshCurrentUser() on the MainPanel.
     */

    public void itemStateChanged(ItemEvent ie) {
	getParentContainer().getMainPanel().refreshCurrentUser();
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
	    profileCombo.setSelectedItem(selectedProfile);

	profileCombo.addItemListener(this);
	
	proptDict.put("UserProfile", profileCombo);

	inputPanel.add(inputRow);
	
	// Create Address panel

	StringTokenizer tokens = new StringTokenizer(Pooka.getProperty("MessageWindow.Input.DefaultFields", "To:CC:BCC:Subject"), ":");
	String currentHeader = null;
	JLabel hdrLabel = null;
	JTextArea inputField = null;

	while (tokens.hasMoreTokens()) {
	    inputRow = new Box(BoxLayout.X_AXIS);
	    currentHeader=tokens.nextToken();
	    hdrLabel = new JLabel(Pooka.getProperty("MessageWindow.Input.." + currentHeader + ".label", currentHeader) + ":", SwingConstants.RIGHT);
	    hdrLabel.setPreferredSize(new Dimension(75,hdrLabel.getPreferredSize().height));
	    inputRow.add(hdrLabel);

	    if (aMsg.getMessage() instanceof MimeMessage) {
		MimeMessage mMsg = (MimeMessage)aMsg.getMessage();
		try {
		    inputField = new JTextArea(mMsg.getHeader(Pooka.getProperty("MessageWindow.Input." + currentHeader + ".MIMEHeader", "") , ","), 1, 30);
		} catch (MessagingException me) {
		    inputField = new JTextArea(1, 30);
		}
	    } else {
		inputField = new JTextArea(1, 30);
	    }
	    inputField.setLineWrap(true);
	    inputField.setWrapStyleWord(true);
	    //inputField.setBorder(new javax.swing.plaf.basic.BasicBorders.FieldBorder(Color.black, Color.black, Color.black, Color.black));
	    inputField.setBorder(BorderFactory.createEtchedBorder());
	    inputRow.add(inputField);
	    
	    inputPanel.add(inputRow);

	    proptDict.put(Pooka.getProperty("MessageWindow.Input." + currentHeader + ".value", currentHeader), inputField);
	}

	return inputPanel;
    }

    public JTextPane createMessagePanel(MessageProxy aMsg) {
	JTextPane retval = new JTextPane();
	retval.setEditorKit(new MailEditorKit());

	setDefaultFont(retval);

	// see if this message already has a text part, and if so,
	// include it.
	
	String origText = net.suberic.pooka.MailUtilities.getTextPart(aMsg.getMessage(), false);
	if (origText != null && origText.length() > 0) 
	    retval.setText(origText);
	
	// bodyInputPane.setContentType("text");
	return retval;

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

	if (attachmentPanel != null)
	    attachmentPanel.registerKeyboardAction(anAction, aCommand, aKeyStroke, aCondition);
	editorPane.registerKeyboardAction(anAction, aCommand, aKeyStroke, aCondition);
	editorScrollPane.registerKeyboardAction(anAction, aCommand, aKeyStroke, aCondition);
	toolbar.registerKeyboardAction(anAction, aCommand, aKeyStroke, aCondition);
	splitPane.registerKeyboardAction(anAction, aCommand, aKeyStroke, aCondition);
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

	if (attachmentPanel != null)
	    attachmentPanel.unregisterKeyboardAction(aKeyStroke);
	editorPane.unregisterKeyboardAction(aKeyStroke);
	editorScrollPane.unregisterKeyboardAction(aKeyStroke);
	toolbar.unregisterKeyboardAction(aKeyStroke);
	splitPane.unregisterKeyboardAction(aKeyStroke);
    }

    /**
     * This creates and shows a PopupMenu for this component.  
     */
    public void showPopupMenu(JComponent component, MouseEvent e) {
	ConfigurablePopupMenu popupMenu = new ConfigurablePopupMenu();
	popupMenu.configureComponent("NewMessageWindow.popupMenu", Pooka.getResources());	
	popupMenu.setActive(getActions());
	popupMenu.show(component, e.getX(), e.getY());
	    
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

    /**
     * Overrides JComponent.addNotify().
     *
     * We override addNotify() here to set the proper splitPane location.
     */

    public void addNotify() {
        super.addNotify();
        splitPane.setDividerLocation(Math.min(tabbedPane.getPreferredSize().height + 1, Integer.parseInt(Pooka.getProperty("MessageWindow.headerPanel.vsize", "500"))));
    }

    public boolean isEditable() {
	return true;
    }

    public boolean isModified() {
	return modified;
    }

    public void setModified(boolean mod) {
	if (isEditable())
	    modified=mod;
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

    public Hashtable getInputTable() {
	return inputTable;
    }

    public void setInputTable(Hashtable newInputTable) {
	inputTable = newInputTable;
    }

    public Action[] getActions() {
	Action[] returnValue = getDefaultActions();
	
	if (msg.getActions() != null) 
	    returnValue = TextAction.augmentList(msg.getActions(), returnValue);
	    
	if (getEditorPane() != null && getEditorPane().getActions() != null) 
	    returnValue = TextAction.augmentList(getEditorPane().getActions(), returnValue);

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
	    new TestAction()
		};
    }

    //-----------actions----------------


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

    class TestAction extends AbstractAction {
	
	TestAction() {
	    super("test");
	}

	public void actionPerformed(ActionEvent e) {
	    System.out.println("wrapped text is:");
	    System.out.println(net.suberic.pooka.MailUtilities.wrapText(getMessageText()));
	}
    }

}





