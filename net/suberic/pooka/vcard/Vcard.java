package net.suberic.pooka.vcard;
import java.io.*;
import java.util.Properties;
import javax.mail.internet.InternetAddress;

/**
 * A class which represents a vcard address book entry.
 */
public class Vcard implements Comparable, net.suberic.pooka.AddressBookEntry {

  public static final int SORT_BY_ADDRESS = 0;
  
  public static final int SORT_BY_LAST_NAME = 1;
  
  public static final int SORT_BY_FIRST_NAME = 2;
  
  public static final int SORT_BY_PERSONAL_NAME = 3;
  
  Properties properties;
  
  private int sortingMethod = 0;
  
  private InternetAddress address;
  
  /**
   * Creates a new Vcard from a BufferedReader.
   */
  protected Vcard(Properties newProps) {
    properties = newProps;
  }
  
  /**
   * Gets a property on the Vcard.
   */
  public String getProperty(String propertyName) {
    return properties.getProperty(propertyName);
  }
  
  /**
   * Gets the InternetAddress associated with this Vcard.
   */
  public InternetAddress getAddress() {
    try {
      if (address == null) {
	address = new InternetAddress(properties.getProperty("email;internet"), properties.getProperty("fn"));
      }
      return address;
    } catch (java.io.UnsupportedEncodingException uee) {
      return null;
    }
  }
  
  /**
   * Gets the PersonalName property associated with this Vcard.
   */
  public String getPersonalName() {
    try {
      if (address == null) {
	address = new InternetAddress(properties.getProperty("email;internet"), properties.getProperty("fn"));
      }
      return address.getPersonal();
    } catch (java.io.UnsupportedEncodingException uee) {
      return null;
    }
  }

  /**
   * Gets the FirstName property associated with this Vcard.
   */
  public String getFirstName() {
    String name = properties.getProperty("n");
    if (name != null) {
      int index = name.indexOf(";");
      if (index > 0) 
	return name.substring(index + 1);
    }

    return null;
  }

  /**
   * Gets the LastName property associated with this Vcard.
   */
  public String getLastName() {
    String name = properties.getProperty("n");
    if (name != null) {
      int index = name.indexOf(";");
      if (index > 0) 
	return name.substring(index);
    }

    return null;
  }
  
  /**
   * Gets the user information, last name first.
   */
  public String getLastFirst() {
    return getLastName() + ", " + getFirstName();
  }
  
  /**
   * Gets the user information, first name first.
   */
  public String getFirstLast() {
    return getFirstName() + " " + getLastName();
  }
    
  /**
   * Gets the email address (as a string) associated with this Vcard.
   */
  public String getEmailAddress() {
    return address.getAddress();
  }

  //----  Comparable  ----//
  
  /**
   * Compares this Vcard either to another Vcard, or a String which matches
   * the Vcard.  This checks the sortingMethod setting to decide how to 
   * compare to the Vcard or String.
   *
   * @param   o the Object to be compared.
   * @return  a negative integer, zero, or a positive integer as this object
   *		is less than, equal to, or greater than the specified object.
   * 
   * @throws ClassCastException if the specified object's type prevents it
   *         from being compared to this Object.
   */
  
  public int compareTo(Object o) {
    if (o instanceof Vcard) {
      Vcard target = (Vcard) o;
      
      switch (sortingMethod) {
      case (SORT_BY_ADDRESS): 
	int returnValue = getAddress().toString().compareTo(target.getAddress().toString());
	return returnValue;
      case (SORT_BY_LAST_NAME):
	return getLastFirst().compareTo(target.getLastFirst());
      case (SORT_BY_FIRST_NAME):
	return getFirstLast().compareTo(target.getFirstLast());
      case (SORT_BY_PERSONAL_NAME):
	return getPersonalName().compareTo(target.getPersonalName());
      }
      
      return -1;
    } else if (o instanceof String) {
      String compareString = null;
      String matchString = (String) o;
      
      switch (sortingMethod) {
      case (SORT_BY_ADDRESS):
	compareString = getAddress().toString();
	break;
      case (SORT_BY_LAST_NAME):
	compareString = getLastFirst();
	break;
      case (SORT_BY_FIRST_NAME):
	compareString = getFirstLast();
	break;
      }
      
      // see if the string to be matched is shorter; if so, match
      // with just that length.


      int origSize = compareString.length();
      int matchSize = matchString.length();
      if (matchSize < origSize) {
	int returnValue =  compareString.substring(0,matchSize).compareTo(matchString);
	return returnValue;
      } else {
	int returnValue =  compareString.compareTo(matchString);
	return returnValue;
      }
    }
    
    return -1;
  }
  
  //----  parser  ----//
  
  /**
   * Parses a vcard from a BufferedReader.
   */
  public static Vcard parse(BufferedReader reader) throws java.text.ParseException {
    
    try {
      Properties newProps = new Properties();
      
      boolean isDone = false;
      
      String line = getNextLine(reader);
      if (line != null) {
	String[] current = parseLine(line);
	if (current[0] != null && current[1] != null) {
	  if (! (current[0].equalsIgnoreCase("begin") && current[1].equalsIgnoreCase("vcard")))
	    throw new java.text.ParseException("No beginning", 0);
	}
	else {
	  newProps.put(current[0], current[1]);
	}
      } else {
	return null;
      }
      
      while (!isDone) {
	line = getNextLine(reader);
	if (line != null) {
	  String[] current = parseLine(line);
	  if (current[0] != null && current[1] != null) {
	    if (current[0].equalsIgnoreCase("end")) {
	      isDone = true;
	      if (current[1].equalsIgnoreCase("vcard")) 
		newProps.put(current[0], current[1]);
	      else
		throw new java.text.ParseException("incorrect end tag", 0);
	    } else {
	      newProps.put(current[0], current[1]);
	    }
	  }
	} else {
	  isDone = true;
	}
      }
      return new Vcard(newProps);
    } catch (IOException ioe) {
      throw new java.text.ParseException(ioe.getMessage(), 0);
    }
  }
  
  /**
   * Parses a name/value pair from an rfc2425 stream.
   */
  private static String getNextLine(BufferedReader reader) throws IOException {
    String firstLine = reader.readLine();
    boolean isDone = false;
    if (firstLine != null) {
      while (! isDone) {
	reader.mark(256);
	String nextLine = reader.readLine();
	if (nextLine != null) {
	  if (! Character.isWhitespace(nextLine.charAt(0))) {
	    isDone = true;
	    reader.reset();
	  } else {
	    firstLine = firstLine + nextLine.substring(1);
	  }
	} else {
	  isDone = true;
	}
      }
    } 
    return firstLine;
  }
  
  private static String[] parseLine(String firstLine) {
    String[] returnValue = new String[2];
    
    int dividerLoc = firstLine.indexOf(':');
    returnValue[0] = firstLine.substring(0, dividerLoc);
    returnValue[1] = firstLine.substring(dividerLoc + 1);
    
    return returnValue;
  }
}
