package net.suberic.util;

import javax.mail.*;
import java.util.*;

/**
 * This class manages a list of Items.
 */

public class ItemManager implements ValueChangeListener {
  
  private List itemList;
  private HashMap itemIdMap = new HashMap();
  private List listenerList = new LinkedList();

  private String resourceString;
  private VariableBundle sourceBundle;
  private ItemCreator itemCreator;
  
  public ItemManager(String newResourceString, VariableBundle newSourceBundle, ItemCreator newItemCreator) {
    resourceString = newResourceString;
    sourceBundle = newSourceBundle;
    itemCreator = newItemCreator;
    
    createItemList();
    sourceBundle.addValueChangeListener(this, resourceString);
  }
  
  //-----------------------
  // public interface.
  
  /**
   * This returns a Vector with all the currently registered Item
   * objects.
   */
  public synchronized java.util.Vector getItems() {
    return new Vector(itemList);
  }
  
  /**
   * This adds the item with the given name to the item list.
   */
  public synchronized void addItem(String itemName) {
    addItem(new String[] { itemName });
  }
  
  /**
   * This adds the items with the given itemNames to the items list.
   */
  public synchronized void addItem(String[] itemName) {
    if (itemName != null && itemName.length > 0) {
      StringBuffer itemString = new StringBuffer();
      for (int i = 0 ; i < itemName.length; i++) {
	if (getItem(itemName[i]) == null) 
	  itemString.append(itemName[i] + ":");
      }
      if (itemString.length() > 0)
	appendToItemString(new String(itemString.deleteCharAt(itemString.length() -1)));
    }
  }

  /**
   * This adds the given item to the items list.
   */
  public synchronized void addItem(Item newItem) {
    addItem(new Item[] { newItem });
  }

  /**
   * This adds the given items to the items list.
   */
  public synchronized void addItem(Item[] newItem) {

  }
  
  /**
   * This removes the item with the given itemName.
   */
  public synchronized void removeItem(String itemName) {
    if (getItem(itemName) != null)
      removeFromItemString(new String[] { itemName });
  }
  
  /**
   * This removes the items with the given itemNames.
   */
  public synchronized void removeItem(String[] itemNames) {
    // this is probably not necessary at all, but what the hell?
    
    if (itemNames == null || itemNames.length < 1)
      return;
    
    Vector matches = new Vector();
    for ( int i = 0; i < itemNames.length; i++) {
      if (getItem(itemNames[i]) != null)
	matches.add(itemNames[i]);
      
    }
    
    if (matches.size() < 1)
      return;
    
    String[] removedItems = new String[matches.size()];
    
    for (int i = 0; i < matches.size(); i++) 
      removedItems[i] = (String) matches.elementAt(i);
    
    removeFromItemString(removedItems);
  }
  
  /**
   * This removes the given Item.
   */
  public synchronized void removeItem(Item item) {
    if (item != null)
      removeItem(item.getItemID());
  }
  
  /**
   * This removes the given Items.
   */
  public synchronized void removeItem(Item[] item) {
    if (item != null && item.length > 0) {
      String[] itemNames = new String[item.length];
      for (int i = 0; i < item.length; i++) {
	if (item[i] != null)
	  itemNames[i] = item[i].getItemID();
      }
      
      removeItem(itemNames);
    }
  }
  
  /**
   * This compares the itemList object with the Item property, and
   * updates the itemList appropriately.
   */
  public synchronized void refreshItems() {
    Vector newItemList = new Vector();
    
    StringTokenizer tokens =  new StringTokenizer(sourceBundle.getProperty(resourceString, ""), ":");
    
    String itemID;
    while (tokens.hasMoreTokens()) {
      itemID = tokens.nextToken();
      Item currentItem = getItem(itemID);
      if (currentItem != null)
	newItemList.add(currentItem);
      else 
	newItemList.add(itemCreator.createItem(sourceBundle, resourceString, itemID));
    }
    
    if (! newItemList.equals(itemList)) {
      itemList = newItemList;
      //fireItemListChangeEvent();
    }
  }

  /**
   * This returns the Item with the given itemName if it exists
   * in the allItems Vector; otherwise, returns null.
   */
  public synchronized Item getItem(String itemID) {
    Vector allItems = getItems();
    for (int i = 0; i < allItems.size(); i++) {
      Item si = (Item)(allItems.elementAt(i));
      
      if (si.getItemID().equals(itemID)) {
	return si;
      }
    }	
    return null;
  }
  
  /**
   * As defined in net.suberic.util.ValueChangeListener.
   * 
   * This listens for changes to the source property and calls
   * refreshItems() when it gets one.
   */
  public void valueChanged(String changedValue) {
    if (changedValue.equals(resourceString))
      refreshItems();
  }
  
  /**
   * This adds a ItemListChangeListener to the local listener list.
   */
  public void addItemListChangeListener(ItemListChangeListener ilcl) {
    if (! listenerList.contains(ilcl))
      listenerList.add(ilcl);
  }
  
  /**
   * This removes a ItemListChangeListener from the local listener list.
   */
  public void removeItemListChangeListener(ItemListChangeListener ilcl) {
    listenerList.remove(ilcl);
  }
  
  /**
   * This notifies all listeners that the ItemList has changed.
   */
  public void fireItemListChangeEvent(ItemListChangeEvent e) {
    for (int i = 0; i < listenerList.size(); i++)
      ((ItemListChangeListener)listenerList.get(i)).itemListChanged(e);
  }
  

    //---------------------------
    // the background stuff.

  /**
   * This loads and creates all the Items using the resourceString property
   * of the sourceBundle.
   */
  private void createItemList() {
    itemList = new LinkedList();
    String itemID = null;
    
    StringTokenizer tokens =  new StringTokenizer(sourceBundle.getProperty(resourceString, ""), ":");
    
    while (tokens.hasMoreTokens()) {
      itemID=(String)tokens.nextToken();
      Item newItem = itemCreator.createItem(sourceBundle, resourceString, itemID);
      itemList.add(newItem);
      itemIdMap.put(itemID, newItem);
    }
    
  }
  
  /**
   * This appends the newItemString to the "Item" property.
   */
  private void appendToItemString(String newItemString) {
    String oldValue = sourceBundle.getProperty("Item");
    String newValue;
    if (oldValue.length() > 0 && oldValue.charAt(oldValue.length() -1) != ':') {
      newValue = oldValue + ":" + newItemString;
    } else {
      newValue = oldValue + newItemString;
    }
    
    sourceBundle.setProperty(resourceString, newValue);
  }
  
  /**
   * This removes the item names in the itemNames array from the 
   * "Item" property.
   */
  private void removeFromItemString(String[] itemNames) {
    StringTokenizer tokens =  new StringTokenizer(sourceBundle.getProperty(resourceString, ""), ":");
    
    boolean first = true;
    StringBuffer newValue = new StringBuffer();
    String itemID;
    
    while (tokens.hasMoreTokens()) {
      itemID=tokens.nextToken();
      boolean keep=true;
      
      for (int i = 0; keep == true && i < itemNames.length; i++) {
	if (itemID.equals(itemNames[i]))
	  keep = false;
      }
      if (keep) {
	if (!first)
	  newValue.append(":");
	
	newValue.append(itemID);
	first = false;
      }
      
    }
    
    sourceBundle.setProperty(resourceString, newValue.toString());
  }
  
  
}

