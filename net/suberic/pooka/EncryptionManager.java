package net.suberic.pooka;

import net.suberic.pooka.crypto.*;
import javax.mail.internet.MimeMessage;
import javax.mail.MessagingException;

/**
 * The EncryptionManager manages Pooka's encryption facilities.
 */
public class EncryptionManager {

  EncryptionUtils defaultUtils = null;

  EncryptionUtils pgpUtils = null;
  
  EncryptionUtils smimeUtils = null;

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
   * Returns the given EncryptionKey, or null if no such key exists.
   */
  public EncryptionKey getEncryptionKey(String keyId) {
    return null;
  }

  /**
   * Encrypts to given message.  Actually checks all of the recipients
   * configured to see if we have a key for each one.
   */
  public MimeMessage encryptMessage(MimeMessage mMsg) {
    return null;
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
