package net.suberic.pooka.gui;
import net.suberic.pooka.*;
import javax.mail.*;
import javax.mail.internet.MimeMessage;
import javax.mail.event.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.text.TextAction;
import java.util.*;
import net.suberic.util.gui.*;
import net.suberic.util.event.*;
import net.suberic.util.thread.*;
import net.suberic.util.swing.*;

/**
 * This is a JSplitPane which contains both a FolderDisplayPanel for showing
 * the messages in the current folder, as well as a MessageDisplayPanel
 * for previewing messages.
 */

public class PreviewPanel extends JPanel implements FolderDisplayUI, MessageUI {
    private JSplitPane splitPanel;
    private ConfigurableToolbar toolbar;
    private ReadMessageDisplayPanel messageDisplay;
    private FolderDisplayPanel folderDisplay = null;
    private FolderInfo displayedFolder = null;
    private MessageProxy displayedMessage = null;

    private boolean enabled;

    /**
     * Creates an empty PreviewPanel.
     */
    public PreviewPanel() {
	folderDisplay = new FolderDisplayPanel();
	messageDisplay = new ReadMessageDisplayPanel();
	splitPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, folderDisplay, messageDisplay);

	toolbar = new ConfigurableToolbar("FolderWindowToolbar", Pooka.getResources());
	this.setLayout(new BorderLayout());

	this.add("North", toolbar);
	this.add("Center", splitPanel);
    }

    /**
     * Creates a new PreviewPanel for the given Folder.
     */
    public PreviewPanel(FolderInfo folder) {
	displayedFolder = folder;
	folderDisplay = new FolderDisplayPanel(folder);
	messageDisplay = new ReadMessageDisplayPanel();
	splitPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, folderDisplay, messageDisplay);

	toolbar = new ConfigurableToolbar("FolderWindowToolbar", Pooka.getResources());
	this.setLayout(new BorderLayout());

	this.add("North", toolbar);
	this.add("Center", splitPanel);
    }

    /**
     * Opens the display for the given Folder.
     *
     * As defined in interface net.suberic.pooka.gui.FolderDisplayUI.
     */
    public void openFolderDisplay() {

    }

    /**
     * Closes the display for the given Folder.
     *
     * As defined in interface net.suberic.pooka.gui.FolderDisplayUI.
     */
    public void closeFolderDisplay() {
	folderDisplay.removeMessageTable();
	if (displayedFolder != null && displayedFolder.getFolderDisplayUI() == this)
	    displayedFolder.setFolderDisplayUI(null);
	displayedFolder = null;
	setEnabled(false);
    }

    /**
     * Gets the FolderInfo for the currently displayed Folder.
     *
     * As defined in interface net.suberic.pooka.gui.FolderDisplayUI.
     */
    public FolderInfo getFolderInfo() {
	return displayedFolder;
    }

    /**
     * Sets the panel enabled or disabled.
     *
     * As defined in interface net.suberic.pooka.gui.FolderDisplayUI.
     */
    public void setEnabled(boolean newValue) {
	enabled = newValue;
    }

    /**
     * Sets the busy property of the panel.
     *
     * As defined in interface net.suberic.pooka.gui.FolderDisplayUI.
     */
    public void setBusy(boolean newValue) {
	if (newValue)
	    this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	else
	    this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    /**
     * Shows an Input dialog for this panel.
     *
     * As defined in interface net.suberic.pooka.gui.FolderDisplayUI.
     */
    public String showInputDialog(String inputMessage, String title) {
	return JOptionPane.showInputDialog(this, inputMessage, title, JOptionPane.QUESTION_MESSAGE);
    }	

    /**
     * Opens the MessageUI for the current Message.
     * 
     * As defined in interface net.suberic.pooka.gui.MessageUI.
     */
    public void openMessageUI() {
	messageDisplay.setMessageProxy(displayedMessage);
	try {
	    messageDisplay.configureMessageDisplay();
	} catch (MessagingException me) {
	    showError(Pooka.getProperty("error.MessageInternalFrame.errorLoadingMessage", "Error loading Message:  ") + "\n" + me.getMessage(), Pooka.getProperty("error.MessageInternalFrame.errorLoadingMessage.title", "Error loading message."));
	    me.printStackTrace();

	}
	setEnabled(true);
    }

    /**
     * Closes the MessageUI for the current Message.
     * 
     * As defined in interface net.suberic.pooka.gui.MessageUI.
     */
    public void closeMessageUI() {
	//getMessageDisplay().clearPanel();
	getMessageDisplay().setMessageProxy(null);
	if (displayedMessage != null && displayedMessage.getMessageUI() == this)
	    displayedMessage.setMessageUI(null);
	
	displayedMessage = null;
    }

    /**
     * Gets the currently shown MessageProxy.
     * 
     * As defined in interface net.suberic.pooka.gui.MessageUI.
     */
    public MessageProxy getMessageProxy() {
	return displayedMessage;
    }

    /**
     * Shows an Input Dialog with the given Input Panels and title.
     * 
     * As defined in interface net.suberic.pooka.gui.MessageUI.
     */
    public String showInputDialog(Object[] inputPanels, String title) {
	return JOptionPane.showInputDialog(this, inputPanels, title, JOptionPane.QUESTION_MESSAGE);
    }   

    /**
     * Shows a Confirm dialog.
     * 
     * As defined in interface net.suberic.pooka.gui.MessageUI.
     */
    public int showConfirmDialog(String message, String title, int optionType, int messageType) {
	return JOptionPane.showConfirmDialog(this, message, title, messageType);
    }

    /**
     * Gets the default UserProfile for this component.
     *
     * As defined in interface net.suberic.pooka.UserProfileContainer.
     */
    public UserProfile getDefaultProfile() {
	if (getMessageProxy() != null)
	    return getMessageProxy().getDefaultProfile();
	else 
	    return getFolderInfo().getDefaultProfile();
    }

    /**
     * Shows an Error with the given parameters.
     *
     * As defined in interface net.suberic.pooka.gui.ErrorHandler.
     */
    public void showError(String errorMessage) {
	showError(errorMessage, Pooka.getProperty("Error", "Error"));
    }

    /**
     * Shows an Error with the given parameters.
     *
     * As defined in interface net.suberic.pooka.gui.ErrorHandler.
     */
    public void showError(String errorMessage, String title) {
	JOptionPane.showMessageDialog(this, errorMessage, title, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Shows an Error with the given parameters.
     *
     * As defined in interface net.suberic.pooka.gui.ErrorHandler.
     */
    public void showError(String errorMessage, String title, Exception e) {
	showError(errorMessage + e.getMessage(), title);
	e.printStackTrace();
    }

    // MessageLoadedListener
    
    /**
     * 
     * Defined in net.suberic.pooka.event.MessageLoadedListener.
     */
    public void handleMessageLoaded(net.suberic.pooka.event.MessageLoadedEvent e) {
	
    }

    // ConnectionListener
    
    /**
     *
     */
    public void closed(ConnectionEvent e) {

    }

    /**
     *
     */
    public void disconnected(ConnectionEvent e) {

    }

    /**
     *
     */
    public void opened(ConnectionEvent e) {

    }

    // MessageCounteListener
    /**
     *
     */
    public void messagesAdded(MessageCountEvent e) {

    }

    public void messagesRemoved(MessageCountEvent e) { 

    }


    /**
     * Gets the currently available Actions for this component.
     *
     * As defined in interface net.suberic.pooka.gui.ActionContainer.
     */
    public Action[] getActions() {
	if (enabled) {
	    Action[] returnValue = null;

	    if (getFolderDisplay() != null)
		returnValue = getFolderDisplay().getActions();
	    
	    if (getMessageDisplay() != null && getMessageDisplay().getActions() != null) {
		if (returnValue != null) {
		    return TextAction.augmentList(returnValue, getMessageDisplay().getActions());
		} else {
		    return getMessageDisplay().getActions();
		}
	    }

	    return returnValue;

	} else
	    return null;
    }

    public ReadMessageDisplayPanel getMessageDisplay() {
	return messageDisplay;
    }

    public FolderDisplayPanel getFolderDisplay() {
	return folderDisplay;
    }
}
