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

import org.opennms.netmgt.provision.persist.requisition.RequisitionCategory;
import org.opennms.netmgt.provision.persist.requisition.RequisitionMonitoredService;

/**
 * OnmsMonitoredServiceRequisition
 *
 * @author brozow
 */
public class OnmsMonitoredServiceRequisition {

    private RequisitionMonitoredService m_svc;
    private final List<OnmsServiceCategoryRequisition> m_categoryReqs;

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

    public void visit(RequisitionVisitor visitor) {
        visitor.visitMonitoredService(this);
        for (OnmsServiceCategoryRequisition cat : m_categoryReqs) {
            cat.visit(visitor);
        }
        visitor.completeMonitoredService(this);
    }

    public String getServiceName() {
        return m_svc.getServiceName();
    }

    
}
