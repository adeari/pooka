package net.suberic.pooka.crypto;

import net.suberic.pooka.*;
import javax.mail.*;
import javax.mail.internet.*;

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
  
}
