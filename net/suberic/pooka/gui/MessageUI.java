package net.suberic.pooka.gui;
import net.suberic.pooka.*;

public interface MessageUI {

    public void closeMessageWindow();

    public MessageProxy getMessageProxy();

    public String getMessageText();

    public String getMessageContentType();

    public void showError(String errorMessage, String title);

    public void showError(String errorMessage, String title, Exception e);

    public String showInputDialog(String inputMessage, String title);

    public void setBusy(boolean newValue);
}
