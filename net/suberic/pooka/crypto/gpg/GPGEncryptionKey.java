package net.suberic.pooka.crypto.gpg;

import net.suberic.pooka.crypto.*;

import net.suberic.util.*;

public class GPGEncryptionKey extends EncryptionKey {

  String alias;
  String passphrase;

  /**
   * Creates a new GPGEncryptionKey.
   */
  public GPGEncryptionKey(String aliasName, String newPassphrase) {
    alias = aliasName;
    passphrase = newPassphrase;
  }

  /**
   * Returns the alias for this Key.
   */
  public String getAlias() {
    return alias;
  }

  /**
   * Returns the passphrase for this Key.
   */
  public String getPassphrase() {
    return passphrase;
  }
  
}
