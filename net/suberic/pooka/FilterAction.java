package net.suberic.pooka;
import net.suberic.pooka.gui.MessageProxy;

public interface FilterAction {

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
    public MessageProxy[] performFilter(MessageProxy[] filteredMessages);
}
