package net.suberic.util.thread;
import java.util.HashMap;
import javax.swing.Action;
import java.awt.event.*;
import java.util.Vector;

/**
 * This is a Thread which handles ActionEvents.  Instead of having the 
 * Action handled by the main thread, instead it is put into a queue on 
 * this thread, which will then handle the Action.
 */
public class ActionThread extends Thread {

    public ActionThread(String threadName) {
	super(threadName);
    }

    public class ActionEventPair {
	public Action action;
	public ActionEvent event;

	public ActionEventPair(Action newAction, ActionEvent newEvent) {
	    action=newAction;
	    event=newEvent;
	}
    }

    // the action queue.
    private Vector actionQueue = new Vector();

    // a flag to catch any time when the thread might be ending its run()
    // method, but not dead yet.
    private boolean dying;

    public void run() {
	ActionEventPair pair = popQueue();
	while (pair != null) {
	    pair.action.actionPerformed(pair.event);
	    pair = popQueue();
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
	    dying=true;
	    return null;
	}
    }

    /**
     * This adds an item to the queue.  It also starts up the Thread if it's
     * not already running.
     */
    public synchronized void addToQueue(Action action, ActionEvent event) {
	actionQueue.addElement(new ActionEventPair(action, event));
	if (!this.isAlive()) {
	    this.start();
	    dying = false;
	} else if (dying) {
	    boolean restarted = false;
	    while (!restarted) {
		try {
		    Thread.sleep(100);
		} catch (InterruptedException e) {
		}
		try {
		    this.start();
		    restarted=true;
		} catch (IllegalThreadStateException itse) {

		}
	    }
	}
	
	
    }
	
}
