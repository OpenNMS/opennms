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
package org.opennms.netmgt.bsm.service.model.graph.internal;

import org.opennms.netmgt.bsm.service.model.Application;
import org.opennms.netmgt.bsm.service.model.BusinessService;
import org.opennms.netmgt.bsm.service.model.IpService;
import org.opennms.netmgt.bsm.service.model.functions.reduce.ReductionFunction;
import org.opennms.netmgt.bsm.service.model.graph.GraphVertex;

public class GraphVertexImpl extends GraphElement implements GraphVertex, Comparable<GraphVertexImpl> {
    private final BusinessService m_businessService;
    private final IpService m_ipService;
    private final String m_reductionKey;
    private final Application m_application;
    private ReductionFunction m_reductionFunction;
    int m_level = -1;

    protected GraphVertexImpl(ReductionFunction reduceFunction, BusinessService businessService) {
        this(reduceFunction, businessService, null, null, null);
    }

    protected GraphVertexImpl(ReductionFunction reduceFunction, IpService ipService) {
        this(reduceFunction, null, ipService, null, null);
    }

    protected GraphVertexImpl(ReductionFunction reduceFunction, Application application) {
        this(reduceFunction, null, null, application, null);
    }

    protected GraphVertexImpl(ReductionFunction reduceFunction, String reductionKey) {
        this(reduceFunction, null, null, null, reductionKey);
    }

    public GraphVertexImpl(ReductionFunction reduceFunction, BusinessService businessService, IpService ipService, Application application, String reductionKey) {
        m_businessService = businessService;
        m_ipService = ipService;
        m_reductionKey = reductionKey;
        m_application = application;
        m_reductionFunction = reduceFunction;
    }

    @Override
    public ReductionFunction getReductionFunction() {
        return m_reductionFunction;
    }

    @Override
    public String getReductionKey() {
        return m_reductionKey;
    }

    @Override
    public BusinessService getBusinessService() {
        return m_businessService;
    }

    @Override
    public IpService getIpService() {
        return m_ipService;
    }


    public void setLevel(int level) {
        m_level = level;
    }

    @Override
    public int getLevel() {
        return m_level;
    }

    @Override
    public Application getApplication() {
        return m_application;
    }

    @Override
    public String toString() {
        return com.google.common.base.MoreObjects.toStringHelper(this)
                .add("businessService", m_businessService)
                .add("ipService", m_ipService)
                .add("reductionKey", m_reductionKey)
                .add("application", m_application)
                .add("level", m_level)
                .add("reductionFunction", m_reductionFunction)
                .toString();
    }

    /**
     * This is used to ensure that list of vertices returned by
     * the RCA and IA algorithms are in consistent order.
     */
    @Override
    public int compareTo(GraphVertexImpl other) {
        int i = getBusinessService() == null ?
                (other.getBusinessService() == null ? 0 : -1) :
                (other.getBusinessService() == null ? 1 : getBusinessService().getId().compareTo(other.getBusinessService().getId()));
        if (i != 0) return i;

        i = getIpService() == null ?
                (other.getIpService() == null ? 0 : -1) :
                (other.getIpService() == null ? 1 : Integer.compare(getIpService().getId(), other.getIpService().getId()));
        if (i != 0) return i;

        i = getApplication() == null ?
                (other.getApplication() == null ? 0 : -1) :
                (other.getApplication() == null ? 1 : Integer.compare(getApplication().getId(), other.getApplication().getId()));
        if (i != 0) return i;

        i = getReductionKey() == null ?
                (other.getReductionKey() == null ? 0 : -1) :
                (other.getReductionKey() == null ? 1 : getReductionKey().compareTo(other.getReductionKey()));
        return i;
    }
}
