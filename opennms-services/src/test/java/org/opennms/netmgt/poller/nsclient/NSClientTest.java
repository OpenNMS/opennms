package org.opennms.netmgt.poller.nsclient;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class NSClientTest {
    
    NsclientManager m_nsclientManager;

    String[] counters = {
            "\\Processor(_Total)\\% Processor Time",
            "\\Processor(_Total)\\% Interrupt Time", 
            "\\Processor(_Total)\\% Privileged Time",
            "\\Processor(_Total)\\% User Time"
    };

    @Before
    public void setUp() throws Exception {
    	// Change this to your NSClient test server
        m_nsclientManager = new NsclientManager("192.168.149.250", 12489);
    }
    
    @Test
    @Ignore
    public void testGetCounters() throws Exception {
        for (String counter : counters) {
            m_nsclientManager.init();
            NsclientPacket result = getCounter(counter);
            m_nsclientManager.close();
            System.err.println(counter + "=" + result.getResponse());
            boolean isAvailable = (result.getResultCode() == NsclientPacket.RES_STATE_OK);
            Assert.assertTrue(isAvailable);            
        }
    }
    
    @Test
    @Ignore
    public void testGetCountersWithSharedConnection() throws Exception {
        m_nsclientManager.init();
        for (String counter : counters) {
            NsclientPacket result = getCounter(counter);
            System.err.println(counter + "=" + result.getResponse());
            boolean isAvailable = (result.getResultCode() == NsclientPacket.RES_STATE_OK);
            Assert.assertTrue(isAvailable);            
        }
        m_nsclientManager.close();
    }

    private NsclientPacket getCounter(String counter) throws NsclientException {
        NsclientCheckParams params = new NsclientCheckParams(counter);
        NsclientPacket result = m_nsclientManager.processCheckCommand(NsclientManager.CHECK_COUNTER, params);
        return result;
    }

}
