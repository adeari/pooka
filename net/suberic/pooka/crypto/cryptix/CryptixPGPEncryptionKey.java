package net.suberic.pooka.crypto.cryptix;

import cryptix.message.*;
import cryptix.openpgp.*;
import cryptix.pki.*;

import net.suberic.util.*;
import net.suberic.pooka.crypto.*;

public class CryptixPGPEncryptionKey extends EncryptionKey {

  KeyBundle keyBundle = null;
  char[] passphrase = null;

  public CryptixPGPEncryptionKey(KeyBundle kb, char[] pphrase) {
    keyBundle = kb;
    passphrase = pphrase;
  }

  /**
   * Returns the KeyBundle associated with this EncryptionKey.
   */
  public KeyBundle getKeyBundle() {
    return keyBundle;
  }

  /**
   * Returns the passphrase for the KeyBundle.
   */
  public char[] getPassphrase() {
    return passphrase;
  }
}
