package net.suberic.pooka.gui.crypto;

import net.suberic.pooka.*;
import net.suberic.crypto.*;

import javax.swing.*;

import java.util.*;

/**
 * Selects a Key.
 */
public class CryptoKeySelector {

  /**
   * Selects a public key.
   */
  public static java.security.Key selectPublicKey() throws java.security.GeneralSecurityException {

    Set publicKeys = Pooka.getCryptoManager().publicKeyAliases();
    String alias = showKeySet(publicKeys, "Select public key.");
    if (alias == null) 
      return null;
    
    return Pooka.getCryptoManager().getPublicKey(alias);
  }

  /**
   * Selects a private key.
   */
  public static java.security.Key selectPrivateKey()  throws java.security.GeneralSecurityException {

    Set privateKeys = Pooka.getCryptoManager().privateKeyAliases();
    String alias = showKeySet(privateKeys, "Select private key.");
    if (alias == null) 
      return null;
    
    char[] passphrase = showPassphraseDialog();
    return Pooka.getCryptoManager().getPrivateKey(alias, passphrase);
    
  }
    
  /**
   * Shows a dialog for selecting a key from a set.
   */
  public static String showKeySet(Set keys, String title) {

    Vector keyList = new Vector(keys);
    JList displayList = new JList(keyList);
    int value = JOptionPane.showConfirmDialog(Pooka.getMainPanel(), new JScrollPane(displayList), title, JOptionPane.YES_NO_OPTION);
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
