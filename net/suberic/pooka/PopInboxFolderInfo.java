package net.suberic.pooka;
import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.event.MessageCountEvent;
import java.io.*;
import java.util.Vector;
import net.suberic.pooka.cache.ChangeCache;

/**
 * This represents the Inbox of a Pop3 folder.  It has an mbox backend, but
 * uses the pop folder to get new messages.
 */
public class PopInboxFolderInfo extends FolderInfo {

    Store popStore;
    Folder popInbox;
    ChangeCache changeAdapter;
    String mailHome;

    public static String UID_HEADER = "X-Pooka-Pop-UID";

    /**
     * Creates a new FolderInfo from a parent StoreInfo and a Folder 
     * name.
     */
    
    public PopInboxFolderInfo(StoreInfo parent, String fname) {
	super(parent, fname);

	// create the pop folder.
	configurePopStore(parent.getStoreID());
    }

    private void configurePopStore(String storeID) {
	String user = Pooka.getProperty("Store." + storeID + ".user", "");
	String password = Pooka.getProperty("Store." + storeID + ".password", "");
	if (!password.equals(""))
	    password = net.suberic.util.gui.PasswordEditorPane.descrambleString(password);
	String server = Pooka.getProperty("Store." + storeID + ".server", "");
	String protocol = Pooka.getProperty("Store." + storeID + ".protocol", "");
	
	URLName url = new URLName(protocol, server, -1, "", user, password);
	
	mailHome = Pooka.getProperty("Store." + storeID + ".mailDir", "");
	if (mailHome.equals("")) {
	    mailHome = Pooka.getProperty("Pooka.defaultMailSubDir", "");
	    if (mailHome.equals(""))
		mailHome = System.getProperty("user.home") + File.separator + ".pooka";
	    
	    mailHome = mailHome + File.separator + storeID;
	}
	String inboxFileName = mailHome + File.separator + Pooka.getProperty("Pooka.inboxName", "INBOX");
	String userHomeName = mailHome + File.separator + Pooka.getProperty("Pooka.subFolderName", "folders");

	try {
	    File userHomeDir = new File(userHomeName);
	    if (! userHomeDir.exists())
		userHomeDir.mkdirs();
	    
	    File inboxFile = new File(inboxFileName);
	    if (! inboxFile.exists())
		inboxFile.createNewFile();
	} catch (Exception e) {
	    System.out.println("caught exception: " + e);
	    e.printStackTrace();
	}
	
	changeAdapter = new ChangeCache(new File(mailHome));

	try {
	    // this will use a new session so we can get the corrent mbox
	    // properties set.
	    java.util.Properties sysProps = System.getProperties();
	    sysProps.setProperty("mail.mbox.inbox", inboxFileName);
	    sysProps.setProperty("mail.mbox.userhome", userHomeName);
	    Session session = javax.mail.Session.getInstance(sysProps, Pooka.defaultAuthenticator);

	    if (Pooka.isDebug()) {
		System.out.println("session.getProperty(mail.mbox.inbox) = " + session.getProperty("mail.mbox.inbox"));
		System.out.println("url is " + url);
	    }
	    popStore = session.getStore(url);
	} catch (NoSuchProviderException nspe) {
	    nspe.printStackTrace();
	    // available=false;
	}
    }


    public void openFolder(int mode) throws MessagingException {
	super.openFolder(mode);
	//checkFolder();
    }

    public synchronized void loadAllMessages() throws MessagingException {
	if (folderTableModel == null) {
	    super.loadAllMessages();
	    checkFolder();
	}
    }

    /**
     * Checks the pop folder for new messages.  If deleteOnServerOnLocalDelete 
     * is set to true, this will also go through and remove any messages
     * on the server that have been removed on the local client.
     */
    public void checkFolder() {
	if (Pooka.isDebug())
	    System.out.println("checking folder " + getFolderName());
	
	Folder f = null;
	try {
	    if (isConnected() && popStore != null) {
		if (Pooka.isDebug())
		    System.out.println("checking folder " + getFolderName() + ":  opening pop store.");
		popStore.connect();
		f = popStore.getDefaultFolder().getFolder("INBOX");
		if (f != null) {
		    f.open(Folder.READ_WRITE);
		    Message[] msgs = getNewMessages(f);
		    
		    if (msgs != null && msgs.length > 0) {
			MimeMessage[] msgsToAppend = new MimeMessage[msgs.length];
			for (int i = 0; i < msgs.length; i++) {
			    msgsToAppend[i] = new MimeMessage((MimeMessage) msgs[i]);
			    String uid = getUID(msgs[i], f);
			    msgsToAppend[i].addHeader(UID_HEADER, uid);
			}
			if (Pooka.isDebug()) 
			    System.out.println(Thread.currentThread() + ":  running appendMessages; # of added messages is " + msgsToAppend.length);

			getFolder().appendMessages(msgsToAppend);
			
			if (! leaveMessagesOnServer()) {
			    if (Pooka.isDebug())
				System.out.println("removing all messages.");
			    
			    for (int i = 0; i < msgs.length; i++) {
				msgs[i].setFlag(Flags.Flag.DELETED, true);
				if (Pooka.isDebug())
				    System.out.println("marked message " + i + " to be deleted.  isDelted = " + msgs[i].isSet(Flags.Flag.DELETED));
			    }
			}
		    }
		    
		    if (isDeletingOnServer()) {
			removeDeletedMessages(f);
		    }

		    f.close(true);
		    popStore.close();
		}
		resetMessageCounts();
	    }
	} catch ( MessagingException me ) {
	    me.printStackTrace();
	    try {
		if (f != null && f.isOpen())
		    f.close(false);
	    } catch (Exception e ) {
	    }

	    try {
		popStore.close();
	    } catch (Exception e) {
	    }
	} 
	
    }

    protected void runMessagesRemoved(MessageCountEvent mce) {
	if (folderTableModel != null) {
	    Message[] removedMessages = mce.getMessages();
	    if (Pooka.isDebug())
		System.out.println("removedMessages was of size " + removedMessages.length);
	    MessageInfo mi;
	    Vector removedProxies=new Vector();
	    for (int i = 0; i < removedMessages.length; i++) {
		if (Pooka.isDebug())
		    System.out.println("checking for existence of message.");

		// first, if we're removed from the pop3 folder on deletion,
		// we need to note that this message has been removed.

		if (isDeletingOnServer()) {
		    try {
			MimeMessage mm = (MimeMessage) removedMessages[i];
			String uid = mm.getHeader(UID_HEADER, ":");
			if (uid != null)
			    getChangeAdapter().setFlags(uid, new Flags(Flags.Flag.DELETED), true);
		    } catch (Exception e) {
		    }
		}

		mi = getMessageInfo(removedMessages[i]);
		if (mi.getMessageProxy() != null)
		    mi.getMessageProxy().close();
		
		if (mi != null) {
		    if (Pooka.isDebug())
			System.out.println("message exists--removing");
		    removedProxies.add(mi.getMessageProxy());
		    messageToInfoTable.remove(mi);
		}
	    }
	    getFolderTableModel().removeRows(removedProxies);
	}
	resetMessageCounts();
	fireMessageCountEvent(mce);
    }
    
    /**
     * This retrieves new messages from the pop folder.
     */
    public Message[] getNewMessages(Folder f) throws MessagingException {
	if (Pooka.isDebug())
	    System.out.println("getting new messages.");
	Message[] newMessages = f.getMessages();
	if (newMessages.length > 0) {
	    String lastUid = null;
	    try {
		lastUid = readLastUid(); 
	    } catch (IOException ioe) {
	    }

	    if (Pooka.isDebug())
		System.out.println("read:  lastUID is " + lastUid);

	    if (lastUid != null) {
		int lastRead = newMessages.length - 1;
		boolean found = false;
		while (lastRead >= 0 && found == false) {
		    String newUid = getUID(newMessages[lastRead], f);
		    if (Pooka.isDebug())
			System.out.println("offset is " + lastRead + "; newUid is " + newUid);
		    int value = newUid.compareTo(lastUid);
		    if (Pooka.isDebug())
			System.out.println("newUid.compareTo(lastUid) is " + value);
		    if (value <= 0)
			found = true;
		    else 
			lastRead--;
		}

		if (Pooka.isDebug())
		    System.out.println("final lastRead is " + lastRead + "; for reference, newMessages.length = " + newMessages.length);
		if (newMessages.length - lastRead < 2) {
		    // no new messages
		    if (Pooka.isDebug())
			System.out.println("no new messages.");
		    return new Message[0];
		} else {
		    if (Pooka.isDebug())
			System.out.println("returning " + (newMessages.length - lastRead - 1) + " new messages.");
		    Message[] returnValue = new Message[newMessages.length - lastRead - 1];
		    System.arraycopy(newMessages, lastRead + 1, returnValue, 0, newMessages.length - lastRead - 1);
		    
		    try {
			writeLastUid(getUID(newMessages[newMessages.length - 1], f));
		    } catch (IOException ioe) {
		    }

		    return returnValue;
		}
	    } else { 
		// if we don't have a reference to a last uid, then assume
		// all the messages are new.
		
		if (Pooka.isDebug())
		    System.out.println(newMessages.length + " messages in folder.  no last read id.  returning all.");
		if (newMessages.length > 0)
		    try {
			writeLastUid(getUID(newMessages[newMessages.length - 1], f));
		    } catch (IOException ioe) {
		    }
		return newMessages;
	    }
	} else {
	    if (Pooka.isDebug())
		System.out.println("no messages in folder.");
	    // no messages.
	    return newMessages;
	}
    }

    public String readLastUid() throws IOException {
	File uidFile = new File(mailHome + File.separator + ".pooka-lastUid");
	if (uidFile.exists()) {
	    BufferedReader br = new BufferedReader(new FileReader(uidFile));
	    String lastUid = br.readLine();
	    if (Pooka.isDebug())
		System.out.println("lastUid is " + lastUid);
	    
	    br.close();

	    return lastUid;
	}

	return null;
    }

    public void writeLastUid(String lastUid) throws IOException {
	File uidFile = new File(mailHome + File.separator + ".pooka-lastUid");
	if (uidFile.exists()) {
	    uidFile.delete();
	}

	uidFile.createNewFile();

	BufferedWriter bw = new BufferedWriter(new FileWriter(uidFile));
	
	bw.write(lastUid);
	bw.newLine();

	bw.flush();
	bw.close();

    }

    public String getUID(Message m, Folder f) throws MessagingException {
	return ((com.sun.mail.pop3.POP3Folder)f).getUID(m);
    }

    public void removeDeletedMessages(Folder f) throws MessagingException {
	try {
	    getChangeAdapter().writeChanges((com.sun.mail.pop3.POP3Folder)f);
	} catch (java.io.IOException ioe) {
	    throw new MessagingException("Error", ioe);
	}
    }

    public boolean isDeletingOnServer() {
	return Pooka.getProperty(getParentStore().getStoreProperty() + ".deleteOnServerOnLocalDelete", "false").equalsIgnoreCase("true");
	
    }

    public boolean leaveMessagesOnServer() {
	return Pooka.getProperty(getParentStore().getStoreProperty() + ".leaveMessagesOnServer", "false").equalsIgnoreCase("true");
    }

    public ChangeCache getChangeAdapter() {
	return changeAdapter;
    }
    
}

