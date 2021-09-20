/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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
