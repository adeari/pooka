package net.suberic.pooka.resource;

import net.suberic.util.*;
import net.suberic.pooka.ssl.*;
import net.suberic.pooka.*;
import javax.activation.*;

/**
 * A ResourceManager which uses files.
 */
public class FileResourceManager extends ResourceManager {

  /**
   * Creates a VariableBundle to be used.
   */
  public VariableBundle createVariableBundle(String fileName, VariableBundle defaults) {
    try {
      return new net.suberic.util.VariableBundle(new java.io.File(fileName), defaults);
    } catch (java.io.IOException ioe) {
      //new net.suberic.util.VariableBundle(url.openStream(), "net.suberic.pooka.Pooka");
      return defaults;
    }
    
  }

  /**
   * Creates a MailcapCommandMap to be used.
   */
  public MailcapCommandMap createMailcap(String fileName) throws java.io.IOException {
    return new FullMailcapCommandMap(fileName);
  }

  /**
   * Creates a PookaTrustManager.
   */
  public PookaTrustManager createPookaTrustManager(javax.net.ssl.TrustManager[] pTrustManagers, String fileName) {
    return new PookaTrustManager(pTrustManagers, fileName);
  }

  
}
