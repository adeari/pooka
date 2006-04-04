package net.suberic.pooka.resource;

import net.suberic.util.*;
import net.suberic.pooka.ssl.*;
import net.suberic.pooka.*;
import javax.activation.*;

import java.net.*;
import java.io.*;

/**
 * A ResourceManager which uses files.
 */
public class FileResourceManager extends ResourceManager {

  /**
   * Creates a VariableBundle to be used.
   */
  public VariableBundle createVariableBundle(String fileName, VariableBundle defaults) {
    try {
      java.io.File f = new java.io.File(fileName);
      if (! f.exists())
	f.createNewFile();
      return new net.suberic.util.VariableBundle(f, defaults);
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

  public java.io.InputStream getInputStream(String pFileName) 
    throws java.io.IOException {
    try {
      URL url = new URL(pFileName);
      return url.openStream();
    } catch (MalformedURLException mue) {
      return new FileInputStream(new File(pFileName));
    }
  }
  

  public java.io.OutputStream getOutputStream(String pFileName) 
    throws java.io.IOException {
    return new FileOutputStream(new File(pFileName));
  }

  /**
   * Creates an appropriate FolderInfo for the given StoreInfo.  
   */
  public FolderInfo createFolderInfo(StoreInfo pStore, String pName) {
    String storeProperty = pStore.getStoreProperty();
    if (pStore.isPopStore() && pName.equalsIgnoreCase("INBOX")) {
      return new PopInboxFolderInfo(pStore, pName);
    } else if (Pooka.getProperty(storeProperty + ".protocol", "mbox").equalsIgnoreCase("imap")) {
      return  new UIDFolderInfo(pStore, pName);
    } else {
      return new FolderInfo(pStore, pName);
    }
  }

}


