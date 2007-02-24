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

    /*
    Properties storeProperties = createStoreProperties();
    Properties userProperties = createUserProperties();
    Properties smtpProperties = createSmtpProperties();
    //getManager().clearValues();

    addAll(storeProperties);
    addAll(userProperties);
    addAll(smtpProperties);

    // now add the values to the store, user, and smtp server editors,
    // if necessary.
    String accountName = getManager().getCurrentProperty("AddressEntry.editors.store.storeName", "testStore");
    MultiEditorPane mep = (MultiEditorPane) getManager().getPropertyEditor("Store");
    if (mep != null) {
      mep.addNewValue(accountName);
    } else {
      appendProperty("Store", accountName);
    }

    String defaultUser = getManager().getCurrentProperty("AddressEntry.editors.user.userProfile", "__default");
    if (defaultUser.equals("__new")) {
      String userName = getManager().getCurrentProperty("AddressEntry.editors.user.userName", "");
      mep = (MultiEditorPane) getManager().getPropertyEditor("UserProfile");
      if (mep != null) {
        mep.addNewValue(userName);
      } else {
        appendProperty("UserProfile", userName);
      }

      String defaultSmtpServer = getManager().getCurrentProperty("AddressEntry.editors.smtp.outgoingServer", "__default");
      if (defaultSmtpServer.equals("__new")) {
        String smtpServerName = getManager().getCurrentProperty("AddressEntry.editors.smtp.smtpServerName", "");
        mep = (MultiEditorPane) getManager().getPropertyEditor("OutgoingServer");
        if (mep != null) {
          mep.addNewValue(smtpServerName);
        } else {
          // if there's no editor, then set the value itself.
          appendProperty("OutgoingServer", smtpServerName);
        }
      }
    }
    */
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

  /**
   * Creates the smtpProperties from the wizard values.
   */
  public Properties createSmtpProperties() {
    Properties returnValue = new Properties();

    String defaultUser = getManager().getCurrentProperty("AddressEntry.editors.user.userProfile", "__default");
    // only make smtp server changes if there's a new user.
    if (defaultUser.equals("__new")) {
      String userName = getManager().getCurrentProperty("AddressEntry.editors.user.userName", "newuser");

      String defaultSmtpServer = getManager().getCurrentProperty("AddressEntry.editors.smtp.outgoingServer", "__default");
      if (defaultSmtpServer.equals("__new")) {
        String serverName = getManager().getCurrentProperty("AddressEntry.editors.smtp.smtpServerName", "newSmtpServer");
        String server = getManager().getCurrentProperty("AddressEntry.editors.smtp.server", "");
        String port = getManager().getCurrentProperty("AddressEntry.editors.smtp.port", "");
        String authenticated = getManager().getCurrentProperty("AddressEntry.editors.smtp.authenticated", "");
        String user = getManager().getCurrentProperty("AddressEntry.editors.smtp.user", "");
        String password = getManager().getCurrentProperty("AddressEntry.editors.smtp.password", "");

        returnValue.setProperty("OutgoingServer." + serverName + ".server", server);
        returnValue.setProperty("OutgoingServer." + serverName + ".port", port);
        returnValue.setProperty("OutgoingServer." + serverName + ".authenticated", authenticated);
        if (authenticated.equalsIgnoreCase("true")) {

          returnValue.setProperty("OutgoingServer." + serverName + ".user", user );
          returnValue.setProperty("OutgoingServer." + serverName + ".password", password);
        }

        returnValue.setProperty("UserProfile." + userName + ".mailServer", serverName);
      } else {
        returnValue.setProperty("UserProfile." + userName + ".mailServer", defaultSmtpServer);
      }
    }
    return returnValue;
  }

  /**
   * Adds all of the values from the given Properties to the
   * PropertyEditorManager.
   */
  void addAll(Properties props) {
    Set<String> names = props.stringPropertyNames();
    for (String name: names) {
      getManager().setProperty(name, props.getProperty(name));
    }
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
   * Appends the given value to the property.
   */
  public void appendProperty(String property, String value) {
    List<String> current = getManager().getPropertyAsList(property, "");
    current.add(value);
    getManager().setProperty(property, VariableBundle.convertToString(current));
  }

  /**
   * Sets the AddressBook.
   */
  public void setAddressBook(AddressBook pBook) {
    mBook = pBook;
  }
}
