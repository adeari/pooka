package net.suberic.pooka.crypto;

import javax.mail.*;
import javax.mail.internet.*;

public class MultipartEncrypted extends MimeMultipart {

  public MultipartEncrypted(String subType) {
    super(subType);
  }
  public String getContentType() {
    try {
      ContentType ct = new ContentType(super.getContentType());
      ct.setParameter("protocol", "application/pgp-encrypted");
      return ct.toString();
    } catch (Exception e) {
      return super.getContentType();
    }
  }

}
