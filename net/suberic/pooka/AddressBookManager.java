package net.suberic.pooka;

import javax.mail.*;
import java.util.*;
import net.suberic.util.*;

/**
 * This class manages the a list of AddressBooks.
 */

public class AddressBookManager implements ValueChangeListener {

    private Vector addressBookList;
    private Vector valueChangeListenerList = new Vector();
    
    public StoreManager() {
	addressBookList = createAddressBookList();
	Pooka.getResources().addValueChangeListener(this, "AddressBook");
    }

    //-----------------------
    // public interface.

    /**
     * As defined in net.suberic.util.ValueChangeListener.
     * 
     * This listens for changes to the "AddressBook" property and calls
     * refreshAddressBookInfos() when it gets one.
     */
    public void valueChanged(String changedValue) {
	if (changedValue.equals("AddressBook"))
	    refreshAddressBookInfos();
    }

    /**
     * This returns a Vector with all the currently registered AddressBookInfo
     * objects.
     */
    public java.util.Vector getAddressBookList() {
	return new Vector(addressBookList);
    }

    /**
     * This adds the addressBook with the given addressBookName to the allAddressBooks list.
     */
    public void addAddressBook(String addressBookName) {
	if (getAddressBookInfo(addressBookName) == null) {
	    appendToAddressBookString(addressBookName);
	}
    }

    /**
     * This adds the addressBooks with the given addressBookNames to the allAddressBooks list.
     */
    public void addAddressBook(String[] addressBookName) {
	if (addressBookName != null && addressBookName.length > 0) {
	    StringBuffer addressBookString = new StringBuffer();
	    for (int i = 0 ; i < addressBookName.length; i++) {
		if (getAddressBookInfo(addressBookName[i]) == null) 
		    addressBookString.append(addressBookName[i] + ":");
	    }
	    if (addressBookString.length() > 0)
		appendToAddressBookString(new String(addressBookString.deleteCharAt(addressBookString.length() -1)));
	}
    }

    /**
     * This removes the addressBook with the given addressBookName.
     */
    public void removeAddressBook(String addressBookName) {
	if (getAddressBookInfo(addressBookName) != null)
	    removeFromAddressBookString(new String[] { addressBookName });
    }

    /**
     * This removes the addressBooks with the given addressBookNames.
     */
    public void removeAddressBook(String[] addressBookNames) {
	// this is probably not necessary at all, but what the hell?

	if (addressBookNames == null || addressBookNames.length < 1)
	    return;

	Vector matches = new Vector();
	for ( int i = 0; i < addressBookNames.length; i++) {
	    if (getAddressBookInfo(addressBookNames[i]) != null)
		matches.add(addressBookNames[i]);
	
	}

	if (matches.size() < 1)
	    return;

	String[] removedAddressBooks = new String[matches.size()];

	for (int i = 0; i < matches.size(); i++) 
	    removedAddressBooks[i] = (String) matches.elementAt(i);

	removeFromAddressBookString(removedAddressBooks);
    }

    /**
     * This removes the given AddressBookInfo.
     */
    public void removeAddressBook(AddressBookInfo addressBook) {
	if (addressBook != null)
	    removeAddressBook(addressBook.getAddressBookID());
    }

    /**
     * This removes the given AddressBookInfos.
     */
    public void removeAddressBook(AddressBookInfo[] addressBook) {
	if (addressBook != null && addressBook.length > 0) {
	    String[] addressBookNames = new String[addressBook.length];
	    for (int i = 0; i < addressBook.length; i++) {
		if (addressBook[i] != null)
		    addressBookNames[i] = addressBook[i].getAddressBookID();
	    }
	    
	    removeAddressBook(addressBookNames);
	}
    }
    
    /**
     * This compares the addressBookList object with the AddressBook property, and
     * updates the addressBookList appropriately.
     */
    public void refreshAddressBookInfos() {
	Vector newAddressBookList = new Vector();

	StringTokenizer tokens =  new StringTokenizer(Pooka.getProperty("AddressBook", ""), ":");

	String addressBookID;
	while (tokens.hasMoreTokens()) {
	    addressBookID = tokens.nextToken();
	    AddressBookInfo currentAddressBook = getAddressBookInfo(addressBookID);
	    if (currentAddressBook != null)
		newAddressBookList.add(currentAddressBook);
	    else 
		newAddressBookList.add(new AddressBookInfo(addressBookID));
	}
	
	
	if (! newAddressBookList.equals(addressBookList)) {
	    addressBookList = newAddressBookList;
	    fireAddressBookListChangedEvent();
	}
    }

    /**
     * This returns the FolderInfo which corresponds to the given folderName.
     * The folderName should be in the form "/addressBookname/folder/subfolder".
     */
    public FolderInfo getFolder(String folderName) {
	if (folderName.length() >= 1) {
	    int divider = folderName.indexOf('/', 1);
	    while (divider == 0) {
		folderName = folderName.substring(1);
		divider = folderName.indexOf('/');
	    }

	    if (divider > 0) {
		String addressBookName = folderName.substring(0, divider);
		AddressBookInfo addressBook = getAddressBookInfo(addressBookName);
		if (addressBook != null) {
		    return addressBook.getChild(folderName.substring(divider +1));
		} 
	    }
	}

	return null;
    }

    /**
     * This returns the FolderInfo which corresponds to the given folderID.
     * The folderName should be in the form "addressBookname.folderID.folderID".
     */
    public FolderInfo getFolderById(String folderID) {
	// hurm.  the problem here is that '.' is a legal value in a name...

	java.util.Vector allAddressBooks = getAddressBookList();

	for (int i = 0; i < allAddressBooks.size(); i++) {
	    AddressBookInfo currentAddressBook = (AddressBookInfo) allAddressBooks.elementAt(i);
	    if (folderID.startsWith(currentAddressBook.getAddressBookID())) {
		FolderInfo possibleMatch = currentAddressBook.getFolderById(folderID);
		if (possibleMatch != null) {
		    return possibleMatch;
		}
	    }
	}

	return null;
    }

    /**
     * Gets all of the open and available folders known by the system.
     */
    public Vector getAllOpenFolders() {
	Vector returnValue = new Vector();
	Vector currentAddressBooks = getAddressBookList();
	for (int i = 0; i < currentAddressBooks.size(); i++) {
	    returnValue.addAll(((AddressBookInfo) currentAddressBooks.elementAt(i)).getAllFolders());
	}

	return returnValue;
    }
    
    /**
     * This returns the AddressBookInfo with the given addressBookName if it exists
     * in the allAddressBooks Vector; otherwise, returns null.
     */
    public AddressBookInfo getAddressBookInfo(String addressBookID) {
	Vector allAddressBooks = getAddressBookList();
	for (int i = 0; i < allAddressBooks.size(); i++) {
	    AddressBookInfo si = (AddressBookInfo)(allAddressBooks.elementAt(i));

	    if (si.getAddressBookID().equals(addressBookID)) {
		return si;
	    }
	}	
	return null;
    }

    /**
     * This loads all the Sent Folders on the UserProfile object.  This must
     * be called separately because UserProfiles have references to AddressBookInfos
     * and AddressBookInfos have references to UserProfiles.
     */
    public void loadAllSentFolders() {
	Vector profileList = UserProfile.profileList;

	for (int i = 0; i < profileList.size(); i++) {
	    ((UserProfile)profileList.elementAt(i)).loadSentFolder();
	}
    }

    /**
     * This adds a ValueChangeListener to the local listener list.
     */
    public void addValueChangeListener(ValueChangeListener vcl) {
	if (! valueChangeListenerList.contains(vcl))
	    valueChangeListenerList.add(vcl);
    }

    /**
     * This removes a ValueChangeListener from the local listener list.
     */
    public void removeValueChangeListener(ValueChangeListener vcl) {
	valueChangeListenerList.remove(vcl);
    }

    /**
     * This notifies all listeners that the AddressBookList has changed.
     */

    public void fireAddressBookListChangedEvent() {
	for (int i = 0; i < valueChangeListenerList.size(); i++)
	    ((ValueChangeListener)valueChangeListenerList.elementAt(i)).valueChanged("AddressBook");
    }


    //---------------------------
    // the background stuff.

    /**
     * This loads and creates all the AddressBooks using the "AddressBook" property
     * of the main Pooka VariableBundle.
     */
    private Vector createAddressBookList() {
	Vector allAddressBooks = new Vector();
	String addressBookID = null;

	StringTokenizer tokens =  new StringTokenizer(Pooka.getProperty("AddressBook", ""), ":");
	
	while (tokens.hasMoreTokens()) {
	    addressBookID=(String)tokens.nextToken();
	    allAddressBooks.add(new AddressBookInfo(addressBookID));	    
	}
	
	return allAddressBooks;
    }

    /**
     * This appends the newAddressBookString to the "AddressBook" property.
     */
    private void appendToAddressBookString(String newAddressBookString) {
	String oldValue = Pooka.getProperty("AddressBook");
	String newValue;
	if (oldValue.length() > 0 && oldValue.charAt(oldValue.length() -1) != ':') {
	    newValue = oldValue + ":" + newAddressBookString;
	} else {
	    newValue = oldValue + newAddressBookString;
	}

	Pooka.setProperty("AddressBook", newValue);
    }
    
    /**
     * This removes the addressBook names in the addressBookNames array from the 
     * "AddressBook" property.
     */
    private void removeFromAddressBookString(String[] addressBookNames) {
	StringTokenizer tokens =  new StringTokenizer(Pooka.getProperty("AddressBook", ""), ":");
	
	boolean first = true;
	StringBuffer newValue = new StringBuffer();
	String addressBookID;

	while (tokens.hasMoreTokens()) {
	    addressBookID=tokens.nextToken();
	    boolean keep=true;

	    for (int i = 0; keep == true && i < addressBookNames.length; i++) {
		if (addressBookID.equals(addressBookNames[i]))
		    keep = false;
	    }
	    if (keep) {
		if (!first)
		    newValue.append(":");

		newValue.append(addressBookID);
		first = false;
	    }

	}

	Pooka.setProperty("AddressBook", newValue.toString());
    }

    
}

