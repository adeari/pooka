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
   * Signs a section of text.
   */
  public abstract byte[] sign(InputStream rawStream, EncryptionKey key)
    throws EncryptionException;

  /**
   * Checks a signature against a section of text.
   */
  public abstract boolean checkSignature(InputStream rawStream, 
					 byte[] signature, EncryptionKey key)
    throws EncryptionException;


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

  public static boolean isEncrypted(Part pPart) throws MessagingException {
    String contentType = pPart.getContentType().toLowerCase();
    
    System.err.println("contentType = " + contentType);
    if (contentType.startsWith("multipart")) {
      ContentType ct = new ContentType(contentType);
      if (ct.getSubType().equalsIgnoreCase("encrypted")) 
	return true;
      else if (ct.getSubType().equalsIgnoreCase("signed")) 
	return true;
    }
    System.err.println("returning false.");
    return false;
  }
}
