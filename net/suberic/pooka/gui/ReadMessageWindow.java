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
	editorScrollPane.setPreferredSize(getDefaultEditorPaneSize());
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
	    String content = null;
	    if (Pooka.getProperty("Pooka.displayTextAttachments", "").equalsIgnoreCase("true"))
		content = net.suberic.pooka.MailUtilities.getTextAndTextInlines(mMsg, Pooka.getProperty("Pooka.attachmentSeparator", "\n\n"));
	    else
		content = net.suberic.pooka.MailUtilities.getTextPart(mMsg);

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





