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
    
    try {
      CryptixPGPEncryptionKey pgpKey = (CryptixPGPEncryptionKey) key;
      KeyBundle bundle = pgpKey.getKeyBundle();

      LiteralMessageBuilder lmb = 
	LiteralMessageBuilder.getInstance("OpenPGP");

      byte[] msg;

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      
      byte[] bytesRead = new byte[256];
      int numRead = rawStream.read(bytesRead);
      
      while (numRead > -1) {
	baos.write(bytesRead, 0, numRead);
	numRead = rawStream.read(bytesRead);
      }
      msg = baos.toByteArray();

      lmb.init(msg);
      
      LiteralMessage litmsg = (LiteralMessage)lmb.build();
      
      EncryptedMessageBuilder emb = 
	EncryptedMessageBuilder.getInstance("OpenPGP");
      emb.init(litmsg);
      emb.addRecipient(bundle);
      Message encryptedMessage = emb.build();

      PGPArmouredMessage armoured = new PGPArmouredMessage(encryptedMessage);

      return armoured.getEncoded();
      
    } catch (Exception e) {
      throw new EncryptionException(e);
    }
  }

   /**
   * Signs a section of text.
   */
  public byte[] sign(InputStream rawStream, EncryptionKey key)
    throws EncryptionException {
    
    try {
      CryptixPGPEncryptionKey pgpKey = (CryptixPGPEncryptionKey) key;
      KeyBundle bundle = pgpKey.getKeyBundle();
      char[] passphrase = pgpKey.getPassphrase();

      LiteralMessageBuilder lmb = 
	LiteralMessageBuilder.getInstance("OpenPGP");

      byte[] msg;

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      
      byte[] bytesRead = new byte[256];
      int numRead = rawStream.read(bytesRead);
      
      while (numRead > -1) {
	baos.write(bytesRead, 0, numRead);
	numRead = rawStream.read(bytesRead);
      }
      msg = baos.toByteArray();

      lmb.init(msg);
      
      LiteralMessage litMsg = (LiteralMessage)lmb.build();
      
      Message signature = null;

      SignedMessageBuilder smb = 
	SignedMessageBuilder.getInstance("OpenPGP");

      smb.init(litMsg);
      smb.addSigner(bundle, passphrase);

      PGPSignedMessage signedMessage = (PGPSignedMessage)smb.build();
      signature = signedMessage.getDetachedSignature();
      return signature.getEncoded();

    } catch (Exception e) {
      throw new EncryptionException(e);
    }      
  }
 
  /**
   * Checks a signature against a section of text.
   */
  public boolean checkSignature(InputStream rawStream,
				byte[] signature, EncryptionKey key)
    throws EncryptionException {

    try {
      CryptixPGPEncryptionKey pgpKey = (CryptixPGPEncryptionKey) key;
      KeyBundle bundle = pgpKey.getKeyBundle();

      byte[] msg;

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      
      byte[] bytesRead = new byte[256];
      int numRead = rawStream.read(bytesRead);
      
      while (numRead > -1) {
	baos.write(bytesRead, 0, numRead);
	numRead = rawStream.read(bytesRead);
      }
      msg = baos.toByteArray();

      LiteralMessageBuilder lmb = 
	LiteralMessageBuilder.getInstance("OpenPGP");
      lmb.init(msg);

      Message signedContentMessage = lmb.build();

      MessageFactory mf = MessageFactory.getInstance("OpenPGP");
      ByteArrayInputStream bais = new ByteArrayInputStream(signature);
      PGPDetachedSignatureMessage signatureMessage = (PGPDetachedSignatureMessage) mf.generateMessage(bais);
      
      return signatureMessage.verify(signedContentMessage, bundle);
    } catch (Exception e) {
      throw new EncryptionException(e);
    }
  }
 
  /**
   * Returns a KeyStore provider.
   */
  public EncryptionKeyManager createKeyManager() {
    return new CryptixKeyManager();
  }
 
  /**
   * Returns a KeyStore provider.
   */
  public EncryptionKeyManager createKeyManager(java.io.InputStream inputStream, char[] password) throws IOException {
    EncryptionKeyManager keyMgr = new CryptixKeyManager();
    keyMgr.load(inputStream, password);
    return keyMgr;
  }

}
