package net.suberic.pooka;

import net.suberic.util.*;

/**
 * <p>Represents a Network connection.  This is primarily used to keep 
 * track of connections, so that Pooka will know whether or not to attempt
 * to contact a particular server or not.</p>
 *
 * @author Allen Petersen
 * @version $Revision$
 */
public class NetworkConnection implements net.suberic.util.Item {

  String id = null;

  String propertyName = null;

  String connectCommand = null;

  String disconnectCommand = null;

  int status = DISCONNECTED;

  public static int CONNECTED = 0;
  
  public static int DISCONNECTED = 5;

  public static int UNAVAILABLE = 10;

  private java.util.LinkedList listenerList = new java.util.LinkedList();

  /**
   * <p>Creates a new NetworkConnection from the given property.</p>
   */
  public NetworkConnection (String newId) {
    id = newId;
    propertyName = "Connection." + newId;

  }

  /**
   * <p>A command that should be run when this network connection is 
   * brought up.</p>
   *
   * <p>Returns <code>null</code> if no command needs to be run on 
   * connection.</p>
   */
  public String getConnectCommand() {
    return connectCommand;
  }

  /**
   * <p>Configures this conection.</p>
   */
  protected void configure() {
    VariableBundle bundle = Pooka.getResources();

    connectCommand = bundle.getProperty(getItemProperty() + ".connectCommand", "");
    disconnectCommand = bundle.getProperty(getItemProperty() + ".disconnectCommand", "");

    String onStartup = bundle.getProperty(getItemProperty() + ".valueOnStartup", "Unavailable");
    if (onStartup.equalsIgnoreCase("Connected")) {
      this.connect();
    } else if (onStartup.equalsIgnoreCase("Unavailable")) {
      status = UNAVAILABLE;
    }
  }
  
  /**
   * <p>A command that should be run when this network connection is
   * brought down.</p>
   *
   * <p>Returns <code>null</code> if no command needs to be run on 
   * disconnection.</p>
   */
  public String getDisconnectCommand() {
    return disconnectCommand;
  }

  /**
   * <p>Connect to this network service.</p>
   * 
   * @param runConnectCommand whether or not we should run the
   *        <code>connectCommand</code>, if there is one.
   * @return the new status of the server.
   */
  public int connect(boolean runConnectCommand) {
    
    try {
      if (runConnectCommand) {
	String preCommand = getConnectCommand();
	if (preCommand != null && preCommand.length() > 0) {
	  Process p = Runtime.getRuntime().exec(preCommand);
	  p.waitFor();
	}
      }

      if (status != CONNECTED) {
	status = CONNECTED;
	fireConnectionEvent();
      }
    } catch (Exception ex) {
      System.out.println("Could not run connect command:");
      ex.printStackTrace();
    }

    return status;
  }

  /**
   * <p>Connect to this network service.</p>
   * 
   * @return the new status of the server.
   */
  public int connect() {
    return connect(true);
  }

  /**
   * <p>Disconnect from this network service.</p>
   * 
   * @param runDisonnectCommand whether or not we should run the
   *        <code>disconnectCommand</code>, if there is one.
   * @return the new status of the server.
   */
  public int disconnect(boolean runDisconnectCommand) {
    try {
      if (runDisconnectCommand) {
	String postCommand = getDisconnectCommand();
	if (postCommand != null && postCommand.length() > 0) {
	  Process p = Runtime.getRuntime().exec(postCommand);
	  p.waitFor();
	}
      }
      
      if (status != DISCONNECTED) {
	status = DISCONNECTED;
	fireConnectionEvent();
      } else {
      }
    } catch (Exception ex) {
      System.out.println("Could not run disconnect command:");
      ex.printStackTrace();
    }
    
    return status;
  }

  /**
   * <p>Disconnect from this network service.</p>
   * 
   * @return the new status of the server.
   */
  public int disconnect() {
    return disconnect(true);
  }

  /**
   * <p>Mark this network service as unavailable.  Note that if there
   * is a disconnectCommand, this does <em>not</em> run it.</p>
   *
   * @return the new status of the server.
   */
  public int makeUnavailable() {
    if (status != UNAVAILABLE) {
      status = UNAVAILABLE;
      fireConnectionEvent();
    } 
    return status;
  }

  /**
   * <p>Returns the current status of this NetworkConnection.</p>
   */
  public int getStatus() {
    return status;
  }

  /**
   * Notifies all listeners that the Network connection status has
   * changed.
   */
  public void fireConnectionEvent() {
    for (int i = 0; i < listenerList.size(); i++) {
      ((NetworkConnectionListener) listenerList.get(i)).connectionStatusChanged(this, getStatus());
    }
  }

  /**
   * Adds a NetworkConnectionListener to the listener list.
   */
  public void addConnectionListener(NetworkConnectionListener newListener) {
    if (!listenerList.contains(newListener))
      listenerList.add(newListener);
  }

  /**
   * Removes a NetworkConnectionListener from the listener list.
   */
  public void removeConnectionListener(NetworkConnectionListener oldListener) {
    if (listenerList.contains(oldListener))
      listenerList.remove(oldListener);
  }

  /**
   * <p>The Item ID for this NetworkConnection.</p>
   */
  public String getItemID() {
    return id;
  }

  /**
   * <p>The Item property for this NetworkConnection.  This is usually
   * Connection.<i>itemID</i>.</p>
   */
  public String getItemProperty() {
    return propertyName;
  }

  /**
   * Returns the ItemID of this NetworkConnection.
   */
  public String toString() {
    return getItemID();
  }
}
