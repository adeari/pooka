package net.suberic.pooka.crypto;

import net.suberic.pooka.*;

/**
 * This stores the encyrption information about a particular Message. 
 */
public class MessageCryptoInfo {
  
  // the MessageInfo that we're analyzing.
  MessageInfo msgInfo;

  /**
   * Creates a MessageCryptoInfo for this given MessageInfo.
   */
  public MessageCryptoInfo(MessageInfo sourceMsg) {
    msgInfo = sourceMsg;
  }

  /**
   * Returns whether or not this message is signed.
   */
  public boolean isSigned() {
    // FIXME
    return false;
  }

  /**
   * Returns whether or not this message is encrypted.
   */
  public boolean isEncrypted() {
    // FIXME
    return false;
  }

}
