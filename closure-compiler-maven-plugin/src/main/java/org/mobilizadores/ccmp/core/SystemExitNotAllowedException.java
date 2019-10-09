package org.mobilizadores.ccmp.core;

public class SystemExitNotAllowedException extends SecurityException {

  private static final long serialVersionUID = -7418813766690274249L;
  private Integer status;
  
  public SystemExitNotAllowedException(Integer status) {
    super();
    this.status = status;
  }

  public Integer getStatus() {
    return status;
  }

  public void setStatus(Integer status) {
    this.status = status;
  }
}
