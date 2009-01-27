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

    private class TestRowCallback implements RowCallback {
        private List<List<SnmpResult>> m_responses = new ArrayList<List<SnmpResult>>();

        public void rowCompleted(List<SnmpResult> results) {
            m_responses.add(results);
        }
        
        public List<List<SnmpResult>> getResponses() {
            return m_responses;
        }
    }

    private void walk(CollectionTracker c, int maxVarsPerPdu, int maxRepetitions) throws Exception {
        SnmpAgentConfig config = new SnmpAgentConfig();
        config.setAddress(InetAddress.getLocalHost());
        config.setPort(9161);
        config.setVersion(SnmpAgentConfig.VERSION2C);
        config.setMaxVarsPerPdu(maxVarsPerPdu);
        config.setMaxRepetitions(maxRepetitions);
        SnmpWalker walker = SnmpUtils.createWalker(config, "test", c);
        assertNotNull(walker);
        walker.start();
        walker.waitFor();
    }
    
    @Test
    public void testColumnTracker() throws Exception {
        CountingColumnTracker ct = new CountingColumnTracker(SnmpObjId.get(".1.3.6.1.2.1.2.2.1.1"));

        walk(ct, 10, 3);
        assertEquals("number of columns returned must match test data", Long.valueOf(6).longValue(), ct.getCount());
    }
    
    @Test
    public void testTableTracker() throws Exception {
        /*
        * .1.3.6.1.2.1.2.2.1.1.1 = INTEGER: 1
        * .1.3.6.1.2.1.2.2.1.1.2 = INTEGER: 2
        * .1.3.6.1.2.1.2.2.1.1.3 = INTEGER: 3
        * .1.3.6.1.2.1.2.2.1.1.4 = INTEGER: 4
        * .1.3.6.1.2.1.2.2.1.1.5 = INTEGER: 5
        * .1.3.6.1.2.1.2.2.1.1.6 = INTEGER: 6
        * .1.3.6.1.2.1.2.2.1.2.1 = STRING: lo0
        * .1.3.6.1.2.1.2.2.1.2.2 = STRING: gif0
        * .1.3.6.1.2.1.2.2.1.2.3 = STRING: stf0
        * .1.3.6.1.2.1.2.2.1.2.4 = STRING: en0
        * .1.3.6.1.2.1.2.2.1.2.5 = STRING: en1
        * .1.3.6.1.2.1.2.2.1.2.6 = STRING: fw0
        * .1.3.6.1.2.1.2.2.1.10.1 = Counter32: 6808986
        * .1.3.6.1.2.1.2.2.1.10.2 = Counter32: 0
        * .1.3.6.1.2.1.2.2.1.10.3 = Counter32: 0
        * .1.3.6.1.2.1.2.2.1.10.4 = Counter32: 6561336
        * .1.3.6.1.2.1.2.2.1.10.5 = Counter32: 1241157
        * .1.3.6.1.2.1.2.2.1.10.6 = Counter32: 0
        */
        
        SnmpObjId base = SnmpObjId.get(".1.3.6.1.2.1.2.2.1");
        TestRowCallback rc = new TestRowCallback();
        TableTracker tt = new TableTracker(rc, SnmpObjId.get(base, "1"), SnmpObjId.get(base, "2"), SnmpObjId.get(base, "10"));

        walk(tt, 2, 10);

        List<List<SnmpResult>> responses = rc.getResponses();
        assertEquals("number of rows must match test data", 6, responses.size());
        assertEquals("number of columns must match test data", 3, responses.get(0).size());
        assertEquals("row 4, column 0 must be 5", 5, responses.get(4).get(0).getValue().toInt());
        assertEquals("row 1, column 1 must be gif0", "gif0", responses.get(1).get(1).getValue().toString());
        assertEquals("row 3, column 2 must be 6561336", 6561336, responses.get(3).get(2).getValue().toLong());
        System.err.println(responses);
    }

}
