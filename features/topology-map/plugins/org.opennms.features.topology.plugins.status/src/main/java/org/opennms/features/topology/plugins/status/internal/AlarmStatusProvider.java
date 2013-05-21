package org.opennms.features.topology.plugins.status.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.features.topology.api.topo.Status;
import org.opennms.features.topology.api.topo.StatusProvider;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexProvider;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.netmgt.dao.AlarmDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsSeverity;

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
            if(isGroup(vertexRef) && getVertexProvider() != null) {
                return getStatusForGroup(vertexRef);
            } else {
                try {
                    int nodeId = Integer.valueOf(vertexRef.getId());
                    
                    CriteriaBuilder builder = new CriteriaBuilder(OnmsAlarm.class);
                    builder.alias("node", "node");
                    builder.eq("node.id", nodeId);
                    builder.ge("severity", OnmsSeverity.WARNING);
                    builder.orderBy("severity").desc();
                    
                    return getStatusForCriteria(builder);
                }catch(NumberFormatException e) {
                    return createIndeterminateStatus();
                }
            }
            
        } else {
            return createIndeterminateStatus();
        }
        
    }

    private Status getStatusForCriteria(CriteriaBuilder builder) {
        final List<OnmsAlarm> alarms = m_alarmDao.findMatching(builder.toCriteria());
        if(alarms != null && alarms.size() >= 1) {
            final OnmsAlarm alarm = alarms.get(0);
            final OnmsSeverity severity = alarm.getSeverity();
            Status vertexStatus = new AlarmStatus(severity.getId(), severity.getLabel(), getUnAckAlarmCount(alarms));
            return vertexStatus;
        } else {
            return createIndeterminateStatus();
        }
    }

    private int getUnAckAlarmCount(List<OnmsAlarm> alarms) {
        int count = 0;
        
        for(OnmsAlarm alarm : alarms) {
            if(!alarm.isAcknowledged()) {
                count++;
            }
        }
        
        return count;
    }

    private Status getStatusForGroup(VertexRef groupRef) {
        
        
        Collection<Integer> nodeIds = new ArrayList<Integer>();
        
        getChildrenRecursively(groupRef, nodeIds);
        
        if(nodeIds.size() >= 1) {
            CriteriaBuilder builder = new CriteriaBuilder(OnmsAlarm.class);
            builder.alias("node", "node");
            builder.in("node.id", nodeIds);
            builder.ge("severity", OnmsSeverity.WARNING);
            builder.orderBy("node.id").asc();
            builder.orderBy("severity").desc();
            
            return getStatusForCriteria(builder);
        }else {
            return createIndeterminateStatus();
        }
    }

    private void getChildrenRecursively(VertexRef groupRef, Collection<Integer> nodeIds) {
        List<Vertex> vertices = getVertexProvider().getChildren(groupRef);
        for(Vertex vertex : vertices) {
            if(!vertex.isGroup()) {
               nodeIds.add(vertex.getNodeID());
            } else {
                getChildrenRecursively(vertex, nodeIds);
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

}
