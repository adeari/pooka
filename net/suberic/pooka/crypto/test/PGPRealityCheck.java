package net.suberic.pooka.crypto;

import net.suberic.pooka.*;

import javax.mail.*;
import javax.mail.internet.*;

import java.io.*;

import cryptix.message.*;
import cryptix.openpgp.*;
import cryptix.pki.*;

public class PGPRealityCheck {

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

      bundle = keyStore.getKeyBundle("Allen Petersen (test key)");

      String passphrase = "biteme";

      char[] phraseArray = new char[6];
      passphrase.getChars(0, 5, phraseArray, 0);

      cryptix.message.Message msg = null;

      // write an encrypted message.
      String msgString = "This is a test message.\n" +
	"This is another line.\n";
      LiteralMessageBuilder lmb =
	LiteralMessageBuilder.getInstance("OpenPGP");
      lmb.init(msgString);
      LiteralMessage litmsg = (LiteralMessage)lmb.build();


      EncryptedMessageBuilder emb =
	EncryptedMessageBuilder.getInstance("OpenPGP");
      emb.init(litmsg);
      emb.addRecipient(bundle);
      msg = emb.build();
      
      PGPArmouredMessage armoured;
      
      armoured = new PGPArmouredMessage(msg);
      FileOutputStream out = new FileOutputStream("encrypted-akp.asc");
      out.write(armoured.getEncoded());
      out.close();
      

    } catch (Exception e) {
      System.out.println("caught exception:  " + e.getMessage());
      e.printStackTrace();
    }
    
  }

}
