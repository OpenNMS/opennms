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
import org.opennms.test.mock.MockUtil;
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
    
    private final SnmpObjId m_ifTable = SnmpObjId.get(".1.3.6.1.2.1.2.2.1");
    private final SnmpObjId m_ifIndex = SnmpObjId.get(m_ifTable, "1");
    private final SnmpObjId m_ifDescr = SnmpObjId.get(m_ifTable, "2");
    private final SnmpObjId m_ifType = SnmpObjId.get(m_ifTable, "3");
    private final SnmpObjId m_ifMtu = SnmpObjId.get(m_ifTable, "4");
    private final SnmpObjId m_ifSpeed = SnmpObjId.get(m_ifTable, "5");
    private final SnmpObjId m_ifPhysAddress = SnmpObjId.get(m_ifTable, "6");
    private final SnmpObjId m_ifAdminStatus = SnmpObjId.get(m_ifTable, "7");
    private final SnmpObjId m_ifOperStatus = SnmpObjId.get(m_ifTable, "8");
    private final SnmpObjId m_ifLastChange = SnmpObjId.get(m_ifTable, "9");
    private final SnmpObjId m_ifInOctets = SnmpObjId.get(m_ifTable, "10");
    private final SnmpObjId m_ifInUcastPkts = SnmpObjId.get(m_ifTable, "11");
    private final SnmpObjId m_ifInNUcastPkts = SnmpObjId.get(m_ifTable, "12");
    private final SnmpObjId m_ifInDiscards = SnmpObjId.get(m_ifTable, "13");
    private final SnmpObjId m_ifInErrors = SnmpObjId.get(m_ifTable, "14");
    private final SnmpObjId m_ifUnknownProtos = SnmpObjId.get(m_ifTable, "15");
    private final SnmpObjId m_ifOutOctets = SnmpObjId.get(m_ifTable, "16");
    private final SnmpObjId m_ifOutUcastPkts = SnmpObjId.get(m_ifTable, "17");
    private final SnmpObjId m_ifOutNUcastPkts = SnmpObjId.get(m_ifTable, "18");
    private final SnmpObjId m_ifOutDiscards = SnmpObjId.get(m_ifTable, "19");
    private final SnmpObjId m_ifOutErrors = SnmpObjId.get(m_ifTable, "20");
    private final SnmpObjId m_ifOutQLen = SnmpObjId.get(m_ifTable, "21");
    private final SnmpObjId m_ifSpecific = SnmpObjId.get(m_ifTable, "22");

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

    private class ResultTable {
        
        int m_rowsAdded = 0;
        Map<SnmpInstId, SnmpRowResult> m_results = new HashMap<SnmpInstId, SnmpRowResult>();
        
        SnmpValue getResult(SnmpObjId base, SnmpInstId inst) {
            SnmpRowResult row = m_results.get(inst);
            if (row == null) return null;
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

        /**
         * @return
         */
        public int getRowCount() {
            return m_results.size();
        }

        /**
         * @return
         */
        public int getColumnCount() {
            int maxColumns = Integer.MIN_VALUE;
            for(SnmpRowResult row : m_results.values()) {
                maxColumns = Math.max(maxColumns, row.getColumnCount());
            }
            return maxColumns;
        }

    }
    private class TestRowCallback implements RowCallback {
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
 
    /*
     * move row tracking to SnmpTableResult
     * propagate maxRepetitions to children/columns
     * 
     * handle full table
     * handle missing cell
     * handle missing column
     * handle timeout
     * handle errors
     * 
     * work inside an aggregate tracker
     * 
     * ensure 'processedRows' are 'freed'
     * 
     * ensure rows are processed as soon as possible
     * 
     * handle rows with 'non-int' instances
     * 
     * properly handle maxVarsPerPdu correctly
     */
    @Test
    public void testTableTrackerWithFullTable() throws Exception {
        TestRowCallback rc = new TestRowCallback();
        TableTracker tt = new TableTracker(rc, m_ifIndex, m_ifDescr, m_ifSpeed);

        walk(tt, 3, 10);

        ResultTable results = rc.getResults();
        assertTrue("tracker must be finished", tt.isFinished());
        assertEquals("number of rows added must match test data", 6, results.getRowsAdded());
        assertEquals("number of rows must match test data", 6, results.getRowCount());
        assertEquals("number of columns must match test data", 3, results.getColumnCount());
        assertEquals("ifIndex.5 must be 5", 5, results.getResult(m_ifIndex, "5").toInt());
        assertEquals("ifName.2 must be gif0", "gif0", results.getResult(m_ifDescr, "2").toString());
        assertEquals("ifSpeed.3 must be 0", 0, results.getResult(m_ifSpeed, "3").toLong());
        assertEquals("ifSpeed.4 must be 10000000", 10000000, results.getResult(m_ifSpeed, "4").toLong());

    }

    @Test
    @Ignore
    @JUnitSnmpAgent(port=9161, resource="classpath:snmpTestDataIncompleteTable.properties")
    public void testIncompleteTableData() throws Exception {
        TestRowCallback rc = new TestRowCallback();
        TableTracker tt = new TableTracker(rc,
            m_ifIndex, m_ifDescr, m_ifMtu,
            m_ifLastChange, m_ifInUcastPkts, m_ifInErrors,
            m_ifOutUcastPkts, m_ifOutNUcastPkts, m_ifOutErrors
        );

        walk(tt, 4, 3);

        List<SnmpRowResult> responses = rc.getResponses();
        for (int i = 0; i < responses.size(); i++) {
            System.err.println(String.format("%d: %s", i, responses.get(i)));
        }
        ResultTable results = rc.getResults();
        assertTrue("tracker must be finished", tt.isFinished());
        assertEquals("number of rows added must match test data", 6, results.getRowsAdded());
        assertEquals("number of rows must match test data", 6, results.getRowCount());
        assertEquals("number of columns must match test data", 9, results.getColumnCount());
        assertNull("ifMtu.4 should be null", results.getResult(m_ifMtu, "4"));
        assertEquals("ifDescr.5 should be en1", "en1", results.getResult(m_ifDescr, "5").toString());
        assertEquals("ifMtu.6 should be 4078", 4078, results.getResult(m_ifMtu, "6").toInt());
        /*
        assertEquals("ifName.2 must be gif0", "gif0", results.getResult(m_ifDescr, "2").toString());
        assertEquals("ifSpeed.3 must be 0", 0, results.getResult(m_ifSpeed, "3").toLong());
        assertEquals("ifSpeed.4 must be 10000000", 10000000, results.getResult(m_ifSpeed, "4").toLong());
        */
    }
}
