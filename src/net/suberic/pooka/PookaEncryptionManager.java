package net.suberic.pooka;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import java.io.File;
import java.io.FileInputStream;
import java.security.Key;
import java.security.KeyStoreException;
import java.security.GeneralSecurityException;
import java.util.HashSet;
import java.util.ArrayList;

import javax.mail.internet.*;
import javax.mail.*;

import net.suberic.crypto.*;
import net.suberic.util.VariableBundle;
import net.suberic.util.ValueChangeListener;

/**
 * The EncryptionManager manages Pooka's encryption facilities.  It's 
 * basically one-stop shopping for all of your email encryption needs.
 */
public class PookaEncryptionManager implements ValueChangeListener {

  String key;
  VariableBundle sourceBundle;

  EncryptionKeyManager pgpKeyMgr = null;

  EncryptionKeyManager smimeKeyMgr = null;

  char[] keyMgrPasswd = null;

  Map cachedPrivateKeys = new HashMap();

  Map cachedPublicKeys = new HashMap();

  Map addressToPublicKeyMap = null;

  boolean savePasswordsForSession = false;

  boolean needsReload = false;

  /**
   * Creates an EncryptionManager using the given VariableBundle and
   * key property.
   */
  public PookaEncryptionManager(VariableBundle pSourceBundle, String pKey) {
    sourceBundle = pSourceBundle;
    key = pKey;

    // register this for listening to changes to the store filenames and the
    // store passwords.
    sourceBundle.addValueChangeListener(this, key + ".pgp.keyStore.private.filename");
    sourceBundle.addValueChangeListener(this, key + ".pgp.keyStore.private.password");
    sourceBundle.addValueChangeListener(this, key + ".pgp.keyStore.public.filename");

    sourceBundle.addValueChangeListener(this, key + ".smime.keyStore.public.filename");
    sourceBundle.addValueChangeListener(this, key + ".smime.keyStore.private.filename");
    sourceBundle.addValueChangeListener(this, key + ".smime.keyStore.private.password");

    sourceBundle.addValueChangeListener(this, key + ".savePasswordsForSession");

    // load the given pgp and smime stores.
    loadStores(sourceBundle, key);
  }

  /**
   * Loads the stores.
   */
  public void loadStores(VariableBundle sourceBundle, String key) {
    String pgpPublicFilename = sourceBundle.getProperty(key + ".pgp.keyStore.public.filename", "");

    String pgpPrivateFilename = sourceBundle.getProperty(key + ".pgp.keyStore.private.filename", "");
    String pgpPrivatePwString = sourceBundle.getProperty(key + ".pgp.keyStore.private.password", "");
    if (!pgpPrivatePwString.equals(""))
      pgpPrivatePwString = net.suberic.util.gui.propedit.PasswordEditorPane.descrambleString(pgpPrivatePwString);

    // if either store is configured, try loading.
    if (! (pgpPrivateFilename.equals("") && pgpPublicFilename.equals(""))) {
      try {
	EncryptionUtils pgpUtils = EncryptionManager.getEncryptionUtils("PGP");
	if (pgpUtils != null) {
	  pgpKeyMgr = pgpUtils.createKeyManager();
	  try {
	    pgpKeyMgr.loadPrivateKeystore(new FileInputStream(new File(pgpPrivateFilename)), pgpPrivatePwString.toCharArray());
	  } catch (java.io.IOException fnfe) {
	    System.out.println("Error loading PGP private keystore from file " + pgpPrivateFilename + ":  " + fnfe.getMessage());
	  } catch (java.security.GeneralSecurityException gse) {
	    System.out.println("Error loading PGP private keystore from file " + pgpPrivateFilename + ":  " + gse.getMessage());
	  }
	  try {
	    pgpKeyMgr.loadPublicKeystore(new FileInputStream(new File(pgpPublicFilename)), null);
	  } catch (java.io.IOException fnfe) {
	    System.out.println("Error loading PGP public keystore from file " + pgpPublicFilename + ":  " + fnfe.getMessage());
	  } catch (java.security.GeneralSecurityException gse) {
	    System.out.println("Error loading PGP private keystore from file " + pgpPublicFilename + ":  " + gse.getMessage());
	  }      
	}
      } catch (java.security.NoSuchProviderException nspe) {
	System.out.println("Error loading PGP key store:  " + nspe.getMessage());
      } catch (Exception e) {
	System.out.println("Error loading PGP key store:  " + e.getMessage());
      }
    }

    String smimePublicFilename = sourceBundle.getProperty(key + ".smime.keyStore.public.filename", "");

    String smimePrivateFilename = sourceBundle.getProperty(key + ".smime.keyStore.private.filename", "");
    String smimePrivatePwString = sourceBundle.getProperty(key + ".smime.keyStore.private.password", "");
    if (!smimePrivatePwString.equals(""))
      smimePrivatePwString = net.suberic.util.gui.propedit.PasswordEditorPane.descrambleString(smimePrivatePwString);

    // if either store is configured, try loading.
    if (! (smimePrivateFilename.equals("") && smimePublicFilename.equals(""))) {
      try {
	EncryptionUtils smimeUtils = EncryptionManager.getEncryptionUtils("S/MIME");
	if (smimeUtils != null) {
	  smimeKeyMgr = smimeUtils.createKeyManager();
	  try {
	    smimeKeyMgr.loadPrivateKeystore(new FileInputStream(new File(smimePrivateFilename)), smimePrivatePwString.toCharArray());
	  } catch (java.security.GeneralSecurityException gse) {
	    System.out.println("Error loading S/MIME private keystore from file " + smimePrivateFilename + ":  " + gse.getMessage());
	  } catch (java.io.IOException fnfe) {
	    System.out.println("Error loading S/MIME private keystore from file " + smimePrivateFilename + ":  " + fnfe.getMessage());
	  }
	  
	  try {
	    smimeKeyMgr.loadPublicKeystore(new FileInputStream(new File(smimePublicFilename)), smimePrivatePwString.toCharArray());
	  } catch (java.io.IOException fnfe) {
	    System.out.println("Error loading S/MIME public keystore from file " + smimePublicFilename + ":  " + fnfe.getMessage());
	  } catch (java.security.GeneralSecurityException gse) {
	    System.out.println("Error loading S/MIME private keystore from file " + smimePublicFilename + ":  " + gse.getMessage());
	  }      
	}
      } catch (java.security.NoSuchProviderException nspe) {
	System.out.println("Error loading S/MIME key store:  " + nspe.getMessage());
      } catch (Exception e) {
	System.out.println("Error loading S/MIME key store:  " + e.getMessage());
      }
    }

    savePasswordsForSession = Pooka.getProperty(key + ".savePasswordsForSession", "false").equalsIgnoreCase("true");
    
    cachedPrivateKeys = new HashMap();

    cachedPublicKeys = new HashMap();

    addressToPublicKeyMap = null;

  }

  /**
   * As defined in net.suberic.util.ValueChangeListener.
   * 
   */
  public void valueChanged(String changedValue) {
    if (changedValue.equals(key + ".savePasswordsForSession")) {
      savePasswordsForSession = Pooka.getProperty(key + ".savePasswordsForSession", "false").equalsIgnoreCase("true");
    } else {
      // this is crazy.
      needsReload = true;
      javax.swing.SwingUtilities.invokeLater(new Runnable() {

	  public void run() {
	    if (needsReload) {
	      needsReload = false;
	      
	      Thread updateThread = new Thread(new Runnable() {
		  public void run() {
		    loadStores(sourceBundle, key);
		  }
		});
	      
	      updateThread.start();
	    }
	  }
	});
    }
  }
  
  
  /**
   * Adds the private key to the store.
   */
  public void addPrivateKey(String alias, Key privateKey, char[] passphrase, String type) throws GeneralSecurityException {
    EncryptionKeyManager currentMgr = getKeyMgr(type);
    if (currentMgr != null) {
      currentMgr.setPrivateKeyEntry(alias, privateKey, passphrase);
    } else {
      throw new KeyStoreException(type + " KeyStore not initialized.");
    }
  }

  /**
   * Adds the public key to the store.
   */
  public void addPublicKey(String alias, Key publicKey, String type) 
  throws GeneralSecurityException {
    
    EncryptionKeyManager currentMgr = getKeyMgr(type);
    if (currentMgr != null) {
      currentMgr.setPublicKeyEntry(alias, publicKey);
    } else {
      throw new KeyStoreException(type + " KeyStore not initialized.");
    }
  }

  /**
   * Returns the private key(s) for the given email address.
   */
  public Key[] getPrivateKeys(String address) {
    return getPrivateKeys(address, null);
  }

  /**
   * Returns the private key(s) for the given email address and 
   * the given encryption type, or all matching keys if type == null.
   */
  public Key[] getPrivateKeys(String address, String type) {
    return null;
  }

  /**
   * Returns all private keys that have been cached.
   */
  public Key[] getCachedPrivateKeys() {
    return (Key[]) cachedPrivateKeys.values().toArray(new Key[0]);
  }
  
  /**
   * Returns all available private key aliases.
   */
  public Set privateKeyAliases() throws java.security.KeyStoreException {
    return privateKeyAliases(null);
  }

  /**
   * Returns all available private key aliases for the give EncryptionType,
   * or all available aliases if type is null.
   */
  public Set privateKeyAliases(String encryptionType) throws java.security.KeyStoreException {
    if (encryptionType != null && encryptionType.equalsIgnoreCase(EncryptionManager.PGP)) {
      if (pgpKeyMgr != null)
	return new HashSet(pgpKeyMgr.privateKeyAliases());
    } else if (encryptionType != null && encryptionType.equalsIgnoreCase(EncryptionManager.SMIME)) {
      if (smimeKeyMgr != null) {
	return new HashSet(smimeKeyMgr.privateKeyAliases());
      }
    } else {
      // return both.
      Set returnValue = new java.util.HashSet();
      if (pgpKeyMgr != null) {
	try {
	  returnValue.addAll(pgpKeyMgr.privateKeyAliases());
	} catch (KeyStoreException kse) {
	  // FIXME ignore for now?
	}
      }
      if (smimeKeyMgr != null) {
	try {
	  returnValue.addAll(smimeKeyMgr.privateKeyAliases());
	} catch (KeyStoreException kse) {
	  // FIXME ignore for now?
	}
      }

      return returnValue;
    }

    return new HashSet();
  }

  /**
   * Returns the Private key for the given alias.
   */
  public Key getPrivateKey(String alias) throws java.security.KeyStoreException, java.security.NoSuchAlgorithmException, java.security.UnrecoverableKeyException {
    KeyStoreException caughtException = null;

    // first check to see if this is in the cache.
    Key cachedKey = (Key) cachedPrivateKeys.get(alias);
    if (cachedKey != null)
      return cachedKey;

    if (pgpKeyMgr != null || smimeKeyMgr != null) {

      // check to see if this exists anywhere.
      if (pgpKeyMgr != null) {
	try {
	  if (pgpKeyMgr.containsPrivateKeyAlias(alias)) {
	    Key returnValue = pgpKeyMgr.getPrivateKey(alias, null);
	    cachedPrivateKeys.put(alias, returnValue);
	    return returnValue;
	  }
	} catch (KeyStoreException kse) {
	  caughtException = kse;
	}
	
      }
      
      if (smimeKeyMgr!= null) {
	try {
	  if (smimeKeyMgr.containsPrivateKeyAlias(alias)) {
	    Key returnValue = smimeKeyMgr.getPrivateKey(alias, null);
	    cachedPrivateKeys.put(alias, returnValue);
	    return returnValue;
	  }
	} catch (KeyStoreException kse) {
	  if (caughtException == null)
	    caughtException = kse;
	}
	
      }
    }
    
    if (caughtException != null)
      throw caughtException;

    return null;
  }

  /**
   * Returns the Private key for the given alias.
   */
  public Key getPrivateKey(String alias, char[] password) throws java.security.KeyStoreException, java.security.NoSuchAlgorithmException, java.security.UnrecoverableKeyException {

    KeyStoreException caughtException = null;

    // first check to see if this is in the cache.
    Key cachedKey = (Key) cachedPrivateKeys.get(alias);
    if (cachedKey != null)
      return cachedKey;

    Key returnValue = null;
    if (pgpKeyMgr != null) {
      try {
	returnValue = pgpKeyMgr.getPrivateKey(alias, password);
      } catch (KeyStoreException kse) {
	  caughtException = kse;
      }
    }

    if (returnValue == null && smimeKeyMgr != null) {
      try {
	returnValue = smimeKeyMgr.getPrivateKey(alias, password);
      } catch (KeyStoreException kse) {
	if (caughtException == null)
	  caughtException = kse;
      }
    }

    if (returnValue != null) {
      cachedPrivateKeys.put(alias, returnValue);
    }

    if (returnValue == null && caughtException != null)
      throw caughtException;

    return returnValue;
  }

  /**
   * Returns the Public key for the given alias.
   */
  public Key getPublicKey(String alias) throws java.security.KeyStoreException, java.security.NoSuchAlgorithmException, java.security.UnrecoverableKeyException {
    Key returnValue = null;
    if (pgpKeyMgr != null) {
      try {
	returnValue = pgpKeyMgr.getPublicKey(alias);
      } catch (KeyStoreException kse) {
	// FIXME ignore for now?
      }
    }

    if (returnValue == null && smimeKeyMgr != null) {
      try {
	returnValue = smimeKeyMgr.getPublicKey(alias);
      } catch (KeyStoreException kse) {
	// FIXME ignore for now?
      }
    }

    return returnValue;
  }

  /**
   * Returns the public key(s) for the given email address.
   */
  public Key[] getPublicKeys(String address) throws java.security.KeyStoreException, java.security.NoSuchAlgorithmException, java.security.UnrecoverableKeyException {
    return getPublicKeys(address, null);
  }

  /**
   * Returns the public key(s) for the given email address that match
   * the given encryption type, or all matching keys if type == null.
   */
  public Key[] getPublicKeys(String address, String type) throws java.security.KeyStoreException, java.security.NoSuchAlgorithmException, java.security.UnrecoverableKeyException {
    if (addressToPublicKeyMap == null) {
      sortPublicKeys();
    }

    ArrayList list = (ArrayList) addressToPublicKeyMap.get(address);
    if (list == null)
      return new Key[0];
    else if (type == null) {
      return (Key[]) list.toArray(new Key[0]);
    } else {
      ArrayList sortedList = new ArrayList();
      java.util.Iterator iter = list.iterator();
      while (iter.hasNext()) {
	EncryptionKey current = (EncryptionKey) iter.next();
	try {
	  if (current.getEncryptionUtils().getType() == type) {
	    sortedList.add(current);
	  }
	} catch (Exception e) {
	}
      }

      return (Key[]) sortedList.toArray(new Key[0]);
    }
  }

  /**
   * Sorts all available public keys by associated address.
   */
  private synchronized void sortPublicKeys() throws java.security.KeyStoreException, java.security.NoSuchAlgorithmException, java.security.UnrecoverableKeyException {
    if (addressToPublicKeyMap == null) {
      addressToPublicKeyMap = new HashMap();
      Set aliases = publicKeyAliases();
      java.util.Iterator iter = aliases.iterator();
      while (iter.hasNext()) {
	Key current = getPublicKey((String) iter.next());

	if (current instanceof EncryptionKey) {
	  String[] assocAddresses = ((EncryptionKey) current).getAssociatedAddresses();
	  for (int i = 0; assocAddresses != null && i < assocAddresses.length; i++) {
	    String address = assocAddresses[i];
	    ArrayList matches = (ArrayList) addressToPublicKeyMap.get(address);
	    if (matches != null) {
	      if (! matches.contains(current))
	      matches.add(current);
	    } else {
	      matches = new ArrayList();
	      matches.add(current);
	      addressToPublicKeyMap.put(address, matches);
	    }
	  }
	}
      }
    }
  }

  /**
   * Returns all available public key aliases.
   */
  public Set publicKeyAliases() throws java.security.KeyStoreException {
    return publicKeyAliases(null);
  }

  /**
   * Returns available public key aliases for the given encryption type, or
   * all available aliases if null.
   */
  public Set publicKeyAliases(String encryptionType) throws java.security.KeyStoreException {

    if (encryptionType != null && encryptionType.equalsIgnoreCase(EncryptionManager.PGP)) {
      if (pgpKeyMgr != null)
	return new HashSet(pgpKeyMgr.publicKeyAliases());
    } else if (encryptionType != null && encryptionType.equalsIgnoreCase(EncryptionManager.SMIME)) {
      if (smimeKeyMgr != null) {
	return new HashSet(smimeKeyMgr.publicKeyAliases());
      }
    } else {
      // return both.
      Set returnValue = new java.util.HashSet();
      if (pgpKeyMgr != null) {
	try {
	  returnValue.addAll(pgpKeyMgr.publicKeyAliases());
	} catch (KeyStoreException kse) {
	  // FIXME ignore for now?
	}
      }
      if (smimeKeyMgr != null) {
	try {
	  returnValue.addAll(smimeKeyMgr.publicKeyAliases());
	} catch (KeyStoreException kse) {
	  // FIXME ignore for now?
	}
      }
      
      return returnValue;
    }

    return new HashSet();

  }

  /**
   * Encrypts to given message.  Actually checks all of the recipients
   * configured to see if we have a key for each one.
   */
  public MimeMessage encryptMessage(MimeMessage mMsg) throws MessagingException, java.security.GeneralSecurityException, java.io.IOException {
    
    // if we don't have a key, see if we can get a default.
    Key key = null;
    Address[] recipients = mMsg.getRecipients(Message.RecipientType.TO);
    for (int i = 0; key == null && i < recipients.length; i++) {
      if (recipients[i] instanceof InternetAddress) {
	String inetAddr = ((InternetAddress) recipients[i]).getAddress();
	Key[] matchingKeys = getPublicKeys(inetAddr);
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
  public MimeMessage encryptMessage(MimeMessage mMsg, Key key)
    throws MessagingException, java.security.GeneralSecurityException, java.io.IOException {
    if (key != null) {
      if (key instanceof EncryptionKey) {
	return ((EncryptionKey) key).getEncryptionUtils().encryptMessage(Pooka.getDefaultSession(), mMsg, key);
      } else {
	return EncryptionManager.getEncryptionUtils("PGP").encryptMessage(Pooka.getDefaultSession(), mMsg, key);
      }
      
    }
    return mMsg;
  }

  /**
   * Signs the given message.
   */
  public MimeMessage signMessage(MimeMessage mMsg, UserProfile profile, Key key) 
    throws MessagingException, java.io.IOException, java.security.GeneralSecurityException  {
    if (key == null && profile != null) {
      key = profile.getEncryptionKey();
    }
    
    if (key == null) {
      // get user input
    }
    
    if (key != null) {
      if (key instanceof net.suberic.crypto.EncryptionKey) {
	return ((EncryptionKey) key).getEncryptionUtils().signMessage(Pooka.getDefaultSession(), mMsg, key);
      } else {
	return EncryptionManager.getEncryptionUtils("PGP").signMessage(Pooka.getDefaultSession(), mMsg, key);
      }
    } else {
      return mMsg;
    }
  }

  /**
   * Returns the EncryptionKeyManager for this type.
   */
  EncryptionKeyManager getKeyMgr(String type) {
    if (type == EncryptionManager.PGP) 
      return pgpKeyMgr;
    else if (type == EncryptionManager.SMIME)
      return smimeKeyMgr;
    else
      return null;
  }
}
