package org.mobilizadores.ccmp;
/*
 * Licensed to Mobilizadores.org under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. Mobilizadores licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.
 * Complete information can be found at: https://dev.mobilizadores.com/licenses
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * You may obtain a copy of the License at: http://www.apache.org/licenses/LICENSE-2.0
 */
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
