package net.suberic.pooka;
import javax.mail.internet.InternetAddress;

/**
 * Defines the methods used to access Internet Addresses from a given
 * String.
 */
public interface AddressMatcher {

  /**
   * Returns all of the InternetAddresses which match the given String.
   */
  public InternetAddress[] match(String matchString);

  /**
   * Returns all of the InternetAddresses whose FirstName matches the given 
   * String.
   */
  public InternetAddress[] matchFirstName(String matchString);

  /**
   * Returns all of the InternetAddresses whose LastName matches the given 
   * String.
   */
  public InternetAddress[] matchLastName(String matchString);

  /**
   * Returns all of the InternetAddresses whose email addresses match the
   * given String.
   */
  public InternetAddress[] matchEmailAddress(String matchString);

}
