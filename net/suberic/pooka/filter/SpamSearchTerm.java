package net.suberic.pooka.filter;
import javax.mail.*;
import javax.mail.search.*;
import net.suberic.pooka.*;

/**
 * This is a SearchTerm which checks for Spam.
 */
public class SpamSearchTerm extends SearchTerm {

  SpamFilter filter = null;

  /**
   * Creates the given SpamSearchTerm.  
   */
  public SpamSearchTerm (SpamFilter sf) {
    filter = sf;
  }
  
  /**
   * Checks to see if the given Message is a spam of one that 
   * already exists in the FolderInfo.
   */
  public boolean match(Message m) {
    if (filter != null)
      return filter.isSpam(m);
    else
      return false;
  }
}
