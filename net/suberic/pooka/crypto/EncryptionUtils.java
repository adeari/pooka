package net.suberic.pooka.crypto;

import net.suberic.pooka.*;

/**
 * Utilities for encrypting/decrypting messages.
 */
public abstract class EncryptionUtils {

  /**
   * Decrypts a section of text using an EncryptionKey.
   */
  public abstract String decryptText(String encryptedText, EncryptionKey key);

  /**
   * Encrypts a section of text using an EncryptionKey.
   */
  public abstract String encryptText(String plainText, EncryptionKey key);

}
