package net.suberic.pooka.crypto;

import net.suberic.pooka.*;
import javax.mail.*;
import java.io.IOException;

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

  /**
   * Encrypts a Message.
   */
  public abstract Message encryptMessage(Message msg, EncryptionKey key) 
    throws EncryptionException, MessagingException;

  /**
   * Decrypts a Message.
   */
  public abstract Message decryptMessage(Message msg, EncryptionKey key) 
    throws EncryptionException, MessagingException, IOException;

  /**
   * Encrypts a BodyPart;
   */
  public abstract BodyPart encryptBodyPart(BodyPart part, EncryptionKey key) 
    throws EncryptionException, MessagingException;

  /**
   * Decrypts a BodyPart.
   */
  public abstract BodyPart decryptBodyPart(BodyPart part, EncryptionKey key) 
    throws EncryptionException, MessagingException, IOException;

  
}
