package net.suberic.pooka.crypto.cryptix;

import java.io.*;

import net.suberic.pooka.*;
import net.suberic.pooka.crypto.*;

import cryptix.message.*;
import cryptix.openpgp.*;
import cryptix.pki.*;


/**
 * Something which decrypts PGP streams.
 */
public class CryptixPGPProviderImpl implements PGPProviderImpl {

  /**
   * Decrypts a section of text using an EncryptionKey.
   */
  public byte[] decrypt(java.io.InputStream encryptedStream, EncryptionKey key)
    throws EncryptionException {
    
    try {
      CryptixPGPEncryptionKey pgpKey = (CryptixPGPEncryptionKey) key;
      KeyBundle bundle = pgpKey.getKeyBundle();
      char[] passphrase = pgpKey.getPassphrase();
      
      MessageFactory mf = MessageFactory.getInstance("OpenPGP");
      java.util.Collection col = mf.generateMessages(encryptedStream);
      if (col.isEmpty()) {
	throw new EncryptionException("no Messages in Input Stream.");
      }
      
      java.util.Iterator iter = col.iterator();
      
      cryptix.message.Message msg = (cryptix.message.Message) iter.next();
      
      EncryptedMessage cryptMsg = (EncryptedMessage) msg;
      
      cryptix.message.Message decryptedMessage = cryptMsg.decrypt(bundle, passphrase);
      
      if (decryptedMessage instanceof LiteralMessage) {
	LiteralMessage litMsg = (LiteralMessage) decryptedMessage;
	byte[] returnValue = litMsg.getBinaryData();
	return returnValue;
      }
      
      return null;
    } catch (Exception e) {
      e.printStackTrace();
      throw new EncryptionException(e.getMessage());
    }
    
    
  }
  
  /**
   * Encrypts a section of text using an EncryptionKey.
   */
  public byte[] encrypt(java.io.InputStream rawStream, EncryptionKey key)
    throws EncryptionException {
    return null;
  }

   /**
   * Signs a section of text.
   */
  public byte[] sign(InputStream rawStream, EncryptionKey key)
    throws EncryptionException {
    return null;
  }
 
  /**
   * Checks a signature against a section of text.
   */
  public boolean checkSignature(InputStream rawStream,
				byte[] signature, EncryptionKey key)
    throws EncryptionException {
    return false;
  }
 
  /**
   * Returns a KeyStore provider.
   */
  public EncryptionKeyManager createKeyManager() {
    return null;
  }
 
  /**
   * Returns a KeyStore provider.
   */
  public EncryptionKeyManager createKeyManager(java.io.InputStream inputStream, char[] password) throws IOException {
    return null;
  }

}
