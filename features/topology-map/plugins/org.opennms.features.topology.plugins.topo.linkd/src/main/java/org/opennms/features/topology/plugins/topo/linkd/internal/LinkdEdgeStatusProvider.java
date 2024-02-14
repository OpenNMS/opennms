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
package org.opennms.features.topology.plugins.topo.linkd.internal;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.opennms.core.criteria.restrictions.EqRestriction;
import org.opennms.core.criteria.restrictions.NeRestriction;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.api.topo.EdgeStatusProvider;
import org.opennms.features.topology.api.topo.Status;
import org.opennms.features.topology.api.topo.BackendGraph;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsSeverity;

import com.google.common.collect.Maps;

public class LinkdEdgeStatusProvider implements EdgeStatusProvider {

    public static class LinkdEdgeStatus implements Status {

        private final String m_status;

        public LinkdEdgeStatus(String status) {
            m_status = status;
        }

        public LinkdEdgeStatus(OnmsAlarm summary) {
            m_status = summary.getUei().equals(EventConstants.TOPOLOGY_LINK_DOWN_EVENT_UEI) ? "down" : "up";
        }

        @Override
        public String computeStatus() {
            return m_status.toLowerCase();
        }

        @Override
        public Map<String, String> getStatusProperties() {
            Map<String, String> statusMap = new LinkedHashMap<>();
            statusMap.put("status", m_status);

            return statusMap;
        }

        @Override
        public Map<String, String> getStyleProperties() {
            return Maps.newHashMap();
        }

        @Override
        public String toString() {
            return "LinkdEdgeStatus[" + m_status + "]";
        }
    }

    private AlarmDao m_alarmDao;
    private SessionUtils m_sessionUtils;
    private LinkdTopologyFactory m_linkdTopologyFactory;

    @Override
    public String getNamespace() {
        return m_linkdTopologyFactory.getActiveNamespace();
    }

    @Override
    public Map<EdgeRef, Status> getStatusForEdges(BackendGraph graph, Collection<EdgeRef> edges, Criteria[] criteria) {
        Map<EdgeRef, Status> retVal = new LinkedHashMap<>();
EDGES:        for (EdgeRef edgeRef : edges) {
                LinkdEdge edge = (LinkdEdge) graph.getEdge(edgeRef);
                for (OnmsAlarm alarm: getLinkdEdgeDownAlarms()) {
                    if (alarm.getNode() == null)
                        continue;
                    if (alarm.getIfIndex() == null)
                        continue;
                    int alarmnodeid = alarm.getNode().getId();
                    if ( edge.getSourcePort().getVertex().getNodeID() != null 
                            && edge.getSourcePort().getVertex().getNodeID() == alarmnodeid
                            && edge.getSourcePort().getIfIndex() != null
                            && edge.getSourcePort().getIfIndex().intValue() == alarm.getIfIndex().intValue()) {
                        retVal.put(edgeRef, new LinkdEdgeStatus(alarm));
                        continue EDGES;
                    }
                    if ( edge.getTargetPort().getVertex().getNodeID() != null 
                            && edge.getTargetPort().getVertex().getNodeID() == alarmnodeid
                            && edge.getTargetPort().getIfIndex() != null
                            && edge.getTargetPort().getIfIndex().intValue() == alarm.getIfIndex().intValue()) {
                        retVal.put(edgeRef, new LinkdEdgeStatus(alarm));
                        continue EDGES;
                    }                
              }
              retVal.put(edgeRef, new LinkdEdgeStatus("up"));
        }
        return retVal;
    }

    @Override
    public boolean contributesTo(String namespace) {
        return namespace.equals(m_linkdTopologyFactory.getActiveNamespace());
    }

    public AlarmDao getAlarmDao() {
        return m_alarmDao;
    }

    protected List<OnmsAlarm> getLinkdEdgeDownAlarms() {
        return getSessionUtils().withReadOnlyTransaction(() -> {
            org.opennms.core.criteria.Criteria criteria = new org.opennms.core.criteria.Criteria(OnmsAlarm.class);
            criteria.addRestriction(new EqRestriction("uei", EventConstants.TOPOLOGY_LINK_DOWN_EVENT_UEI));
            criteria.addRestriction(new NeRestriction("severity", OnmsSeverity.CLEARED));
            return getAlarmDao().findMatching(criteria);
        });
    }

    public void setAlarmDao(AlarmDao alarmDao) {
        m_alarmDao = alarmDao;
    }

    public LinkdTopologyFactory getLinkdTopologyFactory() {
        return m_linkdTopologyFactory;
    }

    public void setLinkdTopologyFactory(LinkdTopologyFactory linkdTopologyFactory) {
        this.m_linkdTopologyFactory = linkdTopologyFactory;
    }

    public SessionUtils getSessionUtils() {
        return m_sessionUtils;
    }

    public void setSessionUtils(SessionUtils m_sessionUtils) {
        this.m_sessionUtils = m_sessionUtils;
    }
}
