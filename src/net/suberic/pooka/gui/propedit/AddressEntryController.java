package net.suberic.pooka.gui.propedit;
import java.util.*;
import net.suberic.pooka.*;
import net.suberic.util.gui.propedit.*;
import net.suberic.util.VariableBundle;

/**
 * The controller class for the AddressEntry.
 */
public class AddressEntryController extends WizardController {

  AddressBook mBook;
  String mOriginalEntryName = null;

  /**
   * Creates an AddressEntryController.
   */
  public AddressEntryController(String sourceTemplate, WizardEditorPane wep) {
    super(sourceTemplate, wep);
    System.out.println("editing property " + wep.getProperty());
  }

  /**
   * Saves all of the properties for this wizard.
   */
  protected void saveProperties() throws PropertyValueVetoException {
    System.out.println("calling saveProperties.");

    AddressBookEntry entry = mBook.newAddressBookEntry();

    System.out.println("personalname=" + getManager().getCurrentProperty("AddressBook.editor.addressList._newAddress.personalName", ""));
    System.out.println("firstname=" + getManager().getCurrentProperty("AddressBook.editor.addressList._newAddress.firstName", ""));
    System.out.println("lastname=" + getManager().getCurrentProperty("AddressBook.editor.addressList._newAddress.lastName", ""));
    System.out.println("address=" + getManager().getCurrentProperty("AddressBook.editor.addressList._newAddress.address", ""));

    entry.setPersonalName(getManager().getCurrentProperty("AddressBook.editor.addressList._newAddress.personalName", ""));
    entry.setFirstName(getManager().getCurrentProperty("AddressBook.editor.addressList._newAddress.firstName", ""));
    entry.setLastName(getManager().getCurrentProperty("AddressBook.editor.addressList._newAddress.lastName", ""));


    try {
      entry.setAddress(new javax.mail.internet.InternetAddress (getManager().getCurrentProperty("AddressBook.editor.addressList._newAddress.address", "")));
    } catch (Exception e) {
      throw new PropertyValueVetoException(e.getMessage());
    }

    mBook.addAddress(entry);

    // and clear the property.

    getManager().setProperty("AddressBook.editor.addressList._newAddress.personalName", "");
    getManager().setProperty("AddressBook.editor.addressList._newAddress.firstName", "");
    getManager().setProperty("AddressBook.editor.addressList._newAddress.lastName", "");

    getManager().setProperty("AddressBook.editor.addressList._newAddress.address", "");

  }
  /**
   * Finsihes the wizard.
   */
  public void finishWizard() throws PropertyValueVetoException {
    System.out.println("checking state transition (finishWizard).");
    getEditorPane().validateProperty(mState);

    saveProperties();
    getEditorPane().getWizardContainer().closeWizard();
  }

  public void setUniqueProperty(PropertyEditorUI editor, String originalValue, String propertyName) {
    String value = originalValue;
    boolean success = false;
    for (int i = 0 ; ! success &&  i < 10; i++) {
      if (i != 0) {
        value = originalValue + "_" + i;
      }
      try {
        editor.setOriginalValue(value);
        editor.resetDefaultValue();
        getManager().setTemporaryProperty(propertyName, value);
        success = true;
      } catch (PropertyValueVetoException pvve) {
        // on an exception, just start over.
      }
    }
  }

  /**
   * Loads the given entry.
   */
  public void loadEntry(AddressBookEntry pEntry) {
    try {
      System.err.println("loading entry " + pEntry);
      getManager().getPropertyEditor("AddressBook.editor.addressList._newAddress.personalName").setOriginalValue(pEntry.getPersonalName());
      getManager().getPropertyEditor("AddressBook.editor.addressList._newAddress.personalName").resetDefaultValue();
      getManager().getPropertyEditor("AddressBook.editor.addressList._newAddress.firstName").setOriginalValue(pEntry.getFirstName());
      getManager().getPropertyEditor("AddressBook.editor.addressList._newAddress.firstName").resetDefaultValue();
      getManager().getPropertyEditor("AddressBook.editor.addressList._newAddress.lastName").setOriginalValue(pEntry.getLastName());
      getManager().getPropertyEditor("AddressBook.editor.addressList._newAddress.lastName").resetDefaultValue();
      getManager().getPropertyEditor("AddressBook.editor.addressList._newAddress.address").setOriginalValue(pEntry.getAddressString() != null ? pEntry.getAddressString() : "");
      getManager().getPropertyEditor("AddressBook.editor.addressList._newAddress.address").resetDefaultValue();
    } catch (Exception e) {
      e.printStackTrace();
    }
    mOriginalEntryName = pEntry.getPersonalName();
  }

  /**
   * Sets the AddressBook.
   */
  public void setAddressBook(AddressBook pBook) {
    mBook = pBook;
  }
}
