package net.suberic.pooka;
import javax.mail.*;
import javax.mail.search.SearchTerm;
import net.suberic.pooka.gui.MessageProxy;

/**
 * This represents a MessageFilter.  It contains a SearchTerm and an Action
 * which is done on any messages which match the SearchTerm.
 */
public class MessageFilter {
    private SearchTerm searchTerm;
    private FilterAction action;

    /**
     * Create a MessageFilter from a SearchTerm and a FilterAction.
     */
    public MessageFilter(SearchTerm newSearchTerm, FilterAction newAction) {
	searchTerm = newSearchTerm;
	action = newAction;
    }

    /**
     * Create a MessageFilter from a String which represents a Pooka
     * property.  
     *
     */
    public MessageFilter(String sourceProperty) {
	
    }

    /**
     * This runs the searchTerm test for each MessageProxy in the
     * messages array.  Each MessageProxy that matches the searchTerm
     * then has performFilter() run on it.
     * 
     * @return:  any messages which are not removed from the current folder.
     */
    public MessageProxy[] filterMessages(MessageProxy[] messages) {
	MessageProxy[] matches = new MessageProxy[messages.length];
	for (int i = 0; i < messages.length; i++) {
	    if (searchTerm.match(messages[i].getMessage()))
		matches[i] = messages[i];
	}

	MessageProxy[] notRemoved = performFilter(matches);
	
	return notRemoved;
    }

    /**
     * Actually performs the FilterAction on the given MessageProxy array.
     * 
     * @param filteredMessages An array of MessageProxy objects that are to
     * have the filter performed on them.  
     *
     * @return  any messages which are not removed from the current folder.
     */
    public MessageProxy[] performFilter(MessageProxy[] filteredMessages) {
	return action.performFilter(filteredMessages);
    }

    // accessor methods.

    public SearchTerm getSearchTerm() {
	return searchTerm;
    }

    public void setSearchTerm(SearchTerm newTerm) {
	searchTerm = newTerm;
    }

    public FilterAction getAction() {
	return action;
    }

    public void setAction(FilterAction newAction) {
	action=newAction;
    }
}
