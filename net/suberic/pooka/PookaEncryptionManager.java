package net.suberic.pooka;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import java.io.File;
import java.io.FileInputStream;
import java.security.Key;
import java.security.KeyStoreException;
import java.util.HashSet;

import javax.mail.internet.*;
import javax.mail.*;

import net.suberic.crypto.*;
import net.suberic.util.VariableBundle;


/**
 * The EncryptionManager manages Pooka's encryption facilities.  It's 
 * basically one-stop shopping for all of your email encryption needs.
 */
public class PookaEncryptionManager {

  EncryptionKeyManager pgpKeyMgr = null;

  EncryptionKeyManager smimeKeyMgr = null;

  char[] keyMgrPasswd = null;

  Map addressToPrivateKeyMap = null;
  
  Map addressToPublicKeyMap = null;

  Map aliasPasswordMap = new HashMap();

  boolean savePasswordsForSession = false;

  /**
   * Creates an EncryptionManager using the given VariableBundle and
   * key property.
   */
  public PookaEncryptionManager(VariableBundle sourceBundle, String key) {
    // load the given pgp and smime stores.

    String pgpPublicFilename = sourceBundle.getProperty(key + ".pgp.keyStore.public.filename", "");

    String pgpPrivateFilename = sourceBundle.getProperty(key + ".pgp.keyStore.private.filename", "");
    String pgpPrivatePwString = sourceBundle.getProperty(key + ".pgp.keyStore.private.password", "");
    if (!pgpPrivatePwString.equals(""))
      pgpPrivatePwString = net.suberic.util.gui.propedit.PasswordEditorPane.descrambleString(pgpPrivatePwString);

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

    String smimePublicFilename = sourceBundle.getProperty(key + ".smime.keyStore.public.filename", "");

    String smimePrivateFilename = sourceBundle.getProperty(key + ".smime.keyStore.private.filename", "");
    String smimePrivatePwString = sourceBundle.getProperty(key + ".smime.keyStore.private.password", "");
    if (!smimePrivatePwString.equals(""))
      smimePrivatePwString = net.suberic.util.gui.propedit.PasswordEditorPane.descrambleString(smimePrivatePwString);

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

    savePasswordsForSession = Pooka.getProperty(key + ".savePasswordsForSession", "false").equalsIgnoreCase("true");
    
  }


  /**
   * Returns the private key(s) for the given email address.
   */
  public Key[] getPrivateKeys(String address) {
    return null;
  }

  /**
   * Returns all available private keys.
   */
  /*
  public Key[] getPrivateKeys() {
    Key[] returnValue = new Key[0];
    if (pgpKeyMgr != null) {
      Key[] pgpKeys = pgpKeyMgr.getPrivateKeys();
      if (pgpKeys != null && pgpKeys.length > 0)
	returnValue = pgpKeys;
    }

    if (smimeKeyMgr != null) {
      Key[] smimeKeys = smimeKeyMgr.getPrivateKeys();
      if (smimeKeys != null && smimeKeys.length > 0) {
	if (returnValue.length > 0) {
	  Key[] newReturnValue = new Key[returnValue.length + smimeKeys.length];
	  System.arraycopy(returnValue, 0, newReturnValue, 0, returnValue.length);
	  System.arraycopy(smimeKeys, 0, newReturnValue, returnValue.length, smimeKeys.length);
	  returnValue = newReturnValue;
	} else {
	  returnValue = smimeKeys;
	}
      }
    }
    
    return returnValue;
  }
  */

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
    if (pgpKeyMgr != null || smimeKeyMgr != null) {
      char[] password = getPasswordForAlias(alias, false);

      // check to see if this exists anywhere.
      if (pgpKeyMgr != null) {
	try {
	  if (pgpKeyMgr.containsPrivateKeyAlias(alias))
	    return pgpKeyMgr.getPrivateKey(alias,password);
	} catch (KeyStoreException kse) {
	  // FIXME ignore for now?
	}
	
      }
      
      if (smimeKeyMgr!= null) {
	try {
	  if (smimeKeyMgr.containsPrivateKeyAlias(alias))
	    return smimeKeyMgr.getPrivateKey(alias, password);
	} catch (KeyStoreException kse) {
	  // FIXME ignore for now?
	}

      }
    }
    
    return null;
  }

  /**
   * Returns the Private key for the given alias.
   */
  public Key getPrivateKey(String alias, char[] password) throws java.security.KeyStoreException, java.security.NoSuchAlgorithmException, java.security.UnrecoverableKeyException {

    Key returnValue = null;
    if (pgpKeyMgr != null) {
      try {
	returnValue = pgpKeyMgr.getPrivateKey(alias, password);
      } catch (KeyStoreException kse) {
	// FIXME ignore for now?
      }
    }

    if (returnValue == null && smimeKeyMgr != null) {
      try {
	returnValue = smimeKeyMgr.getPrivateKey(alias, password);
      } catch (KeyStoreException kse) {
	// FIXME ignore for now?
      }
    }

    return returnValue;
  }

  /**
   * Returns the password for this alias.
   */
  protected char[] getPasswordForAlias(String alias, boolean check) {
    char[] returnValue = (char[]) aliasPasswordMap.get(alias);
    if (returnValue == null || check) {
      returnValue = net.suberic.pooka.gui.crypto.CryptoKeySelector.showPassphraseDialog(alias);
      if (returnValue != null) {
	if (savePasswordsForSession) {
	  aliasPasswordMap.put(alias, returnValue);
	}
      }
    }

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
  public Key[] getPublicKeys(String address) {
    return null;
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
    if (key == null) {
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

}
