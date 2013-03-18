package org.opennms.features.topology.plugins.status.internal;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.criteria.Criteria;
import org.opennms.features.topology.api.topo.AbstractRef;
import org.opennms.features.topology.api.topo.Status;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.netmgt.dao.AlarmDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsSeverity;

import com.vaadin.data.Item;

public class AlarmStatusProviderTest {
    
    private class TestVertex extends AbstractRef implements Vertex {

        public TestVertex() {
            super("nodes", "1", null);
        }

        @Override
        public String getKey() {
            return null;
        }

        @Override
        public Item getItem() {
            return null;
        }

        @Override
        public String getTooltipText() {
            return null;
        }

        @Override
        public String getIconKey() {
            return null;
        }

        @Override
        public String getStyleName() {
            return null;
        }

        @Override
        public boolean isGroup() {
            return false;
        }

        @Override
        public void setParent(VertexRef parent) {}

        @Override
        public VertexRef getParent() {
            return null;
        }

        @Override
        public Integer getX() {
            return null;
        }

        @Override
        public Integer getY() {
            return null;
        }

        @Override
        public boolean isLocked() {
            return false;
        }

        @Override
        public boolean isSelected() {
            return false;
        }

        @Override
        public String getIpAddress() {
            return null;
        }

        @Override
        public Integer getNodeID() {
            return 1;
        }
        
    }
    
    private AlarmDao m_alarmDao;
    private AlarmStatusProvider m_statusProvider;
    
    @Before
    public void setUp() {
        m_alarmDao = EasyMock.createMock(AlarmDao.class);
        m_statusProvider = new AlarmStatusProvider();
        m_statusProvider.setAlarmDao(m_alarmDao);
    }
    
    
    @Test
    public void testGetAlarmStatus() {
        Vertex vertex = new TestVertex();
        
        
        EasyMock.expect(m_alarmDao.findMatching(EasyMock.notNull(Criteria.class))).andReturn(createNormalStatusList());
        
        EasyMock.replay(m_alarmDao);
        
        Status vertexStatus = m_statusProvider.getStatusForVertex(vertex);
        String computeStatus = vertexStatus.computeStatus();
        assertTrue(computeStatus.equals("indeterminate"));
        
        EasyMock.verify(m_alarmDao);
    }


    private List<OnmsAlarm> createNormalStatusList() {
        List<OnmsAlarm> alarms = new ArrayList<OnmsAlarm>();
        OnmsAlarm onmsAlarm = new OnmsAlarm();
        onmsAlarm.setSeverity(OnmsSeverity.NORMAL);
        alarms.add(onmsAlarm);
        
        return null;
    }

}
