package net.suberic.pooka;
import net.suberic.util.*;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.InternetAddress;

public class UserProfile extends Object {
    Properties mailProperties;
    String name;
    static Vector profileList = new Vector();
    static Vector profileMap = null;
    public static ValueChangeListener vcl = new ValueChangeAdapter() {
	    public void valueChanged(String changedValue) {
		if (changedValue.equals("UserProfile") )
		    UserProfile.updateProfilesFromProperty();
	    }
	};

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

    /**
     * This method updates the ProfileList from the UserProfile property.
     */

    static public void updateProfilesFromProperty() {
	Vector currentValues = Pooka.getResources().getPropertyAsVector("UserProfile", "");
	Vector oldValues = new Vector(profileList);
	Vector newValues = new Vector();
	Vector removeValues = new Vector();

	for (int i = 0; i < currentValues.size(); i++) {
	    UserProfile up = getProfile((String)currentValues.elementAt(i));
	    if (up == null)
		newValues.add(currentValues.elementAt(i));
	    else
		oldValues.removeElement(up);
	}

	for (int i = 0; i < oldValues.size(); i++)
	    removeValues.add(((UserProfile)oldValues.elementAt(i)).getName());

	createProfilesFromList(Pooka.getResources(), newValues);
	removeProfilesFromList(Pooka.getResources(), removeValues);
    }

    static public void createProfiles(VariableBundle mainProperties) {
	StringTokenizer tokens = new StringTokenizer(mainProperties.getProperty("UserProfile", ""), ":");
	Vector newProfiles = new Vector();
	while (tokens.hasMoreTokens())
	    newProfiles.add(tokens.nextToken());
	createProfilesFromList(mainProperties, newProfiles);
    }

    /**
     * This creates a new Profile for each Profile specified in the 
     * newProfileKeys Vector.
     */ 
    static public void createProfilesFromList(VariableBundle mainProperties, Vector newProfileKeys) {
	
	if (profileMap == null)
	    createProfileMap(mainProperties);
	
	// Create each Profile

	String currentProfileName, profileKey;
	Properties userProperties;;
	UserProfile tmpProfile;

	for (int j = 0; j < newProfileKeys.size(); j++) {
	    currentProfileName = (String)(newProfileKeys.elementAt(j));
	    
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

    /**
     * This creates the profile map that we'll use to create new 
     * Profile objects.
     */
    static public void createProfileMap(VariableBundle mainProperties) {
	profileMap = new Vector();
	
	// Initialize Profile Map
	
	StringTokenizer tokens = new StringTokenizer(mainProperties.getProperty("UserProfile.fields", "From:FromPersonal:ReplyTo:ReplyToPersonal:Organization:Signature:sendMailURL"), ":");
	while (tokens.hasMoreTokens()) {
	    profileMap.addElement(tokens.nextToken());
	}
	
    }

    /**
     * This removes each profile specified in the Vector removeKeys.
     * Each entry in the removeKeys Vector should be the String which 
     * returns the corresponding Profile to be removed.
     */
    static public void removeProfilesFromList(VariableBundle mainProperties, Vector removeProfileKeys) {
	if (removeProfileKeys == null)
	    return;

	for (int i = 0; i < removeProfileKeys.size(); i++) {
	    UserProfile tmpProfile = getProfile((String)removeProfileKeys.elementAt(i));
	    if (tmpProfile != null)
		profileList.removeElement(tmpProfile);
	    tmpProfile = null;
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

    /**
     * for compatibility.
     */
    static public UserProfile getDefaultProfile(UserProfileContainer upc) {
	return upc.getDefaultProfile();
    }

    /*    static public UserProfile getDefaultProfile(net.suberic.pooka.gui.MessageProxy msg) {
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
    */

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
