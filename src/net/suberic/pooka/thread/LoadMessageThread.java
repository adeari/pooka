package net.suberic.pooka.thread;

import net.suberic.pooka.*;
import net.suberic.pooka.event.*;
import net.suberic.pooka.gui.LoadMessageTracker;
import net.suberic.pooka.gui.MessageProxy;
import net.suberic.pooka.gui.FolderInternalFrame;
import javax.mail.*;
import javax.mail.internet.*;
import java.util.*;

/**
 * This class does the actual loading of the header information from 
 * the messages in the folder.  It also is set up to communicate with
 * a JProgessBar to show how far the loading has gotten.
 *
 * More specifically, this thread takes an array of Messages and a 
 * Vector of Strings which are the column values which are to be put
 * into the table.  It then loads the values into a Vector of Vectors,
 * each of which contains the information for the Table for a group
 * of Messages.  It then throws a ChangeEvent to the listening
 * FolderTableModel.  The FolderTableModel can then get the information
 * using the getNewMessages() function.
 */

public class LoadMessageThread extends Thread {
  private FolderInfo folderInfo;
  private List loadedMessageInfo = new LinkedList();
  private List columnValues;
  private List loadQueue = new LinkedList();
  private List priorityLoadQueue = new LinkedList();
  private List messageLoadedListeners = new LinkedList();
  private int updateCheckMilliseconds = 60000;
  private int updateMessagesCount = 10;
  private int loadedMessageCount = 0;
  private boolean sleeping = false;

  private boolean stopped = false;

  public int NORMAL = 5;
  public int HIGH = 10;

  /**
   * This creates a new LoadMessageThread from a FolderInfo object.
   */
    
  public LoadMessageThread(FolderInfo newFolderInfo) {
    super("Load Message Thread - " + newFolderInfo.getFolderID());
    folderInfo = newFolderInfo;
    this.setPriority(1);
  }
  
  public void run() {
    int uptime = 0;
    while (! stopped) {
      try {
	loadWaitingMessages();
      } catch (Exception e) {
	e.printStackTrace();
      }

      try {
	sleeping = true;
	if (updateCheckMilliseconds < 1) {
	  while (updateCheckMilliseconds < 1)
	    sleep(60000);
	} else { 
	  sleep(updateCheckMilliseconds);
	}
	sleeping = false;
      } catch (InterruptedException ie) {
	sleeping = false;
      }
    }
  }
  
  
  public void loadWaitingMessages() {
    
    List messages = retrieveLoadQueue();
    int numMessages = messages.size();
    MessageProxy mp;
    
    int updateCounter = 0;
    
    if (! stopped && numMessages > 0) {
      MessageLoadedListener display = getFolderInfo().getFolderDisplayUI();
      if (display != null)
	this.addMessageLoadedListener(display);
      
      fireMessageLoadedEvent(MessageLoadedEvent.LOADING_STARTING, getLoadedMessageCount(), messages.size());
      
      int fetchBatchSize = 50;
      int loadBatchSize = 25;
      try {
	fetchBatchSize = Integer.parseInt(Pooka.getProperty("Pooka.fetchBatchSize", "50"));
      } catch (NumberFormatException nfe) {
      }
      
      try {
	loadBatchSize = Integer.parseInt(Pooka.getProperty("Pooka.loadBatchSize", "25"));
      } catch (NumberFormatException nfe) {
      }
      
      FetchProfile fetchProfile = getFolderInfo().getFetchProfile();

      int i = numMessages - 1;
      while ( ! stopped &&  i >= 0 ) {
	if (Pooka.getProperty("Pooka.openFoldersInBackGround", "false").equalsIgnoreCase("true")) {
	  synchronized(folderInfo.getFolderThread().getRunLock()) {
	    for (int batchCount = 0; ! stopped && i >=0 && batchCount < loadBatchSize; batchCount++) {
	      mp=(MessageProxy)messages.get(i);
	      
	      if (! mp.getMessageInfo().hasBeenFetched()) {
		try {
		  List fetchList = new ArrayList();
		  for (int j = i; fetchList.size() < fetchBatchSize && j >= 0; j--) {
		    MessageInfo fetchInfo = ((MessageProxy) messages.get(j)).getMessageInfo();
		    if (! fetchInfo.hasBeenFetched()) {
		      fetchList.add(fetchInfo);
		      fetchInfo.setFetched(true);
		    }
		  }
		  
		  MessageInfo[] toFetch = new MessageInfo[fetchList.size()];
		  toFetch = (MessageInfo[]) fetchList.toArray(toFetch);
		  getFolderInfo().fetch(toFetch, fetchProfile);
		} catch(MessagingException me) {
		  System.out.println("caught error while fetching for folder " + getFolderInfo().getFolderID() + ":  " + me);
		  me.printStackTrace();
		}
		
	      }
	      
	      try {
		if (! mp.isLoaded())
		  mp.loadTableInfo();
		if (mp.needsRefresh())
		  mp.refreshMessage();
		else if (! mp.matchedFilters()) {
		  mp.matchFilters();
		}
	      } catch (Exception e) {
		e.printStackTrace();
	      }
	      
	      if (++updateCounter >= getUpdateMessagesCount()) {
		fireMessageLoadedEvent(MessageLoadedEvent.MESSAGES_LOADED, getLoadedMessageCount(), messages.size());
		updateCounter = 0;		   
	      }
	      loadedMessageCount++;
	      i--;
	    }
	  } // end synchronized
	} else {
	  for (int batchCount = 0; ! stopped && i >=0 && batchCount < loadBatchSize; batchCount++) {
	    mp=(MessageProxy)messages.get(i);
	    
	    if (! mp.getMessageInfo().hasBeenFetched()) {
	      try {
		List fetchList = new ArrayList();
		for (int j = i; fetchList.size() < fetchBatchSize && j >= 0; j--) {
		  MessageInfo fetchInfo = ((MessageProxy) messages.get(j)).getMessageInfo();
		  if (! fetchInfo.hasBeenFetched()) {
		    fetchList.add(fetchInfo);
		    //fetchInfo.setFetched(true);
		  }
		}
		
		MessageInfo[] toFetch = new MessageInfo[fetchList.size()];
		toFetch = (MessageInfo[]) fetchList.toArray(toFetch);
		synchronized(folderInfo.getFolderThread().getRunLock()) {
		  getFolderInfo().fetch(toFetch, fetchProfile);
		} // end synchronized
	      } catch(MessagingException me) {
		System.out.println("caught error while fetching for folder " + getFolderInfo().getFolderID() + ":  " + me);
		me.printStackTrace();
	      }
	      
	    }
	      
	    try {
	      synchronized(folderInfo.getFolderThread().getRunLock()) {
		if (! mp.isLoaded())
		  mp.loadTableInfo();
		if (mp.needsRefresh()) {
		  mp.refreshMessage();
		} else if (! mp.matchedFilters()) {
		  mp.matchFilters();
		}
	      } // end synchronized
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	    
	    if (++updateCounter >= getUpdateMessagesCount()) {
	      fireMessageLoadedEvent(MessageLoadedEvent.MESSAGES_LOADED, getLoadedMessageCount(), messages.size());
	      updateCounter = 0;		   
	    }
	    loadedMessageCount++;
	    i--;
	  }
	}
      }

      if (updateCounter > 0)
	fireMessageLoadedEvent(MessageLoadedEvent.MESSAGES_LOADED, getLoadedMessageCount(), messages.size());
      
      fireMessageLoadedEvent(MessageLoadedEvent.LOADING_COMPLETE, getLoadedMessageCount(), messages.size());
      
      if (display != null)
	removeMessageLoadedListener(display);
    }
  }
  

  /**
   * Fires a new MessageLoadedEvent to each registered MessageLoadedListener.
   */
  
  public void fireMessageLoadedEvent(int type, int numMessages, int max) {
    for (int i = 0; i < messageLoadedListeners.size(); i ++) {
      ((MessageLoadedListener)messageLoadedListeners.get(i)).handleMessageLoaded(new MessageLoadedEvent(this, type, numMessages, max));
    }
  }
  
  /**
   * Adds a MessageLoadedListener to the messageLoadedListener list.
   */
  public void addMessageLoadedListener(MessageLoadedListener newListener) {
    if (messageLoadedListeners.indexOf(newListener) == -1)
      messageLoadedListeners.add(newListener);
  }
  
  /**
   * Removes a MessageLoadedListener from the messageLoadedListener list,
   * if it's in the list.
   */
  
  public void removeMessageLoadedListener(MessageLoadedListener remListener) {
    if (messageLoadedListeners.indexOf(remListener) > -1)
      messageLoadedListeners.remove(remListener);
  }
  
  /**
   * Adds the MessageProxy(s) to the loadQueue.
   */
  public synchronized void loadMessages(MessageProxy mp) {
    loadMessages(mp, NORMAL);
  }

  /**
   * Adds the MessageProxy(s) to the loadQueue.
   */
  public synchronized void loadMessages(MessageProxy mp, int pPriority) {
    if (pPriority > NORMAL) {
      priorityLoadQueue.add(mp);
    } else {
      loadQueue.add(mp);
    }
    
    if (this.isSleeping())
      this.interrupt();
    
  }
  
  /**
   * Adds the MessageProxy(s) to the loadQueue.
   */
  public synchronized void loadMessages(MessageProxy[] mp) {
    loadMessages(mp, NORMAL);
  }

  /**
   * Adds the MessageProxy(s) to the loadQueue.
   */
  public synchronized void loadMessages(MessageProxy[] mp, int pPriority) {
    if (mp != null && mp.length > 0) {
      if (pPriority > NORMAL) 
	priorityLoadQueue.addAll(Arrays.asList(mp));
      else
	loadQueue.addAll(Arrays.asList(mp));
    }
    
    if (this.isSleeping())
      this.interrupt();
  }
  
  /**
   * Adds the MessageProxy(s) to the loadQueue.
   */
  public synchronized void loadMessages(List mp) {
    loadMessages(mp, NORMAL);
  }

  /**
   * Adds the MessageProxy(s) to the loadQueue.
   */
  public synchronized void loadMessages(List mp, int pPriority) {
    if (mp != null && mp.size() > 0) {
      if (pPriority > NORMAL) 
	priorityLoadQueue.addAll(mp);
      else
	loadQueue.addAll(mp);
    }
    
    if (this.isSleeping())
      this.interrupt();
  }
  
  /**
   * retrieves all the messages from the loadQueue, and resets that
   * List to 0 (an empty List).
   *
   * generally, use retrieveNextBatch() instead.
   */
  public synchronized List retrieveLoadQueue() {
    List returnValue = new LinkedList();
    returnValue.addAll(priorityLoadQueue);
    returnValue.addAll(loadQueue);
    loadQueue = new LinkedList();
    priorityLoadQueue = new LinkedList();
    return returnValue;
  }
  
  /**
   * Retrieves the next pCount messages from the queue, or returns null
   * if there are no entries in the queue.
   */
  public synchronized List retrieveNextBatch(int pCount) {
    int plqLength = priorityLoadQueue.size();
    int lqLength = loadQueue.size();

    // check to see if we actually have anything in the queue.
    if (plqLength + lqLength > 0) {
      List returnValue = new LinkedList();

      // adding the priority queue first
      if (plqLength > 0) {
	// if the priority queue is larger than (or the same size as) the 
	// requested count, then just return it.
	if (plqLength >= pCount) {
	  List subList = priorityLoadQueue.subList(0, pCount - 1);
	  returnValue.addAll(subList);
	  subList.clear();
	  return returnValue;
	} else {
	  // just add of the priority queue, and go on.
	  returnValue.addAll(priorityLoadQueue);
	  priorityLoadQueue.clear();
	}
      }

      // add in the normal queue now.
      if (lqLength > 0) {
	int newCount = pCount - plqLength;
	if (lqLength >= newCount) {
	  List subList = loadQueue.subList(0, newCount -1);
	  returnValue.addAll(subList);
	  subList.clear();
	} else {
	  returnValue.addAll(loadQueue);
	  loadQueue.clear();
	}
      }
      
      return returnValue;
    } else {
      return null;
    }
  }

  public int getUpdateMessagesCount() {
    return updateMessagesCount;
  }
  
  public void setUpdateMessagesCount(int newValue) {
    updateMessagesCount = newValue;
  }
  
  public List getColumnValues() {
    return columnValues;
  }
  
  public void setColumnValues(List newValue) {
    columnValues=newValue;
  }
  
  public FolderInfo getFolderInfo() {
    return folderInfo;
  }
  
  public int getLoadedMessageCount() {
    return loadedMessageCount;
  }
  
  public boolean isSleeping() {
    return sleeping;
  }

  /**
   * Stops the thread.
   */
  public void stopThread() {
    stopped = true;
  }
}






