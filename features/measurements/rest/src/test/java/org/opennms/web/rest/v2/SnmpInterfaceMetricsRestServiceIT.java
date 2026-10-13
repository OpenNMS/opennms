/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.web.rest.v2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.WebApplicationException;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.support.FilesystemResourceStorageDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.rrd.RrdAttributeType;
import org.opennms.netmgt.rrd.RrdDataSource;
import org.opennms.netmgt.rrd.RrdStrategy;
import org.opennms.netmgt.rrd.RrdStrategyFactory;
import org.opennms.netmgt.rrd.rrdtool.RrdCreationTimeProvider;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.web.rest.model.v2.SnmpInterfaceMetricsDTO;
import org.opennms.web.rest.model.v2.SnmpInterfaceMetricsRequestDTO;
import org.opennms.web.rest.model.v2.SnmpInterfaceMetricsResponseDTO;
import org.opennms.web.rest.model.v2.SnmpInterfaceRefDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * The interface metrics summary against a real database and real RRD files:
 * the interfaces are provisioned through the DAOs, the counters are written
 * with the RRD strategy at known rates, and the service reads them back
 * through the measurements store the way the webapp does.
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath*:/META-INF/opennms/component-measurement.xml",
        "classpath:/META-INF/opennms/applicationContext-measurements-rest-test.xml"
})
@JUnitConfigurationEnvironment(systemProperties={
        "org.opennms.rrd.strategyClass=org.opennms.netmgt.rrd.rrdtool.MultithreadedJniRrdStrategy"
})
@JUnitTemporaryDatabase
public class SnmpInterfaceMetricsRestServiceIT {

    private static final int NODE_ID = 1;
    private static final int STEP_SECONDS = 300;
    private static final int SAMPLES = 6;
    // Per step: 12.5 MB/s in (100 Mbit/s) and 1.25 MB/s out (10 Mbit/s).
    private static final long IN_OCTETS_PER_STEP = 3_750_000_000L;
    private static final long OUT_OCTETS_PER_STEP = 375_000_000L;
    // Per step: 0.01 errors/s.
    private static final long ERRORS_PER_STEP = 3L;

    @Autowired
    private SnmpInterfaceMetricsRestService m_service;

    @Autowired
    private MonitoringLocationDao m_locationDao;

    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private FilesystemResourceStorageDao m_resourceStorageDao;

    @Autowired
    private RrdStrategyFactory m_rrdStrategyFactory;

    @Autowired
    private PlatformTransactionManager m_transactionManager;

    @Rule
    public TemporaryFolder m_rrdDirectory = new TemporaryFolder();

    /** Epoch seconds of the last counter sample: the latest step boundary before now. */
    private long m_lastSample;

    @Before
    public void setUp() throws Exception {
        BeanUtils.assertAutowiring(this);

        new TransactionTemplate(m_transactionManager).execute(status -> {
            final OnmsNode node = new OnmsNode(m_locationDao.getDefaultLocation(), "node1");
            node.setId(NODE_ID);
            // Speed only as collected (ifHighSpeed in strings.properties).
            snmpInterface(node, 12, "eth0", "04013f75f101", null);
            // Speed provisioned on the row, no collected ifHighSpeed.
            snmpInterface(node, 13, "eth1", "04013f75f102", 100_000_000L);
            // Known to the database, never collected.
            snmpInterface(node, 14, "eth2", "04013f75f103", null);
            m_nodeDao.save(node);
            m_nodeDao.flush();
            return null;
        });

        m_resourceStorageDao.setRrdDirectory(m_rrdDirectory.getRoot());
        System.setProperty("rrd.base.dir", m_rrdDirectory.getRoot().getAbsolutePath());

        // The last sample sits within the default lookback of "now".
        m_lastSample = System.currentTimeMillis() / 1000L / STEP_SECONDS * STEP_SECONDS;
        // Traffic on both collected interfaces; error counters on the first only.
        writeCounters("eth0-04013f75f101", true);
        writeCounters("eth1-04013f75f102", false);
        m_resourceStorageDao.setStringAttribute(ResourcePath.get("snmp", String.valueOf(NODE_ID), "eth0-04013f75f101"),
                "ifHighSpeed", "1000");
    }

    @After
    public void tearDown() {
        RrdCreationTimeProvider.setProvider(RrdCreationTimeProvider.DEFAULT);
    }

    private static void snmpInterface(final OnmsNode node, final int ifIndex, final String ifName,
                                      final String physAddr, final Long ifSpeed) {
        final OnmsSnmpInterface snmpInterface = new OnmsSnmpInterface(node, ifIndex);
        snmpInterface.setIfName(ifName);
        snmpInterface.setPhysAddr(physAddr);
        snmpInterface.setIfSpeed(ifSpeed);
        snmpInterface.setIfAdminStatus(1);
        snmpInterface.setIfOperStatus(1);
    }

    /**
     * One file per counter, as collectd stores them without storeByGroup. The
     * strategy stamps a new file's start at "now" minus ten seconds, so the
     * clock it reads is pinned to the first sample while the files are made.
     */
    private void writeCounters(final String interfaceDir, final boolean errors) throws Exception {
        final long firstSample = m_lastSample - (SAMPLES - 1) * STEP_SECONDS;
        final File dir = new File(new File(new File(m_rrdDirectory.getRoot(), "snmp"), String.valueOf(NODE_ID)), interfaceDir);
        RrdCreationTimeProvider.setProvider(() -> firstSample * 1000L);
        writeCounter(dir, "ifHCInOctets", firstSample, IN_OCTETS_PER_STEP);
        writeCounter(dir, "ifHCOutOctets", firstSample, OUT_OCTETS_PER_STEP);
        if (errors) {
            writeCounter(dir, "ifInErrors", firstSample, ERRORS_PER_STEP);
            writeCounter(dir, "ifOutErrors", firstSample, ERRORS_PER_STEP);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void writeCounter(final File dir, final String attribute, final long firstSample,
                              final long octetsPerStep) throws Exception {
        final RrdStrategy strategy = m_rrdStrategyFactory.getStrategy();
        final RrdDataSource dataSource = new RrdDataSource(attribute, RrdAttributeType.COUNTER, STEP_SECONDS * 2, "0", "U");
        final Object definition = strategy.createDefinition("test", dir.getAbsolutePath(), attribute, STEP_SECONDS,
                List.of(dataSource), List.of("RRA:AVERAGE:0.5:1:288"));
        assertNotNull("rrd file for " + attribute + " already exists", definition);
        strategy.createFile(definition);
        final Object file = strategy.openFile(new File(dir, attribute + strategy.getDefaultFileExtension()).getAbsolutePath());
        for (int i = 0; i < SAMPLES; i++) {
            strategy.updateFile(file, "test", (firstSample + i * STEP_SECONDS) + ":" + (i * octetsPerStep));
        }
        strategy.closeFile(file);
    }

    private static SnmpInterfaceMetricsRequestDTO request(final int... ifIndexes) {
        final SnmpInterfaceMetricsRequestDTO request = new SnmpInterfaceMetricsRequestDTO();
        request.setInterfaces(java.util.Arrays.stream(ifIndexes)
                .mapToObj(ifIndex -> new SnmpInterfaceRefDTO(NODE_ID, ifIndex))
                .collect(Collectors.toList()));
        return request;
    }

    private static SnmpInterfaceMetricsDTO byIfIndex(final SnmpInterfaceMetricsResponseDTO response, final int ifIndex) {
        return response.getInterfaces().stream()
                .filter(dto -> dto.getIfIndex() == ifIndex)
                .findFirst()
                .orElseThrow(() -> new AssertionError("no result for ifIndex " + ifIndex));
    }

    @Test
    public void readsRatesAndStatusForTheLatestSample() {
        final SnmpInterfaceMetricsResponseDTO response = m_service.getMany(request(12, 13, 14, 99));

        assertEquals(900, response.getLookbackSeconds());
        assertEquals(Long.valueOf(STEP_SECONDS), response.getStepSeconds());
        // The interface the database does not know is left out; the rest keep request order.
        assertEquals(List.of(12, 13, 14),
                response.getInterfaces().stream().map(SnmpInterfaceMetricsDTO::getIfIndex).collect(Collectors.toList()));

        final SnmpInterfaceMetricsDTO collectedSpeed = byIfIndex(response, 12);
        assertEquals(Integer.valueOf(1), collectedSpeed.getIfAdminStatus());
        assertEquals(Integer.valueOf(1), collectedSpeed.getIfOperStatus());
        assertEquals(Long.valueOf(m_lastSample * 1000L), collectedSpeed.getSampledAt());
        assertEquals(Long.valueOf(1_000_000_000L), collectedSpeed.getSpeedBps());
        assertEquals(100_000_000d, collectedSpeed.getInBitsPerSecond(), 1d);
        assertEquals(10_000_000d, collectedSpeed.getOutBitsPerSecond(), 1d);
        assertEquals(10d, collectedSpeed.getInUtilizationPercent(), 0.001d);
        assertEquals(1d, collectedSpeed.getOutUtilizationPercent(), 0.001d);
        assertEquals(0.01d, collectedSpeed.getInErrorsPerSecond(), 0.0001d);
        // A group never collected reads as absent, not zero.
        assertNull(collectedSpeed.getInUnicastPacketsPerSecond());

        final SnmpInterfaceMetricsDTO provisionedSpeed = byIfIndex(response, 13);
        assertEquals(Long.valueOf(100_000_000L), provisionedSpeed.getSpeedBps());
        assertEquals(100d, provisionedSpeed.getInUtilizationPercent(), 0.001d);
        assertEquals(10d, provisionedSpeed.getOutUtilizationPercent(), 0.001d);
        assertNull(provisionedSpeed.getInErrorsPerSecond());

        final SnmpInterfaceMetricsDTO neverCollected = byIfIndex(response, 14);
        assertEquals(Integer.valueOf(1), neverCollected.getIfOperStatus());
        assertNull(neverCollected.getSampledAt());
        assertNull(neverCollected.getInBitsPerSecond());
        assertNull(neverCollected.getSpeedBps());
    }

    @Test
    public void answersAPastInstantFromTheFilesWithoutStatus() {
        final long at = (m_lastSample - 2 * STEP_SECONDS) * 1000L;
        final SnmpInterfaceMetricsRequestDTO request = request(12);
        request.setAt(at);

        final SnmpInterfaceMetricsResponseDTO response = m_service.getMany(request);

        assertEquals(at, response.getAt());
        final SnmpInterfaceMetricsDTO dto = byIfIndex(response, 12);
        assertEquals(Long.valueOf(at), dto.getSampledAt());
        assertEquals(100_000_000d, dto.getInBitsPerSecond(), 1d);
        assertEquals(10d, dto.getInUtilizationPercent(), 0.001d);
        assertNull(dto.getIfAdminStatus());
        assertNull(dto.getIfOperStatus());
    }

    @Test
    public void readsOnlyTheGroupAsked() {
        final SnmpInterfaceMetricsRequestDTO request = request(12);
        request.setMetrics(List.of("errors"));

        final SnmpInterfaceMetricsDTO dto = byIfIndex(m_service.getMany(request), 12);
        assertNull(dto.getInBitsPerSecond());
        assertEquals(0.01d, dto.getInErrorsPerSecond(), 0.0001d);
        assertEquals(0.01d, dto.getOutErrorsPerSecond(), 0.0001d);
        // The collected speed rides along with whichever group was read: the
        // store attaches a resource's string attributes to the counters it finds.
        assertEquals(Long.valueOf(1_000_000_000L), dto.getSpeedBps());
    }

    @Test
    public void answers404ForAnInterfaceTheDatabaseDoesNotKnow() {
        try {
            m_service.getOne(NODE_ID, 99, null, null, List.of());
            fail("expected 404");
        } catch (final WebApplicationException e) {
            assertEquals(404, e.getResponse().getStatus());
        }
    }
}
