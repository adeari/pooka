package net.suberic.pooka.gui.crypto;

import net.suberic.pooka.crypto.*;

/**
 * This defines a system that gets input for encryption/decryption.
 */
public interface CryptoUI {

  /**
   * Selects a public key.
   */
  public EncryptionKey selectPublicKey();

  /**
   * Selects a private key.
   */
  public EncryptionKey selectPrivateKey();

  /**
   * Reads a passphrase into the supplied buffer.
   */
  public char[] selectPassphrase(String alias);

}
