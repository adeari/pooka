package net.suberic.pooka.gui.crypto;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.security.Key;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import net.suberic.util.VariableBundle;
import net.suberic.util.gui.IconManager;
import net.suberic.pooka.Pooka;
import net.suberic.crypto.EncryptionKey;
import net.suberic.pooka.gui.NewMessageProxy;
import net.suberic.pooka.gui.NewMessageCryptoInfo;


/**
 * Displays the cryptography status for a new message.
 */
public class NewMessageCryptoDisplay extends JPanel implements CryptoStatusDisplay {
  
  JButton mSignatureKeyButton = null;
  JButton mEncryptionKeyButton = null;

  JToggleButton mSignatureEnabledButton = null;
  JToggleButton mEncryptionEnabledButton = null;

  JList mAttachKeysList = null;

  NewMessageProxy proxy = null;

  /**
   * A JPanel that shows the encryption status of this message.
   */
  public NewMessageCryptoDisplay(NewMessageProxy nmp) {
    super();

    proxy = nmp;

    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    /*
    Box cryptoBox = new Box(BoxLayout.Y_AXIS);
    Box cryptoLabelBox = new Box(BoxLayout.X_AXIS);

    cryptoLabelBox.add(new JLabel(Pooka.getProperty("NewMessageCryptoPanel.encryptionKey.label", "Encryption Key")));
    
    cryptoLabelBox.add(Box.createHorizontalStrut(5));
    JButton clearEncryptionButton = createClearEncryptionButton();

    cryptoLabelBox.add(clearEncryptionButton);
    
    cryptoLabelBox.add(Box.createHorizontalGlue());

    createEncryptionButton();
    
    cryptoBox.add(cryptoLabelBox);

    Box cryptoButtonBox = Box.createHorizontalBox();
    cryptoButtonBox.add(mEncryptionKeyButton);
    cryptoButtonBox.add(Box.createHorizontalGlue());
    cryptoBox.add(cryptoButtonBox);
    
    Box signatureBox = new Box(BoxLayout.Y_AXIS);
    Box signatureLabelBox = new Box(BoxLayout.X_AXIS);

    signatureLabelBox.add(new JLabel(Pooka.getProperty("NewMessageCryptoPanel.signatureKey.label", "Signature Key")));
    
    signatureLabelBox.add(Box.createHorizontalStrut(5));

    JButton clearSignatureButton = createClearSignatureButton();
    signatureLabelBox.add(clearSignatureButton);

    signatureLabelBox.add(Box.createHorizontalGlue());
    
    createSignatureButton();
    
    signatureBox.add(signatureLabelBox);
    
    Box signatureButtonBox = Box.createHorizontalBox();
    signatureButtonBox.add(mSignatureKeyButton);
    signatureButtonBox.add(Box.createHorizontalGlue());
    signatureBox.add(signatureButtonBox);
    */

    Box cryptoBox = new Box(BoxLayout.X_AXIS);

    cryptoBox.add(new JLabel(Pooka.getProperty("NewMessageCryptoPanel.encryptionKey.label", "Encryption Key")));
    
    cryptoBox.add(Box.createHorizontalStrut(5));

    mEncryptionEnabledButton = createEncryptionEnabledButton();

    createEncryptionButton();

    cryptoBox.add(mEncryptionKeyButton);

    cryptoBox.add(Box.createHorizontalGlue());

    cryptoBox.add(mEncryptionEnabledButton);
    
    Box signatureBox = new Box(BoxLayout.X_AXIS);

    signatureBox.add(new JLabel(Pooka.getProperty("NewMessageCryptoPanel.signatureKey.label", "Signature Key")));
    
    signatureBox.add(Box.createHorizontalStrut(5));

    mSignatureEnabledButton = createSignatureEnabledButton();

    createSignatureButton();

    signatureBox.add(mSignatureKeyButton);

    signatureBox.add(Box.createHorizontalGlue());

    signatureBox.add(mSignatureEnabledButton);
    
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
   * Creates a ToggleButton for enabling/disabling encryption.
   */
  public JToggleButton createEncryptionEnabledButton() {
    IconManager iconManager = Pooka.getUIFactory().getIconManager();
    
    ImageIcon keyIcon = iconManager.getIcon(Pooka.getProperty("NewMessageCryptoDisplay.keyIcon", "Key"));
    ImageIcon noKeyIcon = iconManager.getIcon(Pooka.getProperty("NewMessageCryptoDisplay.noKeyIcon", "NoKey"));
    if (keyIcon != null && noKeyIcon != null) {
      JToggleButton returnValue = new JToggleButton(noKeyIcon, proxy.getCryptoInfo().getEncryptMessage() != NewMessageCryptoInfo.CRYPTO_NO);
      
      returnValue.setSelectedIcon(keyIcon);
      
      returnValue.addActionListener(new ActionListener() {
	  public void actionPerformed(ActionEvent e) {
	    boolean nowSelected = mEncryptionEnabledButton.isSelected();
	    
	    mEncryptionKeyButton.setEnabled(nowSelected);
	    if (nowSelected)
	      proxy.getCryptoInfo().setEncryptMessage(NewMessageCryptoInfo.CRYPTO_YES);
	    else
	      proxy.getCryptoInfo().setEncryptMessage(NewMessageCryptoInfo.CRYPTO_NO);
	  }
	});
      
      returnValue.setSize(keyIcon.getIconHeight(), keyIcon.getIconWidth());
      returnValue.setPreferredSize(new java.awt.Dimension(keyIcon.getIconHeight(), keyIcon.getIconWidth()));
      return returnValue;
    }
    
    return null;
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
    return proxy.getCryptoInfo().getEncryptionKey();
  }

  /**
   * Sets the current encryption key.
   */
  public void setEncryptionKey(Key pEncryptionKey) {
    proxy.getCryptoInfo().setEncryptionKey(pEncryptionKey);
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
   * Creates a ToggleButton for enabling/disabling the signature.
   */
  public JToggleButton createSignatureEnabledButton() {
    IconManager iconManager = Pooka.getUIFactory().getIconManager();
    
    ImageIcon keyIcon = iconManager.getIcon(Pooka.getProperty("NewMessageCryptoDisplay.keyIcon", "Key"));
    ImageIcon noKeyIcon = iconManager.getIcon(Pooka.getProperty("NewMessageCryptoDisplay.noKeyIcon", "NoKey"));
    if (keyIcon != null && noKeyIcon != null) {
      JToggleButton returnValue = new JToggleButton(noKeyIcon, proxy.getCryptoInfo().getSignMessage() != NewMessageCryptoInfo.CRYPTO_NO);
      
      returnValue.setSelectedIcon(keyIcon);

      returnValue.addActionListener(new ActionListener() {
	  public void actionPerformed(ActionEvent e) {
	    boolean nowSelected = mSignatureEnabledButton.isSelected();
	    mSignatureKeyButton.setEnabled(nowSelected);
	    if (nowSelected)
	      proxy.getCryptoInfo().setSignMessage(NewMessageCryptoInfo.CRYPTO_YES);
	    else
	      proxy.getCryptoInfo().setSignMessage(NewMessageCryptoInfo.CRYPTO_NO);
	  }
	});
      returnValue.setSize(new java.awt.Dimension(keyIcon.getIconHeight(), keyIcon.getIconWidth()));
      returnValue.setPreferredSize(new java.awt.Dimension(keyIcon.getIconHeight(), keyIcon.getIconWidth()));

      return returnValue;
    }

    return null;
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
    return proxy.getCryptoInfo().getSignatureKey();
  }

  /**
   * Sets the current encryption key.
   */
  public void setSignatureKey(Key pSignatureKey) {
    proxy.getCryptoInfo().setSignatureKey(pSignatureKey);
    updateSignatureButton();
  }

  /**
   * Sets whether we're going to encrypt or not.
   */
  public void setEncryptMessage(int encryptValue) {
    proxy.getCryptoInfo().setEncryptMessage(encryptValue);
  }

  /**
   * Sets whether we're going to sign or not.
   */
  public void setSignMessage(int signValue) {
    proxy.getCryptoInfo().setSignMessage(signValue);
  }

  /**
   * Attaches an encryption key.
   */
  public void attachEncryptionKey(Key cryptKey) {
    proxy.getCryptoInfo().attachEncryptionKey(cryptKey);
  }

  /**
   * Removes an encryption key.
   */
  public void removeEncryptionKey(Key cryptKey) {
    proxy.getCryptoInfo().removeEncryptionKey(cryptKey);
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
