package net.suberic.pooka.filter;
import net.suberic.pooka.gui.MessageProxy;
import java.util.Vector;

public class DeleteFilterAction implements FilterAction {

    public DeleteFilterAction() {

    }

    /**
     * Runs the filterAction on each MessageProxy in the filteredMessages
     * Vector.
     *
     * @param filteredMessages messages which have met the filter condition
     * and need to have the FilterAction performed on them.
     *
     * @return messages which are removed from their original folder
     * by the filter.
     */
    public Vector performFilter(Vector filteredMessages) {
	Vector deleted = new Vector();
	for (int i = 0; i < filteredMessages.size(); i++) {
	    //	    try {
	    MessageProxy current = (MessageProxy) filteredMessages.elementAt(i);
	    current.deleteMessage(false);
	    deleted.add(current);
	    //    } catch (MessagingException me) {
	    //deleteFailed.add(filteredMessages[i]);
	    //}
	}
	
	return deleted;
    }

    /**
     * Initializes the FilterAction from the sourceProperty given.
     */
    
    public void initializeFilter(String sourceProperty) {
	// no initialization necessary.
    }
}
