package net.suberic.pooka.gui.filechooser;
import javax.swing.*;
import java.io.*;
import javax.mail.*;
import net.suberic.pooka.Pooka;

// TODO make StoreFileWrapper for selecting from available stores
// --jphekman


/**
 * This wraps a Folder or Store in a File object.
 */
public class FolderFileWrapper extends File {
  private Folder folder;
  private FolderFileWrapper parent;
  private FolderFileWrapper[] children = null;
  private String path;
  
  /**
   * Creates a new FolderFileWrapper from a Folder.  This should only
   * be used for direct children of the Folder.  
   */
  public FolderFileWrapper(Folder f, FolderFileWrapper p) {
    super(f.getName());
    folder = f;
    parent = p;
    path = f.getName();
    if (Pooka.isDebug())
      if (p != null)
	System.out.println("creating FolderFileWrapper from parent '" + p.getAbsolutePath() + "' from folder '" + f.getName() + "'");
      else
	System.out.println("creating FolderFileWrapper from parent 'null' from folder '" + f.getName() + "'");
  }
  
  /**
   * Creates a new FolderFileWrapper from a Folder with the given path
   * and parent.  This is used for making relative paths to files, i.e.
   * a child of '/foo' called 'bar/baz'.
   */
  public FolderFileWrapper(Folder f, FolderFileWrapper p, String filePath) {
    super(f.getName());
    folder = f;
    parent = p;
    path = filePath;
    if (Pooka.isDebug())
      if (p != null)
	System.out.println("creating FolderFileWrapper from parent '" + p.getAbsolutePath() + "' called '" + filePath + "'");
      else
	System.out.println("creating FolderFileWrapper from parent 'null' called '" + filePath + "'");
  }
  
  /**
   * returns true.
   */
  public boolean canRead() {
    return true;
  }
  
  /**
   * returns true.
   */
  public boolean canWrite() {
    return true;
  }
  
  /**
   * If the wrapped Folder does not exist, creates the new Folder
   * and returns true.  If it does exist, returns false.  If a
   * MessagingException is thrown, wraps it with an IOException.
   */
  public boolean createNewFile() {
    try {
      if (folder.exists())
	return false;
      else {
	folder.create(Folder.HOLDS_MESSAGES);
	return true;
      }
    } catch (MessagingException me) {
      System.out.println("caught exception: " + me.getMessage());
      me.printStackTrace();
      return false;
    }
  }
  
  
  /**
   * Attempts to delete the Folder.
   */
  public boolean delete() {
    try {
      return folder.delete(true);
    } catch (MessagingException me) {
      System.out.println("caughed exception: " + me.getMessage());
      me.printStackTrace();
      return false;
    }
  }
  
  /**
   * A no-op; we're not deleting any Mail folders on exit.
   */
  public void deleteOnExit() {
  }
  
  /**
   * Equals if the underlying Folder objects are equal.
   */
  public boolean equals(Object obj) {
    if (obj instanceof FolderFileWrapper)
      return ( folder == ((FolderFileWrapper)obj).folder );
    else
      return false;
  }
  
  /**
   * Returns folder.exists().
   */
  public boolean exists() {
    try {
      return folder.exists();
    } catch (MessagingException me) {
      return false;
    }
  }
  
  /**
   * Returns this object.
   */
  public File getAbsoluteFile() {
    System.out.println("calling getAbsoluteFile() on " + getAbsolutePath());
    if (this.isAbsolute())
      return this;
    else 
      return new FolderFileWrapper(getFolder(), getRoot(), getAbsolutePath());
  }
  
  /**
   * returns the root of this tree.
   */
  private FolderFileWrapper getRoot() {
    FolderFileWrapper parent = this;
    while (parent.getParent() != null) {
      parent = (FolderFileWrapper)parent.getParentFile();
    }
    return parent;
  }
  
  /**
   * Returns the Folder's full name.  It does this recursively, by calling
   * the this on the parent and then appending this name.
   */
  public String getAbsolutePath() {
    if (Pooka.isDebug())
      System.out.println("calling getAbsolutePath() on " + path);
    
    if (isAbsolute()) {
      if (Pooka.isDebug())
	System.out.println("returning " + getPath());
      return getPath();
    }
    else {
      if (parent != null) {
	String parentPath = parent.getAbsolutePath();
	if (parentPath != "/") {
	  if (Pooka.isDebug())
	    System.out.println("returning parentPath + slash + getPath() (" + parentPath + "/" + getPath() + ")");
	  
	  return parentPath + "/" + getPath();
	} else {
	  if (Pooka.isDebug())
	    System.out.println("returning just slash + getPath() (/" + getPath() + ")");
	  return "/" + getPath();
	}
      } else {
	if (Pooka.isDebug())
	  System.out.println("returning just /.");
	return "/";
      }
    }
  }
  
  /**
   * returns this.
   */
  public File getCanonicalFile() {
    return this;
  }
  
  /**
   * returns getAbsolutePath();
   */
  public String getCanonicalPath() {
    return getAbsolutePath();
  }
  
  /**
   * Returns the Folder's name.
   */
  public String getName() {
    return folder.getName();
  }
  
  /**
   * Returns the parent's name.
   */
  public String getParent() {
    if (parent != null)
      return parent.getAbsolutePath();
    else
      return null;
  }
  
  /**
   * This returns the parent Folder as a FolderFileWrapper.
   */
  public File getParentFile() {
    return parent;
  }
  
  /**
   * Returns the filePath variable.
   */
  public String getPath() {
    return path;
  }
  
  /**
   * Returns true if this is an absolute reference, false otherwise.
   */
  public boolean isAbsolute() {
    return (parent == null);
  }
  
  /**
   * Tests to see if this can act as a directory.
   */
  public boolean isDirectory() {
    try {
      return ((folder.getType() & Folder.HOLDS_FOLDERS) != 0);
    } catch (MessagingException me) {
      return false;
    }
  }
  
  /**
   * Tests to see if we should call this a File.
   */
  public boolean isFile() {
    try {
      return ((folder.getType() & Folder.HOLDS_MESSAGES) != 0);
    } catch (MessagingException me) {
      return false;
    }
  }
  
  /**
   * Returns false.
   */
  public boolean isHidden() {
    return false;
  }
  
  /**
   * Returns 0.
   */
  public long lastModified() {
    return 0;
  }
  
  /**
   * returns the children of the File.
   */
  public String[] list() {
    if (isDirectory()) {
      if (children == null)
	loadChildren();
      if (children != null) {
	String[] returnValue = new String[children.length];
	for (int i = 0; i < children.length; i++) {
	  returnValue[i] = children[i].getName();
	}
	return returnValue;
      }
    }
    
    return null;
    
  }
  
  /**
   * Returns the children of the File, filterd by the FilenameFilter.
   */
  public String[] list(FilenameFilter filter) {
    String[] children = list();
    String[] matching = new String[children.length];
    int retValueCounter = 0;
    for (int i = 0; i < children.length; i++) {
      if (filter.accept(this, children[i])) {
	matching[retValueCounter++] = children[i];
      }
    }
    
    String[] returnValue = new String[retValueCounter];
    
    for (int i = 0; i < retValueCounter; i++) 
      returnValue[i] = matching[i];
    
    return returnValue;
  }
  
  /**
   * This returns the children of the File as Files.
   */
  public File[] listFiles() {
    if (Pooka.isDebug()) {
      System.out.println("Running listFiles() on '" + getAbsolutePath() + "'");
      //Thread.dumpStack();
    }
    
    if (isDirectory()) {
      if (children == null) {
	if (Pooka.isDebug()) {
	  System.out.println("about to load children.");
	}
	loadChildren();
      }
      
      if (children != null)
	return children;
    } 
    
    return new FolderFileWrapper[0];
  }
  
  public File[] listFiles(FileFilter filter) {
    if (Pooka.isDebug())
      System.out.println("Running listFiles(FileFilter) on '" + getAbsolutePath() + "'");
    
    File[] children = listFiles();
    File[] matching = new File[children.length];
    int retValueCounter = 0;
    for (int i = 0; i < children.length; i++) {
      if (filter.accept(children[i])) {
	matching[retValueCounter++] = children[i];
      }
    }
    
    File[] returnValue = new File[retValueCounter];
    
    for (int i = 0; i < retValueCounter; i++) 
      returnValue[i] = matching[i];
    
    return returnValue;
  }
  
  public File[] listFiles(FilenameFilter filter) {
    if (Pooka.isDebug())
      System.out.println("Running listFiles(FilenameFilter) on '" + getAbsolutePath() + "'");
    
    File[] children = listFiles();
    File[] matching = new File[children.length];
    int retValueCounter = 0;
    for (int i = 0; i < children.length; i++) {
      if (filter.accept(this, children[i].getName())) {
	matching[retValueCounter++] = children[i];
      }
    }
    
    File[] returnValue = new File[retValueCounter];
    
    for (int i = 0; i < retValueCounter; i++) 
      returnValue[i] = matching[i];
    
    return returnValue;
  }
  
  /**
   * This creates a new directory.
   */
  public boolean mkdir() {
    try {
      if (folder.exists())
	return false;
      else {
	folder.create(Folder.HOLDS_FOLDERS);
	return true;
      }
    } catch (MessagingException me) {
      return false;
    }
  }
  
  /**
   * This creates a new directory, also creating any higher-level
   * directories if needed.
   */
  public boolean mkdirs() {
    try {
      if (folder.exists())
	return false;
      
      boolean create = true;
      if (!parent.exists())
	create = parent.mkdirs();
      
      if (create) {
	folder.create(Folder.HOLDS_FOLDERS);
	return true;
      } else
	return false;
    } catch (MessagingException me) {
      return false;
    }
  }
  
  
  /**
   * This renames the underlying Folder.
   */
  public boolean renameTo(File dest) {
    try {
      if (dest instanceof FolderFileWrapper) {
	boolean returnValue = folder.renameTo(((FolderFileWrapper)dest).getFolder());
	if (parent != null)
	  parent.refreshChildren();
	return returnValue;
      } else
	return false;
    } catch (MessagingException me) {
      Pooka.getUIFactory().showError(Pooka.getProperty("error.renamingFolder", "Error renaming folder ") + getName(), me);
      return false;
    }
  }
  
  /**
   * This returns the wrapped Folder.
   */
  public Folder getFolder() {
    return folder;
  }
  
  /**
   * This loads the children of the Folder.
   */
  private synchronized void loadChildren() {
    if (Pooka.isDebug()) {
      System.out.println(Thread.currentThread().getName() + ":  calling loadChildren() on " + getAbsolutePath());
      //Thread.dumpStack();
    }
    
    if (children == null) {
      if (Pooka.isDebug()) {
	System.out.println(Thread.currentThread().getName() + ":  children is null on " + getAbsolutePath() + ".  loading children.");
      }
      if (isDirectory() ||  ! exists()) {
	try {
	  if (Pooka.isDebug())
	    System.out.println(Thread.currentThread().getName() + ":  checking for connection.");
	  
	  if (!folder.getStore().isConnected()) {
	    if (Pooka.isDebug()) {
	      System.out.println(Thread.currentThread().getName() + ":  parent store of " + getAbsolutePath() + " is not connected.  reconnecting.");
	    }
	    folder.getStore().connect();
	  } else {
	    if (Pooka.isDebug())
	      System.out.println(Thread.currentThread().getName() + ":  connection is ok.");
	  }
	  
	  
	  if (Pooka.isDebug())
	    System.out.println(Thread.currentThread().getName() + ":  calling folder.list()");
	  Folder[] childList = folder.list();
	  if (Pooka.isDebug())
	    System.out.println(Thread.currentThread().getName() + ":  folder.list() returned " + childList + "; creating new folderFileWrapper.");
	  
	  FolderFileWrapper[] tmpChildren = new FolderFileWrapper[childList.length];
	  for (int i = 0; i < childList.length; i++) {
	    if (Pooka.isDebug())
	      System.out.println(Thread.currentThread().getName() + ":  calling new FolderFileWrapper for " + childList[i].getName() + " (entry # " + i);
	    
	    tmpChildren[i] = new FolderFileWrapper(childList[i], this);
	  }
	  
	  children = tmpChildren;
	} catch (MessagingException me) {
	  if (Pooka.isDebug())
	    System.out.println("caught exception during FolderFileWrapper.loadChildren() on " + getAbsolutePath() + ".");
	  me.printStackTrace();
	}
      }
    }
    
  }

  /**
   * This refreshes the children of the Folder.
   */
  public synchronized void refreshChildren() {
    if (Pooka.isDebug()) {
      System.out.println(Thread.currentThread().getName() + ":  calling refreshChildren() on " + getAbsolutePath());
      //Thread.dumpStack();
    }
    
    if (children == null) {
      if (Pooka.isDebug()) {
	System.out.println(Thread.currentThread().getName() + ":  children is null on " + getAbsolutePath() + ".  calling loadChildren().");
      }
      loadChildren();
    } else {
      if (isDirectory() ||  ! exists()) {
	try {
	  if (Pooka.isDebug())
	    System.out.println(Thread.currentThread().getName() + ":  checking for connection.");
	  
	  if (!folder.getStore().isConnected()) {
	    if (Pooka.isDebug()) {
	      System.out.println(Thread.currentThread().getName() + ":  parent store of " + getAbsolutePath() + " is not connected.  reconnecting.");
	    }
	    folder.getStore().connect();
	  } else {
	    if (Pooka.isDebug())
	      System.out.println(Thread.currentThread().getName() + ":  connection is ok.");
	  }
	  
	  
	  if (Pooka.isDebug())
	    System.out.println(Thread.currentThread().getName() + ":  calling folder.list()");
	  Folder[] childList = folder.list();
	  if (Pooka.isDebug())
	    System.out.println(Thread.currentThread().getName() + ":  folder.list() returned " + childList + "; creating new folderFileWrapper.");
	  
	  FolderFileWrapper[] tmpChildren = new FolderFileWrapper[childList.length];
	  for (int i = 0; i < childList.length; i++) {
	    if (Pooka.isDebug())
	      System.out.println(Thread.currentThread().getName() + ":  calling new FolderFileWrapper for " + childList[i].getName() + " (entry # " + i);
	    
	    // yeah, this is n! or something like that. shouldn't matter--
	    // if we have somebody running with that many folders, or with
	    // that slow of a machine, we're in trouble anyway.

	    //try to get a match.
	    boolean found = false;
	    for (int j = 0; ! found && j < children.length; j++) {
	      if (children[j].getName().equals(childList[i].getName())) {
		tmpChildren[i] = children[j];
		found = true;
	      }
	    }

	    if (! found) {
	      tmpChildren[i] = new FolderFileWrapper(childList[i], this);
	    }
	  }
	  
	  children = tmpChildren;
	} catch (MessagingException me) {
	  Pooka.getUIFactory().showError(Pooka.getProperty("error.refreshingFolder", "error refreshing folder's children: "), me);
	}
      }
    }
    
  }
  
  /* Only accepts relative filenames. */
  public FolderFileWrapper getFileByName(String filename) {
    
    if (Pooka.isDebug())
      System.out.println("calling getFileByName(" + filename + ") on folder " + getName() + " (" + getPath() + ") (abs " + getAbsolutePath() + ")");
    
    String origFilename = new String(filename);
    if (filename == null || filename.length() < 1 || (filename.equals("/") && getParentFile() == null)) {
      if (Pooka.isDebug())
	System.out.println("returning this for getFileByName()");
      return this;
    }
    
    if (this.isAbsolute(filename))
      {
	return null; // FIXME error
      }
    
    // strip out the /'s
    
    String subdirFile = null;
    
    int dirMarker = filename.indexOf('/');
    while (dirMarker == 0) {
      filename = filename.substring(1);
      dirMarker = filename.indexOf('/');
    }
    
    // divide into first component and rest of components
    if (dirMarker > 0) {
      subdirFile = filename.substring(dirMarker + 1);
      filename=filename.substring(0, dirMarker);
    }
    
    FolderFileWrapper currentFile = getChildFile(filename);
    if (currentFile != null && subdirFile != null) {
      // recurse with rest of components
      FolderFileWrapper tmp = currentFile.getFileByName(subdirFile);
      //	    tmp.path = origFilename; 
      
      if (Pooka.isDebug())
	System.out.println("created file " + tmp.getName() + " (" + tmp.getPath() + ") (abs " + tmp.getAbsolutePath() + ") from string " + origFilename + " on folder " + getName() + " (" + getPath() + ") (abs " + getAbsolutePath() + ")");
      
      return tmp;
      
    } else {
      return currentFile;
    }
    
  }
  
  private FolderFileWrapper getChildFile(String filename) {
    if (Pooka.isDebug())
      System.out.println("calling getChildFile on " + getName() + " with filename " + filename);
    
    if (children == null)
      loadChildren();
    
    if (children != null) {
      for (int i = 0; i < children.length; i++) {
	if (children[i].getName().equals(filename))
	  return children[i];
      }
      
      FolderFileWrapper[] newChildren = new FolderFileWrapper[children.length +1];
      for (int i = 0; i < children.length; i++)
	newChildren[i] = children[i];
      
      try {
	newChildren[children.length] = new FolderFileWrapper(folder.getFolder(filename), this);
      } catch (MessagingException me) {
      }
      
      children = newChildren;
      return children[children.length -1];
    }
    
    return this;
    
  }
  
  private boolean isAbsolute(String filename) {
    return filename.startsWith("/");
  }
  
  public String filenameAsRelativeToRoot(String filename) {
    String relative = filename;
    while (relative.length() > 0 & isAbsolute (relative)) {
      relative = relative.substring(1);
    }
    
    return relative;
  }

  
}
