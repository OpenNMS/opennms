package org.opennms.features.topology.plugins.status.internal;

import java.util.*;

import org.hibernate.HibernateException;
import org.hibernate.criterion.CriteriaQuery;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.engine.TypedValue;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.features.topology.api.topo.Status;
import org.opennms.features.topology.api.topo.StatusProvider;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexProvider;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.netmgt.dao.AlarmDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.alarm.AlarmSummary;

public class AlarmStatusProvider implements StatusProvider {
    
    public class AlarmStatus implements Status{

        private int m_statusId;
        private String m_label;
        private int m_alarmCount = 0;

        public AlarmStatus(int id, String label, int count) {
            m_statusId = id;
            m_label = label;
            m_alarmCount = count;
        }

        @Override
        public String computeStatus() {
            return m_label.toLowerCase();
        }

        @Override
        public Map<String, String> getStatusProperties() {
            Map<String, String> statusMap = new HashMap<String, String>();
            statusMap.put("status", m_label.toLowerCase());
            statusMap.put("statusCount", "" + m_alarmCount);
            return statusMap;
        }
        
    }

    private AlarmDao m_alarmDao;
    private VertexProvider m_vertexProvider;
    
    public VertexProvider getVertexProvider() {
        return m_vertexProvider;
    }

    public void setVertexProvider(VertexProvider vertexProvider) {
        m_vertexProvider = vertexProvider;
    }

    public AlarmDao getAlarmDao() {
        return m_alarmDao;
    }

    public void setAlarmDao(AlarmDao alarmDao) {
        m_alarmDao = alarmDao;
    }

    @Override
    public Status getStatusForVertex(VertexRef vertexRef) {
        
        if(vertexRef.getNamespace().equals("nodes")) {
            try {
                Collection<Integer> nodeIds = new ArrayList<Integer>();
                if(isGroup(vertexRef)) {
                    addChildrenRecursively(vertexRef, nodeIds);
                } else {
                    nodeIds.add(Integer.valueOf(vertexRef.getId()));
                }
                return getAlarmStatus(nodeIds);
            } catch (NumberFormatException e) {
                return createIndeterminateStatus();
            }
        } else {
            return createIndeterminateStatus();
        }
        
    }

    @Override
    public Collection<Status> getStatusForVertices(Collection<VertexRef> vertices) {
        Collection<Status> verticesStatus = new ArrayList<Status>();
        for(VertexRef vert : vertices) {
            verticesStatus.add(getStatusForVertex(vert));
        }
        return verticesStatus;
    }

    @Override
    public String getNamespace() {
        return "node-alarm-status";
    }

    private Status getAlarmStatus(Collection<Integer> nodeIds) {
        List<AlarmSummary> alarmSummaries = m_alarmDao.getNodeAlarmSummaries(nodeIds.toArray(new Integer[nodeIds.size()]));
        if(alarmSummaries != null && alarmSummaries.size() >= 1) {
            return calculateAlarmStatus(alarmSummaries);
        } else {
            return createIndeterminateStatus();
        }
    }

    private void addChildrenRecursively(VertexRef groupRef, Collection<Integer> nodeIds) {
        List<Vertex> vertices = getVertexProvider().getChildren(groupRef);
        for(Vertex vertex : vertices) {
            if(!vertex.isGroup()) {
                nodeIds.add(vertex.getNodeID());
            } else {
                addChildrenRecursively(vertex, nodeIds);
            }
        }
    }

    private boolean isGroup(VertexRef vertexRef) {
        if(vertexRef instanceof Vertex) {
            return ((Vertex) vertexRef).isGroup();
        }
        return false;
    }

    private Status createIndeterminateStatus() {
        return new AlarmStatus(OnmsSeverity.INDETERMINATE.getId(), OnmsSeverity.INDETERMINATE.getLabel(), 0);
    }

    private AlarmStatus calculateAlarmStatus(List<AlarmSummary> alarmSummaries) {
        Collections.sort(alarmSummaries, new Comparator<AlarmSummary>() {
            @Override
            public int compare(AlarmSummary o1, AlarmSummary o2) {
                return o1.getMaxSeverity().compareTo(o2.getMaxSeverity());
            }
        });
        OnmsSeverity severity = alarmSummaries.get(0).getMaxSeverity();
        int count = 0;
        for (AlarmSummary eachSummary : alarmSummaries) {
            count += eachSummary.getAlarmCount();
        }
        return new AlarmStatus(severity.getId(), severity.getLabel(), count);
    }
}
