package net.suberic.pooka.gui;

import net.suberic.pooka.Pooka;
import net.suberic.util.VariableBundle;

import javax.swing.*;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.IOException;
import java.io.InputStream;


/**
 * Gives the option of loading configuration settings from an http file.
 */
public class LoadHttpConfigPooka {

  URL mUrl;
  JFrame mFrame;

  /**
   * Starts the dialog.
   */
  public void start() {
    mFrame = new JFrame();
    mFrame.show();
    showChoices();
  }

  /**
   * Shows the choices.
   */
  public void showChoices() {
    String urlString = JOptionPane.showInputDialog("Choose a remote file.");
    if (urlString == null)
      return;

    try {
      URL configUrl = new URL(urlString);
      InputStream is = configUrl.openStream();
      VariableBundle newBundle = new VariableBundle(is, Pooka.getResources());
      Pooka.resources = newBundle;
    } catch (MalformedURLException mue) {
      JOptionPane.showMessageDialog(mFrame, "Malformed URL.");
      showChoices();
    } catch (java.io.IOException ioe) {
      JOptionPane.showMessageDialog(mFrame, "Could not connect to URL:  " + ioe.toString());
      showChoices();
    }

  }

}
