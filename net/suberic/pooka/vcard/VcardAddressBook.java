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
    ArrayList orderedList = new ArrayList();

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
	orderedList.add(newCard);
    }

    /**
     * Sorts the list.
     */
    protected void sortList() {
	
    }

    /**
     * Uses a binary search to find a matching Vcard.
     */
    protected InternetAddress[] binarySearch(String matchString, int minimum, int maximum) {
	boolean matched = false;
	while (! matched) {
	    if ( maximum - minimum < 2) {
		int choice = minimum + maximum / 2;
		Vcard current = (Vcard) orderedList.get(choice);
		// FIXME
	    }
	}

	return null;
    }

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
	return binarySearch(matchString, 0, orderedList.size());
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
	return null;
    }
    
    /**
     * Returns the InternetAddress which precedes the given String 
     * alphabetically.
     */
    public InternetAddress getPreviousMatch(String matchString) {
	return null;
    }
    
}
