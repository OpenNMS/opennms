/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.statsd;

import java.util.Date;
import java.util.SortedSet;

import org.opennms.netmgt.model.AttributeStatistic;

/**
 * <p>ReportInstance interface.</p>
 *
 * @author <a href="dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 */
public interface ReportInstance {

    /**
     * <p>walk</p>
     */
    public void walk();

    /**
     * <p>getResults</p>
     *
     * @return a {@link java.util.SortedSet} object.
     */
    public SortedSet<AttributeStatistic> getResults();

    /**
     * <p>getResourceTypeMatch</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getResourceTypeMatch();

    /**
     * <p>setResourceTypeMatch</p>
     *
     * @param resourceType a {@link java.lang.String} object.
     */
    public void setResourceTypeMatch(String resourceType);

    /**
     * <p>getAttributeMatch</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getAttributeMatch();

    /**
     * <p>setAttributeMatch</p>
     *
     * @param attr a {@link java.lang.String} object.
     */
    public void setAttributeMatch(String attr);

    /**
     * <p>getStartTime</p>
     *
     * @return a long.
     */
    public long getStartTime();

    /**
     * <p>setStartTime</p>
     *
     * @param start a long.
     */
    public void setStartTime(long start);

    /**
     * <p>getEndTime</p>
     *
     * @return a long.
     */
    public long getEndTime();

    /**
     * <p>setEndTime</p>
     *
     * @param end a long.
     */
    public void setEndTime(long end);

    /**
     * <p>getConsolidationFunction</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getConsolidationFunction();

    /**
     * <p>setConsolidationFunction</p>
     *
     * @param cf a {@link java.lang.String} object.
     */
    public void setConsolidationFunction(String cf);

    /**
     * <p>getCount</p>
     *
     * @return a int.
     */
    public int getCount();

    /**
     * <p>setCount</p>
     *
     * @param count a int.
     */
    public void setCount(int count);

    /**
     * <p>getJobStartedDate</p>
     *
     * @return a {@link java.util.Date} object.
     */
    public Date getJobStartedDate();
    
    /**
     * <p>getJobCompletedDate</p>
     *
     * @return a {@link java.util.Date} object.
     */
    public Date getJobCompletedDate();

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getName();

    /**
     * <p>getDescription</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDescription();

    /**
     * <p>getRetainInterval</p>
     *
     * @return a long.
     */
    public long getRetainInterval();

    /**
     * <p>getReportDefinition</p>
     *
     * @return a {@link org.opennms.netmgt.statsd.ReportDefinition} object.
     */
    public ReportDefinition getReportDefinition();
    
    /**
     * <p>setReportDefinition</p>
     *
     * @param definition a {@link org.opennms.netmgt.statsd.ReportDefinition} object.
     */
    public void setReportDefinition(ReportDefinition definition);

    /**
     * <p>setResourceAttributeKey</p>
     *
     * @param resourceAttributeKey a {@link java.lang.String} object.
     */
    public void setResourceAttributeKey(String resourceAttributeKey);
    
    /**
     * <p>getResourceAttributeKey</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getResourceAttributeKey();

    /**
     * <p>setResourceAttributeValueMatch</p>
     *
     * @param resourceAttributeValueMatch a {@link java.lang.String} object.
     */
    public void setResourceAttributeValueMatch(String resourceAttributeValueMatch);

    /**
     * <p>getResourceAttributeValueMatch</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getResourceAttributeValueMatch();
}
