package net.suberic.pooka.crypto.test;

import net.suberic.pooka.*;
import net.suberic.pooka.crypto.*;
import net.suberic.pooka.crypto.bouncy.*;

import javax.mail.*;
import javax.mail.internet.*;

import java.io.*;
import java.util.*;

public class BouncySignTest {

  public static void main(String[] argc) {

    try {
      Properties prop = new Properties();
      //prop.setProperty("mail.debug", "true");
      Session s = Session.getDefaultInstance(prop);

      MimeMessage rawMessage = new MimeMessage(s);
      rawMessage.setText("This is a signed message.  Really!  I am writing this message here.\n\nBeware of the leopard.\n\nIsn't it fun?\n\n\n-allen\n");
      rawMessage.setSubject("Test again (smime signed)");
      rawMessage.setFrom(new InternetAddress("allen@localhost"));
      rawMessage.setRecipient(Message.RecipientType.TO, new InternetAddress("allen@localhost"));

      EncryptionUtils cryptoUtils = new SMIMEEncryptionUtils();
      
      char[]   passwd = { 'h', 'e', 'l', 'l', 'o', ' ', 'w', 'o', 'r', 'l', 'd' };

      EncryptionKeyManager keyMgr = cryptoUtils.createKeyManager(new FileInputStream(new File("/home/allen/ssl/akp/id.p12")), passwd);

      Set aliases = keyMgr.privateKeyAliases();

      Iterator it = aliases.iterator();
      String firstAlias = (String)it.next();
      EncryptionKey key = keyMgr.getPrivateKey(firstAlias, passwd);

      Message signedMessage = cryptoUtils.signMessage(s, rawMessage, key);

      signedMessage.writeTo(System.out);
      signedMessage.setSentDate(new java.util.Date(System.currentTimeMillis()));
      Transport t = s.getTransport(new URLName("smtp://localhost"));
      t.connect();
      t.sendMessage(signedMessage, new InternetAddress[] { new InternetAddress("allen@localhost")});

      System.out.println("message sent!");
      
    } catch (Exception e) {
      System.out.println("caught exception:  " + e.getMessage());
      e.printStackTrace();
    }
    
  }

}
