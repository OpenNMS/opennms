package org.opennms.netmgt.snmp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
        private List<SnmpRowResult> m_responses = new ArrayList<SnmpRowResult>();

        public void rowCompleted(SnmpRowResult row) {
            m_responses.add(row);
        }
        
        public List<SnmpRowResult> getResponses() {
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
        SnmpObjId base = SnmpObjId.get(".1.3.6.1.2.1.2.2.1");
        TestRowCallback rc = new TestRowCallback();
        TableTracker tt = new TableTracker(rc, SnmpObjId.get(base, "1"), SnmpObjId.get(base, "2"), SnmpObjId.get(base, "10"));

        walk(tt, 3, 10);

        List<SnmpRowResult> responses = rc.getResponses();
        assertTrue("tracker must be finished", tt.isFinished());
        assertEquals("number of rows must match test data", 6, responses.size());
        assertEquals("number of columns must match test data", 3, responses.get(0).getColumns());
        assertEquals("row 4, column 0 must be 5", 5, responses.get(4).get(1).getValue().toInt());
        assertEquals("row 1, column 1 must be gif0", "gif0", responses.get(1).get(2).getValue().toString());
        assertEquals("row 3, column 2 must be 6561336", 6561336, responses.get(3).get(10).getValue().toLong());
    }

    @Test
    @JUnitSnmpAgent(port=9161, resource="classpath:snmpTestDataIncompleteTable.properties")
    public void testIncompleteTableData() throws Exception {
        SnmpObjId base = SnmpObjId.get(".1.3.6.1.2.1.2.2.1");
        TestRowCallback rc = new TestRowCallback();
        TableTracker tt = new TableTracker(rc,
            SnmpObjId.get(base, "1"), SnmpObjId.get(base, "4"), SnmpObjId.get(base, "8"),
            SnmpObjId.get(base, "9"), SnmpObjId.get(base, "11"), SnmpObjId.get(base, "14"),
            SnmpObjId.get(base, "17"), SnmpObjId.get(base, "18"), SnmpObjId.get(base, "20")
        );

        walk(tt, 4, 3);

        List<SnmpRowResult> responses = rc.getResponses();
        for (int i = 0; i < responses.size(); i++) {
            System.err.println(String.format("%d: %s", i, responses.get(i)));
        }
        assertTrue("tracker must be finished", tt.isFinished());
        assertEquals("number of rows must match test data", 6, responses.size());
        assertEquals("number of columns must match test data", 9, responses.get(0).getColumns());
        assertNull("row 3, column 1 should be null", responses.get(3).get(1));
        assertEquals("row 2, column 1 should be 3", 3, responses.get(2).get(1).getValue().toInt());
        /*
        assertEquals("row 4, column 0 must be 5", 5, responses.get(4).get(1).getValue().toInt());
        assertEquals("row 1, column 1 must be gif0", "gif0", responses.get(1).get(2).getValue().toString());
        assertEquals("row 3, column 2 must be 6561336", 6561336, responses.get(3).get(10).getValue().toLong());
        */
    }
}
