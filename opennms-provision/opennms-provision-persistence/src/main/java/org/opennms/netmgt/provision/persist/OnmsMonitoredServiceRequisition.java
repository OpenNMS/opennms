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

import org.opennms.netmgt.provision.persist.requisition.RequisitionCategory;
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

    /**
     * <p>Constructor for OnmsMonitoredServiceRequisition.</p>
     *
     * @param svc a {@link org.opennms.netmgt.provision.persist.requisition.RequisitionMonitoredService} object.
     */
    public OnmsMonitoredServiceRequisition(RequisitionMonitoredService svc) {
        m_svc = svc;
        m_categoryReqs = constructCategoryReqs();
    }
    
    private List<OnmsServiceCategoryRequisition> constructCategoryReqs() {
        List<OnmsServiceCategoryRequisition> reqs = new ArrayList<OnmsServiceCategoryRequisition>(m_svc.getCategories().size());
        for (RequisitionCategory cat : m_svc.getCategories()) {
            reqs.add(new OnmsServiceCategoryRequisition(cat));
        }
        return reqs;

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
