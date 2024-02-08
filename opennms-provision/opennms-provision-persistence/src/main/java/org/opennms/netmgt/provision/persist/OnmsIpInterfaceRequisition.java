/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.provision.persist;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.provision.persist.requisition.RequisitionInterface;
import org.opennms.netmgt.provision.persist.requisition.RequisitionMetaData;
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
    private final List<OnmsInterfaceMetaDataRequisition> m_metaDataReqs;

    /**
     * <p>Constructor for OnmsIpInterfaceRequisition.</p>
     *
     * @param iface a {@link org.opennms.netmgt.provision.persist.requisition.RequisitionInterface} object.
     */
    public OnmsIpInterfaceRequisition(RequisitionInterface iface) {
        m_iface = iface;
        m_svcReqs = constructSvcReqs();
        m_metaDataReqs = constructMetaDataRequistions();
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

    private List<OnmsInterfaceMetaDataRequisition> constructMetaDataRequistions() {
        return m_iface.getMetaData().stream()
                .map(OnmsInterfaceMetaDataRequisition::new)
                .collect(Collectors.toList());
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

        m_metaDataReqs.forEach(r -> r.visit(visitor));

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

    public InetAddress getIpAddr() {
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
