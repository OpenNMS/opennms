/* -*-mode:java; c-basic-offset:2; -*- */
/* IdentityRepository
 * Copyright (C) 2002,2007 ymnk, JCraft,Inc.
 *  
 * Written by: ymnk<ymnk@jcaft.com>
 *   
 *   
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public License
 * as published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.
   
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 * 
 * You should have received a copy of the GNU Library General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package org.opennms.sandbox.jcterm;

import org.opennms.sandbox.jcterm.agentproxy.connector.*;
import org.opennms.sandbox.jcterm.agentproxy.usocket.*;
import org.opennms.sandbox.jcterm.agentproxy.AgentProxyException;
import org.opennms.sandbox.jcterm.agentproxy.Connector;
import org.opennms.sandbox.jcterm.agentproxy.RemoteIdentityRepository;
import org.opennms.sandbox.jcterm.agentproxy.USocketFactory;

public class JCTermIdentityRepository extends RemoteIdentityRepository {
  private static Connector con;

  static {
    try {
      if(System.getenv("SSH_AUTH_SOCK")!=null){
        USocketFactory usf = new JNAUSocketFactory();
        con = new SSHAgentConnector(usf);
      }
    }
    catch(AgentProxyException e){ System.err.println(e); }

    try {
      if(System.getProperty("os.name").startsWith("Windows"))
        con = new PageantConnector();
    }
    catch(AgentProxyException e){ System.err.println(e); }
  }

  public JCTermIdentityRepository() {
    super(con);
  }

  public int getStatus() {
    if(con == null)
      return NOTRUNNING;
    return super.getStatus();
  }
}
