package net.suberic.pooka.ldap;
import net.suberic.pooka.*;
import net.suberic.util.VariableBundle;
import javax.naming.*;

/**
 * An Address Book that is accessed through LDAP.
 */
public class LDAPAddressBook implements AddressBook, AddressMatcher {

  InitialDirContext initialContext;

  /**
   * Creates a new LDAPAddressBook from the given name and the given
   * VariableBundle.
   */
  public LDAPAddressBook(String property, VariableBundle bundle) {

  }

  // AddressMatcher

  /**
   * Returns all of the InternetAddresses which match the given String.
   */
  public InternetAddress[] match(String matchString) {

  }

  /**
   * Returns all of the InternetAddresses whose FirstName matches the given 
   * String.
   */
  public InternetAddress[] matchFirstName(String matchString) {

  }

  /**
   * Returns all of the InternetAddresses whose LastName matches the given 
   * String.
   */
  public InternetAddress[] matchLastName(String matchString) {

  }

  /**
   * Returns all of the InternetAddresses whose email addresses match the
   * given String.
   */
  public InternetAddress[] matchEmailAddress(String matchString) {

  }

  /**
   * Returns the InternetAddress which follows the given String alphabetically.
   */
  public InternetAddress getNextMatch(String matchString) {

  }

  /**
   * Returns the InternetAddress which precedes the given String 
   * alphabetically.
   */
  public InternetAddress getPreviousMatch(String matchString) {

  }

  // AddressBook

  /**
   * Gets and appropriate AddressMatcher.
   */
  public AddressMatcher getAddressMatcher() {

  }


}
