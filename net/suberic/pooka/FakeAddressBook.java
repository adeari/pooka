package net.suberic.pooka;
import javax.mail.internet.InternetAddress;

public class FakeAddressBook implements AddressBook, AddressMatcher {

  InternetAddress[] addressList;
  
  public FakeAddressBook() {
    try {
      addressList = new InternetAddress[] {
	  new InternetAddress("allen@suberic.net", "allen at suberic"),
	  new InternetAddress("atg.com", "allen at atg"),
	  new InternetAddress("daddrTest@suberic.net", "d address test"),
	  new InternetAddress("dryad@grlmail.com", "dryad girl")
	    };
    } catch (Exception e) {
    }
  }

  public AddressMatcher getAddressMatcher() {
    return this;
  }
  
  /**
   * Returns all of the InternetAddresses which match the given String.
   */
  public InternetAddress[] match(String matchString) {
    java.util.Vector v = new java.util.Vector();
    for (int i = 0; i < addressList.length; i++) {
      if (matchString.regionMatches(false, 0, addressList[i].toString(), 0, matchString.length())) {
	v.add(addressList[i]);
      }
    }
 
    InternetAddress[] returnValue = new InternetAddress[v.size()];
    for (int i = 0; i < v.size(); i++) {
      returnValue[i] = (InternetAddress) v.elementAt(i);
    }
    
    return returnValue;
    
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

  /**
   * Returns the InternetAddress which follows the given String alphabetically.
   */
  public InternetAddress getNextMatch(String matchString) {
    int i = 0;
    InternetAddress match = null;
    while (i < addressList.length && match == null) {
      InternetAddress current = addressList[i];
      if (current.toString().compareTo(matchString) > 0)
	match = current;
      else
	i++;
    }

    return match;
  }

  /**
   * Returns the InternetAddress which precedes the given String 
   * alphabetically.
   */
  public InternetAddress getPreviousMatch(String matchString) {
    int i = addressList.length - 1;
    InternetAddress match = null;
    while (i >= 0 && match == null) {
      InternetAddress current = addressList[i];
      if (current.toString().compareTo(matchString) < 0)
	match = current;
      else
	i--;
    }

    return match;
  }

}
