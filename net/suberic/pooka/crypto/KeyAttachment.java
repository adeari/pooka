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
public class KeyAttachment extends Attachment {

  boolean parsed = false;

  /**
   * Creates a KeyAttachment out of a MimeBodyPart.
   */
  public KeyAttachment(MimeBodyPart mbp) throws MessagingException {
    super(mbp);
  }
  
  /**
   * Creates a KeyAttachment out of a MimeMessage.  This is typically
   * used when the content of a Message is too large to display, and
   * therefore it needs to be treated as an attachment rather than
   * as the text of the Message.
   */
  public KeyAttachment(MimeMessage msg) throws MessagingException {
    super(msg);
  }

  /**
   * Returns the attached keys.
   */
  public Key[] extractKeys(EncryptionUtils utils) throws MessagingException, java.io.IOException, java.security.GeneralSecurityException {
    net.suberic.crypto.UpdatableMBP mbp = new net.suberic.crypto.UpdatableMBP();

    mbp.setContent(getDataHandler().getContent(), getMimeType().toString());
    mbp.updateMyHeaders();
    
    if (utils == null) {
      utils = net.suberic.crypto.EncryptionManager.getEncryptionUtils(mbp);
    }

    if (utils != null) 
      return utils.extractKeys(mbp);
    else
      return null;
  }

}
