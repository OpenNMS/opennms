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
