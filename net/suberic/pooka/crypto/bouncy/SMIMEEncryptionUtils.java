package net.suberic.pooka.crypto.bouncy;

import net.suberic.pooka.*;
import net.suberic.pooka.crypto.*;

import javax.mail.*;
import javax.mail.internet.*;
import java.io.*;

import org.bouncycastle.mail.smime.*;

/**
 * Utilities for encrypting/decrypting messages.
 */
public class SMIMEEncryptionUtils extends EncryptionUtils {

  /**
   * Encrypts a Message.
   */
  public  MimeMessage encryptMessage(Session s, MimeMessage msg, EncryptionKey key) 
    throws EncryptionException, MessagingException {
    MimeMessage encryptedMessage = new MimeMessage(s);

    java.util.Enumeration enum = msg.getAllHeaderLines();
    while (enum.hasMoreElements()) {
      encryptedMessage.addHeaderLine((String) enum.nextElement());
    }

    //encryptedMessage.setContent(msg.getContent(), msg.getContentType());
    //encryptedMessage.saveChanges();

    return null;
  }

  /**
   * Encrypts a BodyPart;
   */
  public  Multipart encryptPart(Part part, EncryptionKey key) 
    throws EncryptionException, MessagingException {

    try {
      SMIMEEnvelopedGenerator  gen = new SMIMEEnvelopedGenerator();
      
      BouncySMIMEEncryptionKey bKey = (BouncySMIMEEncryptionKey) key;
      
      gen.addKeyTransRecipient(bKey.getCertificate());
      
      MimeBodyPart mp = gen.generate((MimeBodyPart)part, SMIMEEnvelopedGenerator.RC2_CBC, "BC");
      
    } catch (java.security.NoSuchAlgorithmException nsae) {
      throw new EncryptionException(nsae);
    } catch (java.security.NoSuchProviderException nsae) {
      throw new EncryptionException(nsae);
    } catch (org.bouncycastle.mail.smime.SMIMEException sme) {
      throw new MessagingException(sme.getMessage());
    }
    return null;
  }

  /**
   * Decrypts a Message.
   */
  public  javax.mail.internet.MimeMessage decryptMessage(Session s, javax.mail.internet.MimeMessage msg, EncryptionKey key) 
    throws EncryptionException, MessagingException, IOException {
    return null;
  }

  /**
   * Decrypts a BodyPart.
   */
  public  BodyPart decryptBodyPart(BodyPart part, EncryptionKey key) 
    throws EncryptionException, MessagingException, IOException {
    return null;
  }

  /**
   * Decrypts a Multipart.
   */
  public  BodyPart decryptMultipart(Multipart mpart, EncryptionKey key) 
    throws EncryptionException, MessagingException, IOException {
    return null;
  }

  /**
   * Signs a Part.
   */
  public  BodyPart signBodyPart(BodyPart p, EncryptionKey key)
    throws EncryptionException, MessagingException, IOException {

    try {
      BouncySMIMEEncryptionKey bKey = (BouncySMIMEEncryptionKey) key;
      
      SMIMESignedGenerator gen = new SMIMESignedGenerator();
      
      gen.addSigner(bKey.getKeyPair().getPrivate(), bKey.getCertificate(), SMIMESignedGenerator.DIGEST_SHA1);
      
      MimeMultipart mm = gen.generate((MimeBodyPart) p, "BC");
      
      MimeBodyPart returnValue = new MimeBodyPart();
      returnValue.setContent(mm);
      return returnValue;
    } catch (java.security.NoSuchAlgorithmException nsae) {
      throw new EncryptionException(nsae);
    } catch (java.security.NoSuchProviderException nsae) {
      throw new EncryptionException(nsae);
    } catch (org.bouncycastle.mail.smime.SMIMEException sme) {
      throw new MessagingException(sme.getMessage());
    }
    
  }

  /**
   * Signs a Message.
   */
  public  MimeMessage signMessage(Session s, MimeMessage m, EncryptionKey key)
    throws EncryptionException, MessagingException, IOException {

    try {
      BouncySMIMEEncryptionKey bKey = (BouncySMIMEEncryptionKey) key;
      
      SMIMESignedGenerator gen = new SMIMESignedGenerator();
      
      gen.addSigner(bKey.getKeyPair().getPrivate(), bKey.getCertificate(), SMIMESignedGenerator.DIGEST_SHA1);
      
      MimeMultipart mm = gen.generate(m, "BC");
      
      MimeMessage signedMessage = new MimeMessage(s);
      
      java.util.Enumeration enum = m.getAllHeaderLines();
      while (enum.hasMoreElements()) {
	signedMessage.addHeaderLine((String) enum.nextElement());
      }

      signedMessage.setContent(mm);
      
      return signedMessage;
    } catch (java.security.NoSuchAlgorithmException nsae) {
      throw new EncryptionException(nsae);
    } catch (java.security.NoSuchProviderException nsae) {
      throw new EncryptionException(nsae);
    } catch (org.bouncycastle.mail.smime.SMIMEException sme) {
      throw new MessagingException(sme.getMessage());
    }

  }

  /**
   * Checks the signature on a Part.
   */
  public  boolean checkSignature(Part p, EncryptionKey key)
    throws EncryptionException, MessagingException, IOException {
    return false;
  }

  /**
   * Checks the signature on a Message.
   */
  public  boolean checkSignature(MimeMessage m, EncryptionKey key)
    throws EncryptionException, MessagingException, IOException {
    return false;
  }

  /**
   * Checks the signature on a Multipart.
   */
  public  boolean checkSignature(MimeMultipart m, EncryptionKey key)
    throws EncryptionException, MessagingException, IOException {
    return false;
  }

  /**
   * Creates an empty EncryptionKeyManager that's appropriate for this
   * Encryption provider.
   */
  public  EncryptionKeyManager createKeyManager() {
    return null;
  }

  /**
   * Creates and loads an EncryptionKeyManager that's appropriate for this
   * Encryption provider.
   */
  public  EncryptionKeyManager createKeyManager(java.io.InputStream inputStream, char[] passwd) throws java.io.IOException {
    return null;
  }

}
