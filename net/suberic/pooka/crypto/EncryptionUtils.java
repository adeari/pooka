package net.suberic.pooka.crypto;

import net.suberic.pooka.*;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.*;

/**
 * Utilities for encrypting/decrypting messages.
 */
public abstract class EncryptionUtils {

  /**
   * Encrypts a Message.
   */
  public abstract MimeMessage encryptMessage(Session s, MimeMessage msg, EncryptionKey key) 
    throws EncryptionException, MessagingException;

  /**
   * Decrypts a Message.
   */
  public abstract javax.mail.internet.MimeMessage decryptMessage(Session s, javax.mail.internet.MimeMessage msg, EncryptionKey key) 
    throws EncryptionException, MessagingException, IOException;

  /**
   * Encrypts a BodyPart;
   */
  public abstract Multipart encryptPart(Part part, EncryptionKey key) 
    throws EncryptionException, MessagingException;

  /**
   * Decrypts a BodyPart.
   */
  public abstract BodyPart decryptBodyPart(BodyPart part, EncryptionKey key) 
    throws EncryptionException, MessagingException, IOException;

  /**
   * Decrypts a Multipart.
   */
  public abstract BodyPart decryptMultipart(Multipart mpart, EncryptionKey key) 
    throws EncryptionException, MessagingException, IOException;

  /**
   * Signs a Part.
   */
  public abstract BodyPart signBodyPart(BodyPart p, EncryptionKey key)
    throws EncryptionException, MessagingException, IOException;

  /**
   * Checks the signature on a Part.
   */
  public abstract boolean checkSignature(Part p, EncryptionKey key)
    throws EncryptionException, MessagingException, IOException;

  /**
   * Signs a Message.
   */
  public abstract MimeMessage signMessage(Session s, MimeMessage m, EncryptionKey key)
    throws EncryptionException, MessagingException, IOException;

  /**
   * Checks the signature on a Message.
   */
  public abstract boolean checkSignature(MimeMessage m, EncryptionKey key)
    throws EncryptionException, MessagingException, IOException;

  /**
   * Checks the signature on a Multipart.
   */
  public abstract boolean checkSignature(MimeMultipart m, EncryptionKey key)
    throws EncryptionException, MessagingException, IOException;

  /**
   * Creates an empty EncryptionKeyManager that's appropriate for this
   * Encryption provider.
   */
  public abstract EncryptionKeyManager createKeyManager();

  /**
   * Creates and loads an EncryptionKeyManager that's appropriate for this
   * Encryption provider.
   */
  public abstract EncryptionKeyManager createKeyManager(java.io.InputStream inputStream, char[] passwd) throws java.io.IOException;

  /**
   * Returns whether or not this email is encrypted.
   */
  public static boolean isEncrypted(Part pPart) throws MessagingException {
    String contentType = pPart.getContentType().toLowerCase();
    
    if (contentType.startsWith("multipart")) {
      ContentType ct = new ContentType(contentType);
      if (ct.getSubType().equalsIgnoreCase("encrypted")) 
	return true;
    }
    return false;
  }

}
