package net.suberic.pooka.crypto;

import cryptix.message.*;
import cryptix.openpgp.*;
import cryptix.pki.*;

import net.suberic.util.*;

public class PGPEncryptionKey extends EncryptionKey {

  KeyBundle keyBundle = null;
  char[] passphrase = null;

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
