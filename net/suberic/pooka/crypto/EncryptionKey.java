package net.suberic.pooka.crypto;

import net.suberic.util.*;

public class EncryptionKey implements Item {

  String keyId;

  /**
   * The Item ID.  For example, if you were to have a list of users, a
   * given user's itemID may be "defaultUser".
   */
  public String getItemID() {
    return keyId;
  }
  
  /**
   * The Item property.  For example, if you were to have a list of users, a
   * given user's itemProperty may be "Users.defaultUser".
   */
  public String getItemProperty() {
    return "EncryptionKey." + keyId;
  }
  
}
