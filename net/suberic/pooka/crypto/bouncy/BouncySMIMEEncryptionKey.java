package net.suberic.pooka.crypto.bouncy;

import net.suberic.pooka.*;
import net.suberic.pooka.crypto.*;

import java.security.*;

import java.security.cert.*;

/**
 * Represents an EncryptionKey for use with the BouncyCastle SMIME 
 * implementation.
 */
public class BouncySMIMEEncryptionKey extends EncryptionKey {

  KeyPair mKeyPair;

  X509Certificate mCertificate;

  /**
   * Returns the KeyPair for this key.
   */
  public KeyPair getKeyPair() {
    return mKeyPair;
  }
  /**
   * Sets the KeyPair for this key.
   */
  public void setKeyPair(KeyPair pKeyPair) {
    mKeyPair = pKeyPair;
  }


  /**
   * Returns the certificate for this key.
   */
  public X509Certificate getCertificate() {
    return mCertificate;
  }
  /**
   * Sets the certificate for this key.
   */
  public void setCertificate(X509Certificate pCertificate) {
    mCertificate = pCertificate;
  }

}
