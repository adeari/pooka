package net.suberic.pooka.gui;

import java.util.*;
import javax.swing.*;
import java.awt.event.*;

import net.suberic.pooka.*;
import net.suberic.util.gui.ConfigurablePopupMenu;

/**
 * This class monitors the status of the network connection(s) that
 * Pooka uses.
 *
 * @author Allen Petersen
 * @version $Revision$
 */
public class ConnectionMonitor extends JPanel implements NetworkConnectionListener {

  /** the Image for CONNECTED connections. */
  public  ImageIcon connectedImage = null;

  /** the Image for DISCONNECTED connections. */
  public ImageIcon disconnectedImage = null;

  /** the Image for DOWN connections. */
  public ImageIcon downImage = null;
  
  /** The combo box for selecting which connection to show.  */
  JComboBox comboBox;

  /** The panel where the status is shown. */
  JLabel statusPanel;

  ConfigurablePopupMenu popupMenu;

  /** The default actions supported by this component. */
  Action[] defaultActions = new Action[] {
    new ConnectAction(),
    new DisconnectAction()
      };

  /**
   * Creates a new, empty ConnectionMonitor.
   */
  public ConnectionMonitor() {
    loadImages();
    setupComponents();
  }

  /**
   * Creates the graphical parts of this component.  There are basically 
   * two parts here:  a JComboBox for the list of connections, and a 
   * JPanel to show the status of the current connection.
   */
  private void setupComponents() {
    comboBox = new JComboBox();
    comboBox.addItemListener(new ItemListener() {
	public void itemStateChanged(ItemEvent e) {
	  if (e.getStateChange() == ItemEvent.SELECTED) {
	    updateStatus();
	  }
	}
      });

    statusPanel = new JLabel();
    statusPanel.addMouseListener(new MouseAdapter() {
	
	public void mousePressed(MouseEvent e) {
	  if (SwingUtilities.isRightMouseButton(e)) {
	    showPopupMenu(e);
	  }
	}
      });
	

    this.add(comboBox);
    this.add(statusPanel);
  }

  /**
   * Loads the images for the ConnectionMonitor.
   */
  private void loadImages() {
    java.net.URL url = this.getClass().getResource(Pooka.getProperty("ConnectionMonitor.connectedIcon", "images/TrafficGreen.gif")); 
    if (url != null) {
      connectedImage = new ImageIcon(url);
    }
    url = this.getClass().getResource(Pooka.getProperty("ConnectionMonitor.disconnectedIcon", "images/TrafficYellow.gif")); 
    if (url != null) {
      disconnectedImage = new ImageIcon(url);
    }
    url = this.getClass().getResource(Pooka.getProperty("ConnectionMonitor.downIcon", "images/TrafficRed.gif")); 
    if (url != null) {
      downImage = new ImageIcon(url);
    }
  }
  /**
   * This creates and shows a PopupMenu for this component.  
   */
  public void showPopupMenu(MouseEvent e) {
    if (popupMenu == null) {
      popupMenu = new ConfigurablePopupMenu();
      popupMenu.configureComponent("ConnnectionMonitor.popupMenu", Pooka.getResources());	
      popupMenu.setActive(getActions());
    }

    popupMenu.show(this, e.getX(), e.getY());
    
  }

  /**
   * Updates the status for the currently selected Connection.
   */
  public void updateStatus() {
    NetworkConnection selectedConnection = getSelectedConnection();
    if (selectedConnection != null) {
      int status = selectedConnection.getStatus();
      if (status == NetworkConnection.CONNECTED) {
	statusPanel.setIcon(connectedImage);
      } else if (status == NetworkConnection.DISCONNECTED) {
	statusPanel.setIcon(disconnectedImage);
      } else if (status == NetworkConnection.DOWN) {
	statusPanel.setIcon(downImage);
      }
    }
  }

  /**
   * Notifies this component that the state of a network connection has
   * changed.
   */
  public void connectionStatusChanged(NetworkConnection connection, int newStatus) {
    NetworkConnection currentConnection = getSelectedConnection();
    if (connection == currentConnection) {
      updateStatus();
    }
  }

  /**
   * Adds the given NetworkConnection(s) to the JComboBox list.
   */
  public void addConnections(NetworkConnection[] newConnections) {
    if (newConnections != null && newConnections.length > 0) {
      for (int i = 0 ; i < newConnections.length; i++) {
	if (newConnections[i] != null) {
	  comboBox.addItem(newConnections[i]);
	  newConnections[i].addConnectionListener(this);
	}
      }
    }
  }

  /**
   * Returns the currently selected NetworkConnection.
   */
  public NetworkConnection getSelectedConnection() {
    return (NetworkConnection) comboBox.getSelectedItem();
  }

  /**
   * Returns the Actions supported by this Component.
   */
  public Action[] getActions() {
    return defaultActions;
  }

  public class ConnectAction extends AbstractAction {

    ConnectAction() {
      super("connection-connect");
    }

    public void actionPerformed(ActionEvent e) {
      NetworkConnection connection = getSelectedConnection();
      if (connection != null && connection.getStatus() != NetworkConnection.CONNECTED) {
	connection.connect();
      }
    }
  }

  public class DisconnectAction extends AbstractAction {

    DisconnectAction() {
      super("disconnection-connect");
    }

    public void actionPerformed(ActionEvent e) {
      NetworkConnection connection = getSelectedConnection();
      if (connection != null && connection.getStatus() == NetworkConnection.CONNECTED) {
	connection.disconnect();
      }
    }
  }
}
