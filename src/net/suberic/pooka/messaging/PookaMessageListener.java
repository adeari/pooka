package net.suberic.pooka.messaging;

import java.net.*;
import java.nio.channels.*;

import java.io.*;

import javax.mail.*;

import net.suberic.pooka.*;

/**
 * This class listens on a socket for messages from other Pooka clients.
 */
public class PookaMessageListener extends Thread {
  
  ServerSocket mSocket = null;
  boolean mStopped = false;
  
  /**
   * Creates a new PookaMessageListener.
   */
  public PookaMessageListener() {
    System.err.println("creating new PookaMessageListener.");
    start();
  }

  /**
   * Opens the socket and listens to it.
   */
  public void run() {
    try {
      createSocket();
      while (! mStopped) {
	Socket currentSocket = mSocket.accept();
	System.err.println("got connection.");
	BufferedReader reader = new BufferedReader(new InputStreamReader(currentSocket.getInputStream()));
	handleMessage(reader.readLine());
	currentSocket.close();
	System.err.println("closing socket.");
      }
    } catch (Exception e) {
      System.out.println("error in MessagingListener.");
      e.printStackTrace();
    }
  }


  /**
   * Creats the socket to listen to.
   */
  public void createSocket() throws Exception {
    System.err.println("creating new PookaMessageListener socket.");
    mSocket = new ServerSocket(PookaMessagingConstants.S_PORT);
    mSocket.accept();
  }

  /**
   * Handles the received message.
   */
  public void handleMessage(String pMessage) {
    System.out.println("handling message:  '" + pMessage + "'.");
    /*
    if (pMessage.startsWith(PookaMessagingConstants.S_NEW_MESSAGE)) {
      // see if there's an address to send to.
      String address = null;
      if (pMessage.length() > PookaMessagingConstants.S_NEW_MESSAGE.length()) {
	address = pMessage.substring(PookaMessagingConstants.S_NEW_MESSAGE.length() + 1);
      }
    }
    */
  }
  
  /**
   * Sends a message.
   */
  public void sendMessage(String pAddress, UserProfile pProfile) {
          
    final String fAddress = pAddress;
    final UserProfile fProfile = pProfile;

    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      public void run() {
	try {
	  javax.mail.internet.MimeMessage mm = new javax.mail.internet.MimeMessage(Pooka.getMainPanel().getSession());
	  mm.setRecipients(javax.mail.Message.RecipientType.TO, fAddress);
	  
	  NewMessageInfo info = new NewMessageInfo(mm);
	  net.suberic.pooka.gui.NewMessageProxy proxy = new net.suberic.pooka.gui.NewMessageProxy(info);
	  
	  net.suberic.pooka.gui.MessageUI nmu = Pooka.getUIFactory().createMessageUI(proxy);
	  nmu.openMessageUI();
	} catch (MessagingException me) {
	  Pooka.getUIFactory().showError(Pooka.getProperty("error.NewMessage.errorLoadingMessage", "Error creating new message:  ") + "\n" + me.getMessage(), Pooka.getProperty("error.NewMessage.errorLoadingMessage.title", "Error creating new message."), me);
	}
      }
      });

    
  }

}
