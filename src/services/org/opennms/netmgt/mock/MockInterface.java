//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2004 The OpenNMS Group, Inc.  All rights reserved.
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

package org.opennms.netmgt.mock;

import java.util.List;

import org.opennms.netmgt.poller.ServiceMonitor;

/**
 * @author brozow
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class MockInterface extends MockContainer {

    String m_ipAddr;

    /**
     * @param ipAddr
     */
    public MockInterface(MockNode node, String ipAddr) {
        super(node);
        m_ipAddr = ipAddr;
    }

    /**
     * @param svcName
     */
    public MockService addService(String svcName, int serviceId) {
        return (MockService) addMember(new MockService(this, svcName, serviceId));
    }

    /**
     * @return
     */
    public String getIpAddr() {
        return m_ipAddr;
    }

    Object getKey() {
        return m_ipAddr;
    }

    public MockNetwork getNetwork() {
        return getNode().getNetwork();
    }

    /**
     * @return
     */
    public MockNode getNode() {
        return (MockNode) getParent();
    }

    public int getNodeId() {
        return getNode().getNodeId();
    }

    public String getNodeLabel() {
        return getNode().getLabel();
    }

    public int getPollStatus() {
        final String critSvc = getNetwork().getCriticalService();
        class IFStatusCalculator extends MockVisitorAdapter {
            int status = ServiceMonitor.SERVICE_UNAVAILABLE;

            public int getStatus() {
                return status;
            }

            public void visitService(MockService svc) {
                if (critSvc == null || critSvc.equals(svc.getName())) {
                    if (svc.getPollStatus() == ServiceMonitor.SERVICE_AVAILABLE)
                        status = ServiceMonitor.SERVICE_AVAILABLE;
                }
            }

        }
        ;
        IFStatusCalculator calc = new IFStatusCalculator();
        visit(calc);
        return calc.getStatus();
    }

    /**
     * @param svcName
     */
    public MockService getService(String svcName) {
        return (MockService) getMember(svcName);
    }

    /**
     * @return
     */
    public List getServices() {
        return getMembers();
    }

    /**
     * @param svc
     */
    public void removeService(MockService svc) {
        removeMember(svc);
    }

    public String toString() {
        return "If[" + m_ipAddr + "]";
    }

    /**
     * @param v
     */
    public void visit(MockVisitor v) {
        super.visit(v);
        v.visitInterface(this);
        visitMembers(v);
    }

}
