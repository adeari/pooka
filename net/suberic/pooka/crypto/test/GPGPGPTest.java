package net.suberic.pooka.crypto.test;

import net.suberic.pooka.*;
import net.suberic.pooka.crypto.*;
import net.suberic.pooka.crypto.gpg.*;

import javax.mail.*;
import javax.mail.internet.*;

import java.io.*;

public class GPGPGPTest {

  public static void main(String[] argc) {
    
    try {
      File f = new File("encry.822");
      FileInputStream fis = new FileInputStream(f);
      Session s = Session.getDefaultInstance(System.getProperties());

      MimeMessage encryptedMessage = new MimeMessage(s, fis);

      PGPMimeEncryptionUtils cryptoUtils = new PGPMimeEncryptionUtils();
      
      cryptoUtils.setPGPProviderImpl(new GPGPGPProviderImpl());

      EncryptionKey key = new GPGEncryptionKey("allen", "biteme");

      MimeMessage decryptedMessage = cryptoUtils.decryptMessage(s, encryptedMessage, key);

      decryptedMessage.writeTo(System.out);
      
    } catch (Exception e) {
      System.out.println("caught exception:  " + e.getMessage());
      e.printStackTrace();
    }
    
  }

}
