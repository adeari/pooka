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

  /**
   * Signs a section of text.
   */
  public byte[] sign(InputStream rawStream, EncryptionKey key)
    throws EncryptionException;

  /**
   * Checks a signature against a section of text.
   */
  public boolean checkSignature(InputStream rawStream, 
					 byte[] signature, EncryptionKey key)
    throws EncryptionException;
  
  /**
   * Extracts public key information.
   */
  public EncryptionKey[] extractKeys(InputStream rawStream);

  /** 
   * Packages up the public keys in a form to be sent as a public key message.
   */
  public byte[] packageKeys(EncryptionKey[] keys);
  
  /**
   * Returns a KeyStore provider.
   */
  public abstract EncryptionKeyManager createKeyManager() throws EncryptionException;
  
  /**
   * Returns a KeyStore provider.
   */
  public abstract EncryptionKeyManager createKeyManager(java.io.InputStream inputStream, char[] password) throws IOException, EncryptionException;
}
