package net.suberic.pooka.gui.crypto;

import javax.swing.*;
import java.awt.*;

import net.suberic.util.VariableBundle;
import net.suberic.pooka.Pooka;

public class CryptoPanel extends JPanel implements CryptoStatusDisplay {
  
  JButton encryptionButton;
  JButton signatureButton;

  // the various icons
  static ImageIcon notEncryptedIcon;
  static ImageIcon uncheckedEncryptedIcon;
  static ImageIcon decryptedSuccessfullyIcon;
  static ImageIcon decryptedUnsuccessfullyIcon;
  static ImageIcon notSignedIcon;
  static ImageIcon uncheckedSignedIcon;
  static ImageIcon signatureVerifiedIcon;
  static ImageIcon signatureBadIcon;
  static ImageIcon signatureFailedVerificationIcon;

  // the various status colors
  static Color signedEncryptedColor = Color.MAGENTA;
  static Color signedColor = Color.GREEN;
  static Color encryptedColor = Color.BLUE;
  static Color uncheckedColor = Color.YELLOW;
  static Color failedColor = Color.RED;

  static boolean iconsLoaded = false;

  // the current status
  int currentCryptStatus = NOT_ENCRYPTED;
  int currentSigStatus = NOT_SIGNED;

  /**
   * A JPanel that shows the encryption status of this message.
   */
  public CryptoPanel() {
    super();
    if (! iconsLoaded) {
      Class thisClass = this.getClass();
      synchronized(thisClass) {
	if (! iconsLoaded) {
	  loadIcons("CryptoPanel", thisClass, Pooka.getResources());
	  iconsLoaded = true;
	}
      }
    }

    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

    encryptionButton = createEncryptionButton();
    signatureButton = createSignatureButton();

    this.add(encryptionButton);
    this.add(signatureButton);
  }

  /**
   * Creates an Encryption Button.
   */
  public JButton createEncryptionButton() {
    JButton returnValue = new JButton();
    return returnValue;
  }
  
  /**
   * Creates a Signature Button.
   */
  public JButton createSignatureButton() {
    JButton returnValue = new JButton();
    return returnValue;
  }
  
  /**
   * Updates the encryption information.
   */
  public void cryptoUpdated(int newSignatureStatus, int newEncryptionStatus) {

    if (newSignatureStatus != currentSigStatus) {
      currentSigStatus = newSignatureStatus;

      if (currentSigStatus == NOT_SIGNED) {
	signatureButton.setIcon(notSignedIcon);
      } else if (currentSigStatus == UNCHECKED_SIGNED) {
	signatureButton.setIcon(uncheckedSignedIcon);
      } else if (currentSigStatus == SIGNATURE_VERIFIED) {
	signatureButton.setIcon(signatureVerifiedIcon);
      } else if (currentSigStatus == SIGNATURE_BAD) {
	signatureButton.setIcon(signatureBadIcon);
      }
    }

    if (newEncryptionStatus != currentCryptStatus) {
      currentCryptStatus = newEncryptionStatus;

      if (currentCryptStatus == UNCHECKED_ENCRYPTED) {
	encryptionButton.setIcon(uncheckedEncryptedIcon);
      } else if (currentCryptStatus == DECRYPTED_SUCCESSFULLY) {
	encryptionButton.setIcon(decryptedSuccessfullyIcon);
      } else if (currentCryptStatus == DECRYPTED_UNSUCCESSFULLY) {
	encryptionButton.setIcon(decryptedUnsuccessfullyIcon);
      } else {
	encryptionButton.setIcon(notEncryptedIcon);
      }    
      repaint();
    }
  }

  /**
   * Updates the encryption information.
   */
  public void cryptoUpdated(net.suberic.pooka.MessageCryptoInfo cryptoInfo) {

    try {
      int sigStatus = NOT_SIGNED;
      int cryptStatus = NOT_ENCRYPTED;
      
      if (cryptoInfo.isSigned()) {
	if (cryptoInfo.hasCheckedSignature()) {
	  if (cryptoInfo.isSignatureValid()) {
	    sigStatus = SIGNATURE_VERIFIED;
	  } else {
	    sigStatus = SIGNATURE_BAD;
	  }
	} else {
	  sigStatus = UNCHECKED_SIGNED;
	}
      }

      if (cryptoInfo.isEncrypted()) {
	if (cryptoInfo.hasTriedDecryption()) {
	  if (cryptoInfo.isDecryptedSuccessfully()) {
	    cryptStatus = DECRYPTED_SUCCESSFULLY;
	  } else {
	    cryptStatus = DECRYPTED_UNSUCCESSFULLY;
	  }
	} else {
	  cryptStatus = UNCHECKED_ENCRYPTED;
	}
      }
      
      
      cryptoUpdated(sigStatus, cryptStatus);

    } catch (javax.mail.MessagingException me) {
      // ignore here.
    }
  }

  /**
   * This loads all of the icons for this button.
   */
  static void loadIcons(String key, Class thisClass, VariableBundle vars) {

    /*
     * this is going to have several images:
     * Unchecked Encrypted
     * Decrypted Successfully
     * Decrypted Unsuccessfully
     * Unchecked Signed
     * Signature verified
     * Signature bad
     * Signature failed verification
     * ...and maybe more.
     */

    try {
      java.net.URL url =thisClass.getResource(vars.getProperty(key + ".notEncrypted.Image"));
      if (url != null)
	notEncryptedIcon = new ImageIcon(url);
    } catch (java.util.MissingResourceException mre) {
      return;
    }

    try {
      java.net.URL url =thisClass.getResource(vars.getProperty(key + ".uncheckedEncrypted.Image"));
      if (url != null)
	uncheckedEncryptedIcon = new ImageIcon(url);
    } catch (java.util.MissingResourceException mre) {
      return;
    }
    
    try {
      java.net.URL url =thisClass.getResource(vars.getProperty(key + ".decryptedSuccessfully.Image"));
      if (url != null)
	decryptedSuccessfullyIcon = new ImageIcon(url);
    } catch (java.util.MissingResourceException mre) {
      return;
    }
    
    try {
      java.net.URL url =thisClass.getResource(vars.getProperty(key + ".decryptedUnsuccessfully.Image"));
      if (url != null)
	decryptedUnsuccessfullyIcon = new ImageIcon(url);
    } catch (java.util.MissingResourceException mre) {
      return;
    }
    
    try {
      java.net.URL url =thisClass.getResource(vars.getProperty(key + ".uncheckedSigned.Image"));
      if (url != null)
	uncheckedSignedIcon = new ImageIcon(url);
    } catch (java.util.MissingResourceException mre) {
      return;
    }
    
    try {
      java.net.URL url =thisClass.getResource(vars.getProperty(key + ".notSigned.Image"));
      if (url != null)
	notSignedIcon = new ImageIcon(url);
    } catch (java.util.MissingResourceException mre) {
      return;
    }
    
    try {
      java.net.URL url =thisClass.getResource(vars.getProperty(key + ".signatureVerified.Image"));
      if (url != null)
	signatureVerifiedIcon = new ImageIcon(url);
    } catch (java.util.MissingResourceException mre) {
      return;
    }
    
    try {
      java.net.URL url =thisClass.getResource(vars.getProperty(key + ".signatureBad.Image"));
      if (url != null)
	signatureBadIcon = new ImageIcon(url);
    } catch (java.util.MissingResourceException mre) {
      return;
    }
    
    try {
      java.net.URL url =thisClass.getResource(vars.getProperty(key + ".signatureFailedVerification.Image"));
      if (url != null)
	signatureFailedVerificationIcon = new ImageIcon(url);
    } catch (java.util.MissingResourceException mre) {
      return;
    }
    
  }

}
    
