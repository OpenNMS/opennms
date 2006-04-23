/**
 * 
 */
package org.opennms.netmgt.dao;

import java.util.Date;

import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.netmgt.model.OnmsServiceType;

/**
 * @author mhuot
 *
 */
public class OutageDaoTest extends AbstractDaoTestCase {

    public void setUp() throws Exception {
        //setPopulate(false);
        super.setUp();
    }
    
    public void testSave() {
        OnmsOutage outage = new OnmsOutage();
        outage.setEventBySvcLostEvent(new OnmsEvent());
        outage.setIfLostService(new Date());
        OnmsIpInterface ipInterface = new OnmsIpInterface("172.16.1.1", new OnmsNode(getDistPollerDao().load("localhost")));
        OnmsServiceType serviceType = new OnmsServiceType("ICMP");
        outage.setMonitoredService(new OnmsMonitoredService(ipInterface, serviceType));
        getOutageDao().save(outage);
        //it works we're so smart! hehe
        outage = getOutageDao().load(outage.getId());
        assertEquals("ICMP", outage.getMonitoredService().getServiceType().getName());
    }

}
