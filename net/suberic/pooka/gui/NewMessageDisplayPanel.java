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
import javax.swing.plaf.metal.*;
import javax.swing.plaf.*;

/**
 * A window for entering new messages.
 */
public class NewMessageDisplayPanel extends MessageDisplayPanel implements ItemListener {

  JTabbedPane tabbedPane = null;
  Container headerPanel = null;
  boolean modified = false;
  Hashtable inputTable;
  
  JScrollPane headerScrollPane;
  
  private Action[] defaultActions;

  
  /**
   * Creates a NewMessageDisplayPanel from the given Message.
   */
  
  public NewMessageDisplayPanel(NewMessageProxy newMsgProxy) {
    super(newMsgProxy);
    
  }
  
  /**
   * This configures the MessageDisplayPanel.  This means that here is 
   * where we create the headerPanel and editorPane and add them to the 
   * splitPane.
   */
  public void configureMessageDisplay() throws MessagingException {
    
    splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    tabbedPane = new JTabbedPane();
    
    inputTable = new Hashtable();
    
    headerPanel = createHeaderInputPanel(msg, inputTable);
    editorPane = createMessagePanel(msg);
    
    headerScrollPane = new JScrollPane(headerPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    tabbedPane.add(Pooka.getProperty("MessageWindow.HeaderTab", "Headers"), headerScrollPane);
    
    if (getMessageProxy().getAttachments() != null && getMessageProxy().getAttachments().size() > 0)
      addAttachmentPane();
    
    editorScrollPane = new JScrollPane(editorPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    
    splitPane.setTopComponent(tabbedPane);
    splitPane.setBottomComponent(editorScrollPane);
    
    this.add("Center", splitPane);
    
    editorPane.addMouseListener(new MouseAdapter() {
	
	public void mousePressed(MouseEvent e) {
	  if (SwingUtilities.isRightMouseButton(e)) {
	    showPopupMenu(editorPane, e);
	  }
	}
      });
    
    editorPane.addKeyListener(new KeyAdapter() {
	public void keyTyped(KeyEvent e) {
	  setModified(true);
	}
      });

    splitPane.resetToPreferredSizes();

  }

    /**
     * Sets the window to its preferred size.
     */
    public void sizeToDefault() {
	Dimension prefSize = getDefaultEditorPaneSize();
	JScrollBar vsb = editorScrollPane.getVerticalScrollBar();
	if (vsb != null)
	    prefSize.setSize(prefSize.getWidth() + vsb.getPreferredSize().getWidth(), prefSize.getHeight());
	editorScrollPane.setPreferredSize(prefSize);
	int width = prefSize.width;
	this.setPreferredSize(new Dimension(width, width));
	this.setSize(this.getPreferredSize());
    }

    /**
     * as defined in java.awt.event.ItemListener
     *
     * This implementation calls a refreshCurrentUser() on the MainPanel.
     *
     * It also updates the panel's interface style.
     */

    public void itemStateChanged(ItemEvent ie) {
      Pooka.getMainPanel().refreshCurrentUser();
      SwingUtilities.invokeLater(new Runnable() {
	  public void run() {
	    NewMessageUI nmui = ((NewMessageProxy)msg).getNewMessageUI();
	    if (nmui instanceof net.suberic.util.swing.ThemeSupporter) {
	      try {
		Pooka.getUIFactory().getPookaThemeManager().updateUI((net.suberic.util.swing.ThemeSupporter) nmui, (java.awt.Component) nmui);
	      } catch (Exception e) {
		System.err.println("error setting theme:  " + e);
	      }
	    }
	  }
	});
	
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
	
	UserProfile selectedProfile = Pooka.getMainPanel().getCurrentUser();

	if (selectedProfile != null)
	    profileCombo.setSelectedItem(selectedProfile);

	profileCombo.addItemListener(this);
	
	proptDict.put("UserProfile", profileCombo);

	inputPanel.add(inputRow);
	
	// Create Address panel

	StringTokenizer tokens = new StringTokenizer(Pooka.getProperty("MessageWindow.Input.DefaultFields", "To:CC:BCC:Subject"), ":");
	String currentHeader = null;
	JLabel hdrLabel = null;
	EntryTextArea inputField = null;

	while (tokens.hasMoreTokens()) {
	    inputRow = new Box(BoxLayout.X_AXIS);
	    currentHeader=tokens.nextToken();
	    hdrLabel = new JLabel(Pooka.getProperty("MessageWindow.Input.." + currentHeader + ".label", currentHeader) + ":", SwingConstants.RIGHT);
	    hdrLabel.setPreferredSize(new Dimension(75,hdrLabel.getPreferredSize().height));
	    inputRow.add(hdrLabel);

	    if (currentHeader.equalsIgnoreCase("To") || currentHeader.equalsIgnoreCase("CC") || currentHeader.equalsIgnoreCase("BCC") && Pooka.getProperty("Pooka.useAddressCompletion", "false").equalsIgnoreCase("true")) {
	      try {
		inputField = new AddressEntryTextArea(((NewMessageProxy)msg).getNewMessageUI(), ((NewMessageProxy)msg).getNewMessageInfo().getHeader(Pooka.getProperty("MessageWindow.Input." + currentHeader + ".MIMEHeader", "") , ","), 1, 30);
	      } catch (MessagingException me) {
		inputField = new net.suberic.util.swing.EntryTextArea(1, 30);
	      }
	    } else {
	      try {
		inputField = new net.suberic.util.swing.EntryTextArea(((NewMessageProxy)msg).getNewMessageInfo().getHeader(Pooka.getProperty("MessageWindow.Input." + currentHeader + ".MIMEHeader", "") , ","), 1, 30);
	      } catch (MessagingException me) {
		inputField = new net.suberic.util.swing.EntryTextArea(1, 30);
	      }
	    }

	    inputField.setLineWrap(true);
	    inputField.setWrapStyleWord(true);
	    inputField.setBorder(BorderFactory.createEtchedBorder());
	    inputField.addKeyListener(new KeyAdapter() {
		public void keyTyped(KeyEvent e) {
		  setModified(true);
		}
	      });


	    inputRow.add(inputField);
	    
	    inputPanel.add(inputRow);

	    proptDict.put(Pooka.getProperty("MessageWindow.Input." + currentHeader + ".value", currentHeader), inputField);
	}

	return inputPanel;
    }

    /**
     * This creates a new JTextPane for the main text part of the new 
     * message.  It will also include the current text of the message.
     */
    public JTextPane createMessagePanel(MessageProxy aMsg) {
	JTextPane retval = new net.suberic.util.swing.ExtendedEditorPane();
	retval.setEditorKit(new MailEditorKit());

	setDefaultFont(retval);

	// see if this message already has a text part, and if so,
	// include it.
	
	String origText = ((NewMessageInfo)getMessageProxy().getMessageInfo()).getTextPart(false);
	if (origText != null && origText.length() > 0) 
	    retval.setText(origText);
	
	UserProfile profile = getSelectedProfile();
	if (profile.autoAddSignature) {
	    retval.setCaretPosition(retval.getDocument().getLength());
	    if (profile.signatureFirst) {

	    }
	    addSignature(retval);

	}

	// bodyInputPane.setContentType("text");
	return retval;

    }

    /**
     * This adds the current user's signature to the message at the current
     * location of the cursor.
     */
    public void addSignature(JEditorPane editor) {
	String sig = getSelectedProfile().getSignature();
	if (sig != null) {
	    try {
		editor.getDocument().insertString(editor.getCaretPosition(), sig, null);
	    } catch (javax.swing.text.BadLocationException ble) {
		;
	    }
	}
    }

    /**
     * This returns the values in the MesssageWindow as a set of 
     * InternetHeaders.
     */
    public InternetHeaders getMessageHeaders() throws MessagingException {
      InternetHeaders returnValue = new InternetHeaders();
      String key;
      
      Enumeration keys = inputTable.keys();
      while (keys.hasMoreElements()) {
	key = (String)(keys.nextElement());
	
	if (! key.equals("UserProfile")) {
	  String header = new String(Pooka.getProperty("MessageWindow.Header." + key + ".MIMEHeader", key));
	
	  EntryTextArea inputField = (EntryTextArea) inputTable.get(key);
	  String value = null;
	  if (inputField instanceof AddressEntryTextArea) {
	    value = ((AddressEntryTextArea) inputField).getParsedAddresses();
	  } else {
	    value = ((EntryTextArea)(inputTable.get(key))).getText();
	  }

	  returnValue.setHeader(header, value);
	}
      }
      return returnValue;
    }
  
    /**
     * This notifies the MessageDisplayPanel that an attachment has been added
     * at the provided index.  This does not actually add an attachment,
     * but rather should be called by the MessageProxy when an attachment
     * has been added.
     *
     * If an AttachmentPane does not currently exist for this 
     * MessageDisplayPanel, this method will call addAttachmentPane() to 
     * create one.
     */
    public void attachmentAdded(int index) {
	if (getAttachmentPanel() == null)
	    addAttachmentPane();
	else
	    getAttachmentPanel().getTableModel().fireTableRowsInserted(index, index);
    }

    /**
     * This notifies the MessageDisplayPanel that the attachment at the 
     * provided index has been removed.  This does not actually remove
     * the attachment, but rather should be called by the MessageProxy
     * when an attachment has been removed.
     *
     * If this removes the last attachment, the entire AttachmentPane
     * is removed from the MessageDisplayPanel.
     */
    public void attachmentRemoved(int index) {
	try {
	    Vector attach = ((NewMessageProxy)getMessageProxy()).getAttachments();
	    if (attach == null || attach.size() == 0) {
		removeAttachmentPane();
	    } else {
		getAttachmentPanel().getTableModel().fireTableRowsDeleted(index, index);
	    }
	} catch (MessagingException me) {
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
	NewMessageUI nmui = ((NewMessageProxy)msg).getNewMessageUI();
	if (nmui instanceof net.suberic.util.swing.ThemeSupporter) {
	  try {
	    Pooka.getUIFactory().getPookaThemeManager().updateUI((net.suberic.util.swing.ThemeSupporter) nmui, attachmentScrollPane, true);
	  } catch (Exception e) {
	    System.err.println("error setting theme:  " + e);
	  }
	}
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
	splitPane.unregisterKeyboardAction(aKeyStroke);
    }

    /**
     * This creates and shows a PopupMenu for this component.  
     */
    public void showPopupMenu(JComponent component, MouseEvent e) {
	ConfigurablePopupMenu popupMenu = new ConfigurablePopupMenu();
	popupMenu.configureComponent("NewMessageWindow.popupMenu", Pooka.getResources());	
	popupMenu.setActive(getActions());
	NewMessageUI nmui = ((NewMessageProxy)msg).getNewMessageUI();
	if (nmui instanceof net.suberic.util.swing.ThemeSupporter) {
	  try {
	    Pooka.getUIFactory().getPookaThemeManager().updateUI((net.suberic.util.swing.ThemeSupporter) nmui, popupMenu, true);
	  } catch (Exception etwo) {
	    System.err.println("error setting theme:  " + etwo);
	  }
	}
	popupMenu.show(component, e.getX(), e.getY());
	    
    }

    /**
     * As specified by interface net.suberic.pooka.UserProfileContainer.
     *
     * This implementation returns the DefaultProfile of the associated
     * MessageProxy if the MessageDisplayPanel is not editable.  If the 
     * MessageDisplayPanel is editable, it returns the currently selected 
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
   * sets the currently selected Profile.
   */
  public void setSelectedProfile(UserProfile newProfile) {
    if (newProfile != null) {
      ((JComboBox)(inputTable.get("UserProfile"))).setSelectedItem(newProfile);
    }
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
     * performTextAction grabs the focused component on the MessageDisplayPanel
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
	
	if (msg.getActions() != null) { 
	    if (returnValue != null) {
		returnValue = TextAction.augmentList(msg.getActions(), returnValue);
	    } else {
		returnValue = msg.getActions();
	    }
	}
	    
	if (getEditorPane() != null && getEditorPane().getActions() != null) {
	    if (returnValue != null) {
		returnValue = TextAction.augmentList(getEditorPane().getActions(), returnValue);
	    } else {
		returnValue = getEditorPane().getActions();
	    }
	}

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
	    new AddSignatureAction(),
	    new TestAction()
		};
    }

    //-----------actions----------------

    class AddSignatureAction extends AbstractAction {

	AddSignatureAction() {
	    super("message-add-signature");
	}
	
        public void actionPerformed(ActionEvent e) {
	    addSignature(editorPane);
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





