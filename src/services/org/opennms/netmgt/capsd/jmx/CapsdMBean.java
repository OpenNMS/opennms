//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
// Tab Stop = 8
//

package org.opennms.netmgt.capsd.jmx;

import java.net.UnknownHostException;

public interface CapsdMBean {
    public void init();

    public void start();

    public void stop();

    public int getStatus();

    public String getStatusText();

    public String status();

    /**
     * This method is used by other managed beans to forward an IP Address for
     * capability scanning. The If the interface converts properly then it is
     * scanned as a suspect interface for the discovery of all the services and
     * other interfaces that exists on the node.
     * 
     * @param ifAddr
     *            The address of the suspect interface.
     * 
     * @throws java.net.UnknownHostException
     *             Thrown if the address cannot be converted to aa proper
     *             internet address.
     */
    public void scanSuspectInterface(String ifAddr) throws UnknownHostException;

    /**
     * This method is used to force an existing node to be capability rescaned.
     * The main reason for its existance is as a hook for JMX managed beans to
     * invoke forced rescans allowing the main rescan logic to remain in the
     * capsd agent.
     * 
     * @param nodeId
     *            The node identifier from the database.
     */
    public void rescanInterfaceParent(Integer nodeId);
}
