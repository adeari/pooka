package net.suberic.pooka.filter;
import net.suberic.pooka.gui.MessageProxy;
import java.util.Vector;

public class DeleteFilterAction implements FilterAction {

    public DeleteFilterAction() {

    }

    /**
     * Runs the filterAction on each MessageProxy in the filteredMessages
     * array.  
     *
     * @param filteredMessages messages which have met the filter condition
     * and need to have the FilterAction performed on them.
     *
     * @return messages which are not removed from their original folder
     * by the filter.
     */
    public MessageProxy[] performFilter(MessageProxy[] filteredMessages) {
	Vector deleteFailed = new Vector();
	for (int i = 0; i < filteredMessages.length; i++) {
	    //	    try {
		filteredMessages[i].deleteMessage(false);
		//    } catch (MessagingException me) {
		//deleteFailed.add(filteredMessages[i]);
		//}
	}
	
	MessageProxy[] returnValue = new MessageProxy[deleteFailed.size()];
	for (int i = 0; i < deleteFailed.size(); i++) {
	    returnValue[i] = (MessageProxy)deleteFailed.elementAt(i);
	}

	return returnValue;
    }

    /**
     * Initializes the FilterAction from the sourceProperty given.
     */
    
    public void intitializeFilter(String sourceProperty) {
	// no initialization necessary.
    }
}
