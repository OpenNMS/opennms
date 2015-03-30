/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.svclayer;

import java.util.Collection;

import org.opennms.netmgt.model.AggregateStatusView;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.web.svclayer.model.AggregateStatus;
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
     * @return a {@link org.opennms.web.svclayer.model.AggregateStatus} object.
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
