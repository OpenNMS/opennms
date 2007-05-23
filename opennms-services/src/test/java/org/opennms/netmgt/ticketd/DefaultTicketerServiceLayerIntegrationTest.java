package org.opennms.netmgt.ticketd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.opennms.netmgt.dao.AbstractTransactionalDaoTestCase;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.mock.MockEventIpcManager;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.TroubleTicketState;

public class DefaultTicketerServiceLayerIntegrationTest extends
        AbstractTransactionalDaoTestCase {

    private TicketerServiceLayer m_ticketerServiceLayer;
    private TestTicketerPlugin m_ticketerPlugin;

    @Override
    protected String[] getConfigLocations() {
        
        System.setProperty("opennms.ticketer.plugin", TestTicketerPlugin.class.getName());
        
        // TODO I hate this
        EventIpcManagerFactory.setIpcManager(new MockEventIpcManager());
        
        String[] configs = new String[] {
            "classpath:/META-INF/opennms/applicationContext-daemon.xml",
            "classpath:/META-INF/opennms/applicationContext-troubleTicketer.xml",
            "classpath:/org/opennms/netmgt/ticketd/applicationContext-configOverride.xml",
        };
        
        List<String> configLocation = new ArrayList<String>();
        
        configLocation.addAll(Arrays.asList(super.getConfigLocations()));
        configLocation.addAll(Arrays.asList(configs));
        
        return configLocation.toArray(new String[configLocation.size()]);
    }
    
    public void setTicketerPlugin(TestTicketerPlugin ticketerPlugin) {
        m_ticketerPlugin = ticketerPlugin;
    }
    
    public void setTicketerServiceLayer(TicketerServiceLayer ticketerServiceLayer) {
        m_ticketerServiceLayer = ticketerServiceLayer;
    }
    
    public void testWire() {
        assertNotNull(m_ticketerServiceLayer);
        assertNotNull(m_ticketerPlugin);
        
        final int alarmId = 1;
        
        OnmsAlarm alarm = getAlarmDao().get(alarmId);
        assertNull(alarm.getTTicketState());
        assertNull(alarm.getTTicketId());
        
        m_ticketerServiceLayer.createTicketForAlarm(alarmId);
        
        getAlarmDao().flush();
        
        alarm = getAlarmDao().get(alarmId);
        assertEquals(TroubleTicketState.OPEN, alarm.getTTicketState());
        assertNotNull(alarm.getTTicketId());
        assertEquals("testId", alarm.getTTicketId());
        
        m_ticketerServiceLayer.updateTicketForAlarm(alarm.getId(), alarm.getTTicketId());

        getAlarmDao().flush();

        alarm = getAlarmDao().get(alarmId);
        assertEquals(TroubleTicketState.OPEN, alarm.getTTicketState());
        
        m_ticketerServiceLayer.closeTicketForAlarm(alarmId, alarm.getTTicketId());
        
        getAlarmDao().flush();

        alarm = getAlarmDao().get(alarmId);
        assertEquals(TroubleTicketState.CLOSED, alarm.getTTicketState());
        
    }
    
    public static class TestTicketerPlugin implements TicketerPlugin {
        
        public Ticket get(String ticketId) {
            Ticket ticket = new Ticket();
            ticket.setId(ticketId);
            return ticket;
        }

        public void saveOrUpdate(Ticket ticket) {
            ticket.setId("testId");
        }
        
    }

}
