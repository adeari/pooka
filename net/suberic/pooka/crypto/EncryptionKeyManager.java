package net.suberic.pooka.crypto;

import net.suberic.pooka.*;
import net.suberic.util.*;

/**
 * This manages a set of Encryption keys.
 */
public class EncryptionKeyManager extends ItemManager implements ItemCreator {

  /**
   * Create an EncryptionKeyManager which loads information from the given
   * VariableBundle using the given newResourceString, and creates new
   * Items using the given ItemCreator.
   */
  public EncryptionKeyManager(String newResourceString, VariableBundle newSourceBundle, ItemCreator newItemCreator) {
    super(newResourceString, newSourceBundle, newItemCreator);
  }

  /**
   * Creates an item from the given sourceBundle, resourceString, and itemID.
   */
  public Item createItem(VariableBundle sourceBundle, String resourceString, String itemID) {
    return null;
  }
  
}
