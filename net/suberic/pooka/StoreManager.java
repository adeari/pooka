package net.suberic.pooka;

import javax.mail.*;
import java.util.*;
import net.suberic.util.*;

/**
 * This class manages the a list of StoreInfos.  It also provides some
 * convenience methods for accessing FolderInfos within the StoreInfos,
 * and for adding and removing StoreInfos.
 */

public class StoreManager implements ValueChangeListener {

    private Vector storeList;
    private Vector valueChangeListenerList = new Vector();
    
    public StoreManager() {
	storeList = createStoreList();
	Pooka.getResources().addValueChangeListener(this, "Store");
    }

    //-----------------------
    // public interface.

    /**
     * As defined in net.suberic.util.ValueChangeListener.
     * 
     * This listens for changes to the "Store" property and calls
     * refreshStoreInfos() when it gets one.
     */
    public void valueChanged(String changedValue) {
	if (changedValue.equals("Store"))
	    refreshStoreInfos();
    }

    /**
     * This returns a Vector with all the currently registered StoreInfo
     * objects.
     */
    public java.util.Vector getStoreList() {
	return new Vector(storeList);
    }

    /**
     * This adds the store with the given storeName to the allStores list.
     */
    public void addStore(String storeName) {
	if (getStoreInfo(storeName) == null) {
	    appendToStoreString(storeName);
	}
    }

    /**
     * This adds the stores with the given storeNames to the allStores list.
     */
    public void addStore(String[] storeName) {
	if (storeName != null && storeName.length > 0) {
	    StringBuffer storeString = new StringBuffer();
	    for (int i = 0 ; i < storeName.length; i++) {
		if (getStoreInfo(storeName[i]) == null) 
		    storeString.append(storeName[i] + ":");
	    }
	    if (storeString.length() > 0)
		appendToStoreString(new String(storeString.deleteCharAt(storeString.length() -1)));
	}
    }

    /**
     * This removes the store with the given storeName.
     */
    public void removeStore(String storeName) {
	if (getStoreInfo(storeName) != null)
	    removeFromStoreString(new String[] { storeName });
    }

    /**
     * This removes the stores with the given storeNames.
     */
    public void removeStore(String[] storeNames) {
	// this is probably not necessary at all, but what the hell?

	if (storeNames == null || storeNames.length < 1)
	    return;

	Vector matches = new Vector();
	for ( int i = 0; i < storeNames.length; i++) {
	    if (getStoreInfo(storeNames[i]) != null)
		matches.add(storeNames[i]);
	
	}

	if (matches.size() < 1)
	    return;

	String[] removedStores = new String[matches.size()];

	for (int i = 0; i < matches.size(); i++) 
	    removedStores[i] = (String) matches.elementAt(i);

	removeFromStoreString(removedStores);
    }

    /**
     * This removes the given StoreInfo.
     */
    public void removeStore(StoreInfo store) {
	if (store != null)
	    removeStore(store.getStoreID());
    }

    /**
     * This removes the given StoreInfos.
     */
    public void removeStore(StoreInfo[] store) {
	if (store != null && store.length > 0) {
	    String[] storeNames = new String[store.length];
	    for (int i = 0; i < store.length; i++) {
		if (store[i] != null)
		    storeNames[i] = store[i].getStoreID();
	    }
	    
	    removeStore(storeNames);
	}
    }
    
    /**
     * This compares the storeList object with the Store property, and
     * updates the storeList appropriately.
     */
    public void refreshStoreInfos() {
	Vector newStoreList = new Vector();

	StringTokenizer tokens =  new StringTokenizer(Pooka.getProperty("Store", ""), ":");

	String storeID;
	while (tokens.hasMoreTokens()) {
	    storeID = tokens.nextToken();
	    StoreInfo currentStore = getStoreInfo(storeID);
	    if (currentStore != null)
		newStoreList.add(currentStore);
	    else 
		newStoreList.add(new StoreInfo(storeID));
	}
	
	
	if (! newStoreList.equals(storeList)) {
	    storeList = newStoreList;
	    fireStoreListChangedEvent();
	}
    }

    /**
     * This returns the FolderInfo which corresponds to the given folderName.
     * The folderName should be in the form "/storename/folder/subfolder".
     */
    public FolderInfo getFolder(String folderName) {
	if (folderName.length() >= 1) {
	    int divider = folderName.indexOf('/', 1);
	    while (divider == 0) {
		folderName = folderName.substring(1);
		divider = folderName.indexOf('/');
	    }

	    if (divider > 0) {
		String storeName = folderName.substring(0, divider);
		StoreInfo store = getStoreInfo(storeName);
		if (store != null) {
		    return store.getChild(folderName.substring(divider +1));
		} 
	    }
	}

	return null;
    }

    /**
     * This returns the FolderInfo which corresponds to the given folderID.
     * The folderName should be in the form "storename.folderID.folderID".
     */
    public FolderInfo getFolderById(String folderID) {
	// hurm.  the problem here is that '.' is a legal value in a name...

	java.util.Vector allStores = getStoreList();

	for (int i = 0; i < allStores.size(); i++) {
	    StoreInfo currentStore = (StoreInfo) allStores.elementAt(i);
	    if (folderID.startsWith(currentStore.getStoreID())) {
		FolderInfo possibleMatch = currentStore.getFolderById(folderID);
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
	Vector currentStores = getStoreList();
	for (int i = 0; i < currentStores.size(); i++) {
	    returnValue.addAll(((StoreInfo) currentStores.elementAt(i)).getAllFolders());
	}

	return returnValue;
    }
    
    /**
     * This returns the StoreInfo with the given storeName if it exists
     * in the allStores Vector; otherwise, returns null.
     */
    public StoreInfo getStoreInfo(String storeID) {
	Vector allStores = getStoreList();
	for (int i = 0; i < allStores.size(); i++) {
	    StoreInfo si = (StoreInfo)(allStores.elementAt(i));

	    if (si.getStoreID().equals(storeID)) {
		return si;
	    }
	}	
	return null;
    }

    /**
     * This loads all the Sent Folders on the UserProfile object.  This must
     * be called separately because UserProfiles have references to StoreInfos
     * and StoreInfos have references to UserProfiles.
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
     * This notifies all listeners that the StoreList has changed.
     */

    public void fireStoreListChangedEvent() {
	for (int i = 0; i < valueChangeListenerList.size(); i++)
	    ((ValueChangeListener)valueChangeListenerList.elementAt(i)).valueChanged("Store");
    }


    //---------------------------
    // the background stuff.

    /**
     * This loads and creates all the Stores using the "Store" property
     * of the main Pooka VariableBundle.
     */
    private Vector createStoreList() {
	Vector allStores = new Vector();
	String storeID = null;

	StringTokenizer tokens =  new StringTokenizer(Pooka.getProperty("Store", ""), ":");
	
	while (tokens.hasMoreTokens()) {
	    storeID=(String)tokens.nextToken();
	    allStores.add(new StoreInfo(storeID));	    
	}
	
	return allStores;
    }

    /**
     * This appends the newStoreString to the "Store" property.
     */
    private void appendToStoreString(String newStoreString) {
	String oldValue = Pooka.getProperty("Store");
	String newValue;
	if (oldValue.length() > 0 && oldValue.charAt(oldValue.length() -1) != ':') {
	    newValue = oldValue + ":" + newStoreString;
	} else {
	    newValue = oldValue + newStoreString;
	}

	Pooka.setProperty("Store", newValue);
    }
    
    /**
     * This removes the store names in the storeNames array from the 
     * "Store" property.
     */
    private void removeFromStoreString(String[] storeNames) {
	StringTokenizer tokens =  new StringTokenizer(Pooka.getProperty("Store", ""), ":");
	
	boolean first = true;
	StringBuffer newValue = new StringBuffer();
	String storeID;

	while (tokens.hasMoreTokens()) {
	    storeID=tokens.nextToken();
	    boolean keep=true;

	    for (int i = 0; keep == true && i < storeNames.length; i++) {
		if (storeID.equals(storeNames[i]))
		    keep = false;
	    }
	    if (keep) {
		if (!first)
		    newValue.append(":");

		newValue.append(storeID);
		first = false;
	    }

	}

	Pooka.setProperty("Store", newValue.toString());
    }

    
}

