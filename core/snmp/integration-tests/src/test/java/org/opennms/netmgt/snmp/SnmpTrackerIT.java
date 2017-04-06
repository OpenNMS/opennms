/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.snmp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
		"classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml"
})
@JUnitSnmpAgent(host="192.0.2.205", resource="classpath:snmpTestData1.properties")
public class SnmpTrackerIT implements InitializingBean {
	
	private static final Logger LOG = LoggerFactory.getLogger(SnmpTrackerIT.class);
	
	@Autowired
	private SnmpPeerFactory m_snmpPeerFactory;

    public static final class SnmpTableConstants {
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
        private long m_columnCount = 0;
        private final List<ResponseError> m_errors = new ArrayList<>();

        public CountingColumnTracker(final SnmpObjId base) {
            super(base);
        }
        
        public CountingColumnTracker(final SnmpObjId base, final int maxRepetitions, final int maxRetries) {
            super(base, maxRepetitions, maxRetries);
        }
        
        public long getColumnCount() {
            return m_columnCount;
        }
        
        public List<ResponseError> getResponseErrors() {
            return m_errors;
        }

        @Override
        protected void storeResult(final SnmpResult res) {
        	LOG.debug("storing result: {}", res);
            m_columnCount++;
        }

        @Override
        public ResponseProcessor buildNextPdu(PduBuilder pduBuilder) {
            final ResponseProcessor rp = super.buildNextPdu(pduBuilder);
            final ResponseProcessor errorRp = new ResponseProcessor() {

                @Override
                public void processResponse(final SnmpObjId snmpObjId, SnmpValue val) {
                    rp.processResponse(snmpObjId, val);
                }

                @Override
                public boolean processErrors(final int errorStatus, final int errorIndex) {
                    final boolean retry = rp.processErrors(errorStatus, errorIndex);
                    m_errors.add(new ResponseError(ErrorStatus.fromStatus(errorStatus), retry));
                    return retry;
                }
            };
            return errorRp;
        }
    }

    static private class ResponseError {
        private ErrorStatus m_status;
        private boolean m_retry;

        public ResponseError(final ErrorStatus status, final boolean retry) {
            m_status = status;
            m_retry = retry;
        }

        public ErrorStatus getStatus() {
            return m_status;
        }

        public boolean getRetry() {
            return m_retry;
        }
    }

    static private final class ResultTable {
    	private int m_rowsAdded = 0;
    	private Map<SnmpInstId, SnmpRowResult> m_results = new HashMap<SnmpInstId, SnmpRowResult>();
        
        SnmpValue getResult(final SnmpObjId base, final SnmpInstId inst) {
        	final SnmpRowResult row = m_results.get(inst);
            if (row == null) {
                return null;
            }
            return row.getValue(base);
        }

        SnmpValue getResult(final SnmpObjId base, final String inst) {
            return getResult(base, new SnmpInstId(inst));
        }
        
        int getRowsAdded() {
            return m_rowsAdded;
        }
        
        void addSnmpRowResult(final SnmpRowResult row) {
            m_rowsAdded++;
            m_results.put(row.getInstance(), row);
        }

        public int getRowCount() {
            return m_results.size();
        }

        public int getColumnCount() {
        	int maxColumns = Integer.MIN_VALUE;
            for(final SnmpRowResult row : m_results.values()) {
                maxColumns = Math.max(maxColumns, row.getColumnCount());
            }
            return maxColumns;
        }

    }
    static private class TestRowCallback implements RowCallback {
        private final List<SnmpRowResult> m_responses = new ArrayList<SnmpRowResult>();
        private final ResultTable m_results = new ResultTable();

        @Override
        public void rowCompleted(final SnmpRowResult row) {
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

    private void walk(final CollectionTracker c, final int maxVarsPerPdu, final int maxRepetitions, final int maxRetries) throws Exception {
    	final SnmpAgentConfig config = m_snmpPeerFactory.getAgentConfig(InetAddressUtils.addr("192.0.2.205"));
        config.setVersion(SnmpAgentConfig.VERSION2C);
        config.setMaxVarsPerPdu(maxVarsPerPdu);
        config.setMaxRepetitions(maxRepetitions);
        config.setRetries(maxRetries);
        final SnmpWalker walker = SnmpUtils.createWalker(config, "test", c);
        assertNotNull(walker);
        walker.start();
        walker.waitFor();
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp() {
        MockLogAppender.setupLogging();
        SnmpPeerFactory.setInstance(m_snmpPeerFactory);
    }
    
    @Test
    public void testColumnTracker() throws Exception {
    	final CountingColumnTracker ct = new CountingColumnTracker(SnmpObjId.get(".1.3.6.1.2.1.2.2.1.1"));
        walk(ct, 10, 3, 0);
        assertEquals("number of columns returned must match test data", 6l, ct.getColumnCount());
    }
 
    @Test
    @JUnitSnmpAgent(host="192.0.2.205", resource="classpath:snmpTestDataError.properties")
    public void testColumnTrackerWithError() throws Exception {
        final CountingColumnTracker ct = new CountingColumnTracker(SnmpObjId.get(".1.3.6.1.3.17"));
        walk(ct, 10, 3, 3);
        final List<ResponseError> errors = ct.getResponseErrors();
        assertEquals("Number of errors before giving up should be 3", 3, errors.size());
        for (final ResponseError error : errors) {
            assertEquals("buildNextPdu should indicate a retry should be attempted", true, error.getRetry());
            assertEquals(".1.3.6.1.3.17 should return an AUTHORIZATION_ERROR(16)", ErrorStatus.AUTHORIZATION_ERROR, error.getStatus());
        }
    }

    @Test
    public void testTableTrackerWithFullTable() throws Exception {
    	final TestRowCallback rc = new TestRowCallback();
    	final TableTracker tt = new TableTracker(rc, SnmpTableConstants.ifIndex, SnmpTableConstants.ifDescr, SnmpTableConstants.ifSpeed);

        walk(tt, 3, 10, 0);

        final ResultTable results = rc.getResults();
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
    @JUnitSnmpAgent(host="192.0.2.205", resource="classpath:snmpTestDataIncompleteTable.properties")
    public void testIncompleteTableData() throws Exception {
    	final TestRowCallback rc = new TestRowCallback();
        final TableTracker tt = new TableTracker(rc,
            SnmpTableConstants.ifIndex, SnmpTableConstants.ifDescr, SnmpTableConstants.ifMtu,
            SnmpTableConstants.ifLastChange, SnmpTableConstants.ifInUcastPkts, SnmpTableConstants.ifInErrors,
            SnmpTableConstants.ifOutUcastPkts, SnmpTableConstants.ifOutNUcastPkts, SnmpTableConstants.ifOutErrors
        );

        walk(tt, 4, 3, 0);

        printResponses(rc);
        final ResultTable results = rc.getResults();
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
    	final TestRowCallback rc = new TestRowCallback();
        final TableTracker[] tt = new TableTracker[2];
        tt[0] = new TableTracker(rc, SnmpTableConstants.ifIndex, SnmpTableConstants.ifDescr);
        tt[1] = new TableTracker(rc, SnmpTableConstants.ifMtu, SnmpTableConstants.ifLastChange);
        final AggregateTracker at = new AggregateTracker(tt);

        walk(at, 4, 10, 0);

        printResponses(rc);
    }

    private void printResponses(final TestRowCallback rc) {
    	final List<SnmpRowResult> responses = rc.getResponses();
        for (int i = 0; i < responses.size(); i++) {
            final SnmpRowResult row = responses.get(i);
            LOG.debug("{}: instance={}", i, row.getInstance());
            for (final SnmpResult res : row.getResults()) {
            	LOG.debug("    {}={}", res.getBase(), res.getValue());
            }
        }
    }
    
}
