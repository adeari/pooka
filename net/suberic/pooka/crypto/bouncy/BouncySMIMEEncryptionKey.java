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

  PrivateKey mPrivate;

  PublicKey mPublic;

  X509Certificate mCertificate;

  /**
   * Returns the PublicKey for this key.
   */
  public PublicKey getPublic() {
    return mPublic;
  }
  /**
   * Returns the PrivateKey for this key.
   */
  public PrivateKey getPrivate() {
    return mPrivate;
  }
  /**
   * Sets the KeyPair for this key.
   */
  public void setKeyPair(KeyPair pKeyPair) {
    mPublic = pKeyPair.getPublic();
    mPrivate = pKeyPair.getPrivate();
  }
  /**
   * Sets the PublicKey for this key.
   */
  public void setPublic(PublicKey pPublic) {
    mPublic = pPublic;
  }
  /**
   * Sets the PublicKey for this key.
   */
  public void setPrivate(PrivateKey pPrivate) {
    mPrivate = pPrivate;
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
