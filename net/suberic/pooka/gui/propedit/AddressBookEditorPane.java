package net.suberic.pooka.gui.propedit;
import net.suberic.pooka.*;
import net.suberic.util.*;
import net.suberic.util.gui.propedit.*;
import net.suberic.pooka.gui.propedit.*;
import net.suberic.util.gui.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.Cursor;
import javax.swing.Action;

/**
 * A property editor which edits an AddressBook.
 */
public class AddressBookEditorPane extends SwingPropertyEditor {
  
  AddressBook book;
  JPanel editPanel;
  JPanel searchEntryPanel;
  JTextField searchEntryField;
  JTable addressTable;
  JButton editButton, addButton, deleteButton, searchButton;
  boolean enabled = true;

  PropertyEditorManager manager;

  Action[] defaultActions = new Action[] {
    new AddAction(),
    new EditAction(),
    new DeleteAction()
      };

  ConfigurablePopupMenu popupMenu;

  /**
   * @param propertyName The property to be edited.  
   * @param template The property that will define the layout of the 
   *                 editor.
   * @param manager The PropertyEditorManager that will manage the
   *                   changes.
   * @param isEnabled Whether or not this editor is enabled by default. 
   */
  public void configureEditor(String propertyName, String template, PropertyEditorManager newManager, boolean isEnabled) {
    property=propertyName;
    manager=newManager;
    editorTemplate = template;
    originalValue = manager.getProperty(property, "");

    // we're going to have "AddressBook." at the beginning, and 
    // ".addressListEditor" at the end...
    String bookName = property.substring(12, property.length() - 18);
    book = Pooka.getAddressBookManager().getAddressBook(bookName);

    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    this.setBorder(BorderFactory.createEtchedBorder());

    createSearchEntryPanel();
    createEditPanel();
    createAddressTable();

    labelComponent = this;
    
    this.add(searchEntryPanel);
    //this.add(new JScrollPane(addressTable));
    JScrollPane addressPane = new JScrollPane(addressTable);
    try {
      addressPane.setPreferredSize(new java.awt.Dimension(Integer.parseInt(manager.getProperty("Pooka.addressBookEditor.hsize", "300")), Integer.parseInt(manager.getProperty("Pooka.addressBookEditor.vsize", "100"))));
    } catch (Exception e) {
      addressPane.setPreferredSize(new java.awt.Dimension(300, 100));
    }
    this.add(addressPane);
    this.add(editPanel);

    popupMenu = new ConfigurablePopupMenu();
    popupMenu.configureComponent("AddressBookEditor.popupMenu", manager.getFactory().getSourceBundle());
    popupMenu.setActive(getActions());

    this.setEnabled(isEnabled);
  }

  /**
   * Creates the panel which has the entry fields -- i.e., "Enter string to
   * match", an entry field, and a search button.
   */
  public void createSearchEntryPanel() {
    searchEntryPanel = new JPanel();
    searchEntryPanel.add(new JLabel(manager.getProperty("AddressBookEditor.matchString", "Match String: ")));

    searchEntryField = new JTextField(30);
    searchEntryPanel.add(searchEntryField);

    Action a = new SearchAction();

    searchButton = new JButton(manager.getProperty("AddressBookEditor.title.Search", "Search"));
    searchButton.addActionListener(a);
    searchEntryPanel.add(searchButton);
    
  }

  /**
   * Creates the AddressTable.
   */
  public void createAddressTable() {    
    addressTable = new JTable();
    addressTable.setCellSelectionEnabled(false);
    addressTable.setColumnSelectionAllowed(false);
    addressTable.setRowSelectionAllowed(true);

    addressTable.addMouseListener(new MouseAdapter() {
	public void mouseClicked(MouseEvent e) {
	  if (e.getClickCount() == 2) {
	    int rowIndex = addressTable.rowAtPoint(e.getPoint());
	    if (rowIndex != -1) {
	      addressTable.setRowSelectionInterval(rowIndex, rowIndex);
	      AddressBookEntry selectedEntry = getSelectedEntry();
	      if (selectedEntry != null) {
		editEntry(selectedEntry);
	      }
	    }
	  }
	}
	
	public void mousePressed(MouseEvent e) {
	  if (e.isPopupTrigger()) {
	    // see if anything is selected
	    int rowIndex = addressTable.rowAtPoint(e.getPoint());
	    if (rowIndex == -1 || !addressTable.isRowSelected(rowIndex) ) {
	      addressTable.setRowSelectionInterval(rowIndex, rowIndex);
	    }
	    
	    showPopupMenu(addressTable, e);
	  }
	}

	public void mouseReleased(MouseEvent e) {
	  if (e.isPopupTrigger()) {
	    // see if anything is selected
	    int rowIndex = addressTable.rowAtPoint(e.getPoint());
	    if (rowIndex == -1 || !addressTable.isRowSelected(rowIndex) ) {
	      addressTable.setRowSelectionInterval(rowIndex, rowIndex);
	    }
	    
	    showPopupMenu(addressTable, e);
	  }
	}
      });
    
    updateTableModel(new AddressBookEntry[0]);
    
  }
  
  /**
   * Creates the panel which has the editor fields, such as add/delete/edit
   * buttons.
   */
  public void createEditPanel() {
    editPanel = new JPanel();

    Action a = new AddAction();
    addButton = new JButton(manager.getProperty("AddressBookEditor.title.Add", "Add"));
    addButton.addActionListener(a);
    editPanel.add(addButton);

    a = new EditAction();
    editButton = new JButton(manager.getProperty("AddressBookEditor.title.Edit", "Edit"));
    editButton.addActionListener(a);
    editPanel.add(editButton);

    a = new DeleteAction();
    deleteButton = new JButton(manager.getProperty("AddressBookEditor.title.Delete", "Delete"));
    deleteButton.addActionListener(a);
    editPanel.add(deleteButton);
    
  }

  /**
   * Performs a search using the string value in the searchEntryField.  Updates
   * the addressTable with the results.
   */
  public void performSearch() {
    AddressBookEntry[] matchingEntries = book.getAddressMatcher().match(searchEntryField.getText());
    updateTableModel(matchingEntries);
  }

  /**
   * Adds a new entry.
   */
  public void performAdd() {
    AddressBookEntry newEntry = new net.suberic.pooka.vcard.Vcard(new java.util.Properties());
    try {
      newEntry.setAddresses(new javax.mail.internet.InternetAddress[] { new javax.mail.internet.InternetAddress("example@example.com") });
    } catch (Exception e) { }
    if (newEntry.getAddresses() != null) {
      book.addAddress(newEntry);
      ((AddressBookTableModel)addressTable.getModel()).addEntry(newEntry);
    }
    editEntry(newEntry);
  }

  /**
   * Edits the current entry.
   */
  public void performEdit() {
    AddressBookEntry e = getSelectedEntry();
    if (e != null)
      editEntry(e);
  }

  /**
   * Deletes the current entry.
   */
  public void performDelete() {
    AddressBookEntry e = getSelectedEntry();
    if (e != null) {
      book.removeAddress(e);
      ((AddressBookTableModel)addressTable.getModel()).removeEntry(e);
    }
  }

  /**
   * Gets the currently selected entry.
   */
  public AddressBookEntry getSelectedEntry() {
    int index = addressTable.getSelectedRow();
    if (index > -1)
      return ((AddressBookTableModel)addressTable.getModel()).getEntryAt(index);
    else
      return null;
  }

  /**
   * Brings up an editor for the current entry.
   */
  public void editEntry(AddressBookEntry entry) {
    AddressEntryEditor editor = new AddressEntryEditor(manager, entry);
    manager.getFactory().showNewEditorWindow(manager.getProperty("AddressEntryEditor.title", "Address Entry"), editor);
  }

  /**
   * Brings up the current popup menu.
   */
  public void showPopupMenu(JComponent component, MouseEvent e) {
    popupMenu.show(component, e.getX(), e.getY());
  }

  /**
   * Updates the TableModel with the new entries.
   */
  public void updateTableModel(AddressBookEntry[] entries) {
    AddressBookTableModel newTableModel = new AddressBookTableModel(entries);
    addressTable.setModel(newTableModel);
  }

  public void setValue() {
    if (book != null) {
      try {
	book.saveAddressBook();
      } catch (Exception e) {
	Pooka.getUIFactory().showError(Pooka.getProperty("error.AddressBook.saveAddressBook", "Error saving Address Book:  ") + e.getMessage());
	e.printStackTrace();
      }
    }
  }
  
  public java.util.Properties getValue() {
    return new java.util.Properties();
  }
  
  public void resetDefaultValue() {
    try {
      book.loadAddressBook();
    } catch (Exception e) {
      Pooka.getUIFactory().showError(Pooka.getProperty("error.AddressBook.loadAddressBook", "Error reloading Address Book:  ") + e.getMessage());
      e.printStackTrace();
    }
    performSearch();
  }
  
  public boolean isChanged() {
    return false;
  }

  public void setEnabled(boolean newValue) {
    if (book != null)
      enabled = newValue;
    else
      enabled = false;

    searchButton.setEnabled(enabled);
    addButton.setEnabled(enabled);
    editButton.setEnabled(enabled);
    deleteButton.setEnabled(enabled);
    searchEntryField.setEnabled(enabled);
  }


  public void setBusy(boolean newValue) {
    if (newValue)
      this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    else
      this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
  }

  public class AddressBookTableModel extends javax.swing.table.AbstractTableModel {
    
    AddressBookEntry[] entries;

    public AddressBookTableModel(AddressBookEntry[] newEntries) {
      entries = newEntries;
    }

    public int getRowCount() {
      return entries.length;
    }

    public int getColumnCount() {
      return 4;
    }

    public String getColumnName(int index) {
      if (index == 0) {
	return Pooka.getProperty("AddressBookTable.personalName", "Name");
      } else if (index == 1) {
	return Pooka.getProperty("AddressBookTable.firstName", "First Name");
      } else if (index == 2) {
	return Pooka.getProperty("AddressBookTable.lastName", "Last Name");
      } else if (index == 3) {
	return Pooka.getProperty("AddressBookTable.address", "Email Address");
      } else {
	return null;
      }
    }

    public Object getValueAt(int row, int column) {
      if (row < 0 || column < 0 || row >= getRowCount() || column >= getColumnCount())
	return null;

      AddressBookEntry currentEntry = entries[row];

      if (column == 0) {
	return currentEntry.getID();
      }
      if (column == 1) {
	return currentEntry.getFirstName();
      }
      if (column == 2) {
	return currentEntry.getLastName();
      }
      if (column == 3) {
	return currentEntry.getAddressString();
      }

      return null;
    }

    /**
     * Returns the AddressBookEntry at the given index.
     */
    public AddressBookEntry getEntryAt(int index) {
      return entries[index];
    }
    
    /**
     * Adds the given AddressBookEntry to the end of the table.
     */
    public void addEntry(AddressBookEntry e) {
      AddressBookEntry[] newEntries;
      int length; 

      if (entries != null) {
	length = entries.length;
	newEntries = new AddressBookEntry[length + 1];
	System.arraycopy(entries, 0, newEntries, 0, length);
      } else {
	length = 0;
	newEntries = new AddressBookEntry[1];
      }
      newEntries[length] = e;

      entries = newEntries;

      fireTableRowsInserted(length, length);
    }

    /**
     * Removes the given AddressBookEntry from the table, if present.
     */
    public void removeEntry(AddressBookEntry e) {
      boolean found = false;

      for (int i = 0; !found && i < entries.length; i++) {
	if (e == entries[i]) {
	  found = true;
	  int removedRow = i;
	  AddressBookEntry[] newEntries = new AddressBookEntry[entries.length - 1];
	  if (removedRow != 0)
	    System.arraycopy(entries, 0, newEntries, 0, removedRow);

	  if (removedRow != entries.length -1) 
	    System.arraycopy(entries, removedRow + 1, newEntries, removedRow, entries.length - removedRow - 1);

	  entries = newEntries;
	  fireTableRowsDeleted(removedRow, removedRow);
	}
      }
    }
  }

  /**
   * Returns the actions associated with this editor.
   */
  public Action[] getActions() {
    return defaultActions;
  }

  public class SearchAction extends AbstractAction {
    public SearchAction() {
      super("address-search");
    }

    public void actionPerformed(ActionEvent e) {
      setBusy(true);
      performSearch();
      setBusy(false);
    }
  }

  public class AddAction extends AbstractAction {
    public AddAction() {
      super("address-add");
    }

    public void actionPerformed(ActionEvent e) {
      setBusy(true);
      performAdd();
      setBusy(false);
    }
  }

  public class EditAction extends AbstractAction {
    public EditAction() {
      super("address-edit");
    }

    public void actionPerformed(ActionEvent e) {
      setBusy(true);
      performEdit();
      setBusy(false);
    }
  }

  public class DeleteAction extends AbstractAction {
    public DeleteAction() {
      super("address-delete");
    }

    public void actionPerformed(ActionEvent e) {
      setBusy(true);
      performDelete();
      setBusy(false);
    }
  }

}
