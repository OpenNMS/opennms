/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2007 May 16: Added fetch methods. - dj@opennms.org
 * 2007 Apr 05: Created this file. - dj@opennms.org
 *
 * Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
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
package org.opennms.netmgt.dao;

import java.io.File;
import java.io.InputStream;

import org.opennms.netmgt.model.OnmsAttribute;
import org.springframework.dao.DataAccessException;

/**
 * <p>RrdDao interface.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 */
public interface RrdDao {

    /**
     * Get the value for an attribute over a period of time.
     *
     * @param attribute the attribute
     * @param cf consolidation function (usually "AVERAGE")
     * @param start start time in milliseconds
     * @param end end time in milliseconds
     * @return value
     */
    public double getPrintValue(OnmsAttribute attribute, String cf, long start, long end);
    
    /**
     * Get the value for an attribute over a period of time.
     *
     * @param attribute the attribute
     * @param rraConsolidationFunction consolidation function (usually "AVERAGE")
     * @param startTimeInMillis start time in milliseconds
     * @param endTimeInMillis end time in milliseconds
     * @return value
     * @param printFunctions a {@link java.lang.String} object.
     */
    public double[] getPrintValues(OnmsAttribute attribute,String rraConsolidationFunction, 
			long startTimeInMillis, long endTimeInMillis, String... printFunctions);

    /**
     * Create an RRD graph with the given command where RRD files are relative to the workDir.
     *
     * @param command RRD graph command
     * @param workDir RRD files are relative to this directory
     * @return PNG graph image
     */
    public InputStream createGraph(String command, File workDir);

    /**
     * Gets the offset of the top of the graph box from the top of the image.
     *
     * @return offset in pixels
     */
    public int getGraphTopOffsetWithText();

    /**
     * Gets the offset of the left side of the graph box from the left side of the image.
     *
     * @return offset in pixels
     */
    public int getGraphLeftOffset();

    /**
     * Gets the offset of the right side of the graph box from the right side of the image.
     *
     * @return offset in pixels
     */
    public int getGraphRightOffset();

    /**
     * This method issues an round robin fetch command to retrieve the last
     * value of the data source stored in the specified RRD file.
     * NOTE: This method assumes that each RRD file contains a single
     * data source.
     *
     * @param attribute
     *            The attribute for which fetch the last value.  Must be a
     *            RrdGraphAttribute.
     * @param interval
     *            Fetch interval.  This should equal RRD step size.
     * @return Retrived value or null if some errors occur
     * @throws org.springframework.dao.DataAccessException
     *             if an error occurs retrieving the last value
     */
    public Double getLastFetchValue(OnmsAttribute attribute, int interval) throws DataAccessException;
    
    /**
     * This method issues an round robin fetch command to retrieve the last
     * value of the data source stored in the specified RRD file.
     * NOTE: This method assumes that each RRD file contains a single
     * data source.
     *
     * @param attribute
     *            The attribute for which fetch the last value.  Must be a
     *            RrdGraphAttribute.
     * @param interval
     *            Fetch interval in milliseconds.  This should equal the RRD
     *            step size.
     * @param range
     *            Interval in milliseconds for how long we should look back
     *            in time for a non-NaN value.  This should a multiple of
     *            the RRD step size.
     * @return Retrived value or null if some errors occur
     * @throws org.springframework.dao.DataAccessException
     *             if an error occurs retrieving the last value
     */
    public Double getLastFetchValue(OnmsAttribute attribute, int interval, int range)throws DataAccessException;

}
