package org.mobilizadores.ccmp.core;

import java.io.File;

public class FileCompressThread extends Thread {

  private File file;
  
  public FileCompressThread(File file, Runnable runnable) {
    super(runnable);
    this.file = file;
  }
  public File getFile() {
    return file;
  }
}
