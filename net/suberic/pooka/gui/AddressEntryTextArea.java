package net.suberic.pooka.gui;
import net.suberic.pooka.Pooka;
import java.awt.event.KeyEvent;
import javax.swing.*;
import javax.mail.internet.InternetAddress;

/**
 * This is a JTextArea which uses an AddressMatcher to fill in completed
 * addresses.
 */
public class AddressEntryTextArea extends net.suberic.util.swing.EntryTextArea {

  // the update thread for all AddressEntryTextAreas
  static Thread updateThread;

  // the list of all AddressEntryTextAreas
  static java.util.WeakHashMap areaList = new java.util.WeakHashMap();

  // the underlying NewMessageInfo
  NewMessageUI messageUI;

  // the last time this field got a key hit
  long lastKeyTime;

  // the last time this field was updated
  long lastMatchedTime;

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
    super.processComponentKeyEvent(e);
    lastKeyTime = new java.util.Date().getTime();
    if (updateThread != null)
      updateThread.interrupt();
    else
      createUpdateThread();
  }

  /**
   * After a sufficient amount of time has passed, updates the entry area
   * with a found value.  Called by the updateThread.
   */
  protected void updateTextValue() {
    final long lastModifiedTime = lastKeyTime;
    //long currentTime = currentTime
    net.suberic.pooka.AddressMatcher matcher = messageUI.getSelectedProfile().getAddressMatcher();

    String entryString = getAddressText();
    final InternetAddress[] matchedAddresses = matcher.match(entryString);

    try {
      SwingUtilities.invokeAndWait(new Runnable() {
	  public void run() {
	    // make sure no keys have been pressed since we did the match.
	    if (lastModifiedTime == lastKeyTime) {
	      if (matchedAddresses.length > 0) {
		String newAddress = matchedAddresses[0].toString();
		updateAddressText(newAddress);
	      } else {
		updateAddressText(Pooka.getProperty("error.noMatchingAddresses", "<no matching addresses>"));
	      }

	      lastMatchedTime = new java.util.Date().getTime();
	    }
	  }
	});
    } catch (Exception e) {
    }

  }

  /**
   * This gets the currently selected address field.
   */
  public String getAddressText() {
    Selection currentSelection = getCurrentSelection();
    return currentSelection.text;
  }

  /**
   * Gets the current Selection.
   */
  Selection getCurrentSelection() {
    int caretPosition = getCaretPosition();

    String currentText = getText();

    // get the area bounded by commas, or by the beginning and end of 
    // the text.
    int beginOffset = currentText.lastIndexOf(',', caretPosition) +1;
    int endOffset = currentText.indexOf(',', caretPosition) -1;
    if (endOffset < 0)
      endOffset = currentText.length();
    
    // strip whitespace
    while(beginOffset < endOffset && Character.isWhitespace(currentText.charAt(beginOffset)))
      beginOffset++;

    return new Selection(beginOffset, endOffset, currentText.substring(beginOffset, endOffset));
  }
  
  /**
   * This updates the currently selected address field with the new value.
   */
  public void updateAddressText(String newAddress) {
    Selection current = getCurrentSelection();
    int length = current.text.length();
    // the text should always match the newAddress.  really.  :)
    this.insert(newAddress.substring(length), current.beginOffset + length);
    this.setSelectionStart(current.beginOffset + length);
    this.setSelectionEnd(current.beginOffset + newAddress.length());
  }

  private class Selection {
    int beginOffset;
    int endOffset;
    String text;

    Selection(int newBegin, int newEnd, String newText) {
      beginOffset = newBegin;
      endOffset = newEnd;
      text = newText;
    }
  }

  //----------- updater thread ----------------

  static synchronized void createUpdateThread() {
    if (updateThread == null) {
      updateThread = new Thread(new Updater(), "AddressEntryTextArea - Update Thread");
      updateThread.start();
    }
  }

  static class Updater implements Runnable {

    long sleepTime = 60000;

    Updater() {
    }

    public void run() {
      sleepTime = 0;
      java.util.Set entrySet = areaList.entrySet();
      while(! entrySet.isEmpty()) {
	sleepTime = 60000;
	java.util.Iterator entryIter = entrySet.iterator();
	while (entryIter.hasNext()) {
	  long currentTime = new java.util.Date().getTime();
	  AddressEntryTextArea area = (AddressEntryTextArea) ((java.util.Map.Entry)entryIter.next()).getKey();
	  if (area.lastKeyTime > area.lastMatchedTime) {
	    if (area.lastKeyTime + area.delayInMilliSeconds < currentTime) {
	      area.updateTextValue();
	    } else {
	      sleepTime = Math.min(sleepTime, (area.delayInMilliSeconds + area.lastKeyTime) - currentTime);
	    }
	  }
	}

	try {
	  Thread.currentThread().sleep(sleepTime);
	} catch (InterruptedException e) {
	}
      }
    }
  }
}
