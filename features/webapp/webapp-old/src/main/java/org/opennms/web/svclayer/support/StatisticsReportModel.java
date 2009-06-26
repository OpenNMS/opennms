/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2002-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified 
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2008 Apr 28: Create separate methods for getting pretty resource objects
 * 				(addresses bug 2406) - jeffg@opennms.org
 * 2008 Feb 16: Make OnmsResource objects returned by Datum.getResource() and
 *              Datum.getResourceParentsReversed() actually PrettyOnmsResource
 *              objects (whose toString method output is human-readable)   
 * 2007 Sep 09: Add support to store a Throwable instead of an OnmsResource. - dj@opennms.org
 * 2007 Apr 10: Created this file. - dj@opennms.org
 * 
 * Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
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
package org.opennms.web.svclayer.support;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.StatisticsReport;
import org.opennms.web.controller.statisticsReports.PrettyOnmsResource;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.util.Assert;
import org.springframework.validation.BindException;

/**
 * Model object for web statistics reports.
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class StatisticsReportModel {
    public static class Datum implements Comparable<Datum> {
        private Double m_value;
        private OnmsResource m_resource;
        private Throwable m_resourceThrowable;
    
        public int compareTo(Datum o) {
            return m_value.compareTo(o.getValue());
        }
    
        public OnmsResource getResource() {
            return m_resource;
        }
        
        public OnmsResource getPrettyResource() {
        	return new PrettyOnmsResource(m_resource);
        }
    
        public void setResource(OnmsResource resource) {
            m_resource = resource;
        }
        
        public String getResourceParentLabel() {
            Assert.state(m_resource != null, "the resource must be set before calling this method");
            
            StringBuffer buffer = new StringBuffer();
            
            OnmsResource parent = m_resource.getParent();
            while (parent != null) {
                if (buffer.length() > 0) {
                    buffer.append("<br/>");
                }
                buffer.append(parent.getResourceType().getLabel());
                buffer.append(": ");
                buffer.append(parent.getLabel());
                
                parent = parent.getParent();
            }
            
            return buffer.toString();
        }
        
        public List<OnmsResource> getResourceParentsReversed() {
            if (m_resource == null) {
                return new ArrayList<OnmsResource>(0);
            }
            
            List<OnmsResource> resources = new ArrayList<OnmsResource>();
            
            OnmsResource parent = m_resource.getParent();
            while (parent != null) {
                resources.add(0, parent);
                parent = parent.getParent();
            }
            
            return resources;
        }
        
        public List<OnmsResource> getPrettyResourceParentsReversed() {
            if (m_resource == null) {
                return new ArrayList<OnmsResource>(0);
            }
            
            List<OnmsResource> resources = new ArrayList<OnmsResource>();
            
            OnmsResource parent = new PrettyOnmsResource(m_resource.getParent());
            while (parent != null) {
                resources.add(0, new PrettyOnmsResource(parent));
                parent = parent.getParent();
            }
            
            return resources;
        }
        
        public String getResourceThrowableId() {
            Throwable t = getResourceThrowable();
            
            if (t == null) {
                return null;
            } else if (t instanceof ObjectRetrievalFailureException) {
                return ((ObjectRetrievalFailureException) t).getIdentifier().toString();
            } else {
                return "No identifier";
            }
        }
    
        public Double getValue() {
            return m_value;
        }
    
        public void setValue(Double value) {
            m_value = value;
        }

        public void setResourceThrowable(Throwable resourceThrowable) {
            m_resourceThrowable = resourceThrowable;
        }
        
        public Throwable getResourceThrowable() {
            return m_resourceThrowable;
        }
    }
    
    private BindException m_errors;
    private StatisticsReport m_report;
    private SortedSet<Datum> m_data = new TreeSet<Datum>();
    
    public SortedSet<Datum> getData() {
        return m_data;
    }
    public void setData(SortedSet<StatisticsReportModel.Datum> data) {
        m_data = data;
    }
    public void addData(Datum datum) {
        m_data.add(datum);
    }
    public BindException getErrors() {
        return m_errors;
    }
    public void setErrors(BindException errors) {
        m_errors = errors;
    }
    public StatisticsReport getReport() {
        return m_report;
    }
    public void setReport(StatisticsReport report) {
        m_report = report;
    }
    
}