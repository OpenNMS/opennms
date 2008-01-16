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
package org.opennms.netmgt.statsd;

import java.util.Date;
import java.util.SortedSet;

import org.opennms.netmgt.model.AttributeStatistic;

/**
 * @author <a href="dj@opennms.org">DJ Gregor</a>
 */
public interface ReportInstance {

    public void walk();

    public SortedSet<AttributeStatistic> getResults();

    public String getResourceTypeMatch();

    public void setResourceTypeMatch(String resourceType);

    public String getAttributeMatch();

    public void setAttributeMatch(String attr);

    public long getStartTime();

    public void setStartTime(long start);

    public long getEndTime();

    public void setEndTime(long end);

    public String getConsolidationFunction();

    public void setConsolidationFunction(String cf);

    public int getCount();

    public void setCount(int count);

    public Date getJobStartedDate();
    
    public Date getJobCompletedDate();

    public String getName();

    public String getDescription();

    public long getRetainInterval();

    public ReportDefinition getReportDefinition();
    
    public void setReportDefinition(ReportDefinition definition);

    public void setResourceAttributeKey(String resourceAttributeKey);
    
    public String getResourceAttributeKey();

    public void setResourceAttributeValueMatch(String resourceAttributeValueMatch);

    public String getResourceAttributeValueMatch();
}
