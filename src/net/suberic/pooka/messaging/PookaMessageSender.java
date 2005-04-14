package net.suberic.pooka.messaging;

import java.net.*;
import java.nio.channels.*;
import java.io.*;

import net.suberic.pooka.Pooka;

/**
 * This class sends messages to a Pooka network client.
 */
public class PookaMessageSender {
  
  /** the socket that's connected to a PookaMessageListener. */
  Socket mSocket = null;

  /** whether or not we have a connection open. */
  boolean mConnected = false;

  /**
   * This opens a connection to the server port of the running Pooka
   * instance.
   */
  public void openConnection() throws java.net.UnknownHostException,
				      java.io.IOException, 
				      SecurityException {
    int port; 
    try {
      port = Integer.parseInt(Pooka.getProperty("Pooka.messaging.port", ""));
    } catch (Exception e) {
      port = PookaMessagingConstants.S_PORT;
    }
    mSocket = new Socket("localhost",port);

    mConnected = true;
  }

  /**
   * Sends a new message message to the server.
   */
  public void openNewEmail(String pAddress, String pUserProfile) throws java.io.IOException {
    StringBuffer sendBuffer = new StringBuffer();
    sendBuffer.append(PookaMessagingConstants.S_NEW_MESSAGE);
    if (pAddress != null && pAddress.length() > 0) {
      sendBuffer.append(" ");
      sendBuffer.append(pAddress);
      if (pUserProfile != null && pUserProfile.length() > 0) {
	sendBuffer.append(" ");
	sendBuffer.append(pUserProfile);
      }
    }

    sendMessage(sendBuffer.toString());
  }

  /**
   * Checks the version running on the server with this client to make
   * sure they're both on the same page.
   */
  public boolean checkVersion() throws java.io.IOException {
    sendMessage(PookaMessagingConstants.S_CHECK_VERSION);

    String response = retrieveResponse();
    System.err.println("got response " + response);
    
    return (response != null && response.equals(Pooka.getPookaManager().getLocalrc()));
  }

  /**
   * Starts an instance of Pooka.
   */
  public void sendStartPookaMessage() throws java.io.IOException {
    sendMessage(PookaMessagingConstants.S_START_POOKA);
  }

  /**
   * Closes the connection.
   */
  public void closeConnection() {
    if (mConnected || mSocket != null) {
      try {
	sendMessage(PookaMessagingConstants.S_BYE);
      } catch (java.io.IOException ioe) {
	// ignore -- we're closing anyway.
      } finally {
	mSocket = null;
	mConnected = false;
      }
    }
  }
  
  /**
   * Sends a message.
   */
  public void sendMessage(String pMessage) throws java.io.IOException {
    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(mSocket.getOutputStream()));
    System.err.println("sending message '" + pMessage);
    writer.write(pMessage);
    writer.newLine();
    writer.flush();
  }
      
  /**
   * Gets a response from the (already open) connection.
   */
  public String retrieveResponse() throws java.io.IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
    return reader.readLine();
  }

  /**
   * Returns if this is connected or not.
   */
  public boolean isConnected() {
    return mConnected;
  }
}
