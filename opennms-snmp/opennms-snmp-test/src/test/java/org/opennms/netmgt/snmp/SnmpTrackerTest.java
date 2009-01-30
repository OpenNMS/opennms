package org.opennms.netmgt.snmp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.mock.snmp.JUnitSnmpAgent;
import org.opennms.mock.snmp.JUnitSnmpAgentExecutionListener;
import org.opennms.test.mock.MockLogAppender;
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

    public static class SnmpTableConstants {
        static final SnmpObjId ifTable = SnmpObjId.get(".1.3.6.1.2.1.2.2.1");
        static final SnmpObjId ifIndex = SnmpObjId.get(ifTable, "1");
        static final SnmpObjId ifDescr = SnmpObjId.get(ifTable, "2");
        static final SnmpObjId ifType = SnmpObjId.get(ifTable, "3");
        static final SnmpObjId ifMtu = SnmpObjId.get(ifTable, "4");
        static final SnmpObjId ifSpeed = SnmpObjId.get(ifTable, "5");
        static final SnmpObjId ifPhysAddress = SnmpObjId.get(ifTable, "6");
        static final SnmpObjId ifAdminStatus = SnmpObjId.get(ifTable, "7");
        static final SnmpObjId ifOperStatus = SnmpObjId.get(ifTable, "8");
        static final SnmpObjId ifLastChange = SnmpObjId.get(ifTable, "9");
        static final SnmpObjId ifInOctets = SnmpObjId.get(ifTable, "10");
        static final SnmpObjId ifInUcastPkts = SnmpObjId.get(ifTable, "11");
        static final SnmpObjId ifInNUcastPkts = SnmpObjId.get(ifTable, "12");
        static final SnmpObjId ifInDiscards = SnmpObjId.get(ifTable, "13");
        static final SnmpObjId ifInErrors = SnmpObjId.get(ifTable, "14");
        static final SnmpObjId ifUnknownProtos = SnmpObjId.get(ifTable, "15");
        static final SnmpObjId ifOutOctets = SnmpObjId.get(ifTable, "16");
        static final SnmpObjId ifOutUcastPkts = SnmpObjId.get(ifTable, "17");
        static final SnmpObjId ifOutNUcastPkts = SnmpObjId.get(ifTable, "18");
        static final SnmpObjId ifOutDiscards = SnmpObjId.get(ifTable, "19");
        static final SnmpObjId ifOutErrors = SnmpObjId.get(ifTable, "20");
        static final SnmpObjId ifOutQLen = SnmpObjId.get(ifTable, "21");
        static final SnmpObjId ifSpecific = SnmpObjId.get(ifTable, "22");
    }

    static private class CountingColumnTracker extends ColumnTracker {
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

    static private class ResultTable {
        
        int m_rowsAdded = 0;
        Map<SnmpInstId, SnmpRowResult> m_results = new HashMap<SnmpInstId, SnmpRowResult>();
        
        SnmpValue getResult(SnmpObjId base, SnmpInstId inst) {
            SnmpRowResult row = m_results.get(inst);
            if (row == null) {
                return null;
            }
            return row.getValue(base);
        }

        SnmpValue getResult(SnmpObjId base, String inst) {
            return getResult(base, new SnmpInstId(inst));
        }
        
        int getRowsAdded() {
            return m_rowsAdded;
        }
        
        void addSnmpRowResult(SnmpRowResult row) {
            m_rowsAdded++;
            m_results.put(row.getInstance(), row);
        }

        public int getRowCount() {
            return m_results.size();
        }

        public int getColumnCount() {
            int maxColumns = Integer.MIN_VALUE;
            for(SnmpRowResult row : m_results.values()) {
                maxColumns = Math.max(maxColumns, row.getColumnCount());
            }
            return maxColumns;
        }

    }
    static private class TestRowCallback implements RowCallback {
        private List<SnmpRowResult> m_responses = new ArrayList<SnmpRowResult>();
        
        private ResultTable m_results = new ResultTable();

        public void rowCompleted(SnmpRowResult row) {
            m_responses.add(row);
            m_results.addSnmpRowResult(row);
        }
        
        public List<SnmpRowResult> getResponses() {
            return m_responses;
        }
        
        public ResultTable getResults() {
            return m_results;
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
    
    @Before
    public void setUp() {
        MockLogAppender.setupLogging();
    }
    
    @Test
    public void testColumnTracker() throws Exception {
        CountingColumnTracker ct = new CountingColumnTracker(SnmpObjId.get(".1.3.6.1.2.1.2.2.1.1"));

        walk(ct, 10, 3);
        assertEquals("number of columns returned must match test data", Long.valueOf(6).longValue(), ct.getCount());
    }
 
    @Test
    public void testTableTrackerWithFullTable() throws Exception {
        TestRowCallback rc = new TestRowCallback();
        TableTracker tt = new TableTracker(rc, SnmpTableConstants.ifIndex, SnmpTableConstants.ifDescr, SnmpTableConstants.ifSpeed);

        walk(tt, 3, 10);

        ResultTable results = rc.getResults();
        assertTrue("tracker must be finished", tt.isFinished());
        assertEquals("number of rows added must match test data", 6, results.getRowsAdded());
        assertEquals("number of rows must match test data", 6, results.getRowCount());
        assertEquals("number of columns must match test data", 3, results.getColumnCount());
        assertEquals("ifIndex.5 must be 5", 5, results.getResult(SnmpTableConstants.ifIndex, "5").toInt());
        assertEquals("ifName.2 must be gif0", "gif0", results.getResult(SnmpTableConstants.ifDescr, "2").toString());
        assertEquals("ifSpeed.3 must be 0", 0, results.getResult(SnmpTableConstants.ifSpeed, "3").toLong());
        assertEquals("ifSpeed.4 must be 10000000", 10000000, results.getResult(SnmpTableConstants.ifSpeed, "4").toLong());

    }

    @Test
    @JUnitSnmpAgent(port=9161, resource="classpath:snmpTestDataIncompleteTable.properties")
    public void testIncompleteTableData() throws Exception {
        TestRowCallback rc = new TestRowCallback();
        TableTracker tt = new TableTracker(rc,
            SnmpTableConstants.ifIndex, SnmpTableConstants.ifDescr, SnmpTableConstants.ifMtu,
            SnmpTableConstants.ifLastChange, SnmpTableConstants.ifInUcastPkts, SnmpTableConstants.ifInErrors,
            SnmpTableConstants.ifOutUcastPkts, SnmpTableConstants.ifOutNUcastPkts, SnmpTableConstants.ifOutErrors
        );

        walk(tt, 4, 3);

        printResponses(rc);
        ResultTable results = rc.getResults();
        assertTrue("tracker must be finished", tt.isFinished());
        assertEquals("number of rows added must match test data", 6, results.getRowsAdded());
        assertEquals("number of rows must match test data", 6, results.getRowCount());
        assertEquals("number of columns must match test data", 9, results.getColumnCount());
        assertNull("ifMtu.4 should be null", results.getResult(SnmpTableConstants.ifMtu, "4"));
        assertEquals("ifDescr.5 should be en1", "en1", results.getResult(SnmpTableConstants.ifDescr, "5").toString());
        assertEquals("ifMtu.6 should be 4078", 4078, results.getResult(SnmpTableConstants.ifMtu, "6").toInt());
    }

    @Test
    @Ignore("Hmm, what *should* this do?  When using a callback, we don't pass storeResult() up-stream...")
    public void testAggregateTable() throws Exception {
        TestRowCallback rc = new TestRowCallback();
        TableTracker[] tt = new TableTracker[2];
        tt[0] = new TableTracker(rc, SnmpTableConstants.ifIndex, SnmpTableConstants.ifDescr);
        tt[1] = new TableTracker(rc, SnmpTableConstants.ifMtu, SnmpTableConstants.ifLastChange);
        AggregateTracker at = new AggregateTracker(tt);
        
        walk(at, 4, 10);

        printResponses(rc);
    }

    private void printResponses(TestRowCallback rc) {
        List<SnmpRowResult> responses = rc.getResponses();
        for (int i = 0; i < responses.size(); i++) {
            SnmpRowResult row = responses.get(i);
            System.err.println(String.format("%d: instance=%s", i, row.getInstance()));
            for (SnmpResult res : row.getResults()) {
                System.err.println(String.format("    %s=%s", res.getBase(), res.getValue()));
            }
        }
    }
    
}
