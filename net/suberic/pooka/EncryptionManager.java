package net.suberic.pooka;

import net.suberic.pooka.crypto.*;

/**
 * The EncryptionManager manages Pooka's encryption facilities.
 */
public class EncryptionManager {

  EncryptionUtils defaultUtils = null;

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
}
