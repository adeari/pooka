package net.suberic.pooka.gui;

public interface PookaUIFactory {

    /**
     * Creates an appropriate MessageUI object for the given MessageProxy.
     */
    public MessageUI createMessageUI(MessageProxy mp);

    /**
     * Creates an appropriate FolderDisplayUI object for the given
     * FolderInfo.
     */
    public FolderDisplayUI createFolderDisplayUI(net.suberic.pooka.FolderInfo fi);

    /**
     * Shows an Editor Window.
     */
    public void showEditorWindow(String title, java.util.Vector properties);

    /**
     * Returns the PropertyEditorFactory used by this component.
     */
    public net.suberic.util.gui.PropertyEditorFactory getEditorFactory();
}
