package net.suberic.pooka.ssl;

import java.io.*;
import java.net.*;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import java.security.KeyStore;

import com.sun.net.ssl.*;

import net.suberic.pooka.Pooka;

/**
 * An SSLSocketFactory that uses the PookaTrustManager in order to 
 * allow users to manually choose to accpet otherwise untrusted certificates.
 */
public class PookaSSLSocketFactory extends SSLSocketFactory {

  SSLSocketFactory wrappedFactory = null;

  /**
   * Creates a PookaSSLSocketFactory.
   */
  public PookaSSLSocketFactory() {
    if (Pooka.isDebug()) {
      System.out.println("PookaSSLSocketFactory created.");
    }

    try {

      SSLContext sslc = SSLContext.getInstance("TLS");

      KeyStore defaultKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
      // load the KeyStore.
      String java_home = System.getProperty("java.home");
      String library_file = java_home + File.separator + "lib" + File.separator + "security" + File.separator + "cacerts"; 
      String passwd = "changeit";
      
      defaultKeyStore.load(new FileInputStream(library_file), passwd.toCharArray());

      KeyManager[] keyManagers = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm()).getKeyManagers();

      TrustManagerFactory tmFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      tmFactory.init(defaultKeyStore);

      TrustManager[] trustManagers = tmFactory.getTrustManagers();

      String fileName = Pooka.getProperty("Pooka.sslCertFile", "");
      TrustManager[] pookaTrustManagers = new TrustManager[] {
	new PookaTrustManager(trustManagers, fileName)
      };

      sslc.init(keyManagers, pookaTrustManagers, new java.security.SecureRandom());
      wrappedFactory = (SSLSocketFactory) sslc.getSocketFactory();

    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Gets a default PookaSSLSocketFactory.
   */
  public static SocketFactory getDefault() {
    return new PookaSSLSocketFactory();
  }
  
  /**
   * Creates an SSL Socket.
   */
  public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
    if (Pooka.isDebug())
      System.out.println("PookaSSLSocketFactory:  create socket.");
    return wrappedFactory.createSocket(s, host, port, autoClose);
  }
  
  /**
   * Creates an SSL Socket.
   */
  public Socket createSocket(InetAddress host, int port) throws IOException {
    if (Pooka.isDebug())
      System.out.println("PookaSSLSocketFactory:  create socket.");
    return wrappedFactory.createSocket(host, port);
  }
  
  /**
   * Creates an SSL Socket.
   */
  public Socket createSocket(InetAddress address, int port, InetAddress clientAddress, int clientPort) throws IOException {
    if (Pooka.isDebug())
      System.out.println("PookaSSLSocketFactory:  create socket.");
    return wrappedFactory.createSocket(address, port, clientAddress, clientPort);
  }
  
  /**
   * Creates an SSL Socket.
   */
  public Socket createSocket(String host, int port) throws IOException {
    if (Pooka.isDebug())
      System.out.println("PookaSSLSocketFactory:  create socket.");
    return wrappedFactory.createSocket(host, port);
  }
  
  /**
   * Creates an SSL Socket.
   */
  public Socket createSocket(String host, int port, InetAddress clientHost, int clientPort) throws IOException {
    if (Pooka.isDebug())
      System.out.println("PookaSSLSocketFactory:  create socket.");
    return wrappedFactory.createSocket(host, port, clientHost, clientPort);
  }
  
  /**
   * Retuns the default cipher suites.
   */
  public String[] getDefaultCipherSuites() {
    return wrappedFactory.getSupportedCipherSuites();
  }

  /**
   * Retuns the supported cipher suites.
   */
  public String[] getSupportedCipherSuites() {
    return wrappedFactory.getSupportedCipherSuites();
  }
}

