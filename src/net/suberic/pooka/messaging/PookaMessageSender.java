package net.suberic.pooka.messaging;

import java.net.*;
import java.nio.channels.*;
import java.io.*;

/**
 * This class sends messages to a Pooka network client.
 */
public class PookaMessageSender {
  
  Socket mSocket = null;

  /**
   * This opens a connection to the server port of the running Pooka
   * instance.
   */
  public void openConnection() throws java.net.UnknownHostException,
				      java.io.IOException, 
				      SecurityException {
    mSocket = new Socket("localhost", PookaMessagingConstants.S_PORT);
  }

  /**
   * Sends a new message message to the server.
   */
  public void sendNewMessage(String pAddress, String pUserProfile) throws java.io.IOException {
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

    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(mSocket.getOutputStream()));
    System.err.println("sending message '" + sendBuffer.toString());
    writer.write(sendBuffer.toString());
    writer.newLine();
    writer.flush();
  }
  
}
