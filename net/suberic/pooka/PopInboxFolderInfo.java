package net.suberic.pooka;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.*;

/**
 * This represents the Inbox of a Pop3 folder.  It has an mbox backend, but
 * uses the pop folder to get new messages.
 */
public class PopInboxFolderInfo extends FolderInfo {

    Store popStore;
    Folder popInbox;

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
	
	String mailHome = Pooka.getProperty("Store." + storeID + ".mailDir", "");
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
	checkFolder();
	
    }

    /**
     * Checks the pop folder for new messages.
     */
    public void checkFolder() {
	if (Pooka.isDebug())
	    System.out.println("checking folder " + getFolderName());
	
	Folder f = null;
	try {
	    if (isOpen() && popStore != null) {
		if (Pooka.isDebug())
		    System.out.println("checking folder " + getFolderName() + ":  opening pop store.");
		popStore.connect();
		f = popStore.getDefaultFolder().getFolder("INBOX");
		if (f != null) {
		    f.open(Folder.READ_WRITE);
		    Message[] msgs = f.getMessages();
		    if (Pooka.isDebug()) {
			if (f == null)
			    System.out.println("got messages; msgs = null.");
			else 
			    System.out.println("got messages; msgs = " + msgs.length);
		    }
		    
		    if (msgs != null && msgs.length > 0) {
			MimeMessage[] msgsToAppend = new MimeMessage[msgs.length];
			for (int i = 0; i < msgs.length; i++) {
			    msgsToAppend[i] = new MimeMessage((MimeMessage) msgs[i]);
			}
			getFolder().appendMessages(msgsToAppend);
			if (! Pooka.getProperty(getFolderProperty() + ".leaveMessagesOnServer", "false").equalsIgnoreCase("true")) {
			    for (int i = 0; i < msgs.length; i++) {
				msgs[i].setFlag(Flags.Flag.DELETED, true);
				if (Pooka.isDebug())
				    System.out.println("marked message " + i + " to be deleted.  isDelted = " + msgs[i].isSet(Flags.Flag.DELETED));
			    }
			} else if (Pooka.getProperty(getFolderProperty() + ".deleteOnServerOnLocalDelete", "false").equalsIgnoreCase("true")) {
			    
			}
		    }
		    f.close(true);
		    if (Pooka.isDebug())
			System.out.println("closed folder.");
		    f.open(Folder.READ_WRITE);
		    msgs = f.getMessages();
		    if (Pooka.isDebug()) {
			if (f == null)
			    System.out.println("got messages; msgs = null.");
			else 
			    System.out.println("got messages; msgs = " + msgs.length);		    
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

    /**
     * This retrieves new messages from the pop folder.
     */
    public Message[] getNewMessages(Folder f) throws MessagingException {
	Message[] newMessages = f.getMessages();
	if (newMessages.length > 0) {
	    String lastUid = null;
	    try {
		lastUid = readLastUid(); 
	    } catch (IOException ioe) {
	    }

	    if (lastUid != null) {
		int offset = newMessages.length - 1;
		while (offset >= 0 && (getUID(newMessages[offset], f).compareTo(lastUid) < 0)) {
		    offset--;
		}
		
		if (newMessages.length - 1 - offset == 0)
		    // no new messages
		    return new Message[0];
		else {
		    Message[] returnValue = new Message[newMessages.length - offset - 1];
		    System.arraycopy(newMessages, offset, returnValue, 0, newMessages.length - offset - 1);
		    
		    try {
			writeLastUid(getUID(newMessages[newMessages.length - 1], f));
		    } catch (IOException ioe) {
		    }

		    return returnValue;
		}
	    } else { 
		// if we don't have a reference to a last uid, then assume
		// all the messages are new.
		
		if (newMessages.length > 0)
		    try {
			writeLastUid(getUID(newMessages[newMessages.length - 1], f));
		    } catch (IOException ioe) {
		    }
		return newMessages;
	    }
	} else {
	    // no messages.
	    return newMessages;
	}
    }

    public String readLastUid() throws IOException {
	String storeID = getParentStore().getStoreID();
	String mailHome = Pooka.getProperty("Store." + storeID + ".mailDir", "");
	File uidFile = new File(mailHome + File.separator + ".pooka-lastUid");
	if (uidFile.exists()) {
	    BufferedReader br = new BufferedReader(new FileReader(uidFile));
	    String lastUid = br.readLine();
	    System.out.println("lastUid is " + lastUid);
	    
	    br.close();

	    return lastUid;
	}

	return null;
    }

    public void writeLastUid(String lastUid) throws IOException {
	String storeID = getParentStore().getStoreID();
	String mailHome = Pooka.getProperty("Store." + storeID + ".mailDir", "");
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
}
