package net.suberic.pooka;
import net.suberic.util.VariableBundle;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.InternetAddress;

public class UserProfile extends Object {
    Properties mailProperties;
    String name;
    static Vector profileList = new Vector();
    static Vector profileMap = null;

    public UserProfile(String newName, Properties newProps) {
	mailProperties = newProps;
	name = newName;
	profileList.addElement(this);
    }

    public void finalize() {
	profileList.removeElement(this);
    }

    public void populateMessage(MimeMessage mMsg) throws MessagingException {
	// I hate this.  I hate having to grab half of these headers on my
	// own.

	Enumeration keys = mailProperties.propertyNames();
	String fromAddr = null, fromPersonal = null, replyAddr = null, replyPersonal = null;

	while (keys.hasMoreElements()) {
	    String key = (String)(keys.nextElement());

	    if (key.equals("sendMailURL")) {
		// just drop it for now.
	    } else if (key.equals("FromPersonal")) {
		fromPersonal = mailProperties.getProperty(key);
	    } else if (key.equals("From")) {
		fromAddr = mailProperties.getProperty(key);
	    } else if (key.equals("ReplyTo")) {
		replyAddr = mailProperties.getProperty(key);
	    } else if (key.equals("ReplyToPersonal")) {
		replyPersonal = mailProperties.getProperty(key);
	    } else if (key.equals("Signature")) {
		// just drop it.
	    } else {
		mMsg.setHeader(key, mailProperties.getProperty(key));
	    }
	    
	    try {
		if (fromAddr != null) 
		    if (fromPersonal != null && !(fromPersonal.equals(""))) 
			mMsg.setFrom(new InternetAddress(fromAddr, fromPersonal));
		    else
			mMsg.setFrom(new InternetAddress(fromAddr));
	    
		if (replyAddr != null && !(replyAddr.equals("")))
		    if (replyPersonal != null)
			mMsg.setReplyTo(new InternetAddress[] {new InternetAddress(replyAddr, replyPersonal)});
		    else
			mMsg.setReplyTo(new InternetAddress[] {new InternetAddress(replyAddr)});
	    
	    } catch (java.io.UnsupportedEncodingException uee) {
		throw new MessagingException("", uee);
	    }
	}
    }

    static public void createProfiles(VariableBundle mainProperties) {
	profileMap = new Vector();

	// Initialize Profile Map

	StringTokenizer tokens = new StringTokenizer(mainProperties.getProperty("UserProfile.fields", "From:FromPersonal:ReplyTo:ReplyToPersonal:Organization:Signature:sendMailURL"), ":");
	while (tokens.hasMoreTokens()) {
	    profileMap.addElement(tokens.nextToken());
	}

	// Create each Profile

	tokens = new StringTokenizer(mainProperties.getProperty("UserProfile", ""), ":");
	
	String currentProfileName, profileKey;
	Properties userProperties;;
	UserProfile tmpProfile;

	while (tokens.hasMoreTokens()) {
	    currentProfileName = (String)(tokens.nextToken());

	    // don't add it if it's empty.
	    if (currentProfileName.length() > 0) {
		userProperties = new Properties();

		for (int i = 0; i < profileMap.size(); i++) {
		    profileKey = (String)profileMap.elementAt(i);
		    userProperties.put(profileKey, mainProperties.getProperty("UserProfile." + currentProfileName + "." + profileKey, ""));
		}
		tmpProfile = new UserProfile(currentProfileName, userProperties);
	    }
	}
    }

    static public Vector getProfileList() {
	return profileList;
    }

    static public UserProfile getProfile(String profileName) {
	for (int i = 0; i < profileList.size(); i++) {
	    UserProfile tmpProfile = (UserProfile)(profileList.elementAt(i));
	    if (tmpProfile.getName().equals(profileName)) 
		return tmpProfile;
	}

	return null;
    }

    static public UserProfile getDefaultProfile(net.suberic.pooka.gui.MessageProxy msg) {
	if (msg.getFolderInfo() != null)
	    return UserProfile.getDefaultProfile(msg.getFolderInfo());
	else
	    return UserProfile.getDefaultProfile();
    }

    static public UserProfile getDefaultProfile(FolderInfo fdr) {
	return fdr.getDefaultProfile();
    }

    static public UserProfile getDefaultProfile(StoreInfo store) {
	return store.getDefaultProfile();
    }

    /**
     * returns the default Profile object as defined by the 
     * "UserProfile.default" property, or null if there are no Profiles
     * defined.
     */
    static public UserProfile getDefaultProfile() {
	UserProfile defaultProfile;

	try {
	    defaultProfile = UserProfile.getProfile(Pooka.getProperty("UserProfile.default"));
	    return defaultProfile;
	} catch (Exception e) {
	    if (profileList.isEmpty())
		return null;
	    else
		return (UserProfile)(profileList.firstElement());
	}
    }

    public String getName() {
	return name;
    }

    public Properties getMailProperties() {
	return mailProperties;
    }


    public String toString() {
	return name;
    }
}
