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

}
