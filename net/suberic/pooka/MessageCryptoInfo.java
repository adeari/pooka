package net.suberic.pooka;

import net.suberic.pooka.crypto.*;
import javax.mail.*;
import javax.mail.internet.*;

import java.util.*;

/**
 * This stores the encyrption information about a particular MessageInfo. 
 */
public class MessageCryptoInfo {
  
  // the MessageInfo that we're analyzing.
  MessageInfo msgInfo;

  // whether we've checked the signature yet
  boolean mCheckedSignature = false;

  // whether we've tried decrypting the message yet.
  boolean mCheckedDecryption = false;

  // whether or not the decryption was successful
  boolean mDecryptSuccessful = false;

  // whether the signature matches or not
  boolean mSignatureValid = false;

  /**
   * Creates a MessageCryptoInfo for this given Message.
   */
  public MessageCryptoInfo(MessageInfo sourceMsg) {
    msgInfo = sourceMsg;
  }

  /**
   * Returns whether or not this message is signed.
   */
  public boolean isSigned() throws MessagingException {
    // FIXME
    return Pooka.getCryptoManager().getDefaultEncryptionUtils().isEncrypted(msgInfo.getMessage());
  }

  /**
   * Returns whether or not this message is encrypted.
   */
  public boolean isEncrypted() throws MessagingException {
    // FIXME
    return Pooka.getCryptoManager().getDefaultEncryptionUtils().isEncrypted(msgInfo.getMessage());
  }

  /**
   * Returns whether or not this message has had its signature checked.
   * Returns false if the message is not signed in the first place.
   */
  public boolean hasCheckedSignature() throws MessagingException {
    if (! isSigned())
      return false;

    return mCheckedSignature;
  }

  /**
   * Returns whether or not this message has had a decryption attempt.
   * Returns false if the message is not encrypted in the first place.
   */
  public boolean hasTriedDecryption() throws MessagingException {
    if (! isEncrypted())
      return false;

    return mCheckedDecryption;
  }

  /**
   * Returns whether or not the signature is valid.  If the signature has not
   * been checked yet, returns false.
   */
  public boolean isSignatureValid() throws MessagingException {
    if (hasCheckedSignature())
      return mSignatureValid;
    else
      return false;
  }

  /**
   * Returns whether or not the signature is valid.  If <code>recheck</code>
   * is set to <code>true</code>, then checks again with the latest keys.
   */
  public boolean checkSignature(EncryptionKey key, boolean recheck) throws EncryptionException, MessagingException, java.io.IOException {
    if (recheck || ! hasCheckedSignature()) {
      EncryptionUtils cryptoUtils = Pooka.getCryptoManager().getDefaultEncryptionUtils();
      mSignatureValid =  cryptoUtils.checkSignature((MimeMessage)msgInfo.getMessage(), key);
      mCheckedSignature = true;
    }

    return mSignatureValid;
  }

  /**
   * Tries to decrypt the message using the given Key.
   */
  public boolean decryptMessage(EncryptionKey key, boolean recheck) 
  throws MessagingException, EncryptionException, java.io.IOException {
    synchronized(this) {
      if (mCheckedDecryption && ! recheck) {
	return mDecryptSuccessful;
      } else {
	mCheckedDecryption = true;
	// run through all of the attachments and decrypt them.
	AttachmentBundle bundle = msgInfo.getAttachmentBundle();
	List attachmentList = bundle.getAttachmentsAndTextPart();
	for (int i = 0; i < attachmentList.size(); i++) {
	  Object o = attachmentList.get(i);
	  if (o instanceof CryptoAttachment) {
	    CryptoAttachment ca = (CryptoAttachment) o;
	    BodyPart bp = ca.decryptAttachment(Pooka.getCryptoManager().getDefaultEncryptionUtils(), key);
	    
	    // check to see what kind of attachment it is.  if it's a 
	    // Multipart, then we need to expand it and add it to the 
	    // attachment list.
	    
	    if (bp.getContent() instanceof Multipart) {
	      AttachmentBundle newBundle = MailUtilities.parseAttachments((Multipart) bp.getContent());
	      bundle.addAll(newBundle);
	    } else if (ca.getMimeType().getPrimaryType().equalsIgnoreCase("text")) {
	      // if it's a text part, then we might need to set it as 
	      // such in the attachment bundle.
	      if (bundle.textPart == null) {
		bundle.textPart = ca;
		bundle.allAttachments.remove(ca);
	      }
	    }
	  }
	}

	mDecryptSuccessful = true;
      }
    }

    return mDecryptSuccessful;
  }
}
