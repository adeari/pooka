package net.suberic.pooka.gui;
import net.suberic.pooka.*;
import javax.swing.*;
import java.awt.FlowLayout;
import net.suberic.pooka.event.*;
import javax.mail.event.*;

/**
 * A status bar which shows things like the folder name, the number of 
 * unread messages in a folder, etc.
 */
public class FolderStatusBar extends JPanel implements MessageCountListener, MessageChangedListener {
    FolderInfo folderInfo;
    JLabel folderLabel;
    JLabel messageCount;
    JPanel loaderPanel;
    LoadMessageTracker tracker = null;
    
    public FolderStatusBar(FolderInfo newFolder) {
	folderInfo = newFolder;
	this.setLayout(new FlowLayout(FlowLayout.LEFT));
	folderLabel = new JLabel(getFolderInfo().getFolderName());
	this.add(folderLabel);
	this.add(new JSeparator(SwingConstants.VERTICAL));
	messageCount = new JLabel();
	updateMessageCount();
	this.add(messageCount);
	this.add(new JSeparator(SwingConstants.VERTICAL));
	loaderPanel = new JPanel();
	this.add(loaderPanel);
    }
    
    public void messageChanged(MessageChangedEvent mce) {
	updateMessageCount();
    }
    
    public void messagesAdded(MessageCountEvent e) {
	updateMessageCount();
    }
    
    public void messagesRemoved(MessageCountEvent e) {
	updateMessageCount();
    }
    
    public void updateMessageCount() {
	SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		    messageCount.setText(getFolderInfo().getUnreadCount() + " " + Pooka.getProperty("FolderFolderStatusBar.unreadMessages", "Unread") + " / " + getFolderInfo().getMessageCount() + " " + Pooka.getProperty("FolderFolderStatusBar.totalMessages", "Total"));
		}
	    });

    }

    public FolderInfo getFolderInfo() {
	return folderInfo;
    }

    public JPanel getLoaderPanel() {
	return loaderPanel;
    }

    public LoadMessageTracker getTracker() {
	return tracker;
    }

    public void setTracker(LoadMessageTracker newTracker) {
	tracker = newTracker;
    }
} // end class FolderFolderStatusBar
