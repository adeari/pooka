package net.suberic.pooka.crypto;

public class EncryptionException extends Exception {

  public EncryptionException(String msg) {
    super(msg);
  }

  public EncryptionException(Exception e) {
    super(e);
  }
}
