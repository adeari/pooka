package net.suberic.pooka.crypto;
import javax.mail.*;
import javax.mail.internet.*;

public class UpdatableMBP extends MimeBodyPart {

  public UpdatableMBP() {
    super();
  }
  public UpdatableMBP(java.io.InputStream is) throws MessagingException{
    super(is);
  }
  public void updateMyHeaders() throws MessagingException {
    updateHeaders();
  }

}
