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
 */

public class FolderDisplayPanel extends JPanel {
    JTable messageTable = null;
    JScrollPane scrollPane = null;
    FolderInfo folderInfo = null;
    boolean enabled = true;

    boolean validated = false;
    int scrollToRowOnValidate = -1;

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
      selectFirstUnread();
    }
    
  }

    /**
     * This creates the messageTable.
     */
    public void createMessageTable() {
	messageTable=new JTable(getFolderInfo().getFolderTableModel());

	if (!Pooka.getProperty("FolderTable.showLines", "true").equals("true")) {
	    messageTable.setShowVerticalLines(false);
	    messageTable.setShowHorizontalLines(false);
	}
	
	for (int i = 0; i < messageTable.getColumnCount(); i++) {
	    messageTable.getColumnModel().getColumn(i).setPreferredWidth(getFolderInfo().getFolderTableModel().getColumnSize(i));
	}
	
	messageTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
	
	messageTable.setDefaultRenderer(Object.class, new FilterFolderCellRenderer());
	messageTable.setDefaultRenderer(Number.class, new FilterFolderCellRenderer());

	messageTable.setCellSelectionEnabled(false);
	messageTable.setColumnSelectionAllowed(false);
	messageTable.setRowSelectionAllowed(true);
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
   * This removes rows from the FolderTableModel.  This is the preferred
   * way to remove rows from the FolderTableModel.
   * 
   * Called from within the FolderThread.
   */
  public void removeRows(Vector removedProxies) {
    /*
      This is here so that we can select the next row and remove the 
      removed rows together in one call to the AWTEventThread.
    */
    final Vector removedProxiesTmp = removedProxies;

    try {
      SwingUtilities.invokeAndWait(new Runnable() {
	  public void run() {
	    moveSelectionOnRemoval(removedProxiesTmp);
	    
	    System.err.println("removing rows from foldertablemodel.");
	    System.err.println("current rowcount = " + getFolderTableModel().getRowCount());
	    getFolderTableModel().removeRows(removedProxiesTmp);
	    System.err.println("after removerows, rowcount = " + getFolderTableModel().getRowCount());
	  }
	});
    } catch (Exception e) {
    }
  }
  
    /**
     * This checks to see if the message which has been removed is 
     * currently selected.  If so, we unselect it and select the next
     * row.
     */
  public void moveSelectionOnRemoval(MessageChangedEvent e) {
    try {
      // don't bother if we're just going to autoexpunge it...
      if ((!Pooka.getProperty("Pooka.autoExpunge", "true").equalsIgnoreCase("true")) && e.getMessageChangeType() == MessageChangedEvent.FLAGS_CHANGED && (e.getMessage().isExpunged() || e.getMessage().getFlags().contains(Flags.Flag.DELETED))) {
	final Message changedMessage = e.getMessage();
	SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
	      MessageProxy selectedProxy = getSelectedMessage();
	      if ( selectedProxy != null && selectedProxy.getMessageInfo().getMessage().equals(changedMessage)) {
		selectNextMessage();
	      }
	    }
	  });
      }
    } catch (MessagingException me) {
    }
  }

    /**
     * This checks to see if the message which has been removed is 
     * currently selected.  If so, we unselect it and select the next
     * row.
     */
    public void moveSelectionOnRemoval(MessageCountEvent e) {
      final Message[] removedMsgs = e.getMessages();
      
      SwingUtilities.invokeLater(new Runnable() {
	  public void run() {
	    MessageProxy selectedProxy = getSelectedMessage();
	    if (selectedProxy != null)  {
	      boolean found = false;
	      Message currentMsg = selectedProxy.getMessageInfo().getMessage();
	      for (int i = 0; (currentMsg != null && found == false && i < removedMsgs.length); i++) {
		if (currentMsg.equals(removedMsgs[i])) {
		  found = true;
		}
	      }
	      
	      if (found) {
		selectNextMessage();
	      }
	    }
	  }
	});
      
    }

    /**
     * This checks to see if the message which has been removed is 
     * currently selected.  If so, we unselect it and select the next
     * row.
     *
     * Should be called on the AWTEventThread while the FolderThread
     * is locked.
     */
    private void moveSelectionOnRemoval(Vector removedProxies) {
      MessageProxy selectedProxy = getSelectedMessage();
      if (selectedProxy != null)  {
	if (selectedProxy instanceof MultiMessageProxy) {
	  MultiMessageInfo mmi = (MultiMessageInfo) selectedProxy.getMessageInfo();
	  int messageCount = mmi.getMessageCount();
	  boolean allAreRemoved = true;
	  for (int i = 0; allAreRemoved && i < messageCount; i++) {
	    MessageProxy currentProxy = mmi.getMessageInfo(i).getMessageProxy();
	    if (! removedProxies.contains(currentProxy))
	      allAreRemoved=false;
	  }
	  
	  if (allAreRemoved) {
	    int newMsg = selectNextMessage();
	    System.err.println("allproxies match:  next message is " + newMsg);
	  } 
	} else {
	  if (removedProxies.contains(selectedProxy)) {
	    int newMsg = selectNextMessage();
	    System.err.println("next message is " + newMsg);
	  }
	  
	}
      }
    }

    /**
     * This recreates the message table with a new FolderTableModel.
     */
    public void resetFolderTableModel(FolderTableModel newModel) {
	if (messageTable != null) {
	    FolderTableModel oldFtm = (FolderTableModel) messageTable.getModel();
	    oldFtm.removeTableModelListener(messageTable);
	    //newModel.addTableModelListener(messageTable);
	    messageTable.setModel(newModel);
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
		    if (e.isPopupTrigger()) {
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

		public void mouseReleased(MouseEvent e) {
		    if (e.isPopupTrigger()) {
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

	messageTable.registerKeyboardAction(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    FolderDisplayUI fdui = getFolderInfo().getFolderDisplayUI();
		    if (fdui != null) {
			fdui.selectNextMessage();
		    }
		}
	    }, KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DOWN, 0),  JComponent.WHEN_FOCUSED);

	messageTable.registerKeyboardAction(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    FolderDisplayUI fdui = getFolderInfo().getFolderDisplayUI();
		    if (fdui != null) {
			fdui.selectPreviousMessage();
		    }
		}
	    }, KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_UP, 0),  JComponent.WHEN_FOCUSED);

	messageTable.registerKeyboardAction(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    MessageProxy selectedMessage = getSelectedMessage();
		    String actionCommand = Pooka.getProperty("MessagePanel.2xClickAction", "file-open");
		    if (selectedMessage != null) {
			Action clickAction = selectedMessage.getAction(actionCommand);
			if (clickAction != null && isEnabled()) {
			    clickAction.actionPerformed (new ActionEvent(this, ActionEvent.ACTION_PERFORMED, actionCommand));
			    
			}
		    }
		}
	    }, KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, 0),  JComponent.WHEN_FOCUSED);
	
	messageTable.registerKeyboardAction(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    MessageProxy selectedMessage = getSelectedMessage();
		    String actionCommand = Pooka.getProperty("MessagePanel.2xClickAction", "file-open");
		    if (selectedMessage != null) {
			Action clickAction = selectedMessage.getAction(actionCommand);
			if (clickAction != null && isEnabled()) {
			    clickAction.actionPerformed (new ActionEvent(this, ActionEvent.ACTION_PERFORMED, actionCommand));
			    
			}
		    }
		}
	    }, KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_SPACE, 0),  JComponent.WHEN_FOCUSED);

	
    }
    
  /**
   * This finds the first unread message (if any) and sets that message
   * to selected, and returns that index.
   */
  public void selectFirstUnread() {
    
    // sigh.
    getFolderInfo().getFolderThread().addToQueue(new javax.swing.AbstractAction() {
	public void actionPerformed(java.awt.event.ActionEvent ae) {
	  final int firstUnread = getFolderInfo().getFirstUnreadMessage();
	  SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
	      int useFirstUnread = firstUnread;
	      if (useFirstUnread < 0) {
		useFirstUnread = messageTable.getRowCount();
	      } else {
		messageTable.setRowSelectionInterval(useFirstUnread, useFirstUnread);
	      }
	      makeSelectionVisible(useFirstUnread);
	    }
	  });
	}
      }, new java.awt.event.ActionEvent(this, 0, "folder-select-first-unread"));
    
  }
  
  /**
   * This scrolls the given row number to visible.
   */
  public void makeSelectionVisible(int rowNumber) {
    messageTable.scrollRectToVisible(messageTable.getCellRect(rowNumber, 1, true));
    
    // on 1.3, the window may not be validated yet when we first want
    // to make the selection visible.  so we have a workaround.
    if (!validated) {
      String javaVersion = System.getProperty("java.version");
      
      if (javaVersion.compareTo("1.3") >= 0) {
	scrollToRowOnValidate = rowNumber;
      }
    }
  }
  
  /**
   * This overrides validate() to work around the fact that we may want
   * to scroll the JTable before we've been validated, which doesn't work
   * under 1.3.
   */
  public void validate() {
    super.validate();
    
    if (! validated) {
      validated = true;
      if (scrollToRowOnValidate != -1) {
	makeSelectionVisible(scrollToRowOnValidate);
	scrollToRowOnValidate = -1;
      }
    }
  }

  
  /**
   * This selects the next message.  If no message is selected, then
   * the first message is selected.
   */
  public int selectNextMessage() {
    int selectedRow = messageTable.getSelectedRow();
    int newRow = selectedRow + 1;
    boolean done = false;
    while (! done && newRow < messageTable.getRowCount() ) {
      MessageProxy mp = getFolderInfo().getMessageProxy(newRow);
      try {
	if (mp.getMessageInfo().getFlags().contains(Flags.Flag.DELETED)) {
	  newRow ++;
	} else {
	  done = true;
	}
      } catch (MessagingException me) {
	newRow ++;
      }
    }
    
    return selectMessage(newRow);
  }
  
  /**
   * This selects the previous message.  If no message is selected, then
   * the last message is selected.
   */
  public int selectPreviousMessage() {
    int[] rowsSelected = messageTable.getSelectedRows();
    int selectedRow = 0;
    if (rowsSelected.length > 0)
      selectedRow = rowsSelected[0];
    else
      selectedRow = messageTable.getRowCount();
    int newRow = selectedRow - 1;
    boolean done = false;
    while (! done && newRow >= 0 ) {
      MessageProxy mp = getFolderInfo().getMessageProxy(newRow);
      try {
	if (mp.getMessageInfo().getFlags().contains(Flags.Flag.DELETED)) {
	  newRow--;
	} else {
	  done = true;
	}
      } catch (MessagingException me) {
	newRow--;
      }
    }
    
    return selectMessage(newRow);
    
  }
  
  /**
   * Selects all of the messages in the FolderTable.
   */
  public void selectAll() {
    messageTable.selectAll();
  }
  
  /**
   * This selects the message at the given row, and also scrolls the
   * MessageTable to make the given row visible.
   *
   * If the number entered is below the range of available messages, then
   * the first message is selected.  If the number entered is above that
   * range, then the last message is selected.  If the MessageTable 
   * contains no messages, nothing happens.
   *
   * @return  the index of the newly selected row.
   */
  public int selectMessage(int messageNumber) {
    int rowCount = messageTable.getRowCount();
      
    System.err.println("selecting message " + messageNumber + " of " + rowCount);
    if (rowCount > 0) {
      int numberToSet = messageNumber;
      
      if (messageNumber < 0) {
	numberToSet = 0;
      } else if (messageNumber >= rowCount) {
	numberToSet = rowCount - 1;
      }
      messageTable.setRowSelectionInterval(numberToSet, numberToSet);
      makeSelectionVisible(numberToSet);
      return numberToSet;
    } else {
      return -1;
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
   * This updates the entry for the given message, if that message is 
   * visible.
   */
  public void repaintMessage(MessageProxy mp) {
    int row = getFolderTableModel().getRowForMessage(mp);
    if (row >=0) {
      getFolderTableModel().fireTableRowsUpdated(row, row);
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
  
  /**
   * Returns the FolderTableModel for this FolderDisplayPanel.
   */
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





