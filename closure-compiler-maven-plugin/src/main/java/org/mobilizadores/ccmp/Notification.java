package org.mobilizadores.ccmp;

public class Notification {

  private String description;
  private String[] args;

  public Notification() {
    super();
  }

  public Notification(String description, String[] args) {
    this.description = description;
    this.args = args;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String[] getArgs() {
    return args;
  }

  public void setArgs(String[] args) {
    this.args = args;
  }

}
