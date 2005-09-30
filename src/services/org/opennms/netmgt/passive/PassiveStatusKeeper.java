//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.passive;

import java.util.HashMap;
import java.util.Map;

import org.opennms.netmgt.daemon.ServiceDaemon;
import org.opennms.netmgt.poller.pollables.PollStatus;

public class PassiveStatusKeeper extends ServiceDaemon {
    
    private static Map m_statusTable = new HashMap();

    public void init() {
        
    }

    public void start() {
        // TODO Auto-generated method stub
        
    }

    public void stop() {
        // TODO Auto-generated method stub
        
    }

    public String getName() {
        return "OpenNMS.PassiveStatusKeeper";    }

    public void pause() {
        // TODO Auto-generated method stub
        
    }

    public void resume() {
        // TODO Auto-generated method stub
        
    }

    public static void setStatus(String nodeLabel, String ipAddr, String svcName, PollStatus pollStatus) {
        m_statusTable .put(nodeLabel+":"+ipAddr+":"+svcName, pollStatus);
    }

    public static Object getStatus(String nodeLabel, String ipAddr, String svcName) {
        PollStatus status = (PollStatus) m_statusTable.get(nodeLabel+":"+ipAddr+":"+svcName);
        return (status == null ? PollStatus.STATUS_UNKNOWN : status);
    }

}
