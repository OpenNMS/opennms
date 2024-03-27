/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
