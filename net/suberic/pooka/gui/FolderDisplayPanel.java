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
 * This is a JPanel which contains a JTable which displays the messages in
 * the table.
 *
 * Note that this class does not actually do any real work.  It does have 
 * Actions, but those are just passed on from the MessageProxy object in
 * the table.  You will need to have another component which implements 
 * FolderDisplayUI to use as the actual UI object for the Folder.  That
 * component can then use the FolderDisplayPanel to display the messages.
 *
 * Note also that this class does not add a MouseListener to the JTable.
 * Again, the parent FolderDisplayUI will need to add that.  This way,
 * the FolderDisplayPanel may be used by parent components which want to
 * have it perform different actions when messages are selected.
 */

public class FolderDisplayPanel extends JPanel {
    JTable messageTable = null;
    JScrollPane scrollPane = null;
    FolderInfo folderInfo = null;
    boolean enabled = true;

    /**
     * Creates an empty FolderDisplayPanel.
     */
    public FolderDisplayPanel() {
	initWindow();
	enabled=false;
    }

    /**
     * Creates a FolderDisplayPanel for the given FolderInfo.
     */

    public FolderDisplayPanel(FolderInfo newFolderInfo) {
	initWindow();
	setFolderInfo(newFolderInfo);
	addMessageTable();
    }

    /**
     * Initializes the window. 
     */

    public void initWindow() {
	scrollPane = new JScrollPane();
	this.setLayout(new BorderLayout());
	this.add("Center", scrollPane);

	this.setPreferredSize(new Dimension(Integer.parseInt(Pooka.getProperty("folderWindow.height", "570")), Integer.parseInt(Pooka.getProperty("folderWindow.width","380"))));
	
	// if the FolderDisplayPanel itself gets the focus, pass it on to
	// the messageTable
	this.addFocusListener(new FocusAdapter() {
		public void focusGained(FocusEvent e) {
		    if (messageTable != null)
			messageTable.requestFocus();
		    if (getFolderInfo() != null && getFolderInfo().hasNewMessages()) {
			getFolderInfo().setNewMessages(false);
			FolderNode fn = getFolderInfo().getFolderNode();
			if (fn != null)
			    fn.getParentContainer().repaint();
		    }
		}
	    });
    }

    /**
     * Creates the JTable for the FolderInfo and adds it to the component.
     */
    public void addMessageTable() {
	if (folderInfo != null) {
	    createMessageTable();
	    scrollPane.getViewport().add(messageTable);
	    int lastUnread = selectFirstUnread();
	    makeSelectionVisible(lastUnread);
	}
	    
    }

    /**
     * This creates the messageTable.
     */
    public void createMessageTable() {
	messageTable=new JTable(getFolderInfo().getFolderTableModel());
	getFolderInfo().getFolderTableModel().addTableModelListener(messageTable);

	if (!Pooka.getProperty("FolderTable.showLines", "true").equals("true")) {
	    messageTable.setShowVerticalLines(false);
	    messageTable.setShowHorizontalLines(false);
	}
	
	for (int i = 0; i < messageTable.getColumnCount(); i++) {
	    messageTable.getColumnModel().getColumn(i).setPreferredWidth(getFolderInfo().getFolderTableModel().getColumnSize(i));
	}
	
	messageTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
	
	messageTable.setDefaultRenderer(Object.class, new DefaultFolderCellRenderer());
	messageTable.setDefaultRenderer(Number.class, new DefaultFolderCellRenderer());

	addListeners();
    }
    
    /**
     * This removes the current message table.
     */
    public void removeMessageTable() {
	if (messageTable != null) {
	    scrollPane.getViewport().remove(messageTable);
	    if (getFolderInfo() != null)
		getFolderInfo().getFolderTableModel().removeTableModelListener(messageTable);
	    messageTable = null;
	}
    }

    /**
     * This adds all the listeners to the current FolderDisplayPanel.
     */

    public void addListeners() {
	// add a mouse listener 
	
	messageTable.addMouseListener(new MouseAdapter() {
		public void mouseClicked(MouseEvent e) {
		    if (e.getClickCount() == 2) {
			int rowIndex = getMessageTable().rowAtPoint(e.getPoint());
			if (rowIndex != -1) {
			    getMessageTable().setRowSelectionInterval(rowIndex, rowIndex);
			    MessageProxy selectedMessage = getSelectedMessage();
			    String actionCommand = Pooka.getProperty("MessagePanel.2xClickAction", "file-open");
			    if (selectedMessage != null) {
				Action clickAction = selectedMessage.getAction(actionCommand);
				if (clickAction != null && isEnabled()) {
				    clickAction.actionPerformed (new ActionEvent(this, ActionEvent.ACTION_PERFORMED, actionCommand));
				    
				}
			    }
			}
		    }
		}
		
		public void mousePressed(MouseEvent e) {
		    if (SwingUtilities.isRightMouseButton(e)) {
			// see if anything is selected
			int rowIndex = getMessageTable().rowAtPoint(e.getPoint());
			if (rowIndex == -1 || !getMessageTable().isRowSelected(rowIndex) ) {
			    getMessageTable().setRowSelectionInterval(rowIndex, rowIndex);
			}
			
			MessageProxy selectedMessage = getSelectedMessage();
			if (selectedMessage != null && isEnabled())
			    selectedMessage.showPopupMenu(getMessageTable(), e);
		    }
		}
	    });

	messageTable.getSelectionModel().addListSelectionListener(new SelectionListener());

	// add sorting by header.

	messageTable.getTableHeader().addMouseListener(new MouseAdapter() {
		public void mouseClicked(MouseEvent e) {
		    TableColumnModel columnModel = messageTable.getColumnModel();
		    int viewColumn = columnModel.getColumnIndexAtX(e.getX()); 
		    int column = messageTable.convertColumnIndexToModel(viewColumn); 
		    if (e.getClickCount() == 1 && column != -1) {
			if (Pooka.isDebug())
			    System.out.println("Sorting ..."); 

			int shiftPressed = e.getModifiers()&InputEvent.SHIFT_MASK; 
			boolean ascending = (shiftPressed == 0); 
			
			MessageProxy selectedMessage = null;

			int rowsSelected = messageTable.getSelectedRowCount();

			if (rowsSelected == 1) 
			    selectedMessage = getFolderInfo().getMessageProxy(messageTable.getSelectedRow());
			else if (rowsSelected > 1)
			    selectedMessage = getFolderInfo().getMessageProxy(messageTable.getSelectedRows()[0]);

			((FolderTableModel)messageTable.getModel()).sortByColumn(column, ascending); 

			if (selectedMessage != null) {
			    int selectedIndex = ((FolderTableModel)messageTable.getModel()).getRowForMessage(selectedMessage);
			    messageTable.setRowSelectionInterval(selectedIndex, selectedIndex);
			    makeSelectionVisible(selectedIndex);
			}
		    }
		}
	    });
    }
    
    /**
     * This finds the first unread message (if any) and sets that message
     * to selected, and returns that index.
     */
    public int selectFirstUnread() {
	int firstUnread = getFolderInfo().getFirstUnreadMessage();
	if (firstUnread < 0)
	    firstUnread = messageTable.getRowCount();
	else 
	    messageTable.setRowSelectionInterval(firstUnread, firstUnread);

	return firstUnread;
    }

    /**
     * This scrolls the given row number to visible.
     */
    public void makeSelectionVisible(int rowNumber) {
	String javaVersion = System.getProperty("java.version");

        if (javaVersion.compareTo("1.3") >= 0) {

	    // this really is awful.  i hope that they fix getCellRect() and
	    // scrollToRect in 1.3

	    int rowHeight = messageTable.getRowHeight();
	    JScrollBar vsb = scrollPane.getVerticalScrollBar();
	    int newValue = Math.min(rowHeight * (rowNumber - 1), vsb.getMaximum() - vsb.getModel().getExtent());
	    vsb.setValue(newValue);
	    newValue = Math.min(rowHeight * (rowNumber - 1), vsb.getMaximum() - vsb.getModel().getExtent());
	    vsb.setValue(newValue);
	} else {
	    messageTable.scrollRectToVisible(messageTable.getCellRect(rowNumber, 1, true));
	}
    }


    /**
     * This method takes the currently selected row(s) and returns the
     * appropriate MessageProxy object.
     *
     * If no rows are selected, null is returned.
     */
    public MessageProxy getSelectedMessage() {
	if (messageTable != null) {
	    int rowsSelected = messageTable.getSelectedRowCount();
	    
	    if (rowsSelected == 1) 
		return getFolderInfo().getMessageProxy(messageTable.getSelectedRow());
	    else if (rowsSelected < 1) 
		return null;
	    else {
		int[] selectedRows = messageTable.getSelectedRows();
		MessageProxy[] msgSelected= new MessageProxy[selectedRows.length];
		for (int i = 0; i < selectedRows.length; i++) 
		    msgSelected[i] = getFolderInfo().getMessageProxy(selectedRows[i]);
		return new MultiMessageProxy(selectedRows, msgSelected, this.getFolderInfo());
	    }
	} else {
	    return null;
	}
    }

    /**
     * This resets the size to that of the parent component.
     */
    public void resize() {
	this.setSize(getParent().getSize());
    }

    // Accessor methods.

    public JTable getMessageTable() {
	return messageTable;
    }

    /**
     * This sets the FolderInfo.
     */
    public void setFolderInfo(FolderInfo newValue) {
	folderInfo=newValue;
    }

    public FolderInfo getFolderInfo() {
	return folderInfo;
    }

    public FolderTableModel getFolderTableModel() {
	if (getFolderInfo() != null)
	    return getFolderInfo().getFolderTableModel();
	else
	    return null;
    }

    /**
     * gets the actions handled both by the FolderDisplayPanel and the 
     * selected Message(s).
     */

    public class SelectionListener implements javax.swing.event.ListSelectionListener {
	SelectionListener() {
	}

	public void valueChanged(javax.swing.event.ListSelectionEvent e) {
	    Pooka.getMainPanel().refreshActiveMenus();
	    getFolderInfo().setNewMessages(false);
	    FolderNode fn = getFolderInfo().getFolderNode();
	    if (fn != null)
		fn.getParentContainer().repaint();
	}
    }

    /**
     * This registers the Keyboard action not only for the FolderDisplayPanel
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

	messageTable.registerKeyboardAction(anAction, aCommand, aKeyStroke, aCondition);
    }
    
    /**
     * This unregisters the Keyboard action not only for the FolderDisplayPanel
     * itself, but also for pretty much all of its children, also.  This
     * is to work around something which I think is a bug in jdk 1.2.
     * (this is not really necessary in jdk 1.3.)
     *
     * Overrides JComponent.unregisterKeyboardAction(KeyStroke aKeyStroke)
     */

    public void unregisterKeyboardAction(KeyStroke aKeyStroke) {
	super.unregisterKeyboardAction(aKeyStroke);

	messageTable.unregisterKeyboardAction(aKeyStroke);
    }

    /**
     * Returns whether or not this window is enabled.  This should be true
     * just about all of the time.  The only time it won't be true is if
     * the Folder is closed or disconnected, and the mail store isn't set
     * up to work in disconnected mode.
     */
    public boolean isEnabled() {
	return enabled;
    }

    /**
     * This sets whether or not the window is enabled.  This should only
     * be set to false when the Folder is no longer available.
     */
    public void setEnabled(boolean newValue) {
	enabled = newValue;
    }

    public Action[] getActions() {
	if (isEnabled()) {
	    Action[] returnValue = null;
	    MessageProxy m = getSelectedMessage();
	    
	    if (m != null) 
		returnValue = m.getActions();
	    
	    if (folderInfo.getActions() != null) {
		if (returnValue != null) {
		    returnValue = TextAction.augmentList(folderInfo.getActions(), returnValue);
		} else {
		    returnValue = folderInfo.getActions();
		}
	    }
	    
	    return returnValue;
	} else {
	    return null;
	}
    }
}





