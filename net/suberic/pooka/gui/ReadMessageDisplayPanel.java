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

public class ReadMessageDisplayPanel extends MessageDisplayPanel {

    public boolean firstShow = true;

    public static int HEADERS_DEFAULT = 0;
    public static int HEADERS_FULL = 1;

    int headerStyle = ReadMessageWindow.HEADERS_DEFAULT;
    boolean showFullHeaders = false;
    
    /**
     * Creates a MessageDisplayPanel from the given Message.
     */
    
    public ReadMessageDisplayPanel(MessageProxy newMsgProxy) {
	super(newMsgProxy);
    }

    /**
     * Configures the MessageDisplayPanel.  This includes creating all 
     * the necessary panels and populating those panels with the information
     * from the MessageProxy.
     */
    public void configureMessageDisplay() throws MessagingException {
	editorPane = createMessagePanel(msg);
	editorScrollPane = new JScrollPane(editorPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	
	if (getMessageProxy().getAttachments() != null && getMessageProxy().getAttachments().size() > 0) {
	    attachmentPanel = new AttachmentPane(msg);
	    attachmentScrollPane = new JScrollPane(attachmentPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	    
	    splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
	    
	    splitPane.setTopComponent(editorScrollPane);
	    splitPane.setBottomComponent(attachmentScrollPane);
	    this.add("Center", splitPane);
	} else {
	    this.add("Center", editorScrollPane);
	}
	
	keyBindings = new ConfigurableKeyBinding(this, "ReadMessageWindow.keyBindings", Pooka.getResources());
	keyBindings.setActive(getActions());
	
	editorPane.addMouseListener(new MouseAdapter() {
		
		public void mousePressed(MouseEvent e) {
		    if (SwingUtilities.isRightMouseButton(e)) {
			showPopupMenu(editorPane, e);
		    }
		}
	    });
    }

    /**
     * This method creates the component that will display the message
     * itself.
     *
     * It returns a JTextPane with the headers and the message body
     * together.
     */

    public JTextPane createMessagePanel(MessageProxy aMsg) throws MessagingException {
	JTextPane retval = new JTextPane();

	setDefaultFont(retval);

	StringBuffer messageText = new StringBuffer();
	
	String content = null;
	if (Pooka.getProperty("Pooka.displayTextAttachments", "").equalsIgnoreCase("true")) {
	    content = getMessageProxy().getMessageInfo().getTextAndTextInlines(Pooka.getProperty("Pooka.attachmentSeparator", "\n\n"), showFullHeaders(), true);
	} else {
	    content = getMessageProxy().getMessageInfo().getTextPart( showFullHeaders(), true);
	}
	
	if (content != null) {
	    messageText.append(content);
	    retval.setEditable(false);
	    retval.setText(messageText.toString());
	} 
	
	return retval;
	
    }
    
    public boolean showFullHeaders() {
	return showFullHeaders;
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
	if (splitPane != null)
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
	popupMenu.configureComponent("ReadMessageWindow.popupMenu", Pooka.getResources());	
	popupMenu.setActive(getActions());
	popupMenu.show(component, e.getX(), e.getY());
	
    }

    /**
     * This sets the size of the MessageDisplayPanel to a reasonable
     * default value.
     */
    public void sizeToDefault() {
    }

    public void addNotify() {
	super.addNotify();
	
	if (firstShow) {
	    Dimension prefSize = getDefaultEditorPaneSize();
	    JScrollBar vsb = editorScrollPane.getVerticalScrollBar();
	    if (vsb != null)
		prefSize.setSize(prefSize.getWidth() + vsb.getPreferredSize().getWidth(), prefSize.getHeight());
	    editorScrollPane.setPreferredSize(prefSize);
	    int width = prefSize.width;
	    this.setPreferredSize(new Dimension(width, width));
	    this.setSize(this.getPreferredSize());
	    if (splitPane != null && attachmentPanel != null) {
		System.out.println("setting splitPane divider location to " + (splitPane.getPreferredSize().getHeight() - attachmentPanel.getPreferredSize().getHeight()));
		splitPane.setDividerLocation((int)(splitPane.getPreferredSize().getHeight() - attachmentPanel.getPreferredSize().getHeight()));
	    }
	    
	    firstShow = false;
	}
    }
    

    //------- Actions ----------//
    
    public Action[] getActions() {
	
	Action[] actionList;
	
	actionList = msg.getActions();

	if (actionList != null) {
	    if (editorPane != null && editorPane.getActions() != null) 
		return TextAction.augmentList(actionList, editorPane.getActions());
	    else
		return editorPane.getActions();
	}
	return actionList;
    }

}
