package net.suberic.pooka.gui;
import net.suberic.pooka.*;
import javax.swing.*;
import java.awt.FlowLayout;
import net.suberic.pooka.event.*;
import javax.mail.event.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * A status bar which shows things like the folder name, the number of 
 * unread messages in a folder, etc.
 */
public class FolderStatusBar extends JPanel implements MessageCountListener, MessageChangedListener {
    FolderInfo folderInfo;
    JLabel folderLabel;
    JLabel messageCount;
    JPanel loaderPanel;
    JPanel gotoPanel;
    LoadMessageTracker tracker = null;
    
    public FolderStatusBar(FolderInfo newFolder) {
	folderInfo = newFolder;
	folderLabel = new JLabel(getFolderInfo().getFolderName());
	messageCount = new JLabel();
	updateMessageCount();
	loaderPanel = new JPanel();
	
	gotoPanel = new JPanel();
	gotoPanel.add(new JLabel(Pooka.getProperty("FolderStatusBar.goto", "Goto Message")));
	final JTextField inputField = new JTextField(5);
	inputField.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    try {
			int msgNum = Integer.parseInt(e.getActionCommand());
			if (getFolderInfo() != null) {
			    FolderDisplayUI fdui = getFolderInfo().getFolderDisplayUI();
			    fdui.selectMessage(msgNum);
			}
		    } catch (NumberFormatException nfe) {
			
		    }
		    inputField.selectAll();
		}
	    });
	gotoPanel.add(inputField);

	/*
	this.setLayout(new FlowLayout(FlowLayout.LEFT));
	this.add(folderLabel);
	this.add(new JSeparator(SwingConstants.VERTICAL));
	this.add(messageCount);
	this.add(new JSeparator(SwingConstants.VERTICAL));
	this.add(loaderPanel);
	this.add(gotoPanel);
	*/

	java.awt.GridBagConstraints constraints = new java.awt.GridBagConstraints();
	java.awt.GridBagLayout layout = new java.awt.GridBagLayout();
	constraints.weightx = 0.0;
	constraints.fill = java.awt.GridBagConstraints.VERTICAL;
	constraints.anchor = java.awt.GridBagConstraints.WEST;
	constraints.ipadx = 5;
	constraints.insets = new java.awt.Insets(0, 10, 0, 10);
	this.setLayout(layout);

	layout.setConstraints(folderLabel, constraints);
	this.add(folderLabel);
	JSeparator js = new JSeparator(SwingConstants.VERTICAL);
	layout.setConstraints(js, constraints);
	this.add(js);
	layout.setConstraints(messageCount, constraints);
	this.add(messageCount);
	js = new JSeparator(SwingConstants.VERTICAL);
	layout.setConstraints(js, constraints);
	this.add(js);
	constraints.fill = java.awt.GridBagConstraints.BOTH;
	constraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
	constraints.weightx = 1.0;
	layout.setConstraints(loaderPanel, constraints);
	this.add(loaderPanel);
	constraints.weightx = 0.0;
	constraints.fill = java.awt.GridBagConstraints.VERTICAL;
	constraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
	constraints.anchor = java.awt.GridBagConstraints.EAST;
	layout.setConstraints(gotoPanel, constraints);
	this.add(gotoPanel);
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
