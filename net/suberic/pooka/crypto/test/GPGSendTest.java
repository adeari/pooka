package net.suberic.pooka.crypto.test;

import net.suberic.pooka.*;
import net.suberic.pooka.crypto.*;
import net.suberic.pooka.crypto.gpg.*;

import javax.mail.*;
import javax.mail.internet.*;

import java.io.*;

public class GPGSendTest {

  public static void main(String[] argc) {

    try {
      Session s = Session.getDefaultInstance(System.getProperties());

      MimeMessage rawMessage = new MimeMessage(s);
      rawMessage.setText("This is an encrypted message.  Really!");
      rawMessage.setSubject("Hi");
      rawMessage.setRecipient(Message.RecipientType.TO, new InternetAddress("allen@localhost"));

      PGPMimeEncryptionUtils cryptoUtils = new PGPMimeEncryptionUtils();
      
      cryptoUtils.setPGPProviderImpl(new GPGPGPProviderImpl());

      EncryptionKey key = new GPGEncryptionKey("allen", "biteme");

      Message encryptedMessage = cryptoUtils.encryptMessage(s, rawMessage, key);

      encryptedMessage.writeTo(System.out);
      Transport.send(encryptedMessage);

      System.out.println("message sent!");
      
    } catch (Exception e) {
      System.out.println("caught exception:  " + e.getMessage());
      e.printStackTrace();
    }
    
  }

}
