package net.suberic.pooka.gui;
import net.suberic.pooka.*;
import net.suberic.util.*;
import net.suberic.util.gui.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.Cursor;

/**
 * A property editor which edits an AddressBook.
 */
public class AddressBookEditorPane extends DefaultPropertyEditor {
  
  String property;
  AddressBook book;
  JPanel searchEntryPanel;
  JTextField searchEntryField;
  JTable addressTable;

  VariableBundle sourceBundle = null;

  public AddressBookEditorPane(String newProperty, String newTemplateType, VariableBundle bundle, boolean isEnabled) {
    configureEditor(newProperty, newTemplateType, bundle, isEnabled);
  }

  public AddressBookEditorPane(String newProperty, String newTemplateType, VariableBundle bundle) {
    configureEditor(newProperty, newTemplateType, bundle, true);
  }
  
  public void configureEditor(PropertyEditorFactory factory, String newProperty, String newTemplateType, VariableBundle bundle, boolean isEnabled) {
    sourceBundle = bundle;

    property=newProperty;
    // we're going to have "AddressBook." at the beginning, and 
    // ".addressListEditor" at the end...
    String bookName = property.substring(12, property.length() - 18);
    book = Pooka.getAddressBookManager().getAddressBook(bookName);

    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    this.setBorder(BorderFactory.createEtchedBorder());

    createSearchEntryPanel();
    createAddressTable();

    labelComponent = this;
    
    this.add(searchEntryPanel);
    this.add(addressTable);
  }

  /**
   * Creates the panel which has the entry fields -- i.e., "Enter string to
   * match", an entry field, and a search button.
   */
  public void createSearchEntryPanel() {
    searchEntryPanel = new JPanel();
    searchEntryPanel.add(new JLabel(sourceBundle.getProperty("AddressBookEditor.matchString", "Match String: ")));

    searchEntryField = new JTextField(30);
    searchEntryPanel.add(searchEntryField);

    Action a = new SearchAction();

    JButton searchButton = new JButton(sourceBundle.getProperty("AddressBookEditor.title.Search", "Search"));
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
	  if (SwingUtilities.isRightMouseButton(e)) {
	    // see if anything is selected
	    int rowIndex = addressTable.rowAtPoint(e.getPoint());
	    if (rowIndex == -1 || !addressTable.isRowSelected(rowIndex) ) {
	      addressTable.setRowSelectionInterval(rowIndex, rowIndex);
	    }
	    
	    showPopupMenu();
	  }
	}
      });
    
    updateTableModel(new AddressBookEntry[0]);
    
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
    
  }

  /**
   * Brings up the current popup menu.
   */
  public void showPopupMenu() {

  }

  /**
   * Updates the TableModel with the new entries.
   */
  public void updateTableModel(AddressBookEntry[] entries) {
    AddressBookTableModel newTableModel = new AddressBookTableModel(entries);
    addressTable.setModel(newTableModel);
  }

  public void setValue() {
  }
  
  public java.util.Properties getValue() {
    return new java.util.Properties();
  }
  
  public void resetDefaultValue() {
  }
  
  public boolean isChanged() {
    return false;
  }

  public void setEnabled(boolean newValue) {
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
	return currentEntry.getPersonalName();
      }
      if (column == 1) {
	return currentEntry.getFirstName();
      }
      if (column == 2) {
	return currentEntry.getLastName();
      }
      if (column == 3) {
	return currentEntry.getAddress();
      }

      return null;
    }

    /**
     * Returns the AddressBookEntry at the given index.
     */
    public AddressBookEntry getEntryAt(int index) {
      return entries[index];
    }
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

}
