package net.suberic.pooka.gui;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.net.URL;

import java.util.*;

import net.suberic.pooka.*;

/**
 * This shows a dialog which allows you to select addresses from an
 * AddressBook.
 */
public class AddressBookSelectionPanel extends JPanel {

  // the list of addresses available
  JList addressList;
  
  // the list of addresses selected
  JList confirmedList;

  // the filter entry
  JTextField filterField;

  // the AddressEntryTextArea that we're using.
  AddressEntryTextArea entryArea;

  /**
   * Creates a new AddressBookSelectionPanel using the given 
   * AddressEntryTextArea.
   */
  public AddressBookSelectionPanel(AddressEntryTextArea entryTextArea) {
    entryArea = entryTextArea;

    configurePanel();
  }

  /**
   * Sets up the panel.  The entryArea should be set already.
   */
  void configurePanel() {
    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    Box addressBox = new Box(BoxLayout.X_AXIS);

    Box choiceBox = new Box(BoxLayout.Y_AXIS);
    JPanel filterPanel = createFilterPanel();
    createAddressList();
    choiceBox.add(filterPanel);
    choiceBox.add(new JScrollPane(addressList));
    addressBox.add(choiceBox);

    JPanel selectionPanel = createSelectionPanel();
    addressBox.add(selectionPanel);

    confirmedList = new JList();
    addressBox.add(new JScrollPane(confirmedList));

    this.add(addressBox);

    Box buttonBox = createButtonBox();

    this.add(buttonBox);
  }

  /**
   * Creates the filter panel.
   */
  JPanel createFilterPanel() {
    JPanel returnValue = new JPanel();
    filterField = new JTextField(20);
    JButton filterButton = new JButton("Search");
    filterButton.addActionListener(new AbstractAction() {
	public void actionPerformed(ActionEvent e) {
	  doFilter(filterField.getText());
	}
      });

    returnValue.add(filterField);
    returnValue.add(filterButton);
    return returnValue;
  }

  /**
   * Creates the address list.
   */
  void createAddressList() {
    addressList = new JList();
    doFilter("");
  }

  /**
   * Creates the Selection Panel.
   */
  JPanel createSelectionPanel() {
    JPanel returnValue = new JPanel();
    returnValue.setLayout(new BoxLayout(returnValue, BoxLayout.Y_AXIS));

    returnValue.add(Box.createVerticalGlue());

    java.net.URL url = this.getClass().getResource("images/Right.gif");
    JButton addButton = new JButton(new ImageIcon(url));
    addButton.addActionListener(new AbstractAction() {
	public void actionPerformed(ActionEvent e) {
	  confirmSelectedAddresses();
	}
      });
    returnValue.add(addButton);

    returnValue.add(Box.createVerticalGlue());

    url = this.getClass().getResource("images/Left.gif");
    JButton removeButton = new JButton(new ImageIcon(url));
    removeButton.addActionListener(new AbstractAction() {
	public void actionPerformed(ActionEvent e) {
	  removeSelectedAddresses();
	}
      });
    returnValue.add(removeButton);

    returnValue.add(Box.createVerticalGlue());

    return returnValue;
  }

  /**
   * Creates the box with the ok and cancel buttons.
   */
  Box createButtonBox() {
    Box returnValue = Box.createHorizontalBox();

    JButton okButton = new JButton("Ok");
    okButton.addActionListener(new AbstractAction() {
	public void actionPerformed(ActionEvent e) {
	  copySelectionsToEntry();
	  closePanel();
	}
      });
    returnValue.add(okButton);


    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(new AbstractAction() {
	public void actionPerformed(ActionEvent e) {
	  closePanel();
	}
      });
    returnValue.add(cancelButton);

    return returnValue;
  }

  /**
   * Updates the addressList using the results of the filter from the
   * filterField.
   */
  public void doFilter(String filterValue) {
    AddressBookEntry[] matchingValues = getAddressMatcher().match(filterValue);
    addressList.setListData(matchingValues);
  }

  /**
   * Gets the currently selected addresses in the addressList.
   */
  public AddressBookEntry[] getSelectedAddresses() {
    if (addressList != null) {
      Object[] selectedValues = addressList.getSelectedValues();
      if (selectedValues != null) {
	AddressBookEntry[] returnValue = new AddressBookEntry[selectedValues.length];
	for (int i = 0; i < selectedValues.length; i++) {
	  returnValue[i] = (AddressBookEntry)selectedValues[i];
	  return returnValue;
	}
      }
    }

    // else...

    return new AddressBookEntry[0];
  }

  /**
   * Gets all of the confirmed list of addresses.
   */
  public AddressBookEntry[] getConfirmedAddresses() {
    if (confirmedList != null) {
      ListModel confirmedValues = confirmedList.getModel();
      AddressBookEntry[] returnValue = new AddressBookEntry[confirmedValues.getSize()];
      for (int i = 0; i < confirmedValues.getSize(); i++) {
	returnValue[i] = (AddressBookEntry)confirmedValues.getElementAt(i);
	return returnValue;
      }
    }

    // else...

    return new AddressBookEntry[0];
  }

  /**
   * Adds the currently selected address(es) in the addressList to the
   * confirmedList.
   */
  public void confirmSelectedAddresses() {
    AddressBookEntry[] selectedValues = getSelectedAddresses();
    ListModel lm = confirmedList.getModel();
    if (lm instanceof DefaultListModel) {
      DefaultListModel dlm = (DefaultListModel) lm;
      for (int i = 0; selectedValues.length; i++) {
	dlm.addElement(selectedValues[i]);
      }
    } else {
      // we have to rebuild it from scratch.
      
    }
  }

  /**
   * Removed the currently selected address(es) in the confirmedList from the
   * confirmedList.
   */
  public void removeSelectedAddresses() {

  }

  /**
   * Copies the entries from the selection list to the AddressEntryTextArea.
   */
  public void copySelectionsToEntry() {

  }

  /**
   * Closes this panel.
   */
  public void closePanel() {

  }
  
  /**
   * Gets the appropriate AddressMatcher.
   */
  public AddressMatcher getAddressMatcher() {
    return null;
  }
}
