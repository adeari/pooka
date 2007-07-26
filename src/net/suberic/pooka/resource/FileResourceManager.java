package net.suberic.pooka.resource;

import net.suberic.util.*;
import net.suberic.pooka.ssl.*;
import net.suberic.pooka.*;
import javax.activation.*;

import java.net.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A ResourceManager which uses files.
 */
public class FileResourceManager extends ResourceManager {

  //static Pattern sVariablePattern = Pattern.compile("\\$\\{[^\\\\$]}");
  static Pattern sRootDirPattern = Pattern.compile("\\$\\{pooka\\.root\\}");

  /**
   * Creates a VariableBundle to be used.
   */
  public VariableBundle createVariableBundle(String fileName, VariableBundle defaults) {
    try {
      java.io.File f = new java.io.File(fileName);
      if (! f.exists())
        f.createNewFile();
      return new net.suberic.util.FileVariableBundle(f, defaults);
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
    String translatedFile = translateName(pFileName);
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
      if (Pooka.getProperty(storeProperty + ".cachingEnabled", Pooka.getProperty(storeProperty + "." + pName + ".cachingEnabled", "false")).equalsIgnoreCase("true") || Pooka.getProperty(storeProperty + ".cacheHeadersOnly", Pooka.getProperty(storeProperty + "." + pName + ".cacheHeadersOnly", "false")).equalsIgnoreCase("true")) {
        return new net.suberic.pooka.cache.CachingFolderInfo(pStore, pName);
      } else {
        return  new UIDFolderInfo(pStore, pName);
      }
    } else {
      return new FolderInfo(pStore, pName);
    }
  }

  /**
   * Translates the given file path.
   */
  public static String translateName(String pFileName) {
    Matcher matcher = sRootDirPattern.matcher(pFileName);
    return matcher.replaceAll(Matcher.quoteReplacement(Pooka.getPookaManager().getPookaRoot().getAbsolutePath()));
  }

}


