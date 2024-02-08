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

import java.util.List;

import org.opennms.web.svclayer.model.ProgressMonitor;
import org.opennms.web.svclayer.model.SimpleWebTable;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * Class designed for gathering Aggreate Status of nodes to be displayed
 * in a cross sectional view of categories.  This service provides the objects
 * requried for a view used by a surveillance/operations team within a NOC.
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 */
@Transactional(readOnly=true)
public interface SurveillanceService {

    /**
     * <p>createSurveillanceTable</p>
     *
     * @param surveillanceViewName a {@link java.lang.String} object.
     * @param progressMonitor a {@link org.opennms.web.svclayer.model.ProgressMonitor} object.
     * @return a {@link org.opennms.web.svclayer.model.SimpleWebTable} object.
     */
    public SimpleWebTable createSurveillanceTable(String surveillanceViewName, ProgressMonitor progressMonitor);
    
    /**
     * <p>getHeaderRefreshSeconds</p>
     *
     * @param viewName a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public Integer getHeaderRefreshSeconds(String viewName);

    /**
     * <p>isViewName</p>
     *
     * @param viewName a {@link java.lang.String} object.
     * @return a boolean.
     */
    public boolean isViewName(String viewName);
    
    /**
     * <p>getViewNames</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<String> getViewNames();
}
