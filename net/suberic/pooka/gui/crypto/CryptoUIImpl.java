package net.suberic.pooka.gui.crypto;

import javax.swing.*;
import java.util.*;

import net.suberic.pooka.crypto.*;
import net.suberic.pooka.*;
import net.suberic.pooka.gui.*;

/**
 * This defines a system that gets input for encryption/decryption.
 */
public class CryptoUIImpl implements CryptoUI {

  /**
   * Selects a public key.
   */
  public EncryptionKey selectPublicKey() {
    EncryptionKeyManager mgr = Pooka.getCryptoManager().getKeyManager();
    try {
      Set publicKeys = mgr.publicKeyAliases();
      
      Vector keyList = new Vector(publicKeys);
      
      JList list = new JList(keyList);
      list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      
      JScrollPane jsp = new JScrollPane(list);
      jsp.setPreferredSize(new java.awt.Dimension(400,200));
      Pooka.getUIFactory().showInputDialog(new java.awt.Component[] {jsp}, "Select the key to use.");
      
      if (list.getSelectedIndex() != -1) {
	mgr.getPublicKey((String) list.getSelectedValue());
      }
      
    } catch (Exception e) {
      e.printStackTrace();
    }

    return null;
  }

  /**
   * Selects a private key.
   */
  public EncryptionKey selectPrivateKey() {
    EncryptionKeyManager mgr = Pooka.getCryptoManager().getKeyManager();
    try {
      Set privateKeys = mgr.privateKeyAliases();
      
      Vector keyList = new Vector(privateKeys);
      
      JList list = new JList(keyList);
      list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      
      JScrollPane jsp = new JScrollPane(list);
      jsp.setPreferredSize(new java.awt.Dimension(400,200));
      Pooka.getUIFactory().showInputDialog(new java.awt.Component[] {jsp}, "Select the key to use.");
      
      if (list.getSelectedIndex() != -1) {
	mgr.getPrivateKey((String) list.getSelectedValue(), null);
      }
      
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Reads a passphrase into the supplied buffer.  Returns null if no entry
   * is made.
   */
  public char[] selectPassphrase(String alias) {
    String passphrase =  Pooka.getUIFactory().showInputDialog("Enter passphrase for " + alias, "Enter passphrase");
    if (passphrase != null) {
      char[] returnValue = new char[passphrase.length()];
      passphrase.getChars(0, passphrase.length(), returnValue, 0);
      return returnValue;
    } else {
      return null;
    }
  }
}
