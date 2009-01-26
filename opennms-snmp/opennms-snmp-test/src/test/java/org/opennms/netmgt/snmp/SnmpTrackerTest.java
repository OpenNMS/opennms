package org.opennms.netmgt.snmp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.mock.snmp.JUnitSnmpAgent;
import org.opennms.mock.snmp.JUnitSnmpAgentExecutionListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;


@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({
    JUnitSnmpAgentExecutionListener.class,
    DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class
})
@ContextConfiguration(locations={"classpath:emptyContext.xml"})
@JUnitSnmpAgent(port=9161, resource="classpath:snmpTestData1.properties")
public class SnmpTrackerTest {

    private void walk(CollectionTracker c) throws Exception {
        SnmpAgentConfig config = new SnmpAgentConfig();
        config.setAddress(InetAddress.getLocalHost());
        config.setPort(9161);
        config.setVersion(SnmpAgentConfig.VERSION2C);
        SnmpWalker walker = SnmpUtils.createWalker(config, "test", c);
        assertNotNull(walker);
        walker.start();
        walker.waitFor();
    }
    
    private class CountingColumnTracker extends ColumnTracker {
        private long m_count = 0;

        public CountingColumnTracker(SnmpObjId base) {
            super(base);
        }
        
        public CountingColumnTracker(SnmpObjId base, int maxRepetitions) {
            super(base, maxRepetitions);
        }
        
        public long getCount() {
            return m_count;
        }
        
        @Override
        protected void storeResult(SnmpResult res) {
            System.err.println(String.format("storing result %s", res));
            m_count++;
        }

    }
    
    @Test
    public void testColumnTracker() throws Exception {
        CountingColumnTracker ct = new CountingColumnTracker(SnmpObjId.get(".1.3.6.1.2.1.2.2.1.1"), 10);
        
        walk(ct);
        assertEquals("columns returned must match test data", Long.valueOf(6).longValue(), ct.getCount());
    }
    
    @Test
    public void testSomething() throws Exception {
        SnmpObjId base = SnmpObjId.get(".1.3.6.1.2.1.2.2.1.1");
        
        RowCallback rc = new RowCallback() {
            private List<List<SnmpResult>> m_responses = new ArrayList<List<SnmpResult>>();
            public void rowCompleted(List<SnmpResult> results) {
                m_responses.add(results);
            }
            
            public List<List<SnmpResult>> getResponses() {
                return m_responses;
            }
        };
        
        TableTracker tt = new TableTracker(rc, base, 10) {
            @Override
            protected void storeResult(SnmpResult res) {
                System.err.println(String.format("storing result %s", res));
                super.storeResult(res);
            }
        };

        
    }
    
}
