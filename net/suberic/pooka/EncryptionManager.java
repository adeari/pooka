package net.suberic.pooka;

import java.util.Map;
import java.util.HashMap;

import java.io.File;
import java.io.FileInputStream;

import javax.mail.internet.*;
import javax.mail.*;

import net.suberic.pooka.crypto.*;
import net.suberic.util.VariableBundle;


/**
 * The EncryptionManager manages Pooka's encryption facilities.  It's 
 * basically one-stop shopping for all of your email encryption needs.
 */
public class EncryptionManager {

  EncryptionUtils defaultUtils = null;

  EncryptionUtils pgpUtils = null;
  
  EncryptionUtils smimeUtils = null;

  EncryptionKeyManager keyMgr = null;

  String keyMgrFilename = null;

  char[] keyMgrPasswd = null;

  Map addressToPrivateKeyMap = null;
  
  Map addressToPublicKeyMap = null;

  Map aliasPasswordMap = new HashMap();

  /**
   * Creates an EncryptionManager using the given VariableBundle and
   * key property.
   */
  public EncryptionManager(VariableBundle sourceBundle, String key) {
    String utilsClassName = sourceBundle.getProperty(key + ".provider", "");
    if (utilsClassName != null) {
      try {
	Class utilsClass = Class.forName(utilsClassName);
	PGPProviderImpl providerImpl = (PGPProviderImpl) utilsClass.newInstance();
	PGPMimeEncryptionUtils cryptoUtils  = new PGPMimeEncryptionUtils();
	cryptoUtils.setPGPProviderImpl(providerImpl);
	defaultUtils = cryptoUtils;
      } catch (Exception e) {
	e.printStackTrace();
      }
    }

    if (defaultUtils != null) {
      keyMgrFilename = sourceBundle.getProperty(key + ".keyStore.filename", "");
      String pwString = sourceBundle.getProperty(key + ".keyStore.password", "");
      char[] pwArray = new char[pwString.length()];
      for (int i = 0; i < pwString.length(); i++) {
	pwArray[i] = pwString.charAt(i);
      }
      keyMgrPasswd = pwArray;

      try {
	keyMgr = defaultUtils.createKeyManager(new FileInputStream(new File(keyMgrFilename)), keyMgrPasswd);
      } catch (Exception e) {
	e.printStackTrace();
      }
    }
  }

  /**
   * Returns the default EncryptionUtilities.
   */
  public EncryptionUtils getDefaultEncryptionUtils() {
    if (defaultUtils == null) {
      synchronized(this) {
	if (defaultUtils == null) {

	  PGPMimeEncryptionUtils cryptoUtils = new PGPMimeEncryptionUtils();
	  
	  cryptoUtils.setPGPProviderImpl(new net.suberic.pooka.crypto.gpg.GPGPGPProviderImpl());
	  
	  defaultUtils = cryptoUtils;
	}
      }
    }

    return defaultUtils;
  }

  /**
   * Returns the EncryptionKeyManager that this EncryptionManager is using.
   */
  public EncryptionKeyManager getKeyManager() {
    return keyMgr;
  }

  /**
   * Returns the private key(s) for the given email address.
   */
  public EncryptionKey[] getPrivateKeys(String address) {
    EncryptionKeyManager mgr = getKeyManager();
    if (mgr != null) {
      
    }

    return null;
  }

  /**
   * Returns the Private key for the given alias.
   */
  public EncryptionKey getPrivateEncryptionKey(String alias) 
  throws EncryptionException {
    EncryptionKeyManager mgr = getKeyManager();
    if (mgr != null) {
      try {
	char[] password = getPasswordForAlias(alias, false);
	return mgr.getPrivateKey(alias, password);
      } catch (java.security.KeyStoreException kse) {
	throw new EncryptionException(kse);
      }
    }

    return null;
  }

  /**
   * Returns the password for this alias.
   */
  protected char[] getPasswordForAlias(String alias, boolean check) {
    // FIXME:  ignoring check for now.
    return (char[]) aliasPasswordMap.get(alias);
  }

  /**
   * Returns the Private key for the given alias.
   */
  public EncryptionKey getPublicEncryptionKey(String alias) 
  throws EncryptionException {
    EncryptionKeyManager mgr = getKeyManager();
    if (mgr != null) {
      try {
	return mgr.getPublicKey(alias);
      } catch (java.security.KeyStoreException kse) {
	throw new EncryptionException(kse);
      }
    }
    
    return null;
  }

  /**
   * Returns the public key(s) for the given email address.
   */
  public EncryptionKey[] getPublicKeys(String address) {

    EncryptionKeyManager mgr = getKeyManager();
    if (mgr != null) {
      
    }

    return null;
  }

  /**
   * Encrypts to given message.  Actually checks all of the recipients
   * configured to see if we have a key for each one.
   */
  public MimeMessage encryptMessage(MimeMessage mMsg) 
    throws EncryptionException, MessagingException  {
    
    // if we don't have a key, see if we can get a default.
    EncryptionKey key = null;
    Address[] recipients = mMsg.getRecipients(Message.RecipientType.TO);
    for (int i = 0; key == null && i < recipients.length; i++) {
      if (recipients[i] instanceof InternetAddress) {
	String inetAddr = ((InternetAddress) recipients[i]).getAddress();
	EncryptionKey[] matchingKeys = getPublicKeys(inetAddr);
	if (matchingKeys != null) {
	  for (int j = 0; key != null && j < matchingKeys.length; j++) {
	    key = matchingKeys[j];
	  }
	}
      }
    }
    
    return encryptMessage(mMsg, key);
  }

  /**
   * Encrypts the given message.  If there's no key, return null.
   */
  public MimeMessage encryptMessage(MimeMessage mMsg, EncryptionKey key)
    throws EncryptionException, MessagingException  {
    if (key != null) {
      return getDefaultEncryptionUtils().encryptMessage(Pooka.getDefaultSession(), mMsg, key);
    } else
      return mMsg;
  }

  /**
   * Signs the given message.
   */
  public MimeMessage signMessage(MimeMessage mMsg, UserProfile profile, EncryptionKey key) 
    throws EncryptionException, MessagingException, java.io.IOException  {
    if (key == null) {
      key = profile.getEncryptionKey();
    }

    if (key == null) {
      // get user input.
    }
    
    if (key != null)
      return getDefaultEncryptionUtils().signMessage(Pooka.getDefaultSession(), mMsg, key);
    else
      return mMsg;
  }

}
