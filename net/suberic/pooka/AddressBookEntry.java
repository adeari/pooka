package net.suberic.pooka;

/**
 * An Address Book Entry.
 */
public interface AddressBookEntry {

  /**
   * Gets a property on the AddressBookEntry.
   */
  public String getProperty(String propertyName);


  /**
   * Sets a property on the AddressBookEntry.
   */
  public void setProperty(String propertyName, String value);

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

  /**
   * sets the InternetAddress associated with this AddressBookEntry.
   */
  public void setAddress(javax.mail.internet.InternetAddress newAddress);

  /**
   * Gets the PersonalName property associated with this AddressBookEntry.
   */
  public void setPersonalName(String newName);

  /**
   * Gets the FirstName property associated with this AddressBookEntry.
   */
  public void setFirstName(String newName);

  /**
   * Gets the LastName property associated with this AddressBookEntry.
   */
  public void setLastName(String newName);

}
