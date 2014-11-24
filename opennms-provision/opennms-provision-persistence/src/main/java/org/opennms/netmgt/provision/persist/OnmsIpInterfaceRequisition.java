/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.provision.persist;

import java.util.ArrayList;
import java.util.List;

import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.provision.persist.requisition.RequisitionInterface;
import org.opennms.netmgt.provision.persist.requisition.RequisitionMonitoredService;

/**
 * OnmsIpInterfaceRequisition
 *
 * @author brozow
 * @version $Id: $
 */
public class OnmsIpInterfaceRequisition {
    
    private RequisitionInterface m_iface;
    private final List<OnmsMonitoredServiceRequisition> m_svcReqs;

    /**
     * <p>Constructor for OnmsIpInterfaceRequisition.</p>
     *
     * @param iface a {@link org.opennms.netmgt.provision.persist.requisition.RequisitionInterface} object.
     */
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

    /**
     * <p>visit</p>
     *
     * @param visitor a {@link org.opennms.netmgt.provision.persist.RequisitionVisitor} object.
     */
    public void visit(RequisitionVisitor visitor) {
        visitor.visitInterface(this);
        for(OnmsMonitoredServiceRequisition svcReq : m_svcReqs) {
            svcReq.visit(visitor);
        }
        visitor.completeInterface(this);
    }

    /**
     * <p>getDescr</p>
     *
     * @return a {@link java.lang.Object} object.
     */
    public Object getDescr() {
        return m_iface.getDescr();
    }

    /**
     * <p>getIpAddr</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getIpAddr() {
        return m_iface.getIpAddr();
    }

    /**
     * <p>getManaged</p>
     *
     * @return a boolean.
     */
    public boolean getManaged() {
        return m_iface.isManaged();
    }

    /**
     * <p>getSnmpPrimary</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public PrimaryType getSnmpPrimary() {
        return m_iface.getSnmpPrimary();
    }

    /**
     * <p>getStatus</p>
     *
     * @return a int.
     */
    public int getStatus() {
        return m_iface.getStatus();
    }
    
    
    

}
