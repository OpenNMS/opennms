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

package org.opennms.web.svclayer.api;

import org.opennms.netmgt.model.PrefabGraph;
import org.opennms.web.svclayer.model.GraphResults;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>GraphResultsService interface.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 * @since 1.8.1
 */
@Transactional(readOnly = true)
public interface GraphResultsService {
    /**
     * <p>findResults</p>
     *
     * @param resources an array of {@link java.lang.String} objects.
     * @param reports an array of {@link java.lang.String} objects.
     * @param start a long.
     * @param end a long.
     * @param relativeTime a {@link java.lang.String} object.
     * @return a {@link org.opennms.web.graph.GraphResults} object.
     */
    public GraphResults findResults(String[] resources,
            String[] reports,
            long start, long end, String relativeTime);

    /**
     * <p>getAllPrefabGraphs</p>
     *
     * @param resourceId a {@link java.lang.String} object.
     * @return an array of {@link org.opennms.netmgt.model.PrefabGraph} objects.
     */
    public PrefabGraph[] getAllPrefabGraphs(String resourceId);
}
