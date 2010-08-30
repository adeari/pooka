package net.suberic.pooka.conf.vb;

import net.suberic.pooka.conf.StoreConfiguration;
import net.suberic.util.*;

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

}
