package net.suberic.pooka.crypto;

import net.suberic.pooka.*;

import java.io.*;

/**
 * Something which decrypts PGP streams.
 */
public interface PGPProviderImpl {

  /**
   * Decrypts a section of text using an EncryptionKey.
   */
  public byte[] decrypt(java.io.InputStream encryptedStream, EncryptionKey key)
    throws EncryptionException;
  
  /**
   * Encrypts a section of text using an EncryptionKey.
   */
  public byte[] encrypt(java.io.InputStream rawStream, EncryptionKey key)
    throws EncryptionException;

}
