package net.suberic.pooka.gui;

import java.util.*;
import javax.swing.*;

import net.suberic.pooka.*;

/**
 * This class monitors the status of the network connection(s) that
 * Pooka uses.
 *
 * @author Allen Petersen
 * @version $Revision$
 */
public class ConnectionMonitor extends JPanel {

  /** the Image for CONNECTED connections. */
  public  ImageIcon connectedImage = null;

  /** the Image for DISCONNECTED connections. */
  public ImageIcon disconnectedImage = null;

  /** the Image for DOWN connections. */
  public ImageIcon downImage = null;
  
  /** 
   * The monitored connections, with links to their panels.
   */
  HashMap connectionMap = new HashMap();
  
  /**
   * Creates a new, empty ConnectionMonitor.
   */
  public ConnectionMonitor() {
    loadImages();
  }

  /**
   * Called when the status of the connection changes.
   */
  public void connectionStatusChanged(NetworkConnection connection, int newStatus) {
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
}
