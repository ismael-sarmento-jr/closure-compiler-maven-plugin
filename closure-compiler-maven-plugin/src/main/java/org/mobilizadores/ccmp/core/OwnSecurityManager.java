package org.mobilizadores.ccmp.core;

import java.security.Permission;

public class OwnSecurityManager extends SecurityManager {
  SecurityManager _prevMgr = System.getSecurityManager();

  public void checkPermission(Permission perm) {}

  public void checkExit(int status) {
    super.checkExit(status);
    throw new SecurityException(); // FIXME verify alternative for this
  }

  public SecurityManager getPreviousMgr() {
    return _prevMgr;
  }

  public void enableSystemExit() {
    SecurityManager mgr = System.getSecurityManager();
    if ((mgr != null) && (mgr instanceof OwnSecurityManager)) {
      OwnSecurityManager smgr = (OwnSecurityManager) mgr;
      System.setSecurityManager(smgr.getPreviousMgr());
    } else
      System.setSecurityManager(null);
  }
}
