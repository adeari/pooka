package net.suberic.pooka.gui.filechooser;
import javax.swing.*;
import javax.mail.*;
import java.io.*;
import net.suberic.pooka.Pooka;

/**
 * This class implements a FileSystemView based on the available Folders in
 * a Store.
 */

public class MailFileSystemView
    extends javax.swing.filechooser.FileSystemView {

    Store store;
    FolderFileWrapper root = null;

    /**
     * This creates a new MailFileSystemView at the top of the Store list; 
     * all available Stores will be listed, with their folders under them.
     */
    public MailFileSystemView() {

    }
    
    /**
     * This creates a MailFileSystemView out of a Store object.
     */
    public MailFileSystemView(Store newStore) {
	store = newStore;
	try {
	    if (!store.isConnected())
		store.connect();
	    getRoots();
	} catch (MessagingException me) {
	    System.out.println("caught messagingException : "
			       + me.getMessage());
	    me.printStackTrace();
	}
    }
    
    /**
     * This creates a new Folder and FolderFileWrapper in the Folder 
     * corresponding to the directory dir with the name filename.
     * 
     * @dir a FolderFileWrapper representing an IMAP folder.
     * @filename a string representing an IMAP folder name.
     */
    public File createFileObject(File dir, String filename) {
	if (Pooka.isDebug()) {
	    if (dir != null)
		System.out.println("calling createFileObject on directory " + dir.getName() + " (" + dir.getPath() + "), filename " + filename);
	    else
		System.out.println("calling createFileObject on directory null, filename " + filename);
	}

	if (dir != null && dir instanceof FolderFileWrapper)
	    return ((FolderFileWrapper)dir).getFileByName(filename);
	else
	    return null;
    }

    /**
     * @filename is an IMAP folder name.
     */
    public File createFileObject(String filename) {

	// todo jph:  strip off any leading directoy separators.  we
	// want to call getFileByName with a relative path (in this case
	// to the root directory) always.

	if (Pooka.isDebug())
	    System.out.println("running createFileObject2 on filename" + filename);

	FolderFileWrapper rootFile = ((FolderFileWrapper) getRoot());
	if (rootFile == null) {
	    if (Pooka.isDebug())
		System.out.println("root == null");
	    return null;
	}

	if (Pooka.isDebug())
	    System.out.println("root != null");

	if (filename.equals("/")) {
	    return rootFile;
	}
	
	return rootFile.getFileByName
	    (rootFile.filenameAsRelativeToRoot(filename));
    }

    public File createNewFolder(File containingDir) {
	if (Pooka.isDebug())
	    System.out.println("running createNewFolder.");

	try {
	    Folder parentFolder = null;
	    if (containingDir instanceof FolderFileWrapper) {
		parentFolder = ((FolderFileWrapper)containingDir).getFolder();
		
		Folder newFolder = parentFolder.getFolder("New_folder");
		for (int i = 1; newFolder.exists(); i++) {
		    newFolder=parentFolder.getFolder("New_folder_" + i);
		}
		
		newFolder.create(Folder.HOLDS_FOLDERS);
		
		return new FolderFileWrapper(newFolder, (FolderFileWrapper)containingDir);
	    } else {
		return null;

		//parentFolder = store.getFolder(containingDir.getAbsolutePath());
	    }
	} catch (MessagingException me) {
	}

	return null;
    }

    public File[] getFiles(File dir, boolean useFileHiding) {
	if (Pooka.isDebug())
	    System.out.println("running getFiles.");

	if (dir instanceof FolderFileWrapper) {
	    return ((FolderFileWrapper)dir).listFiles();
	} else {
	    if (dir == null)
		return null; // FIXME: or set dir to root?
	    
	    // FIXME: ugly?
		File f = ((FolderFileWrapper)getRoot()).getFileByName(dir.getAbsolutePath());
		
		if (f == null) return null; // FIXME: error?
		
		return f.listFiles();
	}
    }

    public File getHomeDirectory() {
	if (Pooka.isDebug())
	    System.out.println("running getHomeDirectory().");

	if (root != null)
	    return root;
	else {
	    return getRoot();
	}
    }

    public File getParentDirectory(File dir) {
	if (Pooka.isDebug())
	    System.out.println("running getParentDirectory");

	if (dir == null)
	    return null; // at root

	if (! (dir instanceof FolderFileWrapper)) {
	    if (getRoot() == null)
		return null; // FIXME error?
	    dir = ((FolderFileWrapper) getRoot()).getFileByName(dir.getAbsolutePath());
	}

	if (dir == null)
	    return null; // FIXME error?

	// FIXME is this cast always okay?
	return ((FolderFileWrapper)dir).getParentFile();
    }

    public File[] getRoots() {
	if (Pooka.isDebug())
	    System.out.println("calling getRoots() on MailFileSystemView.");

	if (root != null)
	    return new File[] { root };
	try {
	    Folder f = store.getDefaultFolder();
	    root = new FolderFileWrapper(f, null);
	    return new File[] { root };
	} catch (MessagingException me) {
	    return null; // FIXME: throw this on
	}
    }

    /**
     * always returns false for now.
     */
    public boolean isHiddenFile(File f) {
	return false;
    }

    /**
     * returns true for '/' only.
     */
    public boolean isRoot(File f) {
	if (f.getParentFile() == null)
	    return true;
	else
	    return false;
    }

    /* Not inherited. */

    public File getRoot() {
	if (root == null)
	    {
		File[] roots = getRoots();
		if (roots != null && roots.length > 0)
		    root = ((FolderFileWrapper) roots[0]);
	    }
	return root;
    }

}
