package net.suberic.pooka.crypto;

import net.suberic.pooka.*;
import net.suberic.crypto.*;

import javax.mail.internet.*;
import javax.mail.*;
import javax.activation.DataHandler;

import java.security.Key;

import java.io.*;

/**
 * An encrypted attachment.
 */
public class CryptoAttachment extends Attachment {

  boolean parsed = false;

  boolean encrypted = false;

  boolean signed = false;

  BodyPart decryptedBodyPart = null;
  
  DataHandler msgDataHandler = null;

  /**
   * Creates a CryptoAttachment out of a MimePart.
   */
  public CryptoAttachment(MimePart mp) throws MessagingException {
    super(mp);
    ContentType ct = new ContentType(mp.getContentType());
    if (ct.getSubType().equalsIgnoreCase("encrypted"))
      encrypted = true;
    else if (ct.getSubType().equalsIgnoreCase("signed"))
      signed = true;
    else if (ct.getPrimaryType().equalsIgnoreCase("application") && ct.getSubType().equalsIgnoreCase("pkcs7-mime")) {
      encrypted = true;
    }
  }

  /**
   * Tries to decrypt this Attachment.
   */
  public BodyPart decryptAttachment(EncryptionUtils utils, Key key)
    throws MessagingException, java.io.IOException, java.security.GeneralSecurityException {
    
    if (decryptedBodyPart != null)
      return decryptedBodyPart;
    else {
      MimeBodyPart mbp = new MimeBodyPart();
      mbp.setDataHandler(super.getDataHandler());
      mbp.setHeader("Content-Type", super.getDataHandler().getContentType());

      decryptedBodyPart = utils.decryptBodyPart(mbp, key);

      return decryptedBodyPart;
    }
    
  }

  // accessor methods.
  
  /**
   * Returns the decrypted version of the wrapped attachment, or null 
   * if the attachment is either not actually encrypted, or cannot be 
   * decrypted.
   */
  /*
  public BodyPart getDecryptedBodyPart() 
    throws MessagingException, java.io.IOException {
    if (decryptedBodyPart != null)
      return decryptedBodyPart;

    return null;
  }
  */

  /**
   * Returns the DataHandler for this Attachment.
   */
  /*
  public DataHandler getDataHandler() {
    if (encrypted) {
      try {
	BodyPart bp = getDecryptedBodyPart();
	
	if (bp != null) {
	  return bp.getDataHandler();
	}
      } catch (Exception e) {
	e.printStackTrace();
      }
    }

    return super.getDataHandler();
  }
  */

  /**
   * Returns the MimeType.
   */
  /*
  public ContentType getMimeType() {
    if (encrypted && decryptedBodyPart != null) {
      try {
	BodyPart bp = getDecryptedBodyPart();
	return new ContentType(bp.getContentType());
      } catch (Exception e) {
	//e.printStackTrace();
      }
    }

    return super.getMimeType();
  }
  */  
}
