package net.suberic.pooka.crypto.test;

import net.suberic.pooka.*;
import net.suberic.pooka.crypto.*;
import net.suberic.pooka.crypto.cryptix.*;

import javax.mail.*;
import javax.mail.internet.*;

import java.io.*;

import cryptix.message.*;
import cryptix.openpgp.*;
import cryptix.pki.*;

public class CryptixPGPTest {

  public static void main(String[] argc) {
    
    try {
      File f = new File("encry.822");
      FileInputStream fis = new FileInputStream(f);
      Session s = Session.getDefaultInstance(System.getProperties());

      MimeMessage encryptedMessage = new MimeMessage(s, fis);

      EncryptionUtils cryptoUtils = new PGPMimeEncryptionUtils();
      
      java.security.Security.addProvider(new cryptix.jce.provider.CryptixCrypto() );
      java.security.Security.addProvider(new cryptix.openpgp.provider.CryptixOpenPGP() );

      /*
      KeyBundle bundle = null;
      MessageFactory mf = null;
      
      FileInputStream in = new FileInputStream("/home/allen/keys.asc");
      
      mf = MessageFactory.getInstance("OpenPGP");
      java.util.Collection msgs = mf.generateMessages(in);
      
      KeyBundleMessage kbm = (KeyBundleMessage)msgs.iterator().next();
      
      bundle = kbm.getKeyBundle();
      
      in.close();
      */
      
      KeyBundle bundle = null;
      
      ExtendedKeyStore keyStore = (ExtendedKeyStore)
                ExtendedKeyStore.getInstance("OpenPGP/KeyRing");
      char[] keyStorePassphrase = new char[] { 'b', 'i', 't', 'e', 'm', 'e' };

      keyStore.load(new FileInputStream(new File("/home/allen/.gnupg/secring.gpg")), null);

      System.out.println("bundle = " + bundle);
      bundle = keyStore.getKeyBundle("Allen Petersen (test key)");

      String passphrase = "biteme";

      char[] phraseArray = new char[6];
      passphrase.getChars(0, 5, phraseArray, 0);

      EncryptionKey key = new CryptixPGPEncryptionKey(bundle, phraseArray);

      MimeMessage decryptedMessage = cryptoUtils.decryptMessage(s, encryptedMessage, key);

      decryptedMessage.writeTo(System.out);
      
    } catch (Exception e) {
      System.out.println("caught exception:  " + e.getMessage());
      e.printStackTrace();
    }
    
  }

}
