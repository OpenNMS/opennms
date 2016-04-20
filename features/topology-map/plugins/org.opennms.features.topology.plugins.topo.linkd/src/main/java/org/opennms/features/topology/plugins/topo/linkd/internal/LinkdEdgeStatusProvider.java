package org.opennms.features.topology.plugins.topo.linkd.internal;

import java.util.Collection;
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

    private AlarmDao m_alarmDao;

    @Override
    public String getNameSpace() {
        return AbstractLinkdTopologyProvider.TOPOLOGY_NAMESPACE_LINKD;
    }

    @Override
    public Map<EdgeRef, Status> getStatusForEdges(EdgeProvider edgeProvider,
            Collection<EdgeRef> edges, Criteria[] criteria) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean contributesTo(String namespace) {
        return namespace.equals(AbstractLinkdTopologyProvider.TOPOLOGY_NAMESPACE_LINKD);
    }

    public AlarmDao getAlarmDao() {
        return m_alarmDao;
    }

    protected List<OnmsAlarm> getLinkDownAlarms() {
        org.opennms.core.criteria.Criteria criteria = new org.opennms.core.criteria.Criteria(OnmsAlarm.class);
        criteria.addRestriction(new EqRestriction("uei", EventConstants.TOPOLOGY_LINK_DOWN_EVENT_UEI));
        criteria.addRestriction(new NeRestriction("severity", OnmsSeverity.CLEARED));
        return getAlarmDao().findMatching(criteria);
    }

    public void setAlarmDao(AlarmDao alarmDao) {
        m_alarmDao = alarmDao;
    }

}
