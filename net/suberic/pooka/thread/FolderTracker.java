package net.suberic.pooka.thread;
import net.suberic.pooka.FolderInfo;
import net.suberic.pooka.Pooka;
import javax.mail.*;

/**
 * This class polls the underlying Folder of a FolderInfo in order to make 
 * sure that the UnreadMessageCount is current and to make sure that the
 * Folder stays open.
 */
public class FolderTracker extends Thread {
    private FolderInfo folderInfo;
    private int updateCheckMilliseconds = 60000;

    /**
     * This creates a new FolderTracker from a FolderInfo object.
     */
    public FolderTracker(FolderInfo newFolderInfo) {
	folderInfo = newFolderInfo;
	String updateString = Pooka.getProperty(folderInfo.getFolderProperty() + ".updateCheckMilliseconds", Pooka.getProperty("Pooka.updateCheckMilliseconds", ""));
	if (!updateString.equals(""))
	    updateCheckMilliseconds = Integer.parseInt(updateString);

	this.setPriority(1);
	
    }

    /**
     * This runs the thread, running checkFolder() every 
     * updateCheckMilliseconds until the thread is interrupted.
     */
    public void run() {
	int uptime = 0;
	try {
	    while (true) {
		checkFolder();
		if (updateCheckMilliseconds < 1) {
		    while (updateCheckMilliseconds < 1)
			sleep(60000);
		} else { 
		    sleep(updateCheckMilliseconds);
		}
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
    public void checkFolder() {
	if (Pooka.isDebug())
	    System.out.println("checking folder " + getFolderInfo().getFolderName());

	// i'm taking this almost directly from ICEMail; i don't know how
	// to keep the stores/folders open, either.  :)

	if (getFolderInfo().isOpen()) {
	    Store s = getFolderInfo().getParentStore().getStore();
	    try {
		Folder f = s.getFolder("nfdsaf238sa");
		f.exists();
	    } catch ( MessagingException me ) {
		try {
		    if ( ! s.isConnected() )
			s.connect();
		} catch ( MessagingException me2 ) {
		}
	    }
	    
	    getFolderInfo().resetUnread();
	}
    }

    public FolderInfo getFolderInfo() {
	return folderInfo;
    }

}
