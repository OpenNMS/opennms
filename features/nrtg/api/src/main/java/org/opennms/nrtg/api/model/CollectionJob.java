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

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * Defines a collection job for a satellite.
 * </p>
 * <p>
 * The smallest sensible job unit to collect in one action.
 * </p>
 * <p>
 * A set of metrics from one interface in one technology at one time.
 * </p>
 * <p>
 * The satellite calls the responsible protocol handler. The protocol handler
 * can optimize the collection of metrics depending on protocol specifics
 * (one/multiple connection, one/multiple session, one/multiple call ...).
 * </p>
 *
 * @author Simon Walter
 */
public interface CollectionJob extends Serializable {

    Set<String> getAllMetrics();

    void setMetricValue(String metricId, String metricType, String value) throws IllegalArgumentException;

    void setMetricValue(String metricId, String value) throws IllegalArgumentException;

    String getMetricValue(String metricId) throws IllegalArgumentException;

    void addMetric(String metricId, Set<String> destinationSet, String onmsLogicMetricId) throws IllegalArgumentException;

    void setParameters(Map<String, Object> parameters);

    Map<String, Object> getParameters();

    MeasurementSet getMeasurementSet();

    void setId(String id);

    String getId();

    void setNodeId(int nodeId);

    int getNodeId();

    void setSite(String site);

    String getSite();

    void setCreationTimestamp(Date creationTimestamp);

    Date getCreationTimestamp();

    void setFinishedTimestamp(Date finishedTimestamp);

    Date getFinishedTimestamp();

    void setNetInterface(String theInterface);

    //ToDo tak change to InetAddress
    String getNetInterface();

    String getService();

    void setService(String service);

    Map<String, MeasurementSet> getMeasurementSetsByDestination();

    void setProtocolConfiguration(String configurationString);

    String getProtocolConfiguration();

    String getMetricType(String metricId) throws IllegalArgumentException;

    String getOnmsLogicMetricId(String metricId);

}
