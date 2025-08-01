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
package org.opennms.netmgt.bsm.service.model;

import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.bsm.service.model.edge.ApplicationEdge;
import org.opennms.netmgt.bsm.service.model.edge.ChildEdge;
import org.opennms.netmgt.bsm.service.model.edge.Edge;
import org.opennms.netmgt.bsm.service.model.edge.IpServiceEdge;
import org.opennms.netmgt.bsm.service.model.edge.ReductionKeyEdge;
import org.opennms.netmgt.bsm.service.model.functions.map.MapFunction;
import org.opennms.netmgt.bsm.service.model.functions.reduce.ReductionFunction;

public interface BusinessService {


    Long getId();

    String getName();

    void setName(String name);

    Map<String, String> getAttributes();

    void setAttributes(Map<String, String> attributes);

    Set<BusinessService> getChildServices();

    Set<BusinessService> getParentServices();

    void save();

    void delete();

    Status getOperationalStatus();

    ReductionFunction getReduceFunction();

    void setReduceFunction(ReductionFunction reductionFunction);

    void setIpServiceEdges(Set<IpServiceEdge> ipServiceEdges);

    void addIpServiceEdge(IpService ipService, MapFunction mapFunction, int weight, String friendlyName);

    void addApplicationEdge(Application application, MapFunction mapFunction, int weight);

    void setApplicationEdges(Set<ApplicationEdge> applicationEdges);

    void setReductionKeyEdges(Set<ReductionKeyEdge> reductionKeyEdges);

    void addReductionKeyEdge(String reductionKey, MapFunction mapFunction, int weight, String friendlyName);

    void setChildEdges(Set<ChildEdge> childEdges);

    void addChildEdge(BusinessService child, MapFunction mapFunction, int weight);

    void removeEdge(Edge edge);

    Set<ReductionKeyEdge> getReductionKeyEdges();
    
    Set<IpServiceEdge> getIpServiceEdges();

    Set<ApplicationEdge> getApplicationEdges();

    Set<ChildEdge> getChildEdges();
    
    Set<? extends Edge> getEdges();
}
