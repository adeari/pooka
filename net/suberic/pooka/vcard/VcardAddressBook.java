package net.suberic.pooka.vcard;
import net.suberic.pooka.*;
import javax.mail.internet.InternetAddress;
import java.util.ArrayList;
import java.io.*;

/**
 * An AddressBook which uses Vcards. 
 */
public class VcardAddressBook implements AddressBook, AddressMatcher {

  String fileName;
  Vcard[] orderedList;
  ArrayList arrayList = new ArrayList();

  int sortingMethod;
  
  /**
   * Creates a new VcardAddressBook from the given Vcard.  It uses the
   * file represented by the given pFileName as the source for the 
   * addresses.
   */
    public VcardAddressBook(String pFileName) throws java.text.ParseException, java.io.IOException {
      fileName = pFileName;
      loadAddressBook();
    }
  
  /**
   * Loads the AddressBook from the saved filename.
   */
  protected void loadAddressBook() throws java.text.ParseException, java.io.IOException {
    File f = new File(fileName);
    if (f.exists()) {
      BufferedReader reader = new BufferedReader(new FileReader(f));
      for(Vcard newCard = Vcard.parse(reader); newCard != null; newCard = Vcard.parse(reader)) {
	insertIntoList(newCard);
      }
    }
    
    sortList();
  }
  
  /**
   * Inserts the given Vcard into the ordered list.
   */
  protected void insertIntoList(Vcard newCard) {
    arrayList.add(newCard);
  }
  
  /**
   * Adds the given Vcard to the address book.
   */
  protected void addAddress(Vcard newCard) {
    Vcard[] newList = new Vcard[orderedList.length + 1];
    int insertLocation = java.utils.Arrays.binarySearch(newCard);
  }
  
  /**
   * Sorts the list.
   */
  protected void sortList() {
    orderedList = new Vcard[arrayList.size()];
    orderedList = (Vcard[]) arrayList.toArray(orderedList);
    java.util.Arrays.sort(orderedList);
  }
  
  /**
   * Uses a binary search to find a matching Vcard.
   */
  /*
  protected InternetAddress[] binarySearch(String matchString, int minimum, int maximum) {
    boolean matched = false;
    InternetAddress[] returnValue = null;

    while (! matched) {
      if ( maximum - minimum < 2) {
	int choice = minimum + maximum / 2;
	Vcard current = (Vcard) orderedList.get(choice);
	int comparison = current.compareTo(matchString);
	if (comparison < 0)
	  maximum = choice;
	else if (comparison > 0)
	  minimum = choice;
	else {
	  // match has been found.
	  matched = true;
	  // get all the matches.

	}
      }
    }
    
    return null;
  }
  */
  
  /**
   * Gets the AddressMatcher for this AddressBook.
   */
  public AddressMatcher getAddressMatcher() {
    return this;
  }
  
  /**
   * Returns all of the InternetAddresses which match the given String.
   */
  public InternetAddress[] match(String matchString) {
    int value = java.util.Arrays.binarySearch(orderedList, matchString);
    // now get all the matches, if any.
    if (value < 0) {
      return new InternetAddress[0];
    }

    if (orderedList[value].compareTo(matchString) == 0) {
      // get all the matches.
      int minimum = value;
      while (minimum > 0 && (orderedList[minimum - 1].compareTo(matchString) == 0))
	minimum--;


      int maximum = value;
      while (maximum < orderedList.length -1 && (orderedList[maximum + 1].compareTo(matchString) == 0))
	maximum++;

      InternetAddress[] returnValue = new InternetAddress[maximum - minimum + 1];

      for(int i = 0; i < returnValue.length; i++) {
	returnValue[i] = orderedList[minimum + i].getAddress();
      }

      return returnValue;
    } else {
      return new InternetAddress[0];
    }
    //return binarySearch(matchString, 0, orderedList.size());
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
    int value = java.util.Arrays.binarySearch(orderedList, matchString);
    // now get all the matches, if any.
    if (value < 0) {
      value = 0;
    }

    // see if the given value matches the string.
    if (orderedList[value].compareTo(matchString) == 0 && value > 0)
      return orderedList[value - 1].getAddress();
    else
      return orderedList[value].getAddress();
  }
  
  /**
   * Returns the InternetAddress which precedes the given String 
   * alphabetically.
   */
  public InternetAddress getPreviousMatch(String matchString) {
    int value = java.util.Arrays.binarySearch(orderedList, matchString);
    // now get all the matches, if any.
    if (value < 0) {
      value = 0;
    }

    // see if the given value matches the string.
    if (orderedList[value].compareTo(matchString) == 0 && value < orderedList.length - 1)
      return orderedList[value + 1].getAddress();
    else
      return orderedList[value].getAddress();
  }
  
}
