package net.suberic.util.thread;
import java.util.HashMap;
import javax.swing.Action;
import java.awt.event.*;
import java.util.Vector;

/**
 * This is a Thread which handles ActionEvents.  Instead of having the 
 * Action handled by the main thread, it is put into a queue on 
 * this thread, which will then handle the Action.
 */
public class ActionThread extends Thread {

  // indicates high priority
  public static int PRIORITY_HIGH = 10;

  // indicates normal priority
  public static int PRIORITY_NORMAL = 5;

  // indicates low priority
  public static int PRIORITY_LOW = 0;

  boolean stopMe = false;

  Object runLock = new Object();

  String mCurrentActionName = "";

  /**
   * Creates an ActionThread with the given threadName.
   */
  public ActionThread(String threadName) {
    super(threadName);
  }
  
  /**
   * Represents an action/event pair.  This also stores the priority of 
   * the event.
   */
  public class ActionEventPair {
    public Action action;
    public ActionEvent event;
    public int priority = PRIORITY_NORMAL;

    /**
     * Creates an NORMAL priority ActionEventPair.
     */
    public ActionEventPair(Action newAction, ActionEvent newEvent) {
      this(newAction, newEvent, PRIORITY_NORMAL);
    }

    /**
     * Creates an ActionEventPair with the given priority.
     */
    public ActionEventPair(Action newAction, ActionEvent newEvent, int newPriority) {
      action=newAction;
      event=newEvent;
      priority = newPriority;
    }
  }
  
  // the action queue.
  private Vector actionQueue = new Vector();
  
  private boolean sleeping;
  
  public void run() {
    while(! stopMe ) {
      sleeping = false;
      ActionEventPair pair = popQueue();
      while (pair != null) {
	try {
	  synchronized(runLock) {
	    
	    pair.action.actionPerformed(pair.event);
	  }
	} catch (Exception e) {
	  e.printStackTrace();
	}
	pair = popQueue();
      }
      try {
	sleeping = true;
	while (true)
	  Thread.sleep(100000000);
      } catch (InterruptedException ie) {
	sleeping = false;
      }
    }
  }
  
  /**
   * This returns the top item in the action queue, if one is available.
   * If not, the method returns null.
   */
  public synchronized ActionEventPair popQueue() {
    if (actionQueue.size() > 0) {
      return (ActionEventPair)actionQueue.remove(0);
    }
    else {
      return null;
    }
  }
  
  /**
   * This adds an item to the queue.  It also starts up the Thread if it's
   * not already running.
   */
  public synchronized void addToQueue(Action action, ActionEvent event) {
    addToQueue(action, event, PRIORITY_NORMAL);
  } 	
  
  /**
   * This adds an item to the queue.  It also starts up the Thread if it's
   * not already running.
   */
  public synchronized void addToQueue(Action action, ActionEvent event, int priority) {
    // see where this should go.
    int index = 0;
    boolean found = false;
    while (! found && index < actionQueue.size()) {
      ActionEventPair current = (ActionEventPair) actionQueue.elementAt(index);
      if (current.priority < priority)
	found = true;
      else
	index++;
    }
    actionQueue.add(index, new ActionEventPair(action, event, priority));
    if (sleeping)
      this.interrupt();
  } 	
  
  public void setStop(boolean newValue) {
    stopMe = newValue;
  }

  public Object getRunLock() {
    return runLock;
  }
}
