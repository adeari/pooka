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
  public static java.security.Key selectPublicKey(String message, String type) throws java.security.GeneralSecurityException {

    Set publicKeys = Pooka.getCryptoManager().publicKeyAliases(type);
    String alias = showKeySet(publicKeys, message, Pooka.getProperty("Pooka.crypto.publicKey.title", "Select public key."));
    if (alias == null) 
      return null;
    
    return Pooka.getCryptoManager().getPublicKey(alias);
  }

  /**
   * Selects a private key.
   */
  public static java.security.Key selectPrivateKey(String message, String type)  throws java.security.GeneralSecurityException {

    Set privateKeys = Pooka.getCryptoManager().privateKeyAliases(type);
    String alias = showKeySet(privateKeys, message, Pooka.getProperty("Pooka.crypto.privateKey.title", "Select private key."));
    if (alias == null) 
      return null;
    
    char[] passphrase = showPassphraseDialog(alias);
    return Pooka.getCryptoManager().getPrivateKey(alias, passphrase);
    
  }
    
  /**
   * Shows a dialog for selecting a key from a set.
   */
  public static String showKeySet(Set keys, String message, String title) {

    Vector keyList = new Vector(keys);
    JList displayList = new JList(keyList);
    //int value = JOptionPane.showConfirmDialog(Pooka.getMainPanel(), new JScrollPane(displayList), title, JOptionPane.YES_NO_OPTION);
    JLabel label = new JLabel(message);
    Object[] messageComponents = new Object[] { label, new JScrollPane(displayList) };
    int value = Pooka.getUIFactory().showConfirmDialog(messageComponents, title, JOptionPane.OK_CANCEL_OPTION);
    if (value != JOptionPane.CANCEL_OPTION) {
      String selectAlias = (String)displayList.getSelectedValue();
      return selectAlias;
    }

    return null;
  }

  /**
   * Shows a dialog for selecting a password.
   */
  public static char[] showPassphraseDialog(String alias) {
    JPasswordField field = new JPasswordField();
    //int value = JOptionPane.showConfirmDialog(Pooka.getMainPanel(), field, "Enter passphrase", JOptionPane.YES_NO_OPTION);
    JLabel label = new JLabel(Pooka.getProperty("Pooka.crypto.passphrase.message", "Enter passphrase for key ") + alias);
    Object[] messageComponents = new Object[] { label, field };
    int value = Pooka.getUIFactory().showConfirmDialog(messageComponents, Pooka.getProperty("Pooka.crypto.passphrase.title", "Enter passphrase"), JOptionPane.OK_CANCEL_OPTION);
    if (value != JOptionPane.CANCEL_OPTION) {
      return field.getPassword();
    }
    
    return null;
  }


  
}
