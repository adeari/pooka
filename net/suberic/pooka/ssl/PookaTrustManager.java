package net.suberic.pooka.ssl;

import java.security.cert.X509Certificate;
import com.sun.net.ssl.*;


/**
 * This wraps the default X509TrustManager so that we can handle untrusted
 * certificate chains.
 */
public class PookaTrustManager implements X509TrustManager {

  X509TrustManager wrappedManager = null;

  /**
   * Creates a new TrustManager that wraps the given manager.
   */
  public PookaTrustManager(TrustManager[] newWrappedManagers) {
    for (int i = 0; i < newWrappedManagers.length; i++) {
      if (newWrappedManagers[i] instanceof X509TrustManager)
	wrappedManager = (X509TrustManager) newWrappedManagers[i];
    }
  }
  
  /**
   * Returns whether or not the client with the given certificates is
   * trusted or not.
   */
  public boolean isClientTrusted(X509Certificate[] cert) {
    if (wrappedManager != null) {
      boolean defaultResponse = wrappedManager.isClientTrusted(cert);
      if (defaultResponse)
	return defaultResponse;
      else {
	// if this isn't acceptable by default, ask.
	return askIsTrusted(cert);
      }
    } else
      return askIsTrusted(cert);
  }

  /**
   * Returns whether or not the server with the given certificates is
   * trusted or not.
   */
  public boolean isServerTrusted(X509Certificate[] cert) {
    if (wrappedManager != null) {
      boolean defaultResponse = wrappedManager.isServerTrusted(cert);
      if (defaultResponse)
	return defaultResponse;
      else {
	// if this isn't acceptable by default, ask.
	return askIsTrusted(cert);
      }
    } else
      return askIsTrusted(cert);
  }

  /**
   * Return an array of certificate authority certificates
   * which are trusted for authenticating peers.
   */
  public X509Certificate[] getAcceptedIssuers() {
    return wrappedManager.getAcceptedIssuers();
  }

  /**
   * Interactively figures out whether or not we want to trust this
   * (default untrusted) certificate.
   */
  public boolean askIsTrusted(X509Certificate[] cert) {
    X509Certificate certToPrint = null;
    for (int i = 0; i < cert.length && certToPrint == null; i++) {
      if (cert[i] != null)
	certToPrint = cert[i];
    }

    int response = -1;
    if (certToPrint != null) {
      StringBuffer msg = new StringBuffer("The following certificate(s) are not trusted.  Accpet them anyway?\n\n");
      msg.append("Issuer:  ");
      msg.append(certToPrint.getIssuerDN().getName());
      msg.append("\n");
      response = net.suberic.pooka.Pooka.getUIFactory().showConfirmDialog(msg.toString(), "Accpet SSL certificate?", javax.swing.JOptionPane.YES_NO_OPTION);
    } else {
      response = net.suberic.pooka.Pooka.getUIFactory().showConfirmDialog("The certificate(s) for this server are not trusted.  Accpet them anyway?", "Accpet SSL certificate?", javax.swing.JOptionPane.YES_NO_OPTION);
    }

    if (response == javax.swing.JOptionPane.YES_OPTION)
      return true;
    else
      return false;
  }
}

