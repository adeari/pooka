package net.suberic.pooka.resource;

import net.suberic.util.*;
import net.suberic.pooka.*;
import net.suberic.pooka.ssl.*;

import javax.activation.*;
import java.io.*;
import java.util.*;

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

  /**
   * Creates an output file which includes only resources that are appropriate
   * to a Diskless client.
   */
  public static void exportResources(File pOutputFile, boolean pIncludePasswords) throws IOException {
    VariableBundle sourceBundle = Pooka.getResources();
    Properties newWritableProperties = new Properties(sourceBundle.getWritableProperties());
    
    // first go through and edit out the inappropriate stores.
    
    List allStores = Pooka.getStoreManager().getStoreList();
    List toRemoveList = new ArrayList();

    Iterator iter = allStores.iterator();
    while (iter.hasNext()) {
      // if they're not imap, exclude them.  if they are imap, set them not
      // to cache.
      StoreInfo current = (StoreInfo) iter.next();

      if (current.getProtocol() != null && current.getProtocol().toLowerCase().startsWith("imap")) {
	newWritableProperties.setProperty(current.getStoreProperty() + ".cachingEnabled", "false");
      } else {
	toRemoveList.add(current.getStoreProperty());
      }
    }

    Enumeration names = newWritableProperties.propertyNames();
    while (names.hasMoreElements()) {
      String current = (String) names.nextElement();
      boolean keep = true;
      if (current.startsWith("Store")) {
	if ((! pIncludePasswords) && current.endsWith("password")) {
	  keep = false;
	}

	for (int i = 0; keep && i < toRemoveList.size(); i++) {
	  if (current.startsWith((String) toRemoveList.get(i))) {
	    keep = false;
	  }
	}
      }

      if (keep) {
	newWritableProperties.setProperty(current, sourceBundle.getProperty(current));
      }

    }

    newWritableProperties.setProperty("Pooka.useLocalFiles", "false");

    FileOutputStream outStream = new FileOutputStream(pOutputFile);
    
    newWritableProperties.store(outStream, null);
    
    outStream.close();
    
    
  }

  
}
