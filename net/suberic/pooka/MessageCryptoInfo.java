package net.suberic.pooka;

import net.suberic.pooka.crypto.*;
import net.suberic.crypto.*;
import javax.mail.*;
import javax.mail.internet.*;

import java.util.*;
import java.security.Key;

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

    checkEncryptionType();

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
   * Checks the encryption of this message.
   */
  void checkEncryptionType() throws MessagingException {
    synchronized(this) {
      if (! mCheckedEncryption) {
	mEncryptionType = net.suberic.crypto.EncryptionManager.checkEncryptionType((MimeMessage) mMsgInfo.getMessage());
	mCheckedEncryption = true;
      }
    }
    
  }

  /**
   * Returns the encryption type of this message.
   */
  public String getEncryptionType() throws MessagingException {
    checkEncryptionType();

    return mEncryptionType;
  }

   
  /**
   * Returns whether or not this message is signed.
   */
  public boolean isSigned() throws MessagingException {

    if (mMsgInfo.hasLoadedAttachments()) {
      List attachments = mMsgInfo.getAttachments();
      for (int i = 0 ; i < attachments.size(); i++) {
	if (attachments.get(i) instanceof SignedAttachment) {
	  return true;
	}
      }

      return false;
    } else {
      EncryptionUtils utils = getEncryptionUtils();
      if (utils != null) {
	return (utils.getEncryptionStatus((MimeMessage) mMsgInfo.getMessage()) == EncryptionUtils.SIGNED);
      } else
	return false;
    }
  }

  /**
   * Returns whether or not this message is encrypted.
   */
  public boolean isEncrypted() throws MessagingException {

    if (mMsgInfo.hasLoadedAttachments()) {
      List attachments = mMsgInfo.getAttachments();
      for (int i = 0 ; i < attachments.size(); i++) {
	if (attachments.get(i) instanceof CryptoAttachment) {
	  return true;
	}
      }
      return false;
    } else {
      EncryptionUtils utils = getEncryptionUtils();
      if (utils != null) {
	return (utils.getEncryptionStatus((MimeMessage) mMsgInfo.getMessage()) == EncryptionUtils.ENCRYPTED);
      } else
	return false;
    }
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
      EncryptionUtils cryptoUtils = getEncryptionUtils();
      //mSignatureValid =  cryptoUtils.checkSignature((MimeMessage)mMsgInfo.getMessage(), key);
      List attachments = mMsgInfo.getAttachments();
      boolean returnValue = false;
      for (int i = 0; i < attachments.size(); i++) {
	Attachment current = (Attachment) attachments.get(i);
	if (current instanceof SignedAttachment) {
	  mSignatureValid = ((SignedAttachment) current).checkSignature(cryptoUtils, key);
	}
      }
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
	    
	    
	    /*
	    if (bp.getContent() instanceof Multipart) {
	      AttachmentBundle newBundle = MailUtilities.parseAttachments((Multipart) bp.getContent());
	      bundle.addAll(newBundle);
	    } else {
	      bundle.removeAttachment(ca);
	      bundle.addAttachment(ca);
	    }
	    */
	    //bundle.removeAttachment(ca);
	    MailUtilities.handlePart((MimeBodyPart) bp, bundle);
	  }
	}

	mDecryptSuccessful = true;
      }
    }

    return mDecryptSuccessful;
  }

  /**
   * Tries to decrypt the Message using all available cached keys.
   */
  public boolean autoDecrypt(UserProfile defaultProfile) {
    try {
      String cryptType = getEncryptionType();
      
      /*
	Key defaultKey = defaultProfile.getPrivateKey(cryptType);
	if (defaultKey != null) {
	try {
	if (decryptMessage(defaultKey, true))
	return true;
	} catch (Exception e) {
	// ignore for now.
	}
	}
      */
      
      // why not just try all of the private keys?  at least, all the
      // ones we have available.
      //java.security.Key[] privateKeys = Pooka.getCryptoManager().getCachedPrivateKeys(cryptType);
      java.security.Key[] privateKeys = Pooka.getCryptoManager().getCachedPrivateKeys();
      
      if (privateKeys != null) {
	for (int i = 0 ; i < privateKeys.length; i++) {
	  try {
	    if (decryptMessage(privateKeys[i], true))
	      return true;
	  } catch (Exception e) {
	    // ignore for now.
	  }
	}
	
      }
    } catch (Exception e) {
    }
    return false;
  }  

  /**
   * Checks the signature of the given message as compared to the 
   * given from address.
   */
  public boolean autoCheckSignature(InternetAddress sender) {
    try {
      String senderAddress = sender.getAddress();
      Key[] matchingKeys = Pooka.getCryptoManager().getPublicKeys(senderAddress,getEncryptionType());
      for (int i = 0 ; i < matchingKeys.length; i++) {
	if (checkSignature(matchingKeys[i], true)) {
	  return true;
	}
      }
    } catch (Exception e) {
    }
    return false;
  }

  /**
   * Extracts the (public) keys from the message.
   */
  public Key[] extractKeys() throws MessagingException, java.io.IOException, java.security.GeneralSecurityException {
    synchronized(this) {
      AttachmentBundle bundle = mMsgInfo.getAttachmentBundle();
      List attachmentList = bundle.getAttachmentsAndTextPart();
      for (int i = 0; i < attachmentList.size(); i++) {
	Object o = attachmentList.get(i);
	if (o instanceof KeyAttachment) {
	  EncryptionUtils utils = getEncryptionUtils();
	  return ((KeyAttachment) o).extractKeys(utils);
	}
      }
    }

    return null;
  }

  /**
   * Returns true if this has been decrypted successfully.
   */
  public boolean isDecryptedSuccessfully() {
    return mDecryptSuccessful;
  }

  /**
   * Returns the MessageInfo.
   */
  public MessageInfo getMessageInfo() {
    return mMsgInfo;
  }
}
