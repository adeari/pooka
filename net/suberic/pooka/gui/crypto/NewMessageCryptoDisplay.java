package net.suberic.pooka.gui.crypto;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.security.Key;

import net.suberic.util.VariableBundle;
import net.suberic.pooka.Pooka;
import net.suberic.crypto.EncryptionKey;
import net.suberic.pooka.gui.NewMessageProxy;


/**
 * Displays the cryptography status for a new message.
 */
public class NewMessageCryptoDisplay extends JPanel implements CryptoStatusDisplay {
  
  JButton mSignatureKeyButton = null;
  JButton mEncryptionKeyButton = null;
  JList mAttachKeysList = null;

  NewMessageProxy proxy = null;
  /**
   * A JPanel that shows the encryption status of this message.
   */
  public NewMessageCryptoDisplay(NewMessageProxy nmp) {
    super();

    proxy = nmp;

    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

    Box cryptoBox = new Box(BoxLayout.Y_AXIS);
    Box cryptoLabelBox = new Box(BoxLayout.X_AXIS);

    cryptoLabelBox.add(new JLabel(Pooka.getProperty("NewMessageCryptoPanel.encryptionKey.label", "Encryption Key")));
    
    JButton clearEncryptionButton = createClearEncryptionButton();
    cryptoLabelBox.add(clearEncryptionButton);
    
    createEncryptionButton();
    
    cryptoBox.add(cryptoLabelBox);
    cryptoBox.add(mEncryptionKeyButton);
    
    Box signatureBox = new Box(BoxLayout.Y_AXIS);
    Box signatureLabelBox = new Box(BoxLayout.X_AXIS);

    signatureLabelBox.add(new JLabel(Pooka.getProperty("NewMessageCryptoPanel.signatureKey.label", "Signature Key")));
    
    JButton clearSignatureButton = createClearSignatureButton();
    signatureLabelBox.add(clearSignatureButton);
    
    createSignatureButton();
    
    signatureBox.add(signatureLabelBox);
    signatureBox.add(mSignatureKeyButton);
    
    this.add(cryptoBox);
    this.add(signatureBox);
  }

  /**
   * Creates an Encryption Button.
   */
  public void createEncryptionButton() {
    mEncryptionKeyButton = new JButton();
    mEncryptionKeyButton.addActionListener(proxy.getAction("message-select-crypt-key"));
    updateEncryptionButton();
  }
  
  /**
   * Updates the enryption button.
   */
  public void updateEncryptionButton() {
    Runnable runMe = new Runnable() {
	public void run() {
	  Key current = getEncryptionKey();
	  if (current == null)
	    mEncryptionKeyButton.setText(Pooka.getProperty("NewMessageCryptoPanel.encryptionKey.none", "< Not Encrypted >"));
	  else
	    mEncryptionKeyButton.setText(current instanceof EncryptionKey ? ((EncryptionKey)current).getDisplayAlias() : current.toString());
	}
      };

    if (SwingUtilities.isEventDispatchThread())
      runMe.run();
    else
      SwingUtilities.invokeLater(runMe);
  }

  /**
   * Creates a button which clears the encryption key.
   */
  public JButton createClearEncryptionButton() {
    JButton returnValue = new JButton(Pooka.getProperty("NewMessageCryptoPanel.clearButton.label", "Clear Key"));
    returnValue.addActionListener(proxy.getAction("message-clear-encrypt"));
    return returnValue;
  }

  /**
   * Returns the current encryption key.
   */
  public Key getEncryptionKey() {
    return proxy.getNewMessageInfo().getEncryptionKey();
  }

  /**
   * Sets the current encryption key.
   */
  public void setEncryptionKey(Key pEncryptionKey) {
    proxy.getNewMessageInfo().setEncryptionKey(pEncryptionKey);
    updateEncryptionButton();
  }

  /**
   * Creates an Signature Button.
   */
  public void createSignatureButton() {
    mSignatureKeyButton = new JButton();
    mSignatureKeyButton.addActionListener(proxy.getAction("message-select-sig-key"));
    updateSignatureButton();
  }
  
  /**
   * Updates the enryption button.
   */
  public void updateSignatureButton() {
    Runnable runMe = new Runnable() {
	public void run() {
	  Key current = getSignatureKey();
	  if (current == null)
	    mSignatureKeyButton.setText(Pooka.getProperty("NewMessageCryptoPanel.signatureKey.none", "< Not Signed >"));
	  else
	    mSignatureKeyButton.setText(current instanceof EncryptionKey ? ((EncryptionKey)current).getDisplayAlias() : current.toString());	}
      };

    if (SwingUtilities.isEventDispatchThread())
      runMe.run();
    else
      SwingUtilities.invokeLater(runMe);
  }

  /**
   * Creates a button which clears the signature key.
   */
  public JButton createClearSignatureButton() {
    JButton returnValue = new JButton(Pooka.getProperty("NewMessageCryptoPanel.clearButton.label", "Clear Key"));
    returnValue.addActionListener(proxy.getAction("message-clear-signature"));
    return returnValue;
  }

  /**
   * Returns the current encryption key.
   */
  public Key getSignatureKey() {
    return proxy.getNewMessageInfo().getSignatureKey();
  }

  /**
   * Sets the current encryption key.
   */
  public void setSignatureKey(Key pSignatureKey) {
    proxy.getNewMessageInfo().setSignatureKey(pSignatureKey);
    updateSignatureButton();
  }

  /**
   * Updates the encryption information.
   */
  public void cryptoUpdated(int newSignatureStatus, int newEncryptionStatus) {

  }
  
  /**
   * Updates the encryption information.
   */
  public void cryptoUpdated(net.suberic.pooka.MessageCryptoInfo cryptoInfo) {
    
  }


}
