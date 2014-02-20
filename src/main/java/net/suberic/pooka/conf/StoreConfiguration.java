package net.suberic.pooka.conf;

import java.util.List;

/**
 * Configures a StoreInfo.
 */
public interface StoreConfiguration {

  /*
  public String getFoo();
  public void setFoo(String foo);

  public boolean getFoo();
  public void setFoo(boolean foo);

  public int getFoo();
  public void setFoo(int foo);
  */
  public boolean getUseMaildir();
  public void setUseMaildir(boolean useMaildir);

  public String getUser();
  public void setUser(String user);

  public String getPassword();
  public void setPassword(String password);

  public String getProtocol();
  public void setProtocol(String protocol);

  public int getPort();
  public void setPort(int port);

  public String getServer();
  public void setServer(String server);

  public String getSSL();
  public void setSSL(String ssl);

  public List<String> getFolderList();
  public void setFolderList(List<String> folderList);

  public boolean getUseSubscribed();
  public void setUseSubscribed(boolean useSubscribed);

  public String getDefaultProfile();
  public void setDefaultProfile(String defaultProfile);

  public String getTrashFolder();
  public void setTrashFolder(String trashFolder);

  public String getConnectionTimeout();
  public void setConnectionTimeout(String connectionTimeout);

  public String getTimeout();
  public void setTimeout(String timeout);

  public String getSSLFallback();
  public void setSSLFallback(String ssl);

  public String getMailDir();
  public void setMailDir(String mailDir);

  public String getDefaultMailSubDir();
  public void setDefaultMailSubDir(String defaultMailSubDir);

  public String getSubFolderName();
  public void setSubFolderName(String SubFolderName);

  public String getInboxLocation();
  public void setInboxLocation(String inboxLocation);

  public String getInboxFileName();
  public void setInboxFileName(String inboxFileName);

  public boolean getOpenFoldersOnConnect();
  public void setOpenFoldersOnConnect(boolean openFoldersOnConnect);

  public boolean getOpenFoldersInBackground();
  public void setOpenFoldersInBackground(boolean openFoldersInBackground);

  public boolean getUseTrashFolder();
  public void setUseTrashFolder(boolean useTrashFolder);

  public boolean getSessionDebug();
  public void setSessionDebug(boolean sessionDebug);

  public String getSessionDebugLogLevel();
  public void setSessionDebugLogLevel(String sessionDebugLogLevel);


}
