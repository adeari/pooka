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

/**
 * This basically is just the GUI representation of the Messages in
 * a Folder.  Most of the real work is done by the FolderInfo
 * class.
 */

public class FolderWindow extends JInternalFrame implements UserProfileContainer {
    JTable messageTable = null;
    JScrollPane scrollPane = null;
    FolderInfo folderInfo = null;
    StatusBar statusBar = null;
    MessagePanel messagePanel = null;

    public class StatusBar extends JPanel implements MessageCountListener, MessageChangedListener {
	JLabel folderLabel;
	JLabel messageCount;
	JPanel loaderPanel;

	public StatusBar() {
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
	    try {
		messageCount.setText(getFolderInfo().getFolder().getUnreadMessageCount() + " " + Pooka.getProperty("FolderStatusBar.unreadMessages", "Unread") + " / " + getFolderInfo().getFolder().getMessageCount() + " " + Pooka.getProperty("FolderStatusBar.totalMessages", "Total"));
	    } catch (MessagingException me) {
	    }
	}
    } // end internal class StatusBar

    /**
     * Creates a Folder window from the given Folder.
     */

    public FolderWindow(FolderInfo newFolderInfo, MessagePanel newMessagePanel) {
	super(newFolderInfo.getFolderName(), true, true, true, true);

	messagePanel = newMessagePanel;

	setFolderInfo(newFolderInfo);

	getFolderInfo().setFolderWindow(this);

	initWindow();

    }

    /**
     * Initializes the window. 
     *
     * TODO:  should get these values from the Resources...
     */

    private void initWindow() {
	scrollPane = new JScrollPane();
	this.getContentPane().setLayout(new BorderLayout());
	this.getContentPane().add("Center", scrollPane);
	setStatusBar(new StatusBar());
	getFolderInfo().addMessageCountListener(getStatusBar());
	getFolderInfo().addMessageChangedListener(getStatusBar());

	this.getContentPane().add("South", getStatusBar());

	this.setPreferredSize(new Dimension(Integer.parseInt(Pooka.getProperty("folderWindow.height", "570")), Integer.parseInt(Pooka.getProperty("folderWindow.width","380"))));
	messageTable=new JTable(getFolderInfo().getFolderTableModel());

	getFolderInfo().getFolderTableModel().addTableModelListener(messageTable);

	messageTable.addMouseListener(new MouseAdapter() {
	    public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
		    int rowIndex = getMessageTable().rowAtPoint(e.getPoint());
		    if (rowIndex != -1) {
			getMessageTable().setRowSelectionInterval(rowIndex, rowIndex);
			MessageProxy selectedMessage = getSelectedMessage();
			String actionCommand = Pooka.getProperty("MessagePanel.2xClickAction", "message-open");
			if (selectedMessage != null) {
			    Action clickAction = selectedMessage.getAction(actionCommand);
			    if (clickAction != null) {
				clickAction.actionPerformed (new ActionEvent(this, ActionEvent.ACTION_PERFORMED, actionCommand));
				
			    }
			}
		    }
		}
	    }
	});

	messageTable.getSelectionModel().addListSelectionListener(new SelectionListener());
	messageTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

	messageTable.setDefaultRenderer(Object.class, new DefaultFolderCellRenderer());
	messageTable.setDefaultRenderer(Number.class, new DefaultFolderCellRenderer());

	scrollPane.getViewport().add(messageTable);

	int firstUnread = getFolderInfo().getFirstUnreadMessage();
	if (firstUnread > -1) {
	    messageTable.setRowSelectionInterval(firstUnread, firstUnread);

	    messageTable.scrollRectToVisible(messageTable.getCellRect(firstUnread, 1, true));
	} else {
	    messageTable.scrollRectToVisible(messageTable.getCellRect(messageTable.getRowCount(), 1, true));
	}
	
	this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    public MessageProxy getSelectedMessage() {
	int rowsSelected = messageTable.getSelectedRowCount();

	if (rowsSelected == 1) 
	    return getFolderInfo().getMessageProxy(messageTable.getSelectedRow());
	else if (rowsSelected < 1) 
	    return null;
	else {
	    Vector msgSelected= new Vector();
	    int[] selectedRows = messageTable.getSelectedRows();
	    for (int i = 0; i < selectedRows.length; i++) 
		msgSelected.add(getFolderInfo().getMessageProxy(selectedRows[i]));
	    return new MultiMessageProxy(selectedRows, msgSelected, this);
	}
    }

    public void resize() {
	this.setSize(getParent().getSize());
    }

    public void closeFolderWindow(){
	try {
	    this.setClosed(true);
	} catch (java.beans.PropertyVetoException e) {
	}
    }

    public void expungeMessages() {
	try {
	    getFolderInfo().getFolder().expunge();
	} catch (MessagingException me) {
	    JOptionPane.showInternalMessageDialog(getDesktopPane(), Pooka.getProperty("error.Message.ExpungeErrorMessage", "Error:  could not expunge messages.") +"\n" + me.getMessage());
	}   
    }

    // Accessor methods.

    public MessagePanel getMessagePanel() {
	return messagePanel;
    }

    public JTable getMessageTable() {
	return messageTable;
    }

    public Folder getFolder() {
	return getFolderInfo().getFolder();
    }

    public void setFolderInfo(FolderInfo newValue) {
	folderInfo=newValue;
    }

    public FolderInfo getFolderInfo() {
	return folderInfo;
    }

    public FolderTableModel getFolderTableModel() {
	return getFolderInfo().getFolderTableModel();
    }

    public StatusBar getStatusBar() {
	return statusBar;
    }

    public void setStatusBar(StatusBar newValue) {
	statusBar = newValue;
    }

    /**
     * gets the actions handled both by the FolderWindow and the 
     * selected Message(s).
     */

    public class SelectionListener implements javax.swing.event.ListSelectionListener {
	SelectionListener() {
	}

	public void valueChanged(javax.swing.event.ListSelectionEvent e) {
	    getMessagePanel().getMainPanel().refreshActiveMenus(getMessagePanel().getMainPanel().getMainMenu());
	}
    }

    /**
     * As specified by net.subeic.pooka.UserProfileContainer
     */

    public UserProfile getDefaultProfile() {
	if (getFolderInfo() != null)
	    return getFolderInfo().getDefaultProfile();
	else
	    return null;
    }

    public Action[] getActions() {
	if (getSelectedMessage() != null) 
	    return TextAction.augmentList(getSelectedMessage().getActions(), getDefaultActions());
	else 
	    return getDefaultActions();
    }

    public Action[] getDefaultActions() {
	return defaultActions;
    }

    //-----------actions----------------

    // The actions supported by the window itself.

    private Action[] defaultActions = {
	new CloseAction(),
	new ExpungeAction()
    };

    class CloseAction extends AbstractAction {

	CloseAction() {
	    super("file-close");
	}
	
        public void actionPerformed(ActionEvent e) {
	    closeFolderWindow();
	}
    }

    public class ExpungeAction extends AbstractAction {

	ExpungeAction() {
	    super("message-expunge");
	}
	
        public void actionPerformed(ActionEvent e) {
	    expungeMessages();
	}
    }

}





