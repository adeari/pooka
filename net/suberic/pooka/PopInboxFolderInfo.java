package net.suberic.pooka;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.File;

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

	    System.out.println("session.getProperty(mail.mbox.inbox) = " + session.getProperty("mail.mbox.inbox"));
	    System.out.println("url is " + url);
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
	// if (Pooka.isDebug())
	    System.out.println("checking folder " + getFolderName());
	
	// i'm taking this almost directly from ICEMail; i don't know how
	// to keep the stores/folders open, either.  :)
	
	Folder f = null;
	try {
	    if (isOpen() && popStore != null) {
		System.out.println("checking folder " + getFolderName() + ":  opening pop store.");
		popStore.connect();
		f = popStore.getDefaultFolder().getFolder("INBOX");
		if (f != null) {
		    f.open(Folder.READ_WRITE);
		    Message[] msgs = f.getMessages();
		    if (f == null)
			System.out.println("got messages; msgs = null.");
		    else 
			System.out.println("got messages; msgs = " + msgs.length);

		    
		    if (msgs != null && msgs.length > 0) {
			MimeMessage[] msgsToAppend = new MimeMessage[msgs.length];
			for (int i = 0; i < msgs.length; i++) {
			    msgsToAppend[i] = new MimeMessage((MimeMessage) msgs[i]);
			}
			getFolder().appendMessages(msgsToAppend);
			for (int i = 0; i < msgs.length; i++) {
			    msgs[i].setFlag(Flags.Flag.DELETED, true);
			    System.out.println("marked message " + i + " to be deleted.  isDelted = " + msgs[i].isSet(Flags.Flag.DELETED));
			}
		    }
		    f.close(true);
		    System.out.println("closed folder.");
		    f.open(Folder.READ_WRITE);
		    msgs = f.getMessages();
		    if (f == null)
			System.out.println("got messages; msgs = null.");
		    else 
			System.out.println("got messages; msgs = " + msgs.length);		    
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
}
