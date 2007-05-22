package org.opennms.netmgt.ticketd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.opennms.netmgt.dao.AbstractTransactionalDaoTestCase;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.mock.MockEventIpcManager;

public class DefaultTicketerServiceLayerIntegrationTest extends
        AbstractTransactionalDaoTestCase {

    private TicketerServiceLayer m_ticketerServiceLayer;

    @Override
    protected String[] getConfigLocations() {
        
        System.setProperty("opennms.ticketer.plugin", "org.opennms.netmgt.ticketd.NullTicketerPlugin");
        
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
    
    public void setTicketerServiceLayer(TicketerServiceLayer ticketerServiceLayer) {
        m_ticketerServiceLayer = ticketerServiceLayer;
    }
    
    public void testStart() {
        assertNotNull(m_ticketerServiceLayer);
            
    }
    
    

}
