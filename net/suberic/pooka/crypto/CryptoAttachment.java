package net.suberic.pooka.crypto;

import net.suberic.pooka.*;
import javax.mail.internet.*;
import javax.mail.*;
import javax.activation.DataHandler;

import java.io.*;

/**
 * An encrypted attachment.
 */
public class CryptoAttachment extends Attachment {

  boolean parsed = false;

  /**
   * Creates a CryptoAttachment out of a MimeBodyPart.
   */
  public CryptoAttachment(MimeBodyPart mbp) throws MessagingException {
    super(mbp);
  }
  
  /**
   * Creates a CryptoAttachment out of a MimeMessage.  This is typically
   * used when the content of a Message is too large to display, and
   * therefore it needs to be treated as an attachment rather than
   * as the text of the Message.
   */
  public CryptoAttachment(MimeMessage msg) throws MessagingException {
    super(msg);
  }

  // accessor methods.
  
  /**
   * Returns the decoded InputStream of this Attachment.
   */
  public InputStream getInputStream() throws java.io.IOException {
    return getDataHandler().getInputStream();
  }
  
  /**
   * Returns the DataHandler for this Attachment.
   */
  public DataHandler getDataHandler() {
    return super.getDataHandler();
  }
  
  /**
   * Returns the content of this attachment as an Object.
   */
  public Object getContent() throws java.io.IOException {
    try {
      return getDataHandler().getContent();
    } catch (UnsupportedEncodingException uee) {
      if (isText()) {
	/**
	 * Just read the InputStream directly into a byte array and
	 * hope for the best.  :)
	 */
	InputStream is = getDataHandler().getInputStream();
	ByteArrayOutputStream bos = new ByteArrayOutputStream();
	int b;
	while ((b = is.read()) != -1)
	  bos.write(b);
	byte[] barray = bos.toByteArray();
	return new String(barray, Pooka.getProperty("Pooka.defaultCharset", "iso-8859-1"));
      } else {
	throw uee;
      }
    }
  }
  
  
}
