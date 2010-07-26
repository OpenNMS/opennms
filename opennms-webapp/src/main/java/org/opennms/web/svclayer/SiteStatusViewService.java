/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: September 12, 2006
 *
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
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
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 * @since 1.8.1
 */
@Transactional(readOnly=true)
public interface SiteStatusViewService {
    
    /**
     * This method returns method returns a collection of aggregated status of nodes for each
     * device category.  In this case a column in the assets table is used as "site"
     * for which to select devices.  The devices are aggregated by the list of categories specified
     * in categoryGrouping.
     *
     * @param statusViewName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.AggregateStatusView} object.
     */
    AggregateStatusView createAggregateStatusView(String statusViewName);
    
    /**
     * <p>createAggregateStatuses</p>
     *
     * @param statusView a {@link org.opennms.netmgt.model.AggregateStatusView} object.
     * @return a {@link java.util.Collection} object.
     */
    Collection<AggregateStatus> createAggregateStatuses(AggregateStatusView statusView);

    /**
     * <p>createAggregateStatuses</p>
     *
     * @param statusView a {@link org.opennms.netmgt.model.AggregateStatusView} object.
     * @param statusSite a {@link java.lang.String} object.
     * @return a {@link java.util.Collection} object.
     */
    Collection<AggregateStatus> createAggregateStatuses(AggregateStatusView statusView, String statusSite);
    
    /**
     * <p>createAggregateStatusesUsingNodeId</p>
     *
     * @param nodeId a int.
     * @param viewName a {@link java.lang.String} object.
     * @return a {@link java.util.Collection} object.
     */
    Collection<AggregateStatus> createAggregateStatusesUsingNodeId(int nodeId, String viewName);
    
    /**
     * <p>getAggregateStatus</p>
     *
     * @param statusViewName a {@link java.lang.String} object.
     * @param statusSite a {@link java.lang.String} object.
     * @param rowLabel a {@link java.lang.String} object.
     * @return a {@link org.opennms.web.svclayer.AggregateStatus} object.
     */
    AggregateStatus getAggregateStatus(String statusViewName, String statusSite, String rowLabel);

    /**
     * <p>getNodes</p>
     *
     * @param statusViewName a {@link java.lang.String} object.
     * @param statusSite a {@link java.lang.String} object.
     * @param rowLabel a {@link java.lang.String} object.
     * @return a {@link java.util.Collection} object.
     */
    Collection<OnmsNode> getNodes(String statusViewName, String statusSite, String rowLabel);
}
