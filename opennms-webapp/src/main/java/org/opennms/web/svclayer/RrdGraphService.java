/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.svclayer;

import java.io.InputStream;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>RrdGraphService interface.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 * @since 1.8.1
 */
@Transactional(readOnly = true)
public interface RrdGraphService {
    /**
     * <p>getPrefabGraph</p>
     *
     * @param resourceId a {@link java.lang.String} object.
     * @param report a {@link java.lang.String} object.
     * @param start a long.
     * @param end a long.
     * @return a {@link java.io.InputStream} object.
     */
    public InputStream getPrefabGraph(String resourceId,
            String report, long start, long end, Integer width, Integer height);
    
    /**
     * <p>getAdhocGraph</p>
     *
     * @param resourceId a {@link java.lang.String} object.
     * @param title a {@link java.lang.String} object.
     * @param dataSources an array of {@link java.lang.String} objects.
     * @param aggregateFunctions an array of {@link java.lang.String} objects.
     * @param colors an array of {@link java.lang.String} objects.
     * @param dataSourceTitles an array of {@link java.lang.String} objects.
     * @param styles an array of {@link java.lang.String} objects.
     * @param start a long.
     * @param end a long.
     * @return a {@link java.io.InputStream} object.
     */
    public InputStream getAdhocGraph(String resourceId,
            String title, String[] dataSources, String[] aggregateFunctions,
            String[] colors, String[] dataSourceTitles, String[] styles,
            long start, long end);
}
