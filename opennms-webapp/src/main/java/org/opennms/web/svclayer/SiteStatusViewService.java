/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2006, 2008 The OpenNMS Group, Inc.  All rights reserved.
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

package org.opennms.web.svclayer;

import java.util.Collection;

import org.opennms.netmgt.model.AggregateStatusView;
import org.opennms.netmgt.model.OnmsNode;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * Service layer API for the use cases that require the monitoed status of
 * OpenNMS entities (i.e. OnmsNode) to be aggregated with status information
 * that provides color and numberic indicators of status.
 * 
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
@Transactional(readOnly=true)
public interface SiteStatusViewService {
    
    /**
     * This method returns method returns a collection of aggregated status of nodes for each
     * device category.  In this case a column in the assets table is used as "site"
     * for which to select devices.  The devices are aggregated by the list of categories specified
     * in categoryGrouping.
     * 
     * @param columnValue
     * @param defs
     * @return
     */
    AggregateStatusView createAggregateStatusView(String statusViewName);
    
    Collection<AggregateStatus> createAggregateStatuses(AggregateStatusView statusView);

    Collection<AggregateStatus> createAggregateStatuses(AggregateStatusView statusView, String statusSite);
    
    Collection<AggregateStatus> createAggregateStatusesUsingNodeId(int nodeId, String viewName);
    
    AggregateStatus getAggregateStatus(String statusViewName, String statusSite, String rowLabel);

    Collection<OnmsNode> getNodes(String statusViewName, String statusSite, String rowLabel);
}
