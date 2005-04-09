package net.suberic.pooka.messaging;

import java.net.*;
import java.nio.channels.*;

import java.io.*;

import javax.mail.*;
import javax.mail.internet.MimeMessage;

import net.suberic.pooka.*;
import net.suberic.pooka.gui.NewMessageProxy;
import net.suberic.pooka.gui.NewMessageFrame;
import net.suberic.pooka.gui.MessageUI;

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
      System.err.println("creating socket.");
      createSocket();
      System.err.println("socket created.");
      while (! mStopped) {
	System.err.println("accepting connection.");
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
    Exception throwMe = null;
    boolean success = false;
    for (int port = PookaMessagingConstants.S_PORT; (! success) && port <  PookaMessagingConstants.S_PORT + 25; port++) {
      try {
	mSocket = new ServerSocket(PookaMessagingConstants.S_PORT);
	success = true;
	Pooka.setProperty("Pooka.messaging.port", Integer.toString(port));
      } catch (Exception e) {
	throwMe = e;
      }
    }

    if (! success)
      throw throwMe;
  }

  /**
   * Handles the received message.
   */
  public void handleMessage(String pMessage) {
    System.err.println("handling message:  '" + pMessage + "'.");

    if (pMessage != null && pMessage.startsWith(PookaMessagingConstants.S_NEW_MESSAGE)) {
      System.err.println("it's a new message command.");
      // see if there's an address to send to.
      String address = null;
      UserProfile profile = null;
      if (pMessage.length() > PookaMessagingConstants.S_NEW_MESSAGE.length()) {
	// go to the next space
	int toAddressEnd = pMessage.indexOf(' ', PookaMessagingConstants.S_NEW_MESSAGE.length() + 1);
	if (toAddressEnd == -1)
	  toAddressEnd = pMessage.length();

	address = pMessage.substring(PookaMessagingConstants.S_NEW_MESSAGE.length() + 1, toAddressEnd);
	if (toAddressEnd != pMessage.length() && toAddressEnd != pMessage.length() + 1) {
	  String profileString = pMessage.substring(toAddressEnd + 1);
	  profile = UserProfile.getProfile(profileString);
	}
      }
      sendMessage(address, profile);
    }

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
	  System.err.println("creating new message.");
	  // create the template first.  this is done so the new message
	  // opens as a top-level window.
	  NewMessageFrame template = new NewMessageFrame(new NewMessageProxy(new NewMessageInfo(new MimeMessage(Pooka.getDefaultSession()))));

	  MimeMessage mm = new MimeMessage(Pooka.getDefaultSession());
	  mm.setRecipients(Message.RecipientType.TO, fAddress);
	  
	  NewMessageInfo info = new NewMessageInfo(mm);
	  if (fProfile != null)
	    info.setDefaultProfile(fProfile);

	  NewMessageProxy proxy = new NewMessageProxy(info);
	  
	  MessageUI nmu = Pooka.getUIFactory().createMessageUI(proxy, template);
	  nmu.openMessageUI();
	} catch (MessagingException me) {
	  Pooka.getUIFactory().showError(Pooka.getProperty("error.NewMessage.errorLoadingMessage", "Error creating new message:  ") + "\n" + me.getMessage(), Pooka.getProperty("error.NewMessage.errorLoadingMessage.title", "Error creating new message."), me);
	}
      }
      });

    
  }

}
