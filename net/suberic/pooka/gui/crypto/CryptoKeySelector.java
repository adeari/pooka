package net.suberic.pooka.gui.crypto;

import net.suberic.pooka.*;
import net.suberic.pooka.crypto.*;

import javax.swing.*;

import java.util.*;

/**
 * Selects a Key.
 */
public class CryptoKeySelector {

  /**
   * Selects a public key.
   */
  public static EncryptionKey selectPublicKey() throws java.security.KeyStoreException, EncryptionException {
    EncryptionKeyManager mgr = Pooka.getCryptoManager().getKeyManager();
    if (mgr != null) {
      Set publicKeys = mgr.publicKeyAliases();
      String alias = showKeySet(publicKeys, "Select public key.");
      if (alias == null) 
	return null;

      return mgr.getPublicKey(alias);
    } else {
      System.out.println("crypto manager == null.");
      return null;
    }
  }

  /**
   * Selects a private key.
   */
  public static EncryptionKey selectPrivateKey()  throws java.security.KeyStoreException, EncryptionException {
    EncryptionKeyManager mgr = Pooka.getCryptoManager().getKeyManager();
    if (mgr != null) {
      Set privateKeys = mgr.privateKeyAliases();
      String alias = showKeySet(privateKeys, "Select private key.");
      if (alias == null) 
	return null;

      char[] passphrase = showPassphraseDialog();
      return mgr.getPrivateKey(alias, passphrase);
    } else {
      System.out.println("crypto manager == null.");
      return null;
    }  
  }
    
  /**
   * Shows a dialog for selecting a key from a set.
   */
  public static String showKeySet(Set keys, String title) {
    Vector keyList = new Vector(keys);
    JList displayList = new JList(keyList);
    int value = JOptionPane.showConfirmDialog(Pooka.getMainPanel(), displayList, title, JOptionPane.YES_NO_OPTION);
    if (value != JOptionPane.NO_OPTION && value != JOptionPane.CANCEL_OPTION) {
      String selectAlias = (String)displayList.getSelectedValue();
      return selectAlias;
    }

    return null;
  }

  /**
   * Shows a dialog for selecting a password.
   */
  public static char[] showPassphraseDialog() {
    JPasswordField field = new JPasswordField();
    int value = JOptionPane.showConfirmDialog(Pooka.getMainPanel(), field, "Enter passphrase", JOptionPane.YES_NO_OPTION);
    if (value != JOptionPane.NO_OPTION && value != JOptionPane.CANCEL_OPTION) {
      return field.getPassword();
    }
    
    return null;
  }


  
}
