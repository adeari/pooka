package net.suberic.pooka.gui;
import net.suberic.pooka.Pooka;
import java.awt.event.KeyEvent;
import java.util.LinkedList;
import javax.swing.*;
import javax.mail.internet.InternetAddress;

/**
 *<p> This is a JTextArea which uses an AddressMatcher to fill in completed
 * addresses.  It also will store 
 */
public class AddressEntryTextArea extends net.suberic.util.swing.EntryTextArea implements java.awt.event.FocusListener {
  
  //---------- static variables -------------//
  // the update thread for all AddressEntryTextAreas
  static Thread updateThread;

  // the list of all AddressEntryTextAreas
  static java.util.WeakHashMap areaList = new java.util.WeakHashMap();

  //---------- instance variables -----------//
  // the list of Addresses
  LinkedList addressList = new LinkedList();

  // if we're doing this by delay or by keystroke
  boolean automaticallyDisplay = false;

  // if by keystroke, the key that is used to request address completion
  javax.swing.KeyStroke completionKey = javax.swing.KeyStroke.getKeyStroke(net.suberic.pooka.Pooka.getProperty("Pooka.addressComplete", "control D"));

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

    this.addFocusListener(this);
  }

  /**
   * Creates a new AddressEntryTextArea using the given NewMessageUI.
   */
  public AddressEntryTextArea(NewMessageUI ui, String text, int rows, int columns) {
    super(text, rows, columns);
    messageUI = ui;
    areaList.put(this, null);

    this.addFocusListener(this);
  }

  /**
   * Makes it so that we listen for key events.  On a key event, we update
   * the last time a key was pressed.
   */
  protected void processComponentKeyEvent(KeyEvent e) {
    super.processComponentKeyEvent(e);
    if (e.getID() == KeyEvent.KEY_PRESSED) {
      int keyCode = e.getKeyCode();
      switch(keyCode) {
      case KeyEvent.VK_TAB:
	break;
      case KeyEvent.VK_UP:
	selectNextEntry();
	break;
      case KeyEvent.VK_DOWN:
	selectPreviousEntry();
	break;
      case KeyEvent.VK_LEFT:
	// ignore
	break;
      case KeyEvent.VK_RIGHT:
	// ignore
	break;
      default:
	if (automaticallyDisplay) {
	  lastKeyTime = new java.util.Date().getTime();
	  if (updateThread != null)
	    updateThread.interrupt();
	  else
	    createUpdateThread();
	} else {
	  if (keyCode == completionKey.getKeyCode() && e.getModifiers() == completionKey.getModifiers()) {
	    lastKeyTime = new java.util.Date().getTime();
	    if (updateThread != null)
	      updateThread.interrupt();
	    else
	      createUpdateThread();
	  }
	}
      }
    }
  }

  /**
   * After a sufficient amount of time has passed, updates the entry area
   * with a found value.  Called by the updateThread.
   */
  protected void updateTextValue() {
    final long lastModifiedTime = lastKeyTime;
    //long currentTime = currentTime
    net.suberic.pooka.AddressMatcher matcher = messageUI.getSelectedProfile().getAddressMatcher();

    final String entryString = getAddressText();

    if (needToMatch(entryString)) {
      net.suberic.pooka.AddressBookEntry[] matchedEntries = matcher.match(entryString);
      InternetAddress[] tmpMatched = new InternetAddress[matchedEntries.length];
      for (int i = 0; i < matchedEntries.length; i++) {
	tmpMatched[i] = matchedEntries[i].getAddress();
      }
      final InternetAddress[] matchedAddresses = tmpMatched;

      try {
	SwingUtilities.invokeAndWait(new Runnable() {
	    public void run() {
	      // make sure no keys have been pressed since we did the match.
	      if (lastModifiedTime == lastKeyTime) {
		if (matchedAddresses.length > 0) {
		  String newAddress = matchedAddresses[0].toString(); 
		  if (!newAddress.equalsIgnoreCase(entryString))
		    updateAddressText(newAddress);
		} else {
		  updateAddressText(entryString + Pooka.getProperty("error.noMatchingAddresses", "<no matching addresses>"));
		}
		
		lastMatchedTime = new java.util.Date().getTime();
	      }
	    }
	  });
      } catch (Exception e) {
      }
    }
  }

  /**
   * This tests to see if the given string needs to be matched or not.
   */
  public boolean needToMatch(String entry) {
    if (entry.length() == 0) 
      return false;
    else
      return true;
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

  /**
   * This updates the currently selected address field with the new value.
   */
  public void replaceAddressText(Selection current, String newAddress) {
    int length = current.text.length();
    // the text should always match the newAddress.  really.  :)
    this.replaceRange(newAddress, current.beginOffset, current.endOffset);
    this.setSelectionStart(current.beginOffset);
    this.setSelectionEnd(current.beginOffset + newAddress.length());
  }

  /**
   * Selects the next available address entry.
   */
  public void selectNextEntry() {
    Selection currentSelection = getCurrentSelection();
    net.suberic.pooka.AddressMatcher matcher = messageUI.getSelectedProfile().getAddressMatcher();
    InternetAddress newValue = matcher.getNextMatch(currentSelection.text).getAddress();
    if (newValue != null) {
      replaceAddressText(currentSelection, newValue.toString());
    }

  }

  /**
   * Selects the previous available address entry.
   */
  public void selectPreviousEntry() {
    Selection currentSelection = getCurrentSelection();
    net.suberic.pooka.AddressMatcher matcher = messageUI.getSelectedProfile().getAddressMatcher();
    InternetAddress newValue = matcher.getPreviousMatch(currentSelection.text).getAddress();
    if (newValue != null) {
      replaceAddressText(currentSelection, newValue.toString());
    }

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

  //----------- focus listener ----------------
  /**
   * a no-op -- don't do anything on focusGained.
   */
  public void focusGained(java.awt.event.FocusEvent e) {
    
  }

  /**
   *
   */
  public void focusLost(java.awt.event.FocusEvent e) {
    lastMatchedTime = new java.util.Date().getTime();
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
	  if (area.lastKeyTime > area.lastMatchedTime || ! area.automaticallyDisplay) {
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

  /**
   * <p>An address entry.  Consists of a text representation, an underlying
   * address representation, and a status.</p>
   */
  public class AddressField {
    public String addressText;
    public String actualAddress;
    public int status;

    public AddressField(String newText, String newActual, int newStatus) {
      addressText = newText;
      actualAddress = newActual;
      status = newStatus;
    }
  }

}
