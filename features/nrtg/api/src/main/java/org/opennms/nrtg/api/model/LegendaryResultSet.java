/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.nrtg.api.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Deprecated
/**
 * Created with IntelliJ IDEA.
 * User: chris
 * Date: 13.06.12
 * Time: 14:45
 * To change this template use File | Settings | File Templates.
 */
public class LegendaryResultSet implements Serializable {
    private Map<String, String> m_metrics = new HashMap<String, String>();
    private Set<String> m_destinationSet;
    private String m_id, m_site;
    private int m_nodeId;
    private Date m_creationTimestamp = new Date();
    private Date m_finishedTimestamp = null;
    private String m_service;
    private String m_interface;


    public LegendaryResultSet(Set<String> destinationSet) {
        this.m_destinationSet = destinationSet;
    }

    public void addMetric(String metricId, String value) {
        m_metrics.put(metricId, value);
    }

    public Map<String, String> getMetrics() {
        return m_metrics;
    }

    public String getMetricValue(String metricId) {
        return m_metrics.get(metricId);
    }

    public String getDestinationString() {
        String destinationString = "";
        for (String destination : m_destinationSet) {
            if (!"".equals(destinationString))
                destinationString += ", ";
            destinationString += destination;
        }

        return destinationString;
    }

    public String getId() {
        return m_id;
    }

    public void setId(String id) {
        this.m_id = id;
    }

    public void setNodeId(int nodeId) {
        this.m_nodeId = nodeId;
    }

    public int getNodeId() {
        return m_nodeId;
    }

    public String getSite() {
        return m_site;
    }

    public void setSite(String site) {
        this.m_site = site;
    }

    public void setCreationTimestamp(Date creationTimestamp) {
        m_creationTimestamp = creationTimestamp;
    }

    public Date getCreationTimestamp() {
        return m_creationTimestamp;
    }

    public Date getFinishedTimestamp() {
        return m_finishedTimestamp;
    }

    public void setFinishedTimestamp(Date finishedTimestamp) {
        m_finishedTimestamp = finishedTimestamp;
    }

    public String getService() {
        return m_service;
    }

    public void setService(String service) {
        this.m_service = service;
    }

    public String getInterface() {
        return m_interface;
    }

    public void setInterface(String netInterface) {
        this.m_interface = netInterface;
    }
}
