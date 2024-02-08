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
package org.opennms.netmgt.bsm.service;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.bsm.service.model.AlarmWrapper;
import org.opennms.netmgt.bsm.service.model.Application;
import org.opennms.netmgt.bsm.service.model.BusinessService;
import org.opennms.netmgt.bsm.service.model.IpService;
import org.opennms.netmgt.bsm.service.model.Status;
import org.opennms.netmgt.bsm.service.model.edge.Edge;
import org.opennms.netmgt.bsm.service.model.functions.reduce.Threshold;
import org.opennms.netmgt.bsm.service.model.functions.reduce.ThresholdResultExplanation;
import org.opennms.netmgt.bsm.service.model.graph.BusinessServiceGraph;
import org.opennms.netmgt.bsm.service.model.graph.GraphEdge;
import org.opennms.netmgt.bsm.service.model.graph.GraphVertex;

public interface BusinessServiceStateMachine {

    /**
     * Sets the list of Business Services that need to managed by the state machine.
     *
     * @param businessServices list of services to manage
     */
    void setBusinessServices(List<BusinessService> businessServices);

    /**
     * Retrieves the current operational status of a Business Service.
     *
     * @param businessService Business Service to query
     * @return the current operational status, or null if the Business Service if not managed by the state machine
     */
    Status getOperationalStatus(BusinessService businessService);

    /**
     * Retrieves the current operational status of a particular IP service.
     *
     * @param ipService IP Service to query
     * @return the current operational status, or null if the IP Service is not monitored by the state machine
     */
    Status getOperationalStatus(IpService ipService);

    /**
     * Retrieves the current operational status of a particular reduction key.
     *
     * @param reductionKey reduction key to query for
     * @return the current operational status, or null if the Reduction Key is not monitored by the state machine
     */
    Status getOperationalStatus(String reductionKey);

    /**
     * Retrieves the current operational status of the element associated with a particular Edge.
     * A call to this method is equal to a call to {@link #getOperationalStatus(String)},
     * {@link #getOperationalStatus(IpService)} or {@link #getOperationalStatus(BusinessService)} depending
     * on the type of the edge.
     *
     * This method DOES NOT return the mapped status of the edge.
     *
     * @param edge edge to query for
     * @return the current operational status, or null if the Edge is not monitored by the state machine
     */
    Status getOperationalStatus(Edge edge);

    /**
     * Updates the states of the Business Services.
     */
    void handleNewOrUpdatedAlarm(AlarmWrapper alarm);

    /**
     * Updates the states of the Business Services using the given list of alarms.
     *
     * The given list of alarms is expected to be the complete set of current alarms,
     * and any alarms missing from this list will be treated as not being present.
     *
     */
    void handleAllAlarms(List<AlarmWrapper> alarms);

    /**
     * Registers a state change handler.
     *
     * @param handler handler to register
     * @param attributes map of service attributes, required for compatibility with the ONMS-OSGi bridge.
     */
    void addHandler(BusinessServiceStateChangeHandler handler, Map<String, String> attributes);

    /**
     * Unregisters a state change handler.
     *
     * @param handler handler to unregister
     * @param attributes map of service attributes, required for compatibility with the ONMS-OSGi bridge.
     * @return true of the handler was previously registered, and false otherwise
     */
    boolean removeHandler(BusinessServiceStateChangeHandler handler, Map<String, String> attributes);

    void renderGraphToPng(File target);

    /**
     * This returns the actual graph of the {@link BusinessServiceStateMachine}.
     *
     * Please DO NOT MODIFY any object in that graph.
     *
     * @return the actual graph of the {@link BusinessServiceStateMachine}. DO NOT MODIFY!
     */
    BusinessServiceGraph getGraph();

    BusinessServiceStateMachine clone(boolean preserveState);

    List<GraphVertex> calculateRootCause(BusinessService businessService);

    Set<GraphEdge> calculateImpacting(BusinessService businessService);

    List<GraphVertex> calculateImpact(BusinessService businessService);

    List<GraphVertex> calculateImpact(IpService ipService);

    List<GraphVertex> calculateImpact(String reductionKey);

    List<GraphVertex> calculateImpact(Application application);

    ThresholdResultExplanation explain(BusinessService businessService, Threshold threshold);
}
