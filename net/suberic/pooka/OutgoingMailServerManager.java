package net.suberic.pooka;

import java.util.*;

import net.suberic.util.*;

/**
 * <p>An object which manages OutgoingMailServer resources.</p>
 *
 * @author Allen Petersen
 * @version $Revision$
 */
public class OutgoingMailServerManager implements ItemCreator, ItemListChangeListener {
  
  private ItemManager manager;
  private LinkedList listenerList = new LinkedList();

  /**
   * <p>Creates a new OutgoingMailServerManager.</p>
   */
  public OutgoingMailServerManager() {
    createOutgoingMailServerList();
  }

  //-----------------------
  // public interface.
  
  /**
   * This listens for ItemListChangeEvents, which result from changes to the 
   * "OutgoingMailServer" property.  The result is that refreshOutgoingMailServers() is called,
   * and then the event is passed to listeners to this object.
   */
  public void itemListChanged(ItemListChangeEvent e) {
    fireItemListChanged(e);
  }

  /**
   * This returns a Vector with all the currently registered OutgoingMailServer
   * objects.
   */
  public java.util.Vector getOutgoingMailServerList() {
    return manager.getItems();
  }
  
  /**
   * This adds the OutgoingMailServer with the given OutgoingMailServerName to the 
   * allOutgoingMailServers list.
   */
  public void addOutgoingMailServer(String OutgoingMailServerName) {
    manager.addItem(OutgoingMailServerName);
  }
  
  /**
   * This adds the OutgoingMailServers with the given OutgoingMailServerNames to the allOutgoingMailServers list.
   */
  public void addOutgoingMailServer(String[] OutgoingMailServerName) {
    manager.addItem(OutgoingMailServerName);
  }
  
  /**
   * This removes the OutgoingMailServer with the given OutgoingMailServerName.
   */
  public void removeOutgoingMailServer(String OutgoingMailServerName) {
    manager.removeItem(OutgoingMailServerName);
  }

  /**
   * This removes the OutgoingMailServers with the given OutgoingMailServerNames.
   */
  public void removeOutgoingMailServer(String[] OutgoingMailServerNames) {
    manager.removeItem(OutgoingMailServerNames);
  }

  /**
   * This removes the given OutgoingMailServer.
   */
  public void removeOutgoingMailServer(OutgoingMailServer OutgoingMailServer) {
    manager.removeItem(OutgoingMailServer);
  }
  
  /**
   * This removes the given OutgoingMailServers.
   */
  public void removeOutgoingMailServer(OutgoingMailServer[] OutgoingMailServers) {
    manager.removeItem(OutgoingMailServers);
  }

  /**
   * This returns the NetwordOutgoingMailServer with the given OutgoingMailServerName if it 
   * exists; otherwise, returns null.
   */
  public OutgoingMailServer getOutgoingMailServer(String OutgoingMailServerID) {
    return (OutgoingMailServer) manager.getItem(OutgoingMailServerID);
  }

  /**
   * This returns the NetwordOutgoingMailServer with the given OutgoingMailServerName if it 
   * exists; otherwise, returns null.
   */
  public OutgoingMailServer getDefaultOutgoingMailServer() {
    return (OutgoingMailServer) manager.getItem("_default");
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
   * This notifies all listeners that the OutgoingMailServerList has changed.
   */
  public void fireItemListChanged(ItemListChangeEvent e) {
    for (int i = 0; i < listenerList.size(); i++)
      ((ItemListChangeListener)listenerList.get(i)).itemListChanged(e);
  }
  

  /**
   * This creates a new OutgoingMailServer.
   */
  public Item createItem(VariableBundle sourceBundle, String resourceString, String itemID) {
    return new OutgoingMailServer(itemID);
  }

  //---------------------------
  // the background stuff.
  
  /**
   * This loads and creates all the OutgoingMailServers using the "OutgoingMailServer" 
   * property of the main Pooka VariableBundle.
   */
  private void createOutgoingMailServerList() {
    manager = new ItemManager("OutgoingMailServer", Pooka.getResources(), this);
    manager.addItemListChangeListener(this);
  }
}
