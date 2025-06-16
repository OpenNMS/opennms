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


import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Result of a {@link CollectionJob}. List of metricId/values pairs with minimal redundancy (timestamp, nodeId, service
 * and interface are stored only once). The list of {@link Measurement} will be generated on the fly.
 * 
 * @author Christian Pape
 * @author Markus Neumann
*/
public class LightweightMeasurementSet implements MeasurementSet {
    private static final long serialVersionUID = 1166779403641774595L;
    private Map<String, ArrayList<String>> m_values = new HashMap<String, ArrayList<String>>();
    private int m_nodeId;
    private String m_interface, m_service;
    private Date m_timestamp = new Date();

    public LightweightMeasurementSet() {
    }

    public LightweightMeasurementSet(int nodeId, String service, String theInterface, Date timestamp) {
        setNodeId(nodeId);
        setService(service);
        setNetInterface(theInterface);
        setTimestamp(timestamp);
    }

    public void addMeasurement(String metricId, String metricType, String value, String onmsLogicMetricId) {
        ArrayList<String> valueTypeList = new ArrayList<String>(3);

        valueTypeList.add(metricType);
        valueTypeList.add(value);
        valueTypeList.add(onmsLogicMetricId);

        m_values.put(metricId, valueTypeList);
    }

    @Override
    public List<Measurement> getMeasurements() {
        ArrayList<Measurement> measurements = new ArrayList<Measurement>();

        for (String metricId : m_values.keySet()) {
            Measurement measurement = new DefaultMeasurement();

            measurement.setTimestamp(getTimestamp());
            measurement.setNetInterface(getNetInterface());
            measurement.setNodeId(getNodeId());
            measurement.setService(getService());
            measurement.setMetricId(metricId);

            ArrayList<String> valueTypeList = m_values.get(metricId);

            measurement.setMetricType(valueTypeList.get(0));
            measurement.setValue(valueTypeList.get(1));
            measurement.setOnmsLogicMetricId(valueTypeList.get(2));

            measurements.add(measurement);
        }

        return measurements;
    }

    public void setNodeId(int nodeId) {
        m_nodeId = nodeId;
    }

    public void setNetInterface(String theInterface) {
        m_interface = theInterface;
    }

    public void setService(String service) {
        m_service = service;
    }

    public void setTimestamp(Date timestamp) {
        m_timestamp = timestamp;
    }

    public int getNodeId() {
        return m_nodeId;
    }

    public String getNetInterface() {
        return m_interface;
    }

    public String getService() {
        return m_service;
    }

    public Date getTimestamp() {
        return m_timestamp;
    }

    @Override
    public String getJson() {
        final StringBuilder buf = new StringBuilder("[");

        boolean first = true;
        for (Measurement m : getMeasurements()) {
            if (!first) {
                buf.append(",");
            } else {
                first = false;
            }
            buf.append("{");
            buf.append("\"metricId\"").append(":\"").append(m.getMetricId()).append("\",");
            buf.append("\"metricType\"").append(":\"").append(m.getMetricType()).append("\",");
            buf.append("\"netInterface\"").append(":\"").append(m.getNetInterface()).append("\",");
            buf.append("\"nodeId\"").append(":").append(m.getNodeId()).append(",");
            buf.append("\"service\"").append(":\"").append(m.getService()).append("\",");
            buf.append("\"timeStamp\"").append(":").append(m.getTimestamp().getTime()).append(",");
            buf.append("\"onmsLogicMetricId\"").append(":\"").append(m.getOnmsLogicMetricId()).append("\",");
            buf.append("\"value\"").append(":").append(m.getValue());
            buf.append("}");
        }

        buf.append("]");
        return buf.toString();
    }

    /**
     * This toString method is for displaying reasons in the webapp NrtGrapher only.
     * It's for prototyping only.
     *
     * @return a {@link String} that contains the metrics and there values in a easy parsable way.
     */
    @Override
    public String toString() {
        return this.getJson();
    }
}
