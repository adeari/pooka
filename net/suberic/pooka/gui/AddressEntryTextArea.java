package net.suberic.pooka.gui;
import java.awt.event.KeyEvent;
import javax.swing.*;

/**
 * This is a JTextArea which uses an AddressMatcher to fill in completed
 * addresses.
 */
public class AddressEntryTextArea extends net.suberic.util.swing.EntryTextArea {

  // the update thread for all AddressEntryTextAreas
  static Thread updateThread;

  // the list of all AddressEntryTextAreas
  static WeakHashMap areaList = new java.util.WeakHashMap();

  // the underlying NewMessageInfo
  NewMessageUI messageUI;

  // the last time this field got a key hit
  long lastKeyTime;

  // the delay in milliseconds between the last key hit and the next update.
  int delayInMilliSeconds = 1000;

  /**
   * Creates a new AddressEntryTextArea using the given NewMessageUI.
   */
  public AddressEntryTextArea(NewMessageUI ui, int rows, int columns) {
    super(rows, columns);
    messageUI = ui;
    areaList.put(this, null);
  }

  /**
   * Creates a new AddressEntryTextArea using the given NewMessageUI.
   */
  public AddressEntryTextArea(NewMessageUI ui, String text, int rows, int columns) {
    super(text, rows, columns);
    messageUI = ui;
    areaList.put(this, null);
  }

  /**
   * Makes it so that we listen for key events.  On a key event, we update
   * the last time a key was pressed.
   */
  protected void processComponentKeyEvent(KeyEvent e) {
    super(e);
    lastKeyTime = new java.util.Date().getTime();
  }

  /**
   * After a sufficient amount of time has passed, updates the entry area
   * with a found value.  Called by the updateThread.
   */
  protected void updateTextValue() {
    final long lastModifiedTime = lastKeyTime;
    //long currentTime = currentTime
    AddressMatcher matcher = messageUI.getAddressMatcher();

    String entryString = getText();
    final InternetAddress[] matchedAddresses = matcher.match(entryString);

    try {
      SwingUtilities.invokeAndWait(new Runnable() {
	  public void run() {
	    if (matchedAddress.length > 0) {
	      String newAddress = matchedAddress[0].toString();
	      if (getText().
	    }
	  }
	});
    } catch (Exception e) {
    }

  }
  
}
