package net.suberic.pooka;

import javax.activation.*;

/**
 * A DataSource based off of a byte array.
 */
public class ByteArrayDataSource implements DataSource {

  byte[] content;

  String name;

  String contentType;

  public ByteArrayDataSource(byte[] newContent, String newName, String newContentType) {
    content = newContent;
    name = newName;
    contentType = newContentType;
  }

  public java.lang.String getContentType() {
    return contentType;
  }

  public java.io.InputStream getInputStream() {
    return new java.io.ByteArrayInputStream(content);
  }

  public java.lang.String getName() {
    return name;
  }

  public java.io.OutputStream getOutputStream() throws java.io.IOException {
    throw new java.io.IOException("Output stream not supported.");
  }
}
