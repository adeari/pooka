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

public class ReadMessageWindow extends MessageWindow {

    public static int HEADERS_DEFAULT = 0;
    public static int HEADERS_FULL = 1;

    int headerStyle = ReadMessageWindow.HEADERS_DEFAULT;
    boolean showFullHeaders = false;
    ConfigurableToolbar toolbar;

    /**
     * Creates a MessageWindow from the given Message.
     */

    public ReadMessageWindow(MessagePanel newParentContainer, MessageProxy newMsgProxy) {
	super(newParentContainer, newMsgProxy);

	configureMessageWindow();
    }
    
    protected void configureMessageWindow() {
	try {
	    this.setTitle(msg.getMessage().getSubject());
	} catch (MessagingException me) {
	    this.setTitle(Pooka.getProperty("Pooka.messageWindow.messageTitle.noSubject", "<no subject>"));
	}

	editorPane = createMessagePanel(msg);
	editorScrollPane = new JScrollPane(editorPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

	toolbar = new ConfigurableToolbar("MessageWindowToolbar", Pooka.getResources());
	
	toolbar.setActive(this.getActions());
	this.getContentPane().add("North", toolbar);

	if (!getMessageProxy().hasLoadedAttachments())
	    getMessageProxy().loadAttachmentInfo();

	if (getMessageProxy().getAttachments() != null && getMessageProxy().getAttachments().size() > 0) {
	    attachmentPanel = new AttachmentPane(msg);
	    attachmentScrollPane = new JScrollPane(attachmentPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

	    splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
	    
	    splitPane.setTopComponent(editorScrollPane);
	    splitPane.setBottomComponent(attachmentScrollPane);
	    this.getContentPane().add("Center", splitPane);
	} else {
	    this.getContentPane().add("Center", editorScrollPane);
	}

	keyBindings = new ConfigurableKeyBinding(this, "ReadMessageWindow.keyBindings", Pooka.getResources());
	keyBindings.setActive(getActions());
    }

    /**
     * Overrides JComponent.addNotify().
     *
     * We override addNotify() here to call resizeByWidth() to set
     * the correct width, and, if there is a splitPane with an attachment
     * panel, to set the correct divider location on the split pane.
     */
    public void addNotify() {
	super.addNotify();
	Dimension prefSize = getDefaultEditorPaneSize();
	JScrollBar vsb = editorScrollPane.getVerticalScrollBar();
	if (vsb != null)
	    prefSize.setSize(prefSize.getWidth() + vsb.getPreferredSize().getWidth(), prefSize.getHeight());
	editorScrollPane.setPreferredSize(prefSize);
	this.resizeByWidth();
	if (splitPane != null && attachmentPanel != null)
	    splitPane.setDividerLocation((int)(splitPane.getSize().getHeight() - attachmentPanel.getPreferredSize().getHeight()));
    }

    /**
     * This method creates the component that will display the message
     * itself.
     *
     * It returns a JTextPane with the headers and the message body
     * together.
     */

    public JTextPane createMessagePanel(MessageProxy aMsg) {
	JTextPane retval = new JTextPane();

	setDefaultFont(retval);

	StringBuffer messageText = new StringBuffer();
	
	if (aMsg.getMessage() instanceof javax.mail.internet.MimeMessage) {
	    javax.mail.internet.MimeMessage mMsg = (javax.mail.internet.MimeMessage) aMsg.getMessage();

	    //	    messageText.append(MailUtilities.getHeaderInformation(mMsg, showFullHeaders()));

	    String content = null;
	    if (Pooka.getProperty("Pooka.displayTextAttachments", "").equalsIgnoreCase("true"))
		content = net.suberic.pooka.MailUtilities.getTextAndTextInlines(mMsg, Pooka.getProperty("Pooka.attachmentSeparator", "\n\n"), showFullHeaders(), true);
	    else
		content = net.suberic.pooka.MailUtilities.getTextPart(mMsg, showFullHeaders(), true);

	    if (content != null) {
		messageText.append(content);
		retval.setEditable(false);
		retval.setText(messageText.toString());
	    } 
	 
	    return retval;

	} else
	    return new JTextPane();
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
	toolbar.registerKeyboardAction(anAction, aCommand, aKeyStroke, aCondition);
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
	toolbar.unregisterKeyboardAction(aKeyStroke);
	splitPane.unregisterKeyboardAction(aKeyStroke);
    }

    //------- Actions ----------//

    public Action[] getActions() {
	
	Action[] actionList;

	if (msg.getActions() != null) {
	    actionList = TextAction.augmentList(msg.getActions(), getDefaultActions());
	} else 
	    actionList = getDefaultActions();

	if (editorPane != null && editorPane.getActions() != null) 
	    return TextAction.augmentList(actionList, editorPane.getActions());
	else
	    return actionList;
    }

}





