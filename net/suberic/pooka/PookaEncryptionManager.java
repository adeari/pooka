package net.suberic.pooka;

import java.util.Map;
import java.util.HashMap;

import java.io.File;
import java.io.FileInputStream;
import java.security.Key;

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

  /**
   * Creates an EncryptionManager using the given VariableBundle and
   * key property.
   */
  public PookaEncryptionManager(VariableBundle sourceBundle, String key) {
    // load the given pgp and smime stores.

    String pgpPublicFilename = sourceBundle.getProperty(key + ".pgp.keyStore.public.filename", "");

    String pgpPrivateFilename = sourceBundle.getProperty(key + ".pgp.keyStore.private.filename", "");
    String pgpPrivatePwString = sourceBundle.getProperty(key + ".pgp.keyStore.private.password", "");

    try {
      EncryptionUtils pgpUtils = EncryptionManager.getEncryptionUtils("PGP");
      if (pgpUtils != null) {
	pgpKeyMgr = pgpUtils.createKeyManager();
	pgpKeyMgr.loadPrivateKeystore(new FileInputStream(new File(pgpPrivateFilename)), pgpPrivatePwString.toCharArray());
	pgpKeyMgr.loadPublicKeystore(new FileInputStream(new File(pgpPublicFilename)), null);
      }
    } catch (Exception e) {
      // FIXME
      e.printStackTrace();
    }

    String smimePublicFilename = sourceBundle.getProperty(key + ".smime.keyStore.public.filename", "");

    String smimePrivateFilename = sourceBundle.getProperty(key + ".smime.keyStore.private.filename", "");
    String smimePrivatePwString = sourceBundle.getProperty(key + ".smime.keyStore.private.password", "");

    try {
      EncryptionUtils smimeUtils = EncryptionManager.getEncryptionUtils("S/MIME");
      if (smimeUtils != null) {
	smimeKeyMgr = smimeUtils.createKeyManager();
	smimeKeyMgr.loadPrivateKeystore(new FileInputStream(new File(smimePrivateFilename)), smimePrivatePwString.toCharArray());
	smimeKeyMgr.loadPublicKeystore(new FileInputStream(new File(smimePublicFilename)), null);
      }
    } catch (Exception e) {
      // FIXME
      e.printStackTrace();
    }

  }


  /**
   * Returns the private key(s) for the given email address.
   */
  public Key[] getPrivateKeys(String address) {
    return null;
  }

  /**
   * Returns the Private key for the given alias.
   */
  public Key getPrivateKey(String alias) {

    if (pgpKeyMgr != null || smimeKeyMgr != null) {
      char[] password = getPasswordForAlias(alias, false);
    }
    if (pgpKeyMgr != null) {
      try {
	return mgr.getPrivateKey(alias, password);
      } catch (java.security.KeyStoreException kse) {

      }
    }

    EncryptionKeyManager mgr = getKeyManager();
    if (mgr != null) {
      try {
	char[] password = getPasswordForAlias(alias, false);
	return mgr.getPrivateKey(alias, password);
      } catch (java.security.KeyStoreException kse) {
      }
    }

    return null;
  }

  /**
   * Returns the password for this alias.
   */
  protected char[] getPasswordForAlias(String alias, boolean check) {
    char[] returnValue = (char[]) aliasPasswordMap.get(alias);
    if (returnValue == null || check) {
      returnValue = net.suberic.pooka.gui.crypto.CryptoKeySelector.showPassphraseDialog();
      if (returnValue != null) {
	aliasPasswordMap.put(alias, returnValue);
      }
    }

    return returnValue;
  }

  /**
   * Returns the Private key for the given alias.
   */
  public Key getPublicKey(String alias) {
    EncryptionKeyManager mgr = getKeyManager();
    if (mgr != null) {
      try {
	return mgr.getPublicKey(alias);
      } catch (java.security.KeyStoreException kse) {
      }
    }
    
    return null;
  }

  /**
   * Returns the public key(s) for the given email address.
   */
  public Key[] getPublicKeys(String address) {

    EncryptionKeyManager mgr = getKeyManager();
    if (mgr != null) {
      
    }

    return null;
  }

  /**
   * Encrypts to given message.  Actually checks all of the recipients
   * configured to see if we have a key for each one.
   */
  public MimeMessage encryptMessage(MimeMessage mMsg) {
    
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
    throws MessagingException  {
    if (key != null) {
      return EncryptionManager.getEncryptionUtils(mMsg).encryptMessage(Pooka.getDefaultSession(), mMsg, key);
    } else
      return mMsg;
  }

  /**
   * Signs the given message.
   */
  public MimeMessage signMessage(MimeMessage mMsg, UserProfile profile, Key key) 
    throws MessagingException, java.io.IOException  {
    if (key == null) {
      key = profile.getEncryptionKey();
    }

    if (key == null) {
      // get user input.
    }
    
    if (key != null)
      return EncryptionManager.getEncryptionUtils("PGP").signMessage(Pooka.getDefaultSession(), mMsg, key);
    else
      return mMsg;
  }

}
