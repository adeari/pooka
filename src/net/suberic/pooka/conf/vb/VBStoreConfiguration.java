package net.suberic.pooka.conf.vb;

import net.suberic.pooka.conf.StoreConfiguration;
import net.suberic.util.*;

import java.util.List;

/**
 * Configures a StoreInfo.
 */
public class VBStoreConfiguration extends VBConfiguration implements StoreConfiguration {
  /*
  @Override
  public String getFoo() {
    return getString(getStoreProperty() + ".foo", "");
  }
  @Override
  public void setFoo(String foo) {
    setString(getStoreProperty() + ".foo", foo);
  }

  @Override
  public boolean getFoo() {
    return getBoolean(getStoreProperty() + ".foo", false);
  }
  @Override
  public void setFoo(boolean foo) {
    setBoolean(getStoreProperty() + ".foo", foo);
  }

  @Override
  public int getFoo() {
    return getInt(getStoreProperty() + ".foo", -1);
  }
  @Override
  public void setFoo(int foo) {
    setInt(getStoreProperty() + ".foo", foo);
  }

  @Override
  public List<String> getFoo() {
    return getList(getStoreProperty() + ".foo", "");
  }
  @Override
  public void setFoo(List<String> foo) {
    setList(getStoreProperty() + ".foo", foo);
  }
  */

  // *****************************************8

  public VBStoreConfiguration(String storeId, VariableBundle vb) {
    setStoreId(storeId);
    setVariableBundle(vb);
  }

  String storeId;
  public String getStoreId() {
    return storeId;
  }
  public void setStoreId(String storeId) {
    this.storeId = storeId;
  }

  /**
   * Returns the StoreProperty for this Store
   */
  public String getStoreProperty() {
    return "Store." + getStoreId();
  }

  @Override
  public boolean getUseMaildir() {
    return getBoolean("useMaildir", false);
  }
  @Override
  public void setUseMaildir(boolean useMaildir) {
    setBoolean("useMaildir", useMaildir);
  }

  @Override
  public String getUser() {
    return getString(getStoreProperty() + ".user", "");
  }
  @Override
  public void setUser(String user) {
    setString(getStoreProperty() + ".user", user);
  }

  @Override
  public String getPassword() {
    return getString(getStoreProperty() + ".password", "");
  }
  @Override
  public void setPassword(String password) {
    setString(getStoreProperty() + ".password", password);
  }

  @Override
  public String getProtocol() {
    return getString(getStoreProperty() + ".protocol", "");
  }
  @Override
  public void setProtocol(String protocol) {
    setString(getStoreProperty() + ".protocol", protocol);
  }

  @Override
  public int getPort() {
    return getInt(getStoreProperty() + ".port", -1);
  }
  @Override
  public void setPort(int port) {
    setInt(getStoreProperty() + ".port", port);
  }

  @Override
  public String getServer() {
    return getString(getStoreProperty() + ".server", "");
  }
  @Override
  public void setServer(String server) {
    setString(getStoreProperty() + ".server", server);
  }

  @Override
  public String getSSL() {
    return getString(getStoreProperty() + ".SSL", "none");
  }
  @Override
  public void setSSL(String sslSetting) {
    setString(getStoreProperty() + ".SSL", sslSetting);
  }

  @Override
  public List<String> getFolderList() {
    return getList(getStoreProperty() + ".folderList", "");
  }
  @Override
  public void setFolderList(List<String> folderList) {
    setList(getStoreProperty() + ".folderList", folderList);
  }

  @Override
  public boolean getUseSubscribed() {
    return getBoolean(getStoreProperty() + ".useSubscribed", false);
  }
  @Override
  public void setUseSubscribed(boolean useSubscribed) {
    setBoolean(getStoreProperty() + ".useSubscribed", useSubscribed);
  }

  @Override
  public String getDefaultProfile() {
    return getString(getStoreProperty() + ".defaultProfile", "");
  }
  @Override
  public void setDefaultProfile(String defaultProfile) {
    setString(getStoreProperty() + ".defaultProfile", defaultProfile);
  }

  @Override
  public String getTrashFolder() {
    return getString(getStoreProperty() + ".trashFolder", "");
  }
  @Override
  public void setTrashFolder(String trashFolder) {
    setString(getStoreProperty() + ".trashFolder", trashFolder);
  }

  @Override
  public String getConnectionTimeout() {
    return getString(getStoreProperty() + ".connectionTimeout", getString("Pooka.connectionTimeout", "-1"));
  }
  @Override
  public void setConnectionTimeout(String connectionTimeout) {
    setString(getStoreProperty() + ".connectionTimeout", connectionTimeout);
  }

  @Override
  public String getTimeout() {
    return getString(getStoreProperty() + ".timeout", getString("Pooka.timeout", "-1"));
  }
  @Override
  public void setTimeout(String Timeout) {
    setString(getStoreProperty() + ".timeout", Timeout);
  }

  @Override
  public String getSSLFallback() {
    return getString(getStoreProperty() + ".SSL.fallback", "none");
  }
  @Override
  public void setSSLFallback(String sslSetting) {
    setString(getStoreProperty() + ".SSL.fallback", sslSetting);
  }

  @Override
  public String getMailDir() {
    return getString(getStoreProperty() + ".mailDir", "");
  }
  @Override
  public void setMailDir(String mailDir) {
    setString(getStoreProperty() + ".mailDir", mailDir);
  }

  @Override
  public String getDefaultMailSubDir() {
    return getString("Pooka.defaultMailSubDir", "");
  }
  @Override
  public void setDefaultMailSubDir(String defaultMailSubDir) {
    setString("Pooka.defaultMailSubDir", defaultMailSubDir);
  }


  @Override
  public String getSubFolderName() {
    return getString("Pooka.subFolderName", "");
  }
  @Override
  public void setSubFolderName(String SubFolderName) {
    setString("Pooka.subFolderName", SubFolderName);
  }


}
