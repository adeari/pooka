package net.suberic.pooka.thread;
import net.suberic.pooka.FolderInfo;
import net.suberic.pooka.Pooka;
import net.suberic.util.thread.*;
import javax.mail.*;
import java.util.*;
import java.awt.event.ActionEvent;

/**
 * This class polls the underlying Folder of a FolderInfo in order to make 
 * sure that the UnreadMessageCount is current and to make sure that the
 * Folder stays open.
 */
public class FolderTracker extends Thread {
  private Vector folders = new Vector();
  private CheckFolderAction action = new CheckFolderAction();
  
  private class UpdateInfo {
    public FolderInfo folder;
    public long updateCheckMilliseconds;
    public long nextUpdateTime;
    
    public UpdateInfo(FolderInfo info, long updateCheck) {
      folder = info;
      updateCheckMilliseconds = updateCheck;
      nextUpdateTime = Calendar.getInstance().getTime().getTime() + updateCheckMilliseconds;
    }
    
    public void update() {
      folder.getFolderThread().addToQueue(getAction(), new ActionEvent(folder, 1, "folder-check"));
      nextUpdateTime = Calendar.getInstance().getTime().getTime() + updateCheckMilliseconds;
    }
    
    public boolean shouldUpdate(long currentTime) {
      return (nextUpdateTime <= currentTime) ;
    }
    
  }
  
  /**
   * This creates a new FolderTracker from a FolderInfo object.
   */
  public FolderTracker() {
    super("Folder Tracker thread");
    this.setPriority(1);
  }

  /**
   * This adds a FolderInfo to the FolderTracker.
   */
  public void addFolder(FolderInfo newFolder) {
    long updateCheckMilliseconds;
    String updateString = Pooka.getProperty(newFolder.getFolderProperty() + ".updateCheckMilliseconds", Pooka.getProperty("Pooka.updateCheckMilliseconds", "60000"));
    try {
      updateCheckMilliseconds = Long.parseLong(updateString);
    } catch (Exception e) {
      updateCheckMilliseconds = 60000;
    }
    
    folders.add(new UpdateInfo(newFolder, updateCheckMilliseconds));
    
  }
  
  /**
   * This removes a FolderInfo from the FolderTracker.
   */
  public void removeFolder(FolderInfo folder) {
    for (int i = 0 ; i < folders.size() ; i++) 
      if (((UpdateInfo)folders.elementAt(i)).folder == folder)
	folders.removeElementAt(i);
  }
  
  /**
   * This runs the thread, running checkFolder() every 
   * updateCheckMilliseconds until the thread is interrupted.
   */
  public void run() {
    try {
      while (true) {
	long currentTime = Calendar.getInstance().getTime().getTime();
	updateFolders(currentTime);
	sleep(getNextUpdateTime(currentTime) - currentTime);
      }
    } catch (InterruptedException ie) {
    }
  }
  
  /**
   * This just checks to see if we can get a NewMessageCount from the
   * folder.  As a brute force method, it also accesses the folder
   * at every check, catching and throwing away any Exceptions that happen.  
   * It's nasty, but it _should_ keep the Folder open..
   */
  public void checkFolder(FolderInfo folderInfo) {
    try {
      folderInfo.checkFolder();
    } catch (MessagingException me) {
      if (Pooka.isDebug()) {
	System.out.println("caught exception checking folder " + folderInfo.getFolderID() + ":  " + me);
	me.printStackTrace();
      }
    }
  }
  
  public void updateFolders(long currentTime) {
    for (int i = 0; i < folders.size(); i++) {
      UpdateInfo info = (UpdateInfo)folders.elementAt(i);
      if (info.shouldUpdate(currentTime))
	info.update();
    }
  }
  
  
  /**
   * This returns the next update time.
   */
  public long getNextUpdateTime(long currentTime) {
    long nextTime = -1;
    for (int i = 0 ; i < folders.size() ; i++) {
      if (nextTime == -1)
	nextTime = ((UpdateInfo)folders.elementAt(i)).nextUpdateTime;
      else
	nextTime = Math.min(nextTime, ((UpdateInfo)folders.elementAt(i)).nextUpdateTime);
    }
    
    if (nextTime == -1)
      return currentTime + 120000;
    else
      return nextTime;
  }
  
  /**
   * This returns the action to run when it's time to update the folder.
   */
  public javax.swing.Action getAction() {
    return action;
  }
  
  public class CheckFolderAction extends javax.swing.AbstractAction {
    public CheckFolderAction() {
      super("folder-check");
    }
    
    public void actionPerformed(java.awt.event.ActionEvent e) {
      checkFolder((FolderInfo)e.getSource());
    }
  }
}
