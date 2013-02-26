package org.opennms.features.topology.plugins.status.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.features.topology.api.topo.Status;
import org.opennms.features.topology.api.topo.StatusProvider;
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
    
    public AlarmDao getAlarmDao() {
        return m_alarmDao;
    }

    public void setAlarmDao(AlarmDao alarmDao) {
        m_alarmDao = alarmDao;
    }

    @Override
    public Status getStatusForVertex(VertexRef vertex) {
        
        if(vertex.getNamespace().equals("nodes")) {
            int nodeId = Integer.valueOf(vertex.getId());
            
            CriteriaBuilder builder = new CriteriaBuilder(OnmsAlarm.class);
            builder.alias("node", "node");
            builder.eq("node.id", nodeId);
            builder.ge("severity", OnmsSeverity.WARNING);
            builder.orderBy("severity").desc();
            builder.limit(1);
            
            
            final List<OnmsAlarm> alarms = m_alarmDao.findMatching(builder.toCriteria());
            if(alarms != null && alarms.size() == 1) {
                final OnmsAlarm alarm = alarms.get(0);
                final OnmsSeverity severity = alarm.getSeverity();
                Status vertexStatus = new AlarmStatus(severity.getId(), severity.getLabel());
                return vertexStatus;
            } else {
                return createNormalStatus();
            }
        } else {
            return createNormalStatus();
        }
        
       
    }

    private Status createNormalStatus() {
        return new AlarmStatus(OnmsSeverity.NORMAL.getId(), OnmsSeverity.NORMAL.getLabel());
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
