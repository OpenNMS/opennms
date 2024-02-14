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
import java.util.List;

/**
 * @author Markus Neumann
 * @author Christian Pape
 */
public class DefaultMeasurementSet implements MeasurementSet {

    private static final long serialVersionUID = 7536809905120941525L;
    private List<Measurement> m_measurements = new ArrayList<>();

    public void addMeasurement(Measurement measurement) {
        m_measurements.add(measurement);
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
            buf.append("\"metricId\"").append(":\"").append(m.getMetricId()).append("\"").append(",");
            buf.append("\"metricType\"").append(":\"").append(m.getMetricType()).append("\"").append(",");
            buf.append("\"netInterface\"").append(":\"").append(m.getNetInterface()).append("\"").append(",");
            buf.append("\"nodeId\"").append(":").append(m.getNodeId()).append(",");
            buf.append("\"service\"").append(":\"").append(m.getService()).append("\"").append(",");
            buf.append("\"timeStamp\"").append(":").append(m.getTimestamp().getTime()).append(",");
            buf.append("\"onmsLogicMetricId\"").append(":\"").append(m.getOnmsLogicMetricId()).append("\",");
            buf.append("\"value\"").append(":").append(m.getValue());
            buf.append("}");
        }

        buf.append("]");
        return buf.toString();
    }

    @Override
    public List<Measurement> getMeasurements() {
        return m_measurements;
    }

    /**
     * This toString method is for displaying reasons in the webapp NrtGrapher only. It's for prototyping only.
     *
     * @return a {@link String} that contains the metrics and there values in a easy parsable way.
     */
    @Override
    public String toString() {
        return this.getJson();
    }
}
