package net.suberic.pooka;
import net.suberic.util.*;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.InternetAddress;

public class UserProfile extends Object implements ValueChangeListener {
  Properties mailProperties;
  String name;
  URLName sendMailURL;
  
  String sentFolderName;
  FolderInfo sentFolder;
  public boolean autoAddSignature = true;
  public boolean signatureFirst = true;
  private SignatureGenerator sigGenerator;
  private AddressBook addressBook = net.suberic.pooka.Pooka.addressBook;
  
  private Vector excludeAddresses;
  
  static Vector profileList = new Vector();
  static Vector mailPropertiesMap = null;
  
  public static ValueChangeListener vcl = new ValueChangeAdapter() {
      public void valueChanged(String changedValue) {
	if (changedValue.equals("UserProfile") )
	  UserProfile.updateProfilesFromProperty();
      }
    };
  
  public UserProfile(String newName, VariableBundle mainProperties) {
    if (mailPropertiesMap == null)
      createMailPropertiesMap(mainProperties);
    
    name = newName;
    
    initializeFromProperties(mainProperties);
    
    profileList.addElement(this);
    
    registerChangeListeners();
  }
  
  /**
   * This populates the UserProfile from the Pooka properties list.
   * Useful on creation as well as when any of the properties change.
   */
  public void initializeFromProperties(VariableBundle mainProperties) {
    if (mailPropertiesMap != null) {
      
      mailProperties = new Properties();
      String profileKey;
      
      for (int i = 0; i < mailPropertiesMap.size(); i++) {
	profileKey = (String)mailPropertiesMap.elementAt(i);
	mailProperties.put(profileKey, mainProperties.getProperty("UserProfile." + name + ".mailHeaders." + profileKey, ""));
      }
      
      sentFolderName=mainProperties.getProperty("UserProfile." + name + ".sentFolder", "");
      
      sendMailURL=new URLName(mainProperties.getProperty("UserProfile." + name + ".sendMailURL", ""));
      sigGenerator=createSignatureGenerator();
      
      String fromAddr = (String)mailProperties.get("From");
      excludeAddresses = new Vector();
      excludeAddresses.add(fromAddr);
      Vector excludeProp = mainProperties.getPropertyAsVector("UserProfile." + name + ".excludeAddresses", "");
      excludeAddresses.addAll(excludeProp);
    }
    
  }
  
  /**
   * Registers this UserProfile as a ValueChangeListener for all of its
   * source properties.
   */
  private void registerChangeListeners() {
    VariableBundle resources = Pooka.getResources();
    if (mailPropertiesMap != null) {
      
      String profileKey;
      
      
      for (int i = 0; i < mailPropertiesMap.size(); i++) {
	profileKey = (String)mailPropertiesMap.elementAt(i);
	resources.addValueChangeListener(this, "UserProfile." + name + ".mailHeaders." + profileKey);
      }
      
    }
    resources.addValueChangeListener(this, "UserProfile." + name + ".sentFolder");
    
    resources.addValueChangeListener(this, "UserProfile." + name + ".sendMailURL");
    
    
  }
  
  /**
   * Modifies the UserProfile if any of its source values change.
   *
   * As specified in net.suberic.util.ValueChangeListener.
   */
  public void valueChanged(String changedValue) {
    initializeFromProperties(Pooka.getResources());
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
      
      if (key.equals("FromPersonal")) {
	fromPersonal = mailProperties.getProperty(key);
      } else if (key.equals("From")) {
	fromAddr = mailProperties.getProperty(key);
      } else if (key.equals("ReplyTo")) {
	replyAddr = mailProperties.getProperty(key);
      } else if (key.equals("ReplyToPersonal")) {
	replyPersonal = mailProperties.getProperty(key);
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
   * This removes the email addresses that define this user from the
   * given message's to fields.
   */
  public void removeFromAddress(Message m) {
    try {
      Address[] toRecs = m.getRecipients(Message.RecipientType.TO);
      Address[] ccRecs = m.getRecipients(Message.RecipientType.CC);
      Address[] bccRecs = m.getRecipients(Message.RecipientType.BCC);
      toRecs = filterAddressArray(toRecs);
      ccRecs = filterAddressArray(ccRecs);
      bccRecs = filterAddressArray(bccRecs);
      
      m.setRecipients(Message.RecipientType.TO, toRecs);
      m.setRecipients(Message.RecipientType.CC, ccRecs);
      m.setRecipients(Message.RecipientType.BCC, bccRecs);
    } catch (MessagingException me) {
    }
  }
  
  private Address[] filterAddressArray(Address[] addresses) {
    if (addresses != null && addresses.length > 0) {
      Vector returnVector = new Vector();
      for (int i = 0; i < addresses.length; i++) {
	String currentAddress = ((InternetAddress) addresses[i]).getAddress();
	boolean found = false;
	for (int j = 0; j < excludeAddresses.size() && found == false ; j++) {
	  String excludeAddr = (String)excludeAddresses.elementAt(j);
	  if (currentAddress.equals(excludeAddr))
	    found = true;
	}
	if (!found)
	  returnVector.add(addresses[i]);
      }
      
      Object[] retArr = returnVector.toArray();
      Address[] returnValue = new Address[retArr.length];
      for (int i = 0; i < retArr.length; i++)
	returnValue[i] = (Address) retArr[i];
      
      return returnValue;
    } else
      return addresses;
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
    
    if (mailPropertiesMap == null)
      createMailPropertiesMap(mainProperties);
    
    // Create each Profile
    
    String currentProfileName, profileKey;
    Properties userProperties;;
    UserProfile tmpProfile;
    
    for (int j = 0; j < newProfileKeys.size(); j++) {
      currentProfileName = (String)(newProfileKeys.elementAt(j));
      
      // don't add it if it's empty.
      if (currentProfileName.length() > 0) {
	tmpProfile = new UserProfile(currentProfileName, mainProperties);
      }
    }
  }
  
  /**
   * This creates the profile map that we'll use to create new 
   * Profile objects.
   */
  static public void createMailPropertiesMap(VariableBundle mainProperties) {
    mailPropertiesMap = new Vector();
    
    // Initialize Profile Map
    
    StringTokenizer tokens = new StringTokenizer(mainProperties.getProperty("UserProfile.mailHeaders.fields", "From:FromPersonal:ReplyTo:ReplyToPersonal:Organization"), ":");
    while (tokens.hasMoreTokens()) {
      mailPropertiesMap.addElement(tokens.nextToken());
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
      if (tmpProfile != null) { 
	profileList.removeElement(tmpProfile);
	Pooka.getResources().removeValueChangeListener(tmpProfile);
	tmpProfile = null;
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
  
  /**
   * for compatibility.
   */
  static public UserProfile getDefaultProfile(UserProfileContainer upc) {
    return upc.getDefaultProfile();
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

  public URLName getSendMailURL() {
    return sendMailURL;
  }
  
  public FolderInfo getSentFolder() {
    if (sentFolderName == null)
      loadSentFolder();
    
    return sentFolder;
  }
  
  public void setSentFolderName(String newValue) {
    sentFolderName = newValue;
    
    loadSentFolder();
  }
  
  /**
   * Loads the sent folder from the UserProfile.username.sentFolder 
   * property.
   */
  public void loadSentFolder() {
    sentFolder = Pooka.getStoreManager().getFolder(sentFolderName);
    
    if (sentFolder != null) {
      sentFolder.setSentFolder(true);
    } 
  }
  
  /**
   * Creates the signatureGenerator for this Profile.
   */
  public SignatureGenerator createSignatureGenerator() {
    try {
      String classname = Pooka.getProperty("UserProfile." + name + ".sigClass", Pooka.getProperty("Pooka.defaultSigGenerator", "net.suberic.pooka.FileSignatureGenerator"));
      Class sigClass = Class.forName(classname);
      SignatureGenerator sigGen = (SignatureGenerator) sigClass.newInstance();
      sigGen.setProfile(this);
      return sigGen;
    } catch (Exception e) {
      SignatureGenerator sigGen = new StringSignatureGenerator();
      sigGen.setProfile(this);
      return sigGen;
    }
  }
  
  /**
   * This returns a signature appropriate for the given text.
   */
  public String getSignature(String text) {
    if (sigGenerator != null)
      return sigGenerator.generateSignature(text);
    else
      return null;
  }
  
  /**
   * Returns the default signature for this UserProfile.  Use 
   * getSignature(String text) instead.
   */
  public String getSignature() {
    if (sigGenerator != null)
      return sigGenerator.generateSignature(null);
    else
      return null;
    //return (Pooka.getProperty("UserProfile." + name + ".signature", null));
  }
  
  public void setSignature(String newValue) {
    Pooka.setProperty("UserProfile." + name + ".signature", newValue);
  }
  
  public AddressMatcher getAddressMatcher() {
    if (addressBook != null)
      return addressBook.getAddressMatcher();
    else
      return null;
  }

  public AddressBook getAddressBook() {
    return addressBook;
  }
}
