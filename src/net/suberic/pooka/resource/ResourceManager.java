package net.suberic.pooka.resource;

import net.suberic.util.*;
import net.suberic.pooka.ssl.*;
import net.suberic.pooka.*;
import javax.activation.*;

/**
 * This interface defines a ResourceManager.
 */
public abstract class ResourceManager {

  /**
   * Creates a VariableBundle to be used.
   */
  public abstract VariableBundle createVariableBundle(String fileName, VariableBundle defaults);

  /**
   * Creates a MailcapCommandMap to be used.
   */
  public abstract MailcapCommandMap createMailcap(String fileName) throws java.io.IOException;

  /**
   * Creates a PookaTrustManager.
   */
  public abstract PookaTrustManager createPookaTrustManager(javax.net.ssl.TrustManager[] pTrustManagers, String fileName);

  /**
   * Gets a resource for reading.  pFileName could be a URL or a file name
   * or some similar identifier that the ResourceManager can use.
   */
  public abstract java.io.InputStream getInputStream(String pFileName) 
    throws java.io.IOException;

  /**
   * Gets a resource for writing.  pFileName could be a URL or a file name
   * or some similar identifier that the ResourceManager can use.
   */
  public abstract java.io.OutputStream getOutputStream(String pFileName) 
    throws java.io.IOException;

}
