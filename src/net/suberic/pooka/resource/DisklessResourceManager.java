package net.suberic.pooka.resource;

import net.suberic.util.*;
import net.suberic.pooka.*;
import net.suberic.pooka.ssl.*;
import javax.activation.*;

/**
 * A PookaResourceManager which uses no files.
 */
public class DisklessResourceManager extends ResourceManager {

  /**
   * Creates a VariableBundle to be used.
   */
  public VariableBundle createVariableBundle(String fileName, VariableBundle defaults) {
    return defaults;
  }

  /**
   * Creates a MailcapCommandMap to be used.
   */
  public MailcapCommandMap createMailcap(String fileName) {
    return new FullMailcapCommandMap();
  }

  /**
   * Creates a PookaTrustManager.
   */
  public PookaTrustManager createPookaTrustManager(javax.net.ssl.TrustManager[] pTrustManagers, String fileName) {
    return new PookaTrustManager(pTrustManagers, null);
  }

  
}
