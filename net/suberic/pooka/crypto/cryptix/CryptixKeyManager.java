package net.suberic.pooka.crypto.cryptix;

import net.suberic.pooka.*;
import net.suberic.pooka.crypto.*;
import net.suberic.util.*;

import java.security.*;
import java.io.*;
import java.util.*;

import cryptix.openpgp.PGPKeyBundle;
import cryptix.openpgp.PGPPublicKey;
import cryptix.openpgp.packet.PGPPublicKeyPacket;

import cryptix.pki.ExtendedKeyStore;
import cryptix.pki.KeyBundle;

/**
 * This manages a set of Encryption keys for use with PGP or S/MIME.
 */
public class CryptixKeyManager implements EncryptionKeyManager {

  int loadCount = 0;
  KeyStore publicKeyStore = null;
  KeyStore privateKeyStore = null;

  public CryptixKeyManager() throws EncryptionException {
    try {
      publicKeyStore = ExtendedKeyStore.getInstance("OpenPGP/KeyRing");
      privateKeyStore = ExtendedKeyStore.getInstance("OpenPGP/KeyRing");
    } catch (Exception e) {
      throw new EncryptionException (e);
    }
  }

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
    throws IOException, EncryptionException {
    PGPKeyBundle bundle = null;

    // FIXME
    try {
      if (loadCount > 0)
	publicKeyStore.load(stream, null);
      else
	privateKeyStore.load(stream, null);
      
      loadCount++;
    } catch (IOException ioe) {
      throw ioe;
    } catch (Exception e) {
      throw new EncryptionException(e);
    }
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
    throws IOException, EncryptionException {
    try {
      publicKeyStore.store(stream, password);
    } catch (IOException ioe) {
      throw ioe;
    } catch (Exception e) {
      throw new EncryptionException(e);
    }
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
    int counter = 0;
    try {
      counter += publicKeyStore.size();
      counter += privateKeyStore.size();
    } catch (Exception e) {
    }
    return counter;
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
    
    KeyBundle bundle = ((ExtendedKeyStore)publicKeyStore).getKeyBundle(alias);
    
    System.err.println("got key bundle " + bundle + " for alias " + alias);

    if (bundle != null) {
      Iterator iter = bundle.getPublicKeys();
      if (! iter.hasNext()) {
	try {
	  System.err.println("no next; publicKeyStore.getKey(" + alias + ", null)=" + publicKeyStore.getKey(alias, null));
	} catch (Throwable t) {
	  System.err.println("no next:  getKey() throws exception " + t);
	  t.printStackTrace();
	}
      }
      while (iter.hasNext()) {
	PGPPublicKey publickey = (PGPPublicKey)iter.next();
	System.err.println("got public key " + publickey);
	PGPPublicKeyPacket keypkt = 
	  (PGPPublicKeyPacket)publickey.getPacket();
	System.err.println("cryptor is " + keypkt.getAlgorithm() + ", a " + keypkt.getAlgorithm().getClass());

      }
      System.err.println("done with public keys.");

      CryptixPGPEncryptionKey key = new CryptixPGPEncryptionKey(bundle, null);
      return key;
    }

    return null;
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
  public EncryptionKey getPrivateKey(String alias, char[] password)
    throws KeyStoreException, EncryptionException {

    KeyBundle bundle = ((ExtendedKeyStore)privateKeyStore).getKeyBundle(alias);
    
    if (bundle != null) {

      Iterator iter = bundle.getPrivateKeys();
      if (! iter.hasNext()) {
	try {
	  System.err.println("no next; privateKeyStore.getKey(" + alias + ", null)=" + privateKeyStore.getKey(alias, null));
	} catch (Throwable t) {
	  System.err.println("no next:  getKey() throws exception " + t);
	  t.printStackTrace();
	}
      }
      
      CryptixPGPEncryptionKey key = new CryptixPGPEncryptionKey(bundle, null);
      return key;
    }

    return null;
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
    // FIXME
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
    // FIXME
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
    // FIXME

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
    // FIXME
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

    HashSet returnValue = new HashSet();
    Enumeration enum = publicKeyStore.aliases();
    while (enum.hasMoreElements())
      returnValue.add(enum.nextElement());
    
    return returnValue;
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

    HashSet returnValue = new HashSet();
    Enumeration enum = privateKeyStore.aliases();
    while (enum.hasMoreElements())
      returnValue.add(enum.nextElement());
    
    return returnValue;
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

    return publicKeyStore.isKeyEntry(alias);
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

    return privateKeyStore.isKeyEntry(alias);
  }
  
}
