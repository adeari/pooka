package net.suberic.pooka.crypto;

import net.suberic.pooka.*;

import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;

import java.io.*;

/**
 * Utilities for encrypting/decrypting messages.
 *
 * This class just handles the parsing of the PGP/MIME message itself.
 * Actual PGP encoding/decoding is left to the PGPProviderImpl class itself.
 */
public class PGPMimeEncryptionUtils extends EncryptionUtils {

  PGPProviderImpl pgpImpl = null;
  /**
   * Returns the PGPProviderImpl.
   */
  public PGPProviderImpl getPGPProviderImpl() {
    return pgpImpl;
  }
  /**
   * Sets the PGPProviderImpl.
   */
  public void setPGPProviderImpl(PGPProviderImpl newPgpImpl) {
    pgpImpl = newPgpImpl;
  }

  /**
   * Decrypts a section of text using an EncryptionKey.
   */
  public byte[] decrypt(java.io.InputStream encryptedStream, EncryptionKey key)
    throws EncryptionException {
    return pgpImpl.decrypt(encryptedStream, key);
  }

  /**
   * Encrypts a section of text using an EncryptionKey.
   */
  public byte[] encrypt(java.io.InputStream rawStream, EncryptionKey key)
    throws EncryptionException {

    return pgpImpl.encrypt(rawStream, key);
  }

  /**
   * Signs a section of text.
   */
  public byte[] sign(InputStream rawStream, EncryptionKey key)
    throws EncryptionException {
    return pgpImpl.sign(rawStream, key);

  }

  /**
   * Checks a signature against a section of text.
   */
  public boolean checkSignature(InputStream rawStream, 
					 byte[] signature, EncryptionKey key)
    throws EncryptionException {
    return pgpImpl.checkSignature(rawStream, signature, key);
  }

  /**
   * Encrypts a Message.
   */
  public MimeMessage encryptMessage(Session s, MimeMessage msg, EncryptionKey key) 
    throws EncryptionException, MessagingException {
    MimeMessage encryptedMessage = new MimeMessage(s);

    java.util.Enumeration enum = msg.getAllHeaderLines();
    while (enum.hasMoreElements()) {
      encryptedMessage.addHeaderLine((String) enum.nextElement());
    }

    Multipart mp = encryptPart(msg, key);

    encryptedMessage.setContent(mp);
    //ContentType cType = new ContentType("multipart/encrypted");
    //cType.setParameter("protocol", "application/pgp-encrypted");
    //encryptedMessage.setContentType(cType.toString());

    return encryptedMessage;
  }

  /**
   * Decrypts a Message.
   */
  public javax.mail.internet.MimeMessage decryptMessage(Session s, javax.mail.internet.MimeMessage msg, EncryptionKey key) 
    throws EncryptionException, MessagingException, java.io.IOException {
    Object o = msg.getContent();
    if (o instanceof Multipart) {
      BodyPart decryptedPart = decryptMultipart((Multipart) o, key);
      MimeMessage m = new MimeMessage(s);
      java.util.Enumeration enum = msg.getAllHeaderLines();
      while (enum.hasMoreElements()) {
	m.addHeaderLine((String) enum.nextElement());
      }
      m.setDataHandler(decryptedPart.getDataHandler());
      return m;
    }

    return null;
  }

  /**
   * Encrypts a BodyPart;
   */
  public Multipart encryptPart(Part part, EncryptionKey key) 
    throws EncryptionException, MessagingException {

    try {
      MimeMultipart mm = new MultipartEncrypted("encrypted");
      
      MimeBodyPart idPart = new MimeBodyPart();
      DataSource ds = new ByteArrayDataSource(new String("Version: 1").getBytes(), "pgpapp", "application/pgp-encrypted");
      idPart.setDataHandler(new javax.activation.DataHandler(ds));

      //idPart.setContent("Version: 1", "application/pgp-encrypted");
      
      MimeBodyPart encryptedContent = new MimeBodyPart();
      byte[] encryptedBytes = encrypt(part.getInputStream(), key);
      ByteArrayDataSource dataSource = new ByteArrayDataSource(encryptedBytes, "message", "application/octet-stream");
      
      javax.activation.DataHandler dh = new javax.activation.DataHandler(dataSource);
      
      encryptedContent.setFileName("message");
      encryptedContent.setDataHandler(dh);

      mm.addBodyPart(idPart);
      mm.addBodyPart(encryptedContent);
      
      return mm;
    
    } catch (IOException ioe) {
      throw new MessagingException(ioe.toString());
    }
  }

  /**
   * Decrypts a BodyPart.
   */
  public BodyPart decryptBodyPart(BodyPart part, EncryptionKey key) 
    throws EncryptionException, MessagingException, java.io.IOException {
    // check the type; should be multipart/encrypted

    String contentType = part.getContentType();
    ContentType ct = new ContentType(contentType);
    if (contentType == null || ! ct.getBaseType().equalsIgnoreCase("multipart/encrypted")) {
      throw new EncryptionException ("error in content type:  expected 'multipart/encrypted', got '" + contentType + "'");
    }
    // FIXME:  check for protocol, too.

    // ok, our content type is ok.  now we should have a multipart here with
    // two entries:  the first should be of type application/pgp-encrypted 
    // with the content Version: 1
    Object content = part.getContent();

    Multipart mpart = (Multipart) content;
    return decryptMultipart(mpart, key);
  }

 /**
   * Decrypts a Multipart.
   */
  public BodyPart decryptMultipart(Multipart mpart, EncryptionKey key) 
    throws EncryptionException, MessagingException, java.io.IOException {

    // should have two internal parts.
    if (mpart.getCount() != 2) {
      throw new EncryptionException ("error in content:  expected 2 parts, got " + mpart.getCount()); 
    }

    // first part should be application/pgp-encrypted
    BodyPart firstPart = mpart.getBodyPart(0);
    String firstPartType = firstPart.getContentType();
    ContentType firstCt = new ContentType(firstPartType);
    if (firstPartType == null || ! firstCt.getBaseType().equalsIgnoreCase("application/pgp-encrypted")) {
      throw new EncryptionException ("error in content:  expected first part of type application/pgp-encrypted, got " + firstPartType);
    }

    // don't bother checking the version for now.

    BodyPart secondPart = mpart.getBodyPart(1);
    String secondPartType = secondPart.getContentType();
    ContentType secondCt = new ContentType(secondPartType);
    if (secondPartType == null || ! secondCt.getBaseType().equalsIgnoreCase("application/octet-stream")) {
      throw new EncryptionException ("error in content:  expected second part of type application/octet-stream, got " + secondPartType);
    }

    String fileName = secondPart.getFileName();
    java.io.InputStream is = secondPart.getInputStream();

    byte[] value = decrypt(is, key);

    ByteArrayInputStream bais = new ByteArrayInputStream(value);
    MimeBodyPart returnValue = new MimeBodyPart(bais);
    
    return returnValue;
  }

  /**
   * Signs a Part.
   */
  public BodyPart signBodyPart(BodyPart p, EncryptionKey key)
    throws EncryptionException, MessagingException, IOException {
    MimeMultipart mpart = new MimeMultipart("signed");

    InputStream is = p.getInputStream();

    byte[] signature = sign(is, key);
    MimeBodyPart sigPart = new MimeBodyPart();

    ByteArrayDataSource dataSource = new ByteArrayDataSource(signature, "signature", "application/pgp-signature");
    
    javax.activation.DataHandler dh = new javax.activation.DataHandler(dataSource);
      
    sigPart.setDataHandler(dh);

    mpart.addBodyPart(p);
    mpart.addBodyPart(sigPart);
    
    MimeBodyPart returnValue = new MimeBodyPart();
    returnValue.setContent(mpart);
    return returnValue;
  }

  /**
   * Checks the signature on a Part.
   */
  public boolean checkSignature(Part p, EncryptionKey key) 
    throws EncryptionException, MessagingException, IOException {
    return false;
  }

  /**
   * Signs a Message.
   */
  public MimeMessage signMessage(Session s, MimeMessage msg, EncryptionKey key)
    throws EncryptionException, MessagingException, IOException {

    MimeMessage signedMessage = new MimeMessage(s);

    java.util.Enumeration enum = msg.getAllHeaderLines();
    while (enum.hasMoreElements()) {
      signedMessage.addHeaderLine((String) enum.nextElement());
    }

    InputStream is = msg.getInputStream();
    MimeBodyPart mbp = new MimeBodyPart(is);
    
    BodyPart signedPart = signBodyPart(mbp, key);

    signedMessage.setContent(signedPart, signedPart.getContentType());

    return signedMessage;
  }

  /**
   * Checks the signature on a Message.
   */
  public boolean checkSignature(MimeMessage msg, EncryptionKey key) {
    return false;
  }

}
