package net.suberic.pooka.crypto.bouncy;

import net.suberic.pooka.*;
import net.suberic.pooka.crypto.*;

import java.io.*;
import java.security.*;
import java.security.cert.*;
import java.util.*;

import javax.mail.*;
import javax.mail.internet.*;

import org.bouncycastle.mail.smime.*;
import org.bouncycastle.cms.*;

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

    try {
      SMIMEEnvelopedGenerator  gen = new SMIMEEnvelopedGenerator();
      
      BouncySMIMEEncryptionKey bKey = (BouncySMIMEEncryptionKey) key;
      
      gen.addKeyTransRecipient(bKey.getCertificate());
      
      MimeBodyPart mp = gen.generate(msg, SMIMEEnvelopedGenerator.RC2_CBC, "BC");
      
      encryptedMessage.setContent(mp, mp.getContentType());
      encryptedMessage.saveChanges();

      return encryptedMessage;
    } catch (java.security.NoSuchAlgorithmException nsae) {
      throw new EncryptionException(nsae);
    } catch (java.security.NoSuchProviderException nsae) {
      throw new EncryptionException(nsae);
    } catch (org.bouncycastle.mail.smime.SMIMEException sme) {
      throw new MessagingException(sme.getMessage());
    }
    
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
    try {
      BouncySMIMEEncryptionKey bKey = (BouncySMIMEEncryptionKey) key;

      RecipientId     recId = new RecipientId();
      X509Certificate cert = bKey.getCertificate();

      Key privateKey = bKey.getKeyPair().getPrivate();

      recId.setSerialNumber(cert.getSerialNumber());
      recId.setIssuer(cert.getIssuerX500Principal().getEncoded());

      SMIMEEnveloped       m = new SMIMEEnveloped(msg);
      RecipientInformationStore   recipients = m.getRecipientInfos();
      RecipientInformation        recipient = recipients.get(recId);

      MimeBodyPart mbp = SMIMEUtil.toMimeBodyPart(recipient.getContent(privateKey, "BC"));

      MimeMessage decryptedMessage = new MimeMessage(s);
      
      java.util.Enumeration enum = msg.getAllHeaderLines();
      while (enum.hasMoreElements()) {
	decryptedMessage.addHeaderLine((String) enum.nextElement());
      }

      decryptedMessage.setContent(mbp, mbp.getContentType());
      
      return decryptedMessage;

    } catch (CMSException cmse) {
      throw new EncryptionException(cmse);
    } catch (java.security.NoSuchProviderException nsae) {
      throw new EncryptionException(nsae);
    } catch (org.bouncycastle.mail.smime.SMIMEException sme) {
      throw new MessagingException(sme.getMessage());
    }
  }

  /**
   * Decrypts a BodyPart.
   */
  public  BodyPart decryptBodyPart(BodyPart part, EncryptionKey key) 
    throws EncryptionException, MessagingException, IOException {
    try {
      BouncySMIMEEncryptionKey bKey = (BouncySMIMEEncryptionKey) key;

      RecipientId     recId = new RecipientId();
      X509Certificate cert = bKey.getCertificate();

      Key privateKey = bKey.getKeyPair().getPrivate();

      recId.setSerialNumber(cert.getSerialNumber());
      recId.setIssuer(cert.getIssuerX500Principal().getEncoded());

      SMIMEEnveloped       m = new SMIMEEnveloped((MimeBodyPart) part);
      RecipientInformationStore   recipients = m.getRecipientInfos();
      RecipientInformation        recipient = recipients.get(recId);

      MimeBodyPart mbp = SMIMEUtil.toMimeBodyPart(recipient.getContent(privateKey, "BC"));

      return mbp;
    } catch (CMSException cmse) {
      throw new EncryptionException(cmse);
    } catch (java.security.NoSuchProviderException nsae) {
      throw new EncryptionException(nsae);
    } catch (org.bouncycastle.mail.smime.SMIMEException sme) {
      throw new MessagingException(sme.getMessage());
    }

  }

  /**
   * Decrypts a Multipart.
   */
  public  BodyPart decryptMultipart(Multipart mpart, EncryptionKey key) 
    throws EncryptionException, MessagingException, IOException {

    MimeBodyPart mbp = new MimeBodyPart();
    mbp.setContent(mpart);
    return decryptBodyPart(mbp,key);
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

    try {
      SMIMESigned s = new SMIMESigned(p);
      
      return checkSignature(s);
    } catch (CMSException cmse) {
      throw new EncryptionException(cmse);
    } catch (org.bouncycastle.mail.smime.SMIMEException sme) {
      throw new MessagingException(sme.getMessage());
    }
  }

  /**
   * Checks the signature on a Message.
   */
  public  boolean checkSignature(MimeMessage m, EncryptionKey key)
    throws EncryptionException, MessagingException, IOException {
    try {
      if (m.isMimeType("multipart/signed")) {
	SMIMESigned s = new SMIMESigned((MimeMultipart)m.getContent());
	return checkSignature(s);
      } else if (m.isMimeType("application/pkcs7-mime")) {
	SMIMESigned s = new SMIMESigned(m);
	return checkSignature(s);
      } else {
	throw new EncryptionException("incorrect mime type -- not SMIME Signed?");
      }
    } catch (CMSException cmse) {
      throw new EncryptionException(cmse);
    } catch (org.bouncycastle.mail.smime.SMIMEException sme) {
      throw new MessagingException(sme.getMessage());
    }

  }

  /**
   * Checks the signature on a Multipart.
   */
  public  boolean checkSignature(MimeMultipart m, EncryptionKey key)
    throws EncryptionException, MessagingException, IOException {
    try {
      SMIMESigned s = new SMIMESigned(m);
      return checkSignature(s);
    } catch (CMSException cmse) {
      throw new EncryptionException(cmse);
    }
  }

  /**
   * Checks a SMIMESigned to make sure that the signature matches.
   */
  private boolean checkSignature(SMIMESigned s) 
    throws EncryptionException, MessagingException, IOException {
    try {
      boolean returnValue = true;
      CertStore certs = s.getCertificatesAndCRLs("Collection", "BC");
      
      SignerInformationStore signers = s.getSignerInfos();
      
      Collection c = signers.getSigners();
      Iterator it = c.iterator();
      
      while (returnValue == true && it.hasNext()) {
	SignerInformation signer = (SignerInformation)it.next();
	Collection certCollection = certs.getCertificates(signer.getSID());
	
	Iterator certIt = certCollection.iterator();
	X509Certificate cert = (X509Certificate)certIt.next();
	
	if (! signer.verify(cert, "BC")) {
	  returnValue = false;
	}

      }
    
      return returnValue;
    } catch (java.security.NoSuchAlgorithmException nsae) {
      throw new EncryptionException(nsae);
    } catch (java.security.NoSuchProviderException nsae) {
      throw new EncryptionException(nsae);
    } catch (java.security.cert.CertificateException nsae) {
      throw new EncryptionException(nsae);
    } catch (java.security.cert.CertStoreException nsae) {
      throw new EncryptionException(nsae);
    } catch (CMSException cmse) {
      throw new EncryptionException(cmse);
    } 
  }


  /**
   * Creates an empty EncryptionKeyManager that's appropriate for this
   * Encryption provider.
   */
  public  EncryptionKeyManager createKeyManager() throws EncryptionException {
    return new BouncySMIMEEncryptionKeyManager();
  }

  /**
   * Creates and loads an EncryptionKeyManager that's appropriate for this
   * Encryption provider.
   */
  public  EncryptionKeyManager createKeyManager(java.io.InputStream inputStream, char[] passwd) throws java.io.IOException, EncryptionException {
    BouncySMIMEEncryptionKeyManager mgr = new BouncySMIMEEncryptionKeyManager();
    mgr.load(inputStream, passwd);
    return mgr;
  }

}
