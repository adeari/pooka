package net.suberic.pooka.event;

import net.suberic.pooka.thread.LoadMessageThread;

public class MessageLoadedEvent {
    LoadMessageThread source;

    public MessageLoadedEvent(LoadMessageThread sourceThread) {
	source = sourceThread;
    }

    public LoadMessageThread getSource() {
	return source;
    }
}
