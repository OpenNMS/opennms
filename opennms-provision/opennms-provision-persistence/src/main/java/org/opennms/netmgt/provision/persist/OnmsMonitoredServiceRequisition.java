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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.opennms.netmgt.provision.persist.requisition.RequisitionCategory;
import org.opennms.netmgt.provision.persist.requisition.RequisitionMetaData;
import org.opennms.netmgt.provision.persist.requisition.RequisitionMonitoredService;

/**
 * OnmsMonitoredServiceRequisition
 *
 * @author brozow
 * @version $Id: $
 */
public class OnmsMonitoredServiceRequisition {

    private RequisitionMonitoredService m_svc;
    private final List<OnmsServiceCategoryRequisition> m_categoryReqs;
    private final List<OnmsServiceMetaDataRequisition> m_metaDataReqs;

    /**
     * <p>Constructor for OnmsMonitoredServiceRequisition.</p>
     *
     * @param svc a {@link org.opennms.netmgt.provision.persist.requisition.RequisitionMonitoredService} object.
     */
    public OnmsMonitoredServiceRequisition(RequisitionMonitoredService svc) {
        m_svc = svc;
        m_categoryReqs = constructCategoryReqs();
        m_metaDataReqs = constructMetaDataRequistions();
    }
    
    private List<OnmsServiceCategoryRequisition> constructCategoryReqs() {
        List<OnmsServiceCategoryRequisition> reqs = new ArrayList<OnmsServiceCategoryRequisition>(m_svc.getCategories().size());
        for (RequisitionCategory cat : m_svc.getCategories()) {
            reqs.add(new OnmsServiceCategoryRequisition(cat));
        }
        return reqs;
    }

    private List<OnmsServiceMetaDataRequisition> constructMetaDataRequistions() {
        return m_svc.getMetaData().stream()
                .map(OnmsServiceMetaDataRequisition::new)
                .collect(Collectors.toList());
    }

    /**
     * @return the svc
     */
    RequisitionMonitoredService getSvc() {
        return m_svc;
    }

    /**
     * <p>visit</p>
     *
     * @param visitor a {@link org.opennms.netmgt.provision.persist.RequisitionVisitor} object.
     */
    public void visit(RequisitionVisitor visitor) {
        visitor.visitMonitoredService(this);
        for (OnmsServiceCategoryRequisition cat : m_categoryReqs) {
            cat.visit(visitor);
        }

        m_metaDataReqs.forEach(r -> r.visit(visitor));

        visitor.completeMonitoredService(this);
    }

    /**
     * <p>getServiceName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getServiceName() {
        return m_svc.getServiceName();
    }

    
}
