package org.opennms.features.topology.plugins.topo.linkd.internal;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.opennms.core.criteria.restrictions.EqRestriction;
import org.opennms.core.criteria.restrictions.NeRestriction;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.EdgeProvider;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.api.topo.EdgeStatusProvider;
import org.opennms.features.topology.api.topo.Status;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsSeverity;

public class LinkdEdgeStatusProvider implements EdgeStatusProvider {

    public static class LinkdEdgeStatus implements Status{

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
            Map<String, String> statusMap = new LinkedHashMap<String, String>();
            statusMap.put("status", m_status);

            return statusMap;
        }

        @Override
        public String toString() {
            return "LinkdEdgeStatus[" + m_status + "]";
        }
    }

    private AlarmDao m_alarmDao;

    @Override
    public String getNamespace() {
        return AbstractLinkdTopologyProvider.TOPOLOGY_NAMESPACE_LINKD;
    }

    @Override
    public Map<EdgeRef, Status> getStatusForEdges(EdgeProvider edgeProvider,
            Collection<EdgeRef> edges, Criteria[] criteria) {
        Map<EdgeRef, Status> retVal = new LinkedHashMap<EdgeRef, Status>();
EDGES:        for (EdgeRef edgeRef : edges) {
                LinkdEdge edge = (LinkdEdge) edgeProvider.getEdge(edgeRef);
                for (OnmsAlarm alarm: getLinkdEdgeDownAlarms()) {
                    if (alarm.getNode().getId() == null)
                        continue;
                    if (alarm.getIfIndex() == null)
                        continue;
                    int alarmnodeid = alarm.getNode().getId().intValue();
                    if ( edge.getSourceNodeid() != null && edge.getSourceNodeid().intValue() == alarmnodeid
                            && edge.getSourceEndPoint() != null
                            && edge.getSourceEndPoint().equals(String.valueOf(alarm.getIfIndex()))) {
                        retVal.put(edgeRef, new LinkdEdgeStatus(alarm));
                        continue EDGES;
                    }
                    if ( edge.getTargetNodeid() != null && edge.getTargetNodeid().intValue() == alarmnodeid
                            && edge.getTargetEndPoint() != null
                            && edge.getTargetEndPoint().equals(String.valueOf(alarm.getIfIndex()))) {
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
        return namespace.equals(AbstractLinkdTopologyProvider.TOPOLOGY_NAMESPACE_LINKD);
    }

    public AlarmDao getAlarmDao() {
        return m_alarmDao;
    }

    protected List<OnmsAlarm> getLinkdEdgeDownAlarms() {
        org.opennms.core.criteria.Criteria criteria = new org.opennms.core.criteria.Criteria(OnmsAlarm.class);
        criteria.addRestriction(new EqRestriction("uei", EventConstants.TOPOLOGY_LINK_DOWN_EVENT_UEI));
        criteria.addRestriction(new NeRestriction("severity", OnmsSeverity.CLEARED));
        return getAlarmDao().findMatching(criteria);
    }

    public void setAlarmDao(AlarmDao alarmDao) {
        m_alarmDao = alarmDao;
    }

}
