package net.suberic.pooka;

import net.suberic.pooka.crypto.*;
import net.suberic.crypto.*;
import javax.mail.*;
import javax.mail.internet.*;

import java.util.*;

/**
 * This stores the encyrption information about a particular MessageInfo. 
 */
public class MessageCryptoInfo {
  
  // the MessageInfo that we're analyzing.
  MessageInfo mMsgInfo;

  // the type of encryption (s/mime, pgp)
  String mEncryptionType = null;

  // whether or not we've checked to see if this is encrypted at all
  boolean mCheckedEncryption = false;

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
    mMsgInfo = sourceMsg;
  }

  /**
   * Returns the EncryptionUtils to use with this MessageCryptoInfo.
   */
  public EncryptionUtils getEncryptionUtils() throws MessagingException {
    if (! mCheckedEncryption) {
      mEncryptionType = net.suberic.crypto.EncryptionManager.checkEncryptionType((MimeMessage) mMsgInfo.getMessage());
    }

    if (mEncryptionType != null) {
      try {
	return net.suberic.crypto.EncryptionManager.getEncryptionUtils(mEncryptionType);
      } catch (java.security.NoSuchProviderException nspe) {
	return null;
      }
    } else {
      return null;
    }
  }

  /**
   * Returns whether or not this message is signed.
   */
  public boolean isSigned() throws MessagingException {
    EncryptionUtils utils = getEncryptionUtils();
    if (utils != null) {
      // FIXME 
      return true;
    } else
      return false;
  }

  /**
   * Returns whether or not this message is encrypted.
   */
  public boolean isEncrypted() throws MessagingException {
    EncryptionUtils utils = getEncryptionUtils();
    if (utils != null) {
      // FIXME
      return true;
    } else
      return false;
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
  public boolean checkSignature(java.security.Key key, boolean recheck) throws MessagingException, java.io.IOException, java.security.GeneralSecurityException {
    if (recheck || ! hasCheckedSignature()) {
      EncryptionUtils cryptoUtils = net.suberic.crypto.EncryptionManager.getEncryptionUtils((MimeMessage) mMsgInfo.getMessage());
      mSignatureValid =  cryptoUtils.checkSignature((MimeMessage)mMsgInfo.getMessage(), key);
      mCheckedSignature = true;
    }

    return mSignatureValid;
  }

  /**
   * Tries to decrypt the message using the given Key.
   */
  public boolean decryptMessage(java.security.Key key, boolean recheck) 
  throws MessagingException, java.io.IOException, java.security.GeneralSecurityException {
    synchronized(this) {
      if (mCheckedDecryption && ! recheck) {
	return mDecryptSuccessful;
      } else {
	mCheckedDecryption = true;
	// run through all of the attachments and decrypt them.
	AttachmentBundle bundle = mMsgInfo.getAttachmentBundle();
	List attachmentList = bundle.getAttachmentsAndTextPart();
	for (int i = 0; i < attachmentList.size(); i++) {
	  Object o = attachmentList.get(i);
	  if (o instanceof CryptoAttachment) {
	    CryptoAttachment ca = (CryptoAttachment) o;

	    // FIXME
	    EncryptionUtils cryptoUtils = getEncryptionUtils();

	    BodyPart bp = ca.decryptAttachment(cryptoUtils, key);
	    
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
	    } else {
	      System.err.println("ca.getMimeType() = " + ca.getMimeType());
	      System.err.println("bp.getMimeType() = " + ((MimeBodyPart) bp).getContentType());
	      System.err.println("bp.getContent() = " + bp.getContent());
	    }
	  }
	}

	mDecryptSuccessful = true;
      }
    }

    return mDecryptSuccessful;
  }
}
