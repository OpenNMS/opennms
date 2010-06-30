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
 * Created: November 12, 2006
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

import java.io.InputStream;

import org.springframework.transaction.annotation.Transactional;

/**
 * <p>RrdGraphService interface.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 * @since 1.6.12
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
            String report, long start, long end);
    
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
