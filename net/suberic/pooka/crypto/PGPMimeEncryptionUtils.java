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
  public byte[] decrypt(java.io.InputStream encryptedStream, EncryptionKey key)
    throws EncryptionException {
    try {
      PGPEncryptionKey pgpKey = (PGPEncryptionKey) key;
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
  public byte[] encryptText(String plainText, EncryptionKey key) 
    throws EncryptionException {
    return null;
  }

  /**
   * Encrypts a Message.
   */
  public javax.mail.Message encryptMessage(javax.mail.Message msg, EncryptionKey key) 
    throws EncryptionException, MessagingException {
    return null;
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
    java.io.InputStream is = secondPart.getInputStream();
    /*
      java.io.InputStreamReader reader = new java.io.InputStreamReader(is);
    
      
      char[] buf = new char[256];
      for (int i = 0; i > -1; i = reader.read(buf, 0, 256)) {
      System.out.print(new String(buf));
      }
    */
    byte[] value = decrypt(is, key);
    ByteArrayDataSource dataSource = new ByteArrayDataSource(value, fileName, "text/plain");

    javax.activation.DataHandler dh = new javax.activation.DataHandler(dataSource);

    BodyPart returnValue = new MimeBodyPart();
    returnValue.setFileName(fileName);
    returnValue.setDataHandler(dh);
    return returnValue;
  }

}
