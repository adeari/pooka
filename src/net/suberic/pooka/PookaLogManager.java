package net.suberic.pooka;

import net.suberic.util.*;

import java.util.logging.*;

/**
 * This class manages logging for Pooka.  It basically provides a bridge
 * between Pooka configuration files and the JDK 1.4 Logging system.
 */

public class PookaLogManager implements ValueChangeListener {

  // the various log settings we use.
  String[] logSettings = new String[] {
    "Pooka.debug",
    "Pooka.debug.session",
    "editors.debug",
    "Pooka.debug.gui",
    "Pooka.debug.gui.focus",
    "Pooka.debug.gui.filechooser"
  };
  
  /**
   * Constructor.  Sets itself as a valueChangeListener for all of the
   * log settings configured.
   */
  public PookaLogManager() {
    VariableBundle globalBundle = Pooka.getResources();
    for (int i = 0; i < logSettings.length; i++) {
      globalBundle.addValueChangeListener(this, logSettings[i]);
    }
    
    refresh();

    configureListeners();

    // set up logging to log all messages.  stupid.
    Logger global = Logger.getLogger("");
    Handler[] globalHandlers = global.getHandlers();
    for (int i = 0; i < globalHandlers.length; i++) {
      globalHandlers[i].setLevel(Level.ALL);
    }
  }
  
  
  /**
   * Refreshes all logging states from the current configuration.
   */
  public void refresh() {
    for (int i = 0; i < logSettings.length; i++) {
      Level newLevel = Level.parse(Pooka.getProperty(logSettings[i] + ".level", "OFF"));
      setLogLevel(logSettings[i], newLevel);
    }
  }

  /**
   * Sets up additional listeners.
   */
  public void configureListeners() {
    // focus listener
    java.awt.KeyboardFocusManager mgr = java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager();
    mgr.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
	public void propertyChange(java.beans.PropertyChangeEvent evt) {
	  Logger logger = Logger.getLogger("Pooka.debug.gui.focus");
	  Level logLevel = Level.FINEST;
	  if (evt.getPropertyName().equalsIgnoreCase("permanentFocusOwner")) {
	    logLevel = Level.FINE;
	  } else if (evt.getPropertyName().equalsIgnoreCase("focusOwner") || evt.getPropertyName().equalsIgnoreCase("focusOwner")) {
	    logLevel = Level.FINER;
	  }
	  String oldValue = "null";
	  String newValue = "null";
	  if (evt.getOldValue() != null) {
	    oldValue = evt.getOldValue().getClass().getName();
	  }
	  if (evt.getNewValue() != null) {
	    newValue = evt.getNewValue().getClass().getName();
	  }
	  logger.log(logLevel, evt.getPropertyName() + ":  oldValue=" + oldValue + "; newValue=" + newValue);
	}
      });

  }

  /**
   * Sets the appropriate log setting.
   */
  public void setLogLevel(String pName, Level pLogLevel) {
    Logger current = Logger.getLogger(pName);
    if (current.getLevel() != pLogLevel) {
      current.setLevel(pLogLevel);
    }
  }

  // ValueChangeListener
  /**
   * Responds to a change in a configured value.
   */
  public void valueChanged(String changedValue) {
    Level newLevel = Level.parse(Pooka.getProperty(changedValue, "OFF"));
    setLogLevel(changedValue, newLevel);
  }
}
