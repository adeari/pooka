package net.suberic.pooka.conf;

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


}
