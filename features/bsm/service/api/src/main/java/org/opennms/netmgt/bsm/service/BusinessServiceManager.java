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

import java.util.List;
import java.util.Set;

import org.opennms.core.criteria.Criteria;
import org.opennms.netmgt.bsm.service.model.Application;
import org.opennms.netmgt.bsm.service.model.BusinessService;
import org.opennms.netmgt.bsm.service.model.IpService;
import org.opennms.netmgt.bsm.service.model.Status;
import org.opennms.netmgt.bsm.service.model.edge.ApplicationEdge;
import org.opennms.netmgt.bsm.service.model.edge.ChildEdge;
import org.opennms.netmgt.bsm.service.model.edge.Edge;
import org.opennms.netmgt.bsm.service.model.edge.IpServiceEdge;
import org.opennms.netmgt.bsm.service.model.edge.ReductionKeyEdge;
import org.opennms.netmgt.bsm.service.model.functions.map.MapFunction;
import org.opennms.netmgt.bsm.service.model.functions.reduce.ReductionFunction;
import org.opennms.netmgt.bsm.service.model.graph.BusinessServiceGraph;

public interface BusinessServiceManager extends NodeManager {

    List<BusinessService> getAllBusinessServices();

    List<BusinessService> search(BusinessServiceSearchCriteria businessServiceSearchCriteria);

    List<BusinessService> findMatching(Criteria criteria);

    int countMatching(Criteria criteria);

    BusinessService createBusinessService();

    Edge getEdgeById(Long edgeId);

    boolean deleteEdge(BusinessService service, Edge edge);

    void saveBusinessService(BusinessService newObject);

    void deleteBusinessService(BusinessService service);

    BusinessService getBusinessServiceById(Long id);

    Set<BusinessService> getFeasibleChildServices(BusinessService service);

    Status getOperationalStatus(BusinessService service);

    Status getOperationalStatus(IpService ipService);

    Status getOperationalStatus(String reductionKey);

    Status getOperationalStatus(Edge edge);

    List<IpService> getAllIpServices();

    List<Application> getAllApplications();

    IpService getIpServiceById(Integer id);

    Application getApplicationById(Integer id);

    /**
     * Triggers a reload of the Business Service Daemon.
     */
    void triggerDaemonReload();

    Set<BusinessService> getParentServices(Long id);

    void setChildEdges(BusinessService parentService, Set<ChildEdge> childEdges);

    boolean addChildEdge(BusinessService parent, BusinessService child, MapFunction mapFunction, int weight);

    void setIpServiceEdges(BusinessService businessService, Set<IpServiceEdge> ipServiceEdges);

    boolean addIpServiceEdge(BusinessService businessService, IpService ipService, MapFunction mapFunction, int weight);

    boolean addIpServiceEdge(BusinessService businessService, IpService ipService, MapFunction mapFunction, int weight, String friendlyName);

    void setApplicationEdges(BusinessService businessService, Set<ApplicationEdge> applicationEdges);

    boolean addApplicationEdge(BusinessService businessService, Application application, MapFunction mapFunction, int weight);

    boolean addReductionKeyEdge(BusinessService businessService, String reductionKey, MapFunction mapFunction, int weight);

    boolean addReductionKeyEdge(BusinessService businessService, String reductionKey, MapFunction mapFunction, int weight, String friendlyName);

    void setReductionKeyEdges(BusinessService businessService, Set<ReductionKeyEdge> reductionKeyEdges);

    void removeEdge(BusinessService businessService, Edge edge);

    BusinessServiceGraph getGraph(List<BusinessService> businessServices);

    /**
     * This returns the actual graph of the underlying {@link BusinessServiceStateMachine}.
     *
     * Please DO NOT MODIFY any object in that graph.
     *
     * @return the actual graph of the underlying {@link BusinessServiceStateMachine}. DO NOT MODIFY!
     */
    BusinessServiceGraph getGraph();

    BusinessServiceStateMachine getStateMachine();

    void setMapFunction(Edge edge, MapFunction mapFunction);

    void setReduceFunction(BusinessService businessService, ReductionFunction reductionFunction);
}
