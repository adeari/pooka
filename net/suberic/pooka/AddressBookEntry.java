package net.suberic.pooka;

/**
 * An Address Book Entry.
 */
public interface AddressBookEntry {

  /**
   * Gets a property on the Vcard.
   */
  public String getProperty(String propertyName);

  /**
   * Gets the InternetAddress associated with this AddressBookEntry.
   */
  public javax.mail.internet.InternetAddress getAddress();

  /**
   * Gets the PersonalName property associated with this AddressBookEntry.
   */
  public String getPersonalName();

  /**
   * Gets the FirstName property associated with this AddressBookEntry.
   */
  public String getFirstName();

  /**
   * Gets the LastName property associated with this AddressBookEntry.
   */
  public String getLastName();

}
