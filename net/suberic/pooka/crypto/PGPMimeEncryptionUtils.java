package net.suberic.pooka.crypto;

import net.suberic.pooka.*;

import javax.mail.*;
import javax.mail.internet.*;

import cryptix.message.*;
import cryptix.openpgp.*;
import cryptix.pki.*;

/**
 * Utilities for encrypting/decrypting messages.
 */
public class PGPMimeEncryptionUtils extends EncryptionUtils {

  /**
   * Decrypts a section of text using an EncryptionKey.
   */
  public byte[] decryptText(String encryptedText, EncryptionKey key)
    throws EncryptionException {
    try {
      PGPEncryptionKey pgpKey = (PGPEncryptionKey) key;
      KeyBundle bundle = pgpKey.getKeyBundle();
      char[] passphrase = pgpKey.getPassphrase();
      
      EncryptedMessage cryptMsg = new EncryptedMessage(encryptedText);
      
      Message decryptedMessage = cryptMsg.decrypt(bundle, passphrase);
      
      if (decryptedMessage instanceof LiteralMessage) {
	LiteralMessage litMsg = (LiteralMessage) decryptedMessage;
	byte[] returnValue = litMsg.getBinaryData();
	return returnValue;
      }

      return null;
    } catch (NotEncryptedToParameterException netpe) {
      throw new EncryptionException(netpe.getMessage());
    } catch (MessageException netpe) {
      throw new MessageException(netpe.getMessage());
    }

  }

  /**
   * Encrypts a section of text using an EncryptionKey.
   */
  public byte[] encryptText(String plainText, EncryptionKey key) 
    throws EncryptionException {
    return null;
  }

  /**
   * Encrypts a Message.
   */
  public Message encryptMessage(Message msg, EncryptionKey key) 
    throws EncryptionException, MessagingException {
    return null;
  }

  /**
   * Decrypts a Message.
   */
  public Message decryptMessage(Message msg, EncryptionKey key) 
    throws EncryptionException, MessagingException, java.io.IOException {
    return null;
  }

  /**
   * Encrypts a BodyPart;
   */
  public BodyPart encryptBodyPart(BodyPart part, EncryptionKey key) 
    throws EncryptionException, MessagingException {
    return null;
  }

  /**
   * Decrypts a BodyPart.
   */
  public BodyPart decryptBodyPart(BodyPart part, EncryptionKey key) 
    throws EncryptionException, MessagingException, java.io.IOException {
    // check the type; should be multipart/encrypted

    String contentType = part.getContentType();
    if (contentType == null || ! contentType.equals("multipart/encrypted")) {
      throw new EncryptionException ("error in content type:  expected 'multipart/encrypted', got '" + contentType + "'");
    }
    // FIXME:  check for protocol, too.

    // ok, our content type is ok.  now we should have a multipart here with
    // two entries:  the first should be of type application/pgp-encrypted 
    // with the content Version: 1

    Object content = part.getContent();
    if (! (content instanceof Multipart)) {
      throw new EncryptionException ("error in content:  expected javax.mail.Multipart, got " + content.getClass());
    }

    Multipart mpart = (Multipart) content;

    // should have two internal parts.
    if (mpart.getCount() != 2) {
      throw new EncryptionException ("error in content:  expected 2 parts, got " + mpart.getCount()); 
    }

    // first part should be application/pgp-encrypted
    BodyPart firstPart = mpart.getBodyPart(0);
    String firstPartType = firstPart.getContentType();
    if (firstPartType == null || ! firstPartType.equals("application/pgp-encrypted")) {
      throw new EncryptionException ("error in content:  expected first part of type application/pgp-encrypted, got " + firstPartType);
    }

    // don't bother checking the version for now.

    BodyPart secondPart = mpart.getBodyPart(1);
    String secondPartType = secondPart.getContentType();
    if (secondPartType == null || ! secondPartType.equals("application/octet-stream")) {
      throw new EncryptionException ("error in content:  expected second part of type application/octet-stream, got " + secondPartType);
    }

    String fileName = secondPart.getFileName();
    String pgpMsg = (String) secondPart.getContent();
    String value = decryptText(pgpMsg, key);
    BodyPart returnValue = new MimeBodyPart();
    returnValue.setFileName(fileName);
    returnValue.setContent(value, "text/plain");
    return returnValue;
  }

}
