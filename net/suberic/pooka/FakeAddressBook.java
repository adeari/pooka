package net.suberic.pooka;
import javax.mail.internet.InternetAddress;

public class FakeAddressBook implements AddressBook, AddressMatcher {
  public FakeAddressBook() {

  }

  public AddressMatcher getAddressMatcher() {
    return this;
  }

    /**
   * Returns all of the InternetAddresses which match the given String.
   */
  public InternetAddress[] match(String matchString) {
    try {
      if (matchString.equals("a")) {
	return new InternetAddress[] {
	  new InternetAddress("allen@suberic.net", "allen at suberic"),
	  new InternetAddress("atg.com", "allen at atg")
	    };
      } else if (matchString.equals("d")) {
	return new InternetAddress[] {
	  new InternetAddress("daddrTest@suberic.net", "d address test"),
	  new InternetAddress("dryad@grlmail.com", "dryad girl")
	    };
      } else {
	return new InternetAddress[0];
      }
    } catch (Exception e) {
      System.out.println("exception " + e);
      e.printStackTrace();
      return new InternetAddress[0];
    }
	
  }

  /**
   * Returns all of the InternetAddresses whose FirstName matches the given 
   * String.
   */
  public InternetAddress[] matchFirstName(String matchString) {
    return match(matchString);
  }

  /**
   * Returns all of the InternetAddresses whose LastName matches the given 
   * String.
   */
  public InternetAddress[] matchLastName(String matchString) {
    return match(matchString);
  }

  /**
   * Returns all of the InternetAddresses whose email addresses match the
   * given String.
   */
  public InternetAddress[] matchEmailAddress(String matchString) {
    return match(matchString);
  }

}
