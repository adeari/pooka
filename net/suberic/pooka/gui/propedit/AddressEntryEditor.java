package net.suberic.pooka.gui.propedit;
import net.suberic.pooka.*;
import net.suberic.util.*;
import net.suberic.util.gui.*;
import net.suberic.util.gui.propedit.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.Cursor;
import java.util.Properties;

/**
 * A property editor which edits an AddressBookEntry.
 */
public class AddressEntryEditor extends CompositeEditorPane {
  AddressBookEntry entry;

  /**
   * Creates an AddressEntryEditor from an AddressBookEntry and a 
   * VariableBundle.
   */
  public AddressEntryEditor(PropertyEditorManager newManager, AddressBookEntry newEntry) {
    System.err.println("creating an AddressEntryEditor.");
    entry = newEntry;

    Properties props = entry.getProperties();
    VariableBundle wrappedBundle = new VariableBundle(props, newManager.getFactory().getSourceBundle());
    PropertyEditorManager wrappedManager = new PropertyEditorManager(wrappedBundle, newManager.getFactory());
    configureEditor("currentAddress", "currentAddress", wrappedManager, true);
  }

  /**
   * Sets the values to the current entry.
   */
  public void setValue() throws PropertyValueVetoException {
    if (isEnabled()) {
      for (int i = 0; i < editors.size(); i++) {
	((SwingPropertyEditor)(editors.get(i))).setValue();
      }
    }

    try {
      entry.setAddresses(javax.mail.internet.InternetAddress.parse(manager.getProperty("currentAddress.address", "")));
    } catch (javax.mail.internet.AddressException ae) {
      ae.printStackTrace();

    }
    entry.setPersonalName(manager.getProperty("currentAddress.personalName", ""));
    entry.setFirstName(manager.getProperty("currentAddress.firstName", ""));
    entry.setLastName(manager.getProperty("currentAddress.lastName", ""));
  }

}
