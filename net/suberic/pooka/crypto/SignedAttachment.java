package net.suberic.pooka.crypto;

import net.suberic.pooka.*;
import net.suberic.crypto.*;

import javax.mail.internet.*;
import javax.mail.*;
import javax.activation.DataHandler;

import java.security.Key;

import java.io.*;

/**
 * A signed attachment.
 */
public class SignedAttachment extends Attachment {

  boolean parsed = false;

  /**
   * Creates a SignedAttachment out of a MimeBodyPart.
   */
  public SignedAttachment(MimeBodyPart mbp) throws MessagingException {
    super(mbp);
  }
  
  /**
   * Returns if the signature matches.
   */
  public boolean checkSignature() 
    throws MessagingException, java.io.IOException {

    return false;
  }

  /**
   * Creates a SignedAttachment out of a MimeMessage.  This is typically
   * used when the content of a Message is too large to display, and
   * therefore it needs to be treated as an attachment rather than
   * as the text of the Message.
   */
  public SignedAttachment(MimeMessage msg) throws MessagingException {
    super(msg);
  }

  /**
   * Returns the content part of the signed attachment.
   */
  public MimeBodyPart getSignedPart() throws javax.mail.MessagingException,
  java.io.IOException {
    Object content = getDataHandler().getContent();
    if (content instanceof MimeMultipart) {
      MimeMultipart mm = (MimeMultipart) content;

      // this should be exactly two parts, one the content, the other the
      // signature.
      for (int i = 0; i < mm.getCount(); i++) {
	// return the first one found.
	MimeBodyPart mbp = (MimeBodyPart) mm.getBodyPart(i);
	ContentType ct = new ContentType(mbp.getContentType());
	if (! ct.getSubType().toLowerCase().endsWith("signature")) {
	  return mbp;
	} 
      }
    }

    return null;
  }

  /**
   * Returns the DataHandler for this Attachment.
   */
  public DataHandler getDataHandler() {
    return super.getDataHandler();
  }


  /**
   * Returns the MimeType.
   */
  /*
  public ContentType getMimeType() {
    try {
      return new ContentType("text/plain");
    } catch (javax.mail.internet.ParseException pe) {
      return null;
    }
  }
  */
}
