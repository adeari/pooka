package net.suberic.pooka;

import net.suberic.pooka.crypto.*;
import javax.mail.internet.MimeMessage;

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
   * Encrypts the given message if we think we should.
   */
  public MimeMessage encryptMessage(MimeMessage mMsg) {
    return mMsg;
  }

  /**
   * Signs the given message if we think we should.
   */
  public MimeMessage signMessage(MimeMessage mMsg, UserProfile profile) {
    return mMsg;
  }

}
