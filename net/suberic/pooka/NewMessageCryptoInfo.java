package net.suberic.pooka;

import java.security.Key;

import java.util.*;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import net.suberic.crypto.*;

/**
 * Encapsulates the encryption info for a new message.
 */
public class NewMessageCryptoInfo extends MessageCryptoInfo {

  List mAttachKeys = new LinkedList();

  List mRecipientMatches = new LinkedList();

  public static int CRYPTO_YES = 0;
  public static int CRYPTO_DEFAULT = 5;
  public static int CRYPTO_NO = 10;

  // whether or not we want to encrypt this message.
  int mEncryptMessage = CRYPTO_DEFAULT;
  
  // whether or not we want to sign this message
  int mSignMessage = CRYPTO_DEFAULT;

  // the configured list of recipients.
  List mRecipientInfos = new LinkedList();

  /**
   * Creates a new NewMessageCryptoInfo.
   */
  public NewMessageCryptoInfo(NewMessageInfo nmi) {
    super(nmi);
  }

  // sign message.
 
  /**
   * Returns whether we're planning on encrypting this message or not.
   */
  public int getSignMessage() {
    return mSignMessage;
  }

  /**
   * Sets whether or not we want to encrypt this message.
   */
  public void setSignMessage(int pSignMessage) {
    mSignMessage = pSignMessage;
  }
  
  // attach keys.

  /**
   * Attaches an encryption key to this message.
   */
  public synchronized void attachEncryptionKey(Key key) {
    if (! mAttachKeys.contains(key))
      mAttachKeys.add(key);
  }

  /**
   * Attaches an encryption key to this message.
   */
  public synchronized void removeEncryptionKey(Key key) {
    if (mAttachKeys.contains(key)) {
      mAttachKeys.remove(key);
    }
    
  }
  
  /**
   * Returns the keys to be attached.
   */
  public List getAttachKeys() {
    return new LinkedList(mAttachKeys);
  }

  // methods.

  /**
   * Creates the attached key parts for this message.
   */
  public List createAttachedKeyParts() {
    LinkedList keyParts = new LinkedList();
    List attachKeys = getAttachKeys();
    if (attachKeys != null) {
      for (int i = 0; i < attachKeys.size(); i++) {
	EncryptionKey currentKey = (EncryptionKey)attachKeys.get(i);
	try {
	  EncryptionUtils utils = currentKey.getEncryptionUtils();
	  keyParts.add(utils.createPublicKeyPart(new Key[] { currentKey }));
	} catch (Exception e) {
	  // FIXME ignore for now.
	  System.out.println("caught exception adding key to message:  " + e);
	  e.printStackTrace();
	}
      }
    }

    return keyParts;
  }

  /**
   * Returns the encrypted and/or signed message(s), as appropriate.
   */
  public List createEncryptedMessages(MimeMessage mm) throws MessagingException {
    List returnValue = new LinkedList();
    
    List recipientInfoList = getCryptoRecipientInfos();
    for (int i = 0; i < recipientInfoList.size(); i++) {
      returnValue.add(((CryptoRecipientInfo) recipientInfoList.get(i)).handleMessage(mm));
    }

    return returnValue;
  }

  /**
   * Returns the configured CryptoRecipientInfos.
   */
  public List getCryptoRecipientInfos() {
    return mRecipientInfos;
  }

  // Recipient/encryption key matches.

  /**
   * This represents a match between a recipient set and an encryption
   * configuration.  The assumption is that all of the following recipients
   * can receive the same message.
   */
  public class CryptoRecipientInfo {

    // the signature key.
    Key mSignatureKey = null;

    // the encryption key
    Key mEncryptionKey = null;

    // the recipients
    Address[] toList = null;
    Address[] ccList = null;
    Address[] bccList = null;

    /**
     * The recipients for this crypto configuration.
     */
    public Address[] getRecipients(Message.RecipientType type) {
      if (type == Message.RecipientType.TO)
	return toList;
      else if (type == Message.RecipientType.CC)
	return ccList;
      else if (type == Message.RecipientType.BCC)
	return bccList;
      else
	return null;
    }

    /**
     * Sets the recipients for the particular type.
     */
    public void setRecipients(Address[] pRecipients, Message.RecipientType type) {
      if (type == Message.RecipientType.TO)
	toList = pRecipients;
      else if (type == Message.RecipientType.CC)
	ccList = pRecipients;
      else if (type == Message.RecipientType.BCC)
	bccList = pRecipients;

    }

    /**
     * The Signature Key for this set of recipients.
     */
    public Key getSignatureKey() {
      return mSignatureKey;
    }

    /**
     * Sets the encryption key for encrypting this message.
     */
    public void setSignatureKey(Key pSignatureKey) {
      mSignatureKey = pSignatureKey;
    }

    /**
     * Sets the encryption key for encrypting this message.
     */
    public void setEncryptionKey(Key pEncryptionKey) {
      mEncryptionKey = pEncryptionKey;
    }
    
    /**
     * Gets the encryption key we're using for this message.
     */
    public Key getEncryptionKey() {
      return mEncryptionKey;
    }

    /**
     * Creates a new MimeMessage using the given recipients and encryption.
     */
    public MimeMessage handleMessage(MimeMessage mm) 
    throws MessagingException {
      MimeMessage returnValue = new MimeMessage(mm);

      returnValue.setRecipients(Message.RecipientType.TO, getRecipients(Message.RecipientType.TO));
      returnValue.setRecipients(Message.RecipientType.CC, getRecipients(Message.RecipientType.CC));
      returnValue.setRecipients(Message.RecipientType.BCC, getRecipients(Message.RecipientType.BCC));

      if (getSignatureKey() != null) {
	try {
	  returnValue = Pooka.getCryptoManager().signMessage(returnValue, null, getSignatureKey());
	} catch (Exception e) {
	  e.printStackTrace();
	}
      }
    
      if (getEncryptionKey() != null) {
	try {
	  returnValue = Pooka.getCryptoManager().encryptMessage(returnValue, getEncryptionKey());
	} catch (Exception e) {
	  e.printStackTrace();
	}
      }

      return returnValue;
    }
  }
  
}
