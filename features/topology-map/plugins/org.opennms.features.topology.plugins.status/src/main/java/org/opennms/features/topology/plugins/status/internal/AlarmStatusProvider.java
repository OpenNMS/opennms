package org.opennms.features.topology.plugins.status.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

        public AlarmStatus(int id, String label) {
            m_statusId = id;
            m_label = label;
        }

        @Override
        public String computeStatus() {
            return m_label.toLowerCase();
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
                    builder.limit(1);
                    
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
        if(alarms != null && alarms.size() == 1) {
            final OnmsAlarm alarm = alarms.get(0);
            final OnmsSeverity severity = alarm.getSeverity();
            Status vertexStatus = new AlarmStatus(severity.getId(), severity.getLabel());
            return vertexStatus;
        } else {
            return createIndeterminateStatus();
        }
    }

    private Status getStatusForGroup(VertexRef groupRef) {
        List<Vertex> vertices = getVertexProvider().getChildren(groupRef);
        Collection<Integer> nodeIds = new ArrayList<Integer>();
        
        for(Vertex vertex : vertices) {
            if(!vertex.isGroup()) {
               nodeIds.add(vertex.getNodeID());
            }
        }
        
        CriteriaBuilder builder = new CriteriaBuilder(OnmsAlarm.class);
        builder.alias("node", "node");
        builder.in("node.id", nodeIds);
        builder.ge("severity", OnmsSeverity.WARNING);
        builder.orderBy("severity").desc();
        builder.limit(1);
        
        return getStatusForCriteria(builder);
    }

    private boolean isGroup(VertexRef vertexRef) {
        if(vertexRef instanceof Vertex) {
            return ((Vertex) vertexRef).isGroup();
        }
        
        return false;
    }

    private Status createIndeterminateStatus() {
        return new AlarmStatus(OnmsSeverity.INDETERMINATE.getId(), OnmsSeverity.INDETERMINATE.getLabel());
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
