package net.suberic.pooka.gui;

public interface PookaUIFactory extends ErrorHandler {

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
     * Creates a ContentPanel which will be used to show messages and folders.
     */
    public ContentPanel createContentPanel();

    /**
     * Shows an Editor Window.
     */
    public void showEditorWindow(String title, java.util.Vector properties);

    /**
     * Returns the PropertyEditorFactory used by this component.
     */
    public net.suberic.util.gui.PropertyEditorFactory getEditorFactory();

    /**
     * Shows a Confirm dialog.
     */
    public int showConfirmDialog(String message, String title, int type);

    /**
     * This shows an Input window.
     */
    public String showInputDialog(String inputMessage, String title);
}
