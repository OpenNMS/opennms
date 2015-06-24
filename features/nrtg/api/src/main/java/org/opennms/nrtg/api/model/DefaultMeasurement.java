/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.nrtg.api.model;

import java.util.Date;

/**
 * {@inheritDoc}
 * @author Christian Pape
 * @author Markus Neumann
 */
public class DefaultMeasurement implements Measurement {

    private static final long serialVersionUID = -7788974682113621268L;
    private String m_interface, m_service, m_metricId, m_value, m_metricType;
    private int m_nodeId;
    private Date m_timestamp;
    private String m_onmsLogicMetricId;

    @Override
    public void setNodeId(int nodeId) {
        m_nodeId = nodeId;
    }

    @Override
    public void setNetInterface(String theInterface) {
        m_interface = theInterface;
    }

    @Override
    public void setService(String service) {
        m_service = service;
    }

    @Override
    public void setMetricId(String metricId) {
        m_metricId = metricId;
    }

    @Override
    public void setMetricType(String metricType) {
        m_metricType = metricType;
    }

    @Override
    public void setValue(String value) {
        m_value = value;
    }

    @Override
    public void setTimestamp(Date timestamp) {
        m_timestamp = timestamp;
    }

    @Override
    public int getNodeId() {
        return m_nodeId;
    }

    @Override
    public String getNetInterface() {
        return m_interface;
    }

    @Override
    public String getService() {
        return m_service;
    }

    @Override
    public String getMetricId() {
        return m_metricId;
    }

    @Override
    public String getMetricType() {
        return m_metricType;
    }

    @Override
    public String getValue() {
        return m_value;
    }

    @Override
    public Date getTimestamp() {
        return m_timestamp;
    }

    @Override
    public void setOnmsLogicMetricId(String onmsLogicMetricId) {
        m_onmsLogicMetricId = onmsLogicMetricId;
    }

    @Override
    public String getOnmsLogicMetricId() {
        return m_onmsLogicMetricId;
    }


    @Override
    public String toString() {
        return "DefaultMeasurement{" + "m_interface=" + m_interface + ", m_service=" + m_service + ", m_metricId=" + m_metricId + ", m_metricType=" + m_metricType + ", m_value=" + m_value + ", m_nodeId=" + m_nodeId + ", m_timestamp=" + m_timestamp + '}';
    }
}
