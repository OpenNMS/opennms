/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.provision.persist;

import java.util.ArrayList;
import java.util.List;

import org.opennms.netmgt.provision.persist.requisition.RequisitionInterface;
import org.opennms.netmgt.provision.persist.requisition.RequisitionMonitoredService;

/**
 * OnmsIpInterfaceRequisition
 *
 * @author brozow
 */
public class OnmsIpInterfaceRequisition {
    
    private RequisitionInterface m_iface;
    private final List<OnmsMonitoredServiceRequisition> m_svcReqs;

    public OnmsIpInterfaceRequisition(RequisitionInterface iface) {
        m_iface = iface;
        m_svcReqs = constructSvcReqs();
    }
    
    RequisitionInterface getInterface() {
        return m_iface;
    }
    
    private List<OnmsMonitoredServiceRequisition> constructSvcReqs() {
        List<OnmsMonitoredServiceRequisition> reqs = new ArrayList<OnmsMonitoredServiceRequisition>(m_iface.getMonitoredServices().size());
        for (RequisitionMonitoredService svc : m_iface.getMonitoredServices()) {
            reqs.add(new OnmsMonitoredServiceRequisition(svc));
        }
        return reqs;
    }

    public void visit(RequisitionVisitor visitor) {
        visitor.visitInterface(this);
        for(OnmsMonitoredServiceRequisition svcReq : m_svcReqs) {
            svcReq.visit(visitor);
        }
        visitor.completeInterface(this);
    }

    public Object getDescr() {
        return m_iface.getDescr();
    }

    public String getIpAddr() {
        return m_iface.getIpAddr();
    }

    public boolean getManaged() {
        return m_iface.isManaged();
    }

    public String getSnmpPrimary() {
        return m_iface.getSnmpPrimary();
    }

    public int getStatus() {
        return m_iface.getStatus();
    }
    
    
    

}
