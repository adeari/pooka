package net.suberic.pooka.cache;

import javax.mail.*;

public class FolderProxy extends javax.mail.Folder {
  String folderName;

  public FolderProxy(String name) {
    super(null);
    folderName = name;
  }
  public String getName() {
    return folderName;
  }
  public String getFullName() {
    return folderName;
  }
  
  public  Folder getParent() throws MessagingException { throw new MessagingException("Proxy."); }

     public  boolean exists() throws MessagingException { throw new MessagingException("Proxy."); }

   public  Folder[] list(String pattern) throws MessagingException { throw new MessagingException("Proxy."); }

   public  int getType() throws MessagingException { throw new MessagingException("Proxy."); } 

    public  char getSeparator() throws MessagingException { throw new MessagingException("Proxy."); }
    public  boolean create(int type) throws MessagingException { throw new MessagingException("Proxy."); }

   public  boolean hasNewMessages() throws MessagingException { throw new MessagingException("Proxy."); }

     public  Folder getFolder(String name)
				throws MessagingException { throw new MessagingException("Proxy."); }
   public  boolean delete(boolean recurse) 
				throws MessagingException { throw new MessagingException("Proxy."); }

 public  boolean renameTo(Folder f) throws MessagingException { throw new MessagingException("Proxy."); }

public  void open(int mode) throws MessagingException { throw new MessagingException("Proxy."); }
  public  void close(boolean expunge) throws MessagingException { throw new MessagingException("Proxy."); }

  public  boolean isOpen() {
    return false;
  }
  public  Flags getPermanentFlags() {
    return null;
  }
  public  int getMessageCount() throws MessagingException { throw new MessagingException("Proxy."); }
  public  Message getMessage(int msgnum)
				throws MessagingException { throw new MessagingException("Proxy."); }

  public  void appendMessages(Message[] msgs)
    throws MessagingException { throw new MessagingException("Proxy."); }

  public  Message[] expunge() throws MessagingException { throw new MessagingException("Proxy."); }

}
