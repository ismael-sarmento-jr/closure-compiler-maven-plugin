package org.mobilizadores.ccmp;

import java.security.Permission;

public class ContextHoldingSecurityManager extends SecurityManager {
  
  SecurityManager _prevMgr = System.getSecurityManager();
  
  public void checkPermission(Permission perm) {}

  public void checkExit(int status) {
    super.checkExit(status);
    throw new SystemExitNotAllowedException(status);
  }

  public SecurityManager getPreviousMgr() {
    return _prevMgr;
  }

  public void enableSystemExit() {
    SecurityManager mgr = System.getSecurityManager();
    if (mgr == this) {
      ContextHoldingSecurityManager smgr = (ContextHoldingSecurityManager) mgr;
      System.setSecurityManager(smgr.getPreviousMgr());
    } else
      System.setSecurityManager(null);
  }
}
