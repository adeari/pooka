package net.suberic.pooka;
import javax.mail.*;
import javax.mail.search.SearchTerm;
import net.suberic.pooka.filter.FilterAction;
import java.util.List;
import java.util.LinkedList;

/**
 * This represents a MessageFilter which acts on the backend of the 
 * mail server.  Basically, this means a filter which, say, modifies incoming
 * messages, or moves them into another Folder.  Compare to 
 * DisplayMessageFilter, which changes the way a Message is displayed.
 */
public class BackendMessageFilter extends MessageFilter {
    private SearchTerm searchTerm;
    private FilterAction action;

    /**
     * Create a MessageFilter from a SearchTerm and a FilterAction.
     */
    public BackendMessageFilter(SearchTerm newSearchTerm, FilterAction newAction) {
	super(newSearchTerm, newAction);
    }

    /**
     * Create a MessageFilter from a String which represents a Pooka
     * property.  
     *
     */
    public BackendMessageFilter(String sourceProperty) {
	super(sourceProperty);
    }

    /**
     * This runs the searchTerm test for each MessageInfo in the
     * messages List.  Each MessageInfo that matches the searchTerm
     * then has performFilter() run on it.
     * 
     * @return:  all messages removed from the current folder.
     */
    public List filterMessages(List messages) {
	List matches = new LinkedList();
	for (int i = 0; i < messages.size(); i++) {
	    if (getSearchTerm().match(((net.suberic.pooka.gui.MessageProxy)messages.get(i)).getMessageInfo().getMessage()))
		matches.add(messages.get(i));
	}

	return performFilter(matches);
    }

    /**
     * Actually performs the FilterAction on the given MessageInfo array.
     * 
     * @param filteredMessages A List of MessageInfo objects that are to
     * have the filter performed on them.  
     *
     * @return  all messagesremoved from the current folder.
     */
    public List performFilter(List filteredMessages) {
	return getAction().performFilter(filteredMessages);
    }

}
