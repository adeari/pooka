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
   * Decrypts a section of text using an EncryptionKey.
   */
  public abstract byte[] decrypt(InputStream encryptedStream, EncryptionKey key)
    throws EncryptionException;

  /**
   * Encrypts a section of text using an EncryptionKey.
   */
  public abstract byte[] encrypt(InputStream rawStream, EncryptionKey key)
    throws EncryptionException;

  /**
   * Encrypts a Message.
   */
  public abstract Message encryptMessage(Session s, Message msg, EncryptionKey key) 
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
