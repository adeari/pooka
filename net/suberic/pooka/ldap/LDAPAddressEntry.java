package net.suberic.pooka.ldap;
import net.suberic.pooka.*;

import javax.naming.*;
import javax.naming.directory.*;

/**
 * An Address Book Entry.
 */
public class LDAPAddressEntry implements AddressBookEntry {
 
  /**
   * Creates an LDAPAddressEntry from a given Attribute set.
   */
  public LDAPAddressEntry(Attributes newAttr) {

  }

  /**
   * Gets a property on the LDAPAddressEntry.
   */
  public String getProperty(String propertyName) {
    return null;
  }


  /**
   * Sets a property on the LDAPAddressEntry.
   */
  public void setProperty(String propertyName, String value) {

  }

  /**
   * Gets the InternetAddress associated with this LDAPAddressEntry.
   */
  public javax.mail.internet.InternetAddress getAddress() {
    return null;
  }

  /**
   * Gets the PersonalName property associated with this LDAPAddressEntry.
   */
  public String getPersonalName() {
    return null;
  }

  /**
   * Gets the FirstName property associated with this LDAPAddressEntry.
   */
  public String getFirstName() {
    return null;
  }

  /**
   * Gets the LastName property associated with this LDAPAddressEntry.
   */
  public String getLastName() {
    return null;
  }

  /**
   * sets the InternetAddress associated with this LDAPAddressEntry.
   */
  public void setAddress(javax.mail.internet.InternetAddress newAddress) {

  }

  /**
   * Gets the PersonalName property associated with this LDAPAddressEntry.
   */
  public void setPersonalName(String newName) {

  }

  /**
   * Gets the FirstName property associated with this LDAPAddressEntry.
   */
  public void setFirstName(String newName) {

  }

  /**
   * Gets the LastName property associated with this LDAPAddressEntry.
   */
  public void setLastName(String newName) {

  }

  /**
   * Gets a Properties representation of the values in the LDAPAddressEntry.
   */
  public java.util.Properties getProperties() {
    return null;
  }

}
