package net.suberic.pooka.crypto.gpg;

import net.suberic.pooka.*;
import net.suberic.pooka.crypto.*;
import net.suberic.util.*;

import java.security.*;
import java.io.*;
import java.util.*;

/**
 * This manages a set of Encryption keys to be used with GPG.
 */
public class GPGEncryptionKeyManager implements EncryptionKeyManager {

  HashMap publicKeyMap = null;
  HashMap privateKeyMap = null;

  boolean loaded = false;

  /*
   * Loads this KeyStore from the given input stream.
   *
   * <p>If a password is given, it is used to check the integrity of the
   * keystore data. Otherwise, the integrity of the keystore is not checked.
   *
   * <p>In order to create an empty keystore, or if the keystore cannot
   * be initialized from a stream (e.g., because it is stored on a hardware
   * token device), you pass <code>null</code>
   * as the <code>stream</code> argument.
   *
   * <p> Note that if this KeyStore has already been loaded, it is
   * reinitialized and loaded again from the given input stream.
   *
   * @param stream the input stream from which the keystore is loaded, or
   * null if an empty keystore is to be created.
   * @param password the (optional) password used to check the integrity of
   * the keystore.
   *
   * @exception IOException if there is an I/O or format problem with the
   * keystore data
   * @exception NoSuchAlgorithmException if the algorithm used to check
   * the integrity of the keystore cannot be found
   */
  public void load(InputStream stream, char[] password)
    throws IOException {
    // we'll ignore the input and just load the default store.

    publicKeyMap = new HashMap();
    privateKeyMap = new HashMap();

    Process p = Runtime.getRuntime().exec("gpg --list-keys");

    try {
      p.waitFor();
    } catch (InterruptedException ie) {
    }
    
    BufferedReader resultReader = new BufferedReader(new InputStreamReader(p.getInputStream()));

    String currentLine = resultReader.readLine();
    while (currentLine != null && currentLine.startsWith("gpg:")) {
      // skip all of the gpg: headers.
      currentLine = resultReader.readLine();
    }

    if (currentLine != null) {
      // read the next two lines.
      currentLine = resultReader.readLine();
      currentLine = resultReader.readLine();
    }    

    // now we actually create the keys.
    while (currentLine != null) {
      try {
	int keyTypeEnd = currentLine.indexOf(' ', 0);
	int keyIdEnd = currentLine.indexOf(' ', keyTypeEnd + 2);
	int keyDateEnd = currentLine.indexOf(' ', keyIdEnd + 1);
	String keyAlias = currentLine.substring(keyDateEnd + 1);
	EncryptionKey key = new GPGEncryptionKey(keyAlias, new String(password));
	publicKeyMap.put(keyAlias, key);
	currentLine = resultReader.readLine();
	while (currentLine != null && currentLine.length() > 0) {
	  currentLine = resultReader.readLine();
	}
      } catch (IndexOutOfBoundsException ioobe) {
	System.out.println("error reading key:  '" + currentLine + "'");
	ioobe.printStackTrace();
      }
      
      currentLine = resultReader.readLine();
    }

    // do the same for the private keys

    Process sp = Runtime.getRuntime().exec("gpg --list-secret-keys");

    try {
      sp.waitFor();
    } catch (InterruptedException ie) {
    }
    
    resultReader = new BufferedReader(new InputStreamReader(sp.getInputStream()));

    currentLine = resultReader.readLine();
    while (currentLine != null && currentLine.startsWith("gpg:")) {
      // skip all of the gpg: headers.
      currentLine = resultReader.readLine();
    }
    
    if (currentLine != null) {
      // read the next two lines.
      currentLine = resultReader.readLine();
      currentLine = resultReader.readLine();
    }    

    // now we actually create the keys.
    while (currentLine != null) {
      try {
	int keyTypeEnd = currentLine.indexOf(' ', 0);
	int keyIdEnd = currentLine.indexOf(' ', keyTypeEnd + 2);
	int keyDateEnd = currentLine.indexOf(' ', keyIdEnd + 1);
	String keyAlias = currentLine.substring(keyDateEnd + 1);
	EncryptionKey key = new GPGEncryptionKey(keyAlias, new String(password));
	privateKeyMap.put(keyAlias, key);
	currentLine = resultReader.readLine();
	while (currentLine != null && currentLine.length() > 0) {
	  currentLine = resultReader.readLine();
	}
      } catch (IndexOutOfBoundsException ioobe) {
	System.out.println("error reading secret key:  '" + currentLine + "'");
	ioobe.printStackTrace();
      }
      
      currentLine = resultReader.readLine();
    }

    loaded = true;
  }

  
  /**
   * Stores this keystore to the given output stream, and protects its
   * integrity with the given password.
   *
   * @param stream the output stream to which this keystore is written.
   * @param password the password to generate the keystore integrity check
   *
   * @exception KeyStoreException if the keystore has not been initialized
   * (loaded).
   * @exception IOException if there was an I/O problem with data
   * @exception NoSuchAlgorithmException if the appropriate data integrity
   * algorithm could not be found
   */
  public void store(OutputStream stream, char[] password)
    throws EncryptionException, IOException {
    // again, we'll actually store all keys directly, so this has no
    // effect.
    return;
  }
  
  /**
   * Retrieves the number of entries in this keystore.
   *
   * @return the number of entries in this keystore
   *
   * @exception KeyStoreException if the keystore has not been initialized
   * (loaded).
   */
  public int size()
    throws KeyStoreException {
    if (loaded) {
      return publicKeyMap.size() + privateKeyMap.size();
    } else {
      throw new KeyStoreException ( "store not loaded." );
    }
  }

  /**
   * Returns the key associated with the given alias, using the given
   * password to recover it.
   *
   * @param alias the alias name
   * @param password the password for recovering the key
   *
   * @return the requested key, or null if the given alias does not exist
   * or does not identify a <i>key entry</i>.
   *
   * @exception KeyStoreException if the keystore has not been initialized
   * (loaded).
   * @exception NoSuchAlgorithmException if the algorithm for recovering the
   * key cannot be found
   * @exception UnrecoverableKeyException if the key cannot be recovered
   * (e.g., the given password is wrong).
   */
  public EncryptionKey getPublicKey(String alias)
    throws KeyStoreException {
    if (! loaded)
      throw new KeyStoreException ( "store not loaded." );

    return (EncryptionKey) publicKeyMap.get(alias);
  }

  /**
   * Returns the key associated with the given alias, using the given
   * password to recover it.
   *
   * @param alias the alias name
   * @param password the password for recovering the key
   *
   * @return the requested key, or null if the given alias does not exist
   * or does not identify a <i>key entry</i>.
   *
   * @exception KeyStoreException if the keystore has not been initialized
   * (loaded).
   * @exception NoSuchAlgorithmException if the algorithm for recovering the
   * key cannot be found
   * @exception UnrecoverableKeyException if the key cannot be recovered
   * (e.g., the given password is wrong).
   */
  public EncryptionKey getPrivateKey(String alias, char[] passphrase)
    throws KeyStoreException {

    if (! loaded)
      throw new KeyStoreException ( "store not loaded." );

    GPGEncryptionKey gpgKey = (GPGEncryptionKey) privateKeyMap.get(alias);

    if (gpgKey == null)
      return null;

    String newAlias = gpgKey.getAlias();

    System.err.println("returning new key with alias " + newAlias + ", passphrase " + new String(passphrase));
    GPGEncryptionKey newKey = new GPGEncryptionKey(newAlias, new String(passphrase));
    return newKey;
  }
  
  
  /**
   * Assigns the given key to the given alias, protecting it with the given
   * password.
   *
   * <p>If the given key is of type <code>java.security.PrivateKey</code>,
   * it must be accompanied by a certificate chain certifying the
   * corresponding public key.
   *
   * <p>If the given alias already exists, the keystore information
   * associated with it is overridden by the given key (and possibly
   * certificate chain).
   *
   * @param alias the alias name
   * @param key the key to be associated with the alias
   * @param password the password to protect the key
   * @param chain the certificate chain for the corresponding public
   * key (only required if the given key is of type
   * <code>java.security.PrivateKey</code>).
   *
   * @exception KeyStoreException if the keystore has not been initialized
   * (loaded), the given key cannot be protected, or this operation fails
   * for some other reason
   */
  public void setPublicKeyEntry(String alias, EncryptionKey key)
    throws KeyStoreException {
    if (! loaded)
      throw new KeyStoreException ( "store not loaded." );

    // not yet implemented
  }
  
  /**
   * Assigns the given key to the given alias, protecting it with the given
   * password.
   *
   * <p>If the given key is of type <code>java.security.PrivateKey</code>,
   * it must be accompanied by a certificate chain certifying the
   * corresponding public key.
   *
   * <p>If the given alias already exists, the keystore information
   * associated with it is overridden by the given key (and possibly
   * certificate chain).
   *
   * @param alias the alias name
   * @param key the key to be associated with the alias
   * @param password the password to protect the key
   * @param chain the certificate chain for the corresponding public
   * key (only required if the given key is of type
   * <code>java.security.PrivateKey</code>).
   *
   * @exception KeyStoreException if the keystore has not been initialized
   * (loaded), the given key cannot be protected, or this operation fails
   * for some other reason
   */
  public void setPrivateKeyEntry(String alias, EncryptionKey key, char[] password)
    throws KeyStoreException {
    if (! loaded)
      throw new KeyStoreException ( "store not loaded." );

    // not yet implemented
  }
  
  /**
   * Deletes the entry identified by the given alias from this keystore.
   *
   * @param alias the alias name
   *
   * @exception KeyStoreException if the keystore has not been initialized,
   * or if the entry cannot be removed.
   */
  public void deletePublicKeyEntry(String alias)
    throws KeyStoreException {

    if (! loaded)
      throw new KeyStoreException ( "store not loaded." );


    // not yet implemented
  }

  
  /**
   * Deletes the entry identified by the given alias from this keystore.
   *
   * @param alias the alias name
   *
   * @exception KeyStoreException if the keystore has not been initialized,
   * or if the entry cannot be removed.
   */
  public void deletePrivateKeyEntry(String alias, char[] password)
    throws KeyStoreException {
    if (! loaded)
      throw new KeyStoreException ( "store not loaded." );

    // not yet implemented
  }
  
  /**
   * Lists all the alias names of this keystore.
   *
   * @return set of the alias names
   *
   * @exception KeyStoreException if the keystore has not been initialized
   * (loaded).
   */
  public Set publicKeyAliases()
    throws KeyStoreException {
    if (! loaded)
      throw new KeyStoreException ( "store not loaded." );

    return new HashSet(publicKeyMap.keySet());
    
  }
  /**
   * Lists all the alias names of this keystore.
   *
   * @return set of the alias names
   *
   * @exception KeyStoreException if the keystore has not been initialized
   * (loaded).
   */
  public Set privateKeyAliases()
    throws KeyStoreException {
    
    if (! loaded)
      throw new KeyStoreException ( "store not loaded." );

    return new HashSet(privateKeyMap.keySet());
  }
  
  
  /**
   * Checks if the given alias exists in this keystore.
   *
   * @param alias the alias name
   *
   * @return true if the alias exists, false otherwise
   *
   * @exception KeyStoreException if the keystore has not been initialized
   * (loaded).
   */
  public boolean containsPublicKeyAlias(String alias)
    throws KeyStoreException {
    if (! loaded)
      return false;

    return publicKeyMap.containsKey(alias);

  }
  /**
   * Checks if the given alias exists in this keystore.
   *
   * @param alias the alias name
   *
   * @return true if the alias exists, false otherwise
   *
   * @exception KeyStoreException if the keystore has not been initialized
   * (loaded).
   */
  public boolean containsPrivateKeyAlias(String alias)
    throws KeyStoreException {
    if (! loaded)
      return false;

    return privateKeyMap.containsKey(alias);
  }
  
  
}
