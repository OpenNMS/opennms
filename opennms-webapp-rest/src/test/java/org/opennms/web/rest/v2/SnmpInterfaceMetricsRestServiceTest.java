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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ws.rs.WebApplicationException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.measurements.api.MeasurementsService;
import org.opennms.netmgt.measurements.api.exceptions.MeasurementException;
import org.opennms.netmgt.measurements.model.QueryRequest;
import org.opennms.netmgt.measurements.model.QueryResponse;
import org.opennms.netmgt.measurements.model.Source;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.web.rest.model.v2.SnmpInterfaceMetricsDTO;
import org.opennms.web.rest.model.v2.SnmpInterfaceMetricsRequestDTO;
import org.opennms.web.rest.model.v2.SnmpInterfaceMetricsResponseDTO;
import org.opennms.web.rest.model.v2.SnmpInterfaceRefDTO;
import org.springframework.test.util.ReflectionTestUtils;

public class SnmpInterfaceMetricsRestServiceTest {

    private SnmpInterfaceMetricsRestService service;
    private MeasurementsService measurements;
    private SnmpInterfaceDao snmpInterfaceDao;

    @Before
    public void setUp() {
        MockLogAppender.setupLogging(true);
        service = new SnmpInterfaceMetricsRestService();
        measurements = mock(MeasurementsService.class);
        snmpInterfaceDao = mock(SnmpInterfaceDao.class);
        ReflectionTestUtils.setField(service, "m_measurementsService", measurements);
        ReflectionTestUtils.setField(service, "m_snmpInterfaceDao", snmpInterfaceDao);
        // Every requested interface exists unless a test says otherwise; the rows
        // come back from one batched query per request, as the service asks.
        when(snmpInterfaceDao.findMatching(any(org.opennms.core.criteria.Criteria.class))).thenAnswer(inv -> {
            final List<OnmsSnmpInterface> found = new ArrayList<>();
            for (final SnmpInterfaceRefDTO ref : requested) {
                final String key = ref.getNodeId() + ":" + ref.getIfIndex();
                if (unknown.contains(key)) {
                    continue;
                }
                found.add(rows.computeIfAbsent(key, k -> row(ref.getNodeId(), ref.getIfIndex(), "if" + ref.getIfIndex())));
            }
            return found;
        });
    }

    private final Map<String, OnmsSnmpInterface> rows = new LinkedHashMap<>();
    private final java.util.Set<String> unknown = new java.util.HashSet<>();
    private List<SnmpInterfaceRefDTO> requested = List.of();

    private static OnmsSnmpInterface row(final int nodeId, final int ifIndex, final String ifName) {
        final OnmsNode node = new OnmsNode();
        node.setId(nodeId);
        final OnmsSnmpInterface snmpInterface = new OnmsSnmpInterface();
        snmpInterface.setNode(node);
        snmpInterface.setIfIndex(ifIndex);
        snmpInterface.setIfName(ifName);
        return snmpInterface;
    }

    private void unknown(final int nodeId, final int ifIndex) {
        unknown.add(nodeId + ":" + ifIndex);
    }

    /** Runs the batch call the way a client would, with the rows the database would return. */
    private SnmpInterfaceMetricsResponseDTO many(final SnmpInterfaceMetricsRequestDTO request) {
        requested = request == null ? List.of() : request.getInterfaces().stream()
                .filter(r -> r != null && r.getNodeId() != null && r.getIfIndex() != null).collect(Collectors.toList());
        return service.getMany(request);
    }

    private static SnmpInterfaceRefDTO ref(final Integer nodeId, final Integer ifIndex) {
        return new SnmpInterfaceRefDTO(nodeId, ifIndex);
    }

    private static SnmpInterfaceMetricsRequestDTO request(final SnmpInterfaceRefDTO... refs) {
        final SnmpInterfaceMetricsRequestDTO request = new SnmpInterfaceMetricsRequestDTO();
        request.setInterfaces(List.of(refs));
        return request;
    }

    private static final long STEP = 300_000L;
    private static final long LAST_SAMPLE = 1_790_093_700_000L;

    /** Timestamps run one step apart and end at LAST_SAMPLE, whatever the longest column's length. */
    private static QueryResponse response(final Map<String, double[]> columns,
                                         final Map<String, Object> constants) {
        final QueryResponse response = new QueryResponse();
        response.setLabels(columns.keySet().toArray(new String[0]));
        response.setColumns(new ArrayList<>(columns.values()));
        response.setConstants(constants);
        final int rows = columns.values().stream().mapToInt(c -> c.length).max().orElse(0);
        final long[] timestamps = new long[rows];
        for (int i = 0; i < rows; i++) {
            timestamps[i] = LAST_SAMPLE - (rows - 1 - i) * STEP;
        }
        response.setTimestamps(timestamps);
        response.setStep(STEP);
        return response;
    }

    private static QueryResponse emptyResponse() {
        return response(new LinkedHashMap<>(), Map.of());
    }

    private void provisioned(final int nodeId, final int ifIndex, final Long ifSpeed,
                             final Integer adminStatus, final Integer operStatus) {
        final OnmsSnmpInterface snmpInterface = row(nodeId, ifIndex, "Gi0/" + ifIndex);
        snmpInterface.setIfSpeed(ifSpeed);
        snmpInterface.setIfAdminStatus(adminStatus);
        snmpInterface.setIfOperStatus(operStatus);
        rows.put(nodeId + ":" + ifIndex, snmpInterface);
    }

    private SnmpInterfaceMetricsDTO first(final SnmpInterfaceMetricsRequestDTO request) {
        return many(request).getInterfaces().get(0);
    }

    private QueryRequest captureRequest() throws MeasurementException {
        final ArgumentCaptor<QueryRequest> captor = ArgumentCaptor.forClass(QueryRequest.class);
        verify(measurements).query(captor.capture());
        return captor.getValue();
    }

    @Test
    public void computesUtilizationFromOctetsAndSpeed() throws Exception {
        final Map<String, double[]> columns = new LinkedHashMap<>();
        // 12.5 MB/s is 100 Mbit/s, a tenth of a gigabit interface.
        columns.put("i0in", new double[] { 12_500_000d });
        columns.put("i0out", new double[] { 1_250_000d });
        when(measurements.query(any())).thenReturn(
                response(columns, Map.of("i0in.ifHighSpeed", "1000")));

        final SnmpInterfaceMetricsDTO dto = first(request(ref(1, 2)));
        assertEquals(1, dto.getNodeId().intValue());
        assertEquals(2, dto.getIfIndex().intValue());
        assertEquals(1_000_000_000L, dto.getSpeedBps().longValue());
        assertEquals(100_000_000d, dto.getInBitsPerSecond(), 1d);
        assertEquals(10d, dto.getInUtilizationPercent(), 0.001d);
        assertEquals(1d, dto.getOutUtilizationPercent(), 0.001d);
    }

    @Test
    public void reportsNoPercentageWithoutAnySpeed() throws Exception {
        final Map<String, double[]> columns = new LinkedHashMap<>();
        columns.put("i0in", new double[] { 12_500_000d });
        columns.put("i0out", new double[] { 0d });
        when(measurements.query(any())).thenReturn(response(columns, Map.of()));

        final SnmpInterfaceMetricsDTO dto = first(request(ref(1, 2)));
        assertNull(dto.getSpeedBps());
        assertNull(dto.getInUtilizationPercent());
        assertNotNull(dto.getInBitsPerSecond());
    }

    @Test
    public void fallsBackToTheProvisionedSpeedWhenIfHighSpeedIsMissing() throws Exception {
        provisioned(1, 2, 100_000_000L, 1, 1);
        final Map<String, double[]> columns = new LinkedHashMap<>();
        columns.put("i0in", new double[] { 1_250_000d });
        when(measurements.query(any())).thenReturn(response(columns, Map.of()));

        final SnmpInterfaceMetricsDTO dto = first(request(ref(1, 2)));
        assertEquals(100_000_000L, dto.getSpeedBps().longValue());
        assertEquals(10d, dto.getInUtilizationPercent(), 0.001d);
    }

    @Test
    public void fallsBackToTheProvisionedSpeedWhenIfHighSpeedIsZero() throws Exception {
        // ifHighSpeed is in Mbit/s and reads 0 below 1 Mbit/s; ifSpeed still knows.
        provisioned(1, 2, 512_000L, 1, 1);
        final Map<String, double[]> columns = new LinkedHashMap<>();
        columns.put("i0in", new double[] { 32_000d });
        when(measurements.query(any())).thenReturn(response(columns, Map.of("i0in.ifHighSpeed", "0")));

        final SnmpInterfaceMetricsDTO dto = first(request(ref(1, 2)));
        assertEquals(512_000L, dto.getSpeedBps().longValue());
        assertEquals(50d, dto.getInUtilizationPercent(), 0.001d);
    }

    @Test
    public void treatsTheSaturated32BitIfSpeedAsUnknownAndLetsIfHighSpeedFillIn() throws Exception {
        provisioned(1, 2, 4_294_967_295L, 1, 1);
        final Map<String, double[]> columns = new LinkedHashMap<>();
        columns.put("i0in", new double[] { 1_250_000_000d });
        when(measurements.query(any())).thenReturn(response(columns, Map.of()));

        final SnmpInterfaceMetricsDTO dto = first(request(ref(1, 2)));
        assertNull(dto.getSpeedBps());
        assertNull(dto.getInUtilizationPercent());
        assertEquals(10_000_000_000d, dto.getInBitsPerSecond(), 1d);

        when(measurements.query(any())).thenReturn(response(columns, Map.of("i0in.ifHighSpeed", "10000")));
        final SnmpInterfaceMetricsDTO withHighSpeed = first(request(ref(1, 2)));
        assertEquals(10_000_000_000L, withHighSpeed.getSpeedBps().longValue());
        assertEquals(100d, withHighSpeed.getInUtilizationPercent(), 0.001d);
    }

    @Test
    public void prefersTheExactProvisionedSpeedOverTheRoundedIfHighSpeed() throws Exception {
        // A T1: ifHighSpeed rounds 1.544 Mb/s up to 2, understating utilization by 23%.
        provisioned(1, 2, 1_544_000L, 1, 1);
        final Map<String, double[]> columns = new LinkedHashMap<>();
        columns.put("i0in", new double[] { 19_300d });
        when(measurements.query(any())).thenReturn(response(columns, Map.of("i0in.ifHighSpeed", "2")));

        final SnmpInterfaceMetricsDTO dto = first(request(ref(1, 2)));
        assertEquals(1_544_000L, dto.getSpeedBps().longValue());
        assertEquals(10d, dto.getInUtilizationPercent(), 0.001d);
    }

    @Test
    public void readsTheCollectedSpeedUnderWhicheverGroupWasAsked() throws Exception {
        // No usable row speed and only the errors group requested: the speed still resolves.
        final Map<String, double[]> columns = new LinkedHashMap<>();
        columns.put("i0ein", new double[] { 0d });
        when(measurements.query(any())).thenReturn(response(columns, Map.of("i0ein.ifHighSpeed", "1000")));
        final SnmpInterfaceMetricsRequestDTO request = request(ref(1, 2));
        request.setMetrics(List.of("errors"));

        assertEquals(1_000_000_000L, first(request).getSpeedBps().longValue());
    }

    @Test
    public void reportsAdminAndOperStatusFromTheDatabase() throws Exception {
        provisioned(1, 2, null, 1, 2);
        when(measurements.query(any())).thenReturn(emptyResponse());

        final SnmpInterfaceMetricsDTO dto = first(request(ref(1, 2)));
        assertEquals(1, dto.getIfAdminStatus().intValue());
        assertEquals(2, dto.getIfOperStatus().intValue());
        assertNull(dto.getSpeedBps());
    }

    @Test
    public void leavesOutAnInterfaceTheDatabaseDoesNotKnow() throws Exception {
        unknown(1, 99);
        when(measurements.query(any())).thenReturn(emptyResponse());

        final List<SnmpInterfaceMetricsDTO> results =
                many(request(ref(1, 2), ref(1, 99), ref(3, 4))).getInterfaces();
        assertEquals(List.of("1:2", "3:4"),
                results.stream().map(r -> r.getNodeId() + ":" + r.getIfIndex()).collect(Collectors.toList()));
        // Nothing is read for it either.
        for (final Source source : captureRequest().getSources()) {
            assertTrue(source.getResourceId(), !source.getResourceId().contains("[if99]"));
        }
    }

    @Test
    public void answers404ForOneInterfaceTheDatabaseDoesNotKnow() throws Exception {
        unknown(1, 99);
        requested = List.of(ref(1, 99));
        try {
            service.getOne(1, 99, null, null, List.of());
            fail("expected 404");
        } catch (final WebApplicationException e) {
            assertEquals(404, e.getResponse().getStatus());
        }
        verify(measurements, never()).query(any());
    }

    @Test
    public void doesNotQueryTheStoreWhenNoRequestedInterfaceExists() throws Exception {
        unknown(1, 99);

        assertTrue(many(request(ref(1, 99))).getInterfaces().isEmpty());
        verify(measurements, never()).query(any());
    }

    @Test
    public void statesTheInstantTheLookbackAndTheStoreStep() throws Exception {
        final Map<String, double[]> columns = new LinkedHashMap<>();
        columns.put("i0in", new double[] { 1d });
        final QueryResponse stored = response(columns, Map.of());
        stored.setStep(3_600_000L);
        when(measurements.query(any())).thenReturn(stored);
        final SnmpInterfaceMetricsRequestDTO request = request(ref(1, 2));
        request.setLookbackSeconds(86_400);

        final long before = System.currentTimeMillis();
        final SnmpInterfaceMetricsResponseDTO response = many(request);
        assertTrue(response.getAt() >= before && response.getAt() <= System.currentTimeMillis());
        assertEquals(3_600, response.getLookbackSeconds());
        assertEquals(3_600L, response.getStepSeconds().longValue());

        final QueryRequest query = captureRequest();
        assertEquals(response.getAt(), query.getEnd());
        assertEquals(3_600_000L, query.getEnd() - query.getStart());
    }

    @Test
    public void answersAPastInstantFromHistoryWithoutStatus() throws Exception {
        provisioned(1, 2, 100_000_000L, 1, 1);
        final Map<String, double[]> columns = new LinkedHashMap<>();
        columns.put("i0in", new double[] { 1_250_000d });
        when(measurements.query(any())).thenReturn(response(columns, Map.of()));
        final SnmpInterfaceMetricsRequestDTO request = request(ref(1, 2));
        final long yesterday = System.currentTimeMillis() - 86_400_000L;
        request.setAt(yesterday);

        final SnmpInterfaceMetricsResponseDTO response = many(request);
        assertEquals(yesterday, response.getAt());
        final SnmpInterfaceMetricsDTO dto = response.getInterfaces().get(0);
        assertNull(dto.getIfAdminStatus());
        assertNull(dto.getIfOperStatus());
        assertEquals(100_000_000L, dto.getSpeedBps().longValue());
        assertEquals(10d, dto.getInUtilizationPercent(), 0.001d);

        final QueryRequest query = captureRequest();
        assertEquals(yesterday, query.getEnd());
        assertEquals(yesterday - 900_000L, query.getStart());
    }

    @Test
    public void readsEveryRateFromTheSameSampleAndSaysWhenItWas() throws Exception {
        final Map<String, double[]> columns = new LinkedHashMap<>();
        // Traffic has the newest row; errors stopped a step earlier, so they read absent there.
        columns.put("i0in", new double[] { 1_000_000d, 12_500_000d, Double.NaN });
        columns.put("i0ein", new double[] { 2d, 3d, Double.NaN });
        columns.put("i0out", new double[] { Double.NaN, Double.NaN, 2_500_000d });
        when(measurements.query(any())).thenReturn(response(columns, Map.of()));

        final SnmpInterfaceMetricsDTO dto = first(request(ref(1, 2)));
        assertEquals(LAST_SAMPLE, dto.getSampledAt().longValue());
        assertEquals(20_000_000d, dto.getOutBitsPerSecond(), 1d);
        assertNull(dto.getInBitsPerSecond());
        assertNull(dto.getInErrorsPerSecond());
    }

    @Test
    public void ignoresARowStampedAfterTheInstant() throws Exception {
        // The store's last row ends after 'at'; the one before it is the answer.
        final Map<String, double[]> columns = new LinkedHashMap<>();
        columns.put("i0in", new double[] { 1_000_000d, 12_500_000d, 2_000_000d });
        when(measurements.query(any())).thenReturn(response(columns, Map.of()));
        final SnmpInterfaceMetricsRequestDTO request = request(ref(1, 2));
        request.setAt(LAST_SAMPLE - STEP / 2);

        final SnmpInterfaceMetricsDTO dto = first(request);
        assertEquals(LAST_SAMPLE - STEP, dto.getSampledAt().longValue());
        assertEquals(100_000_000d, dto.getInBitsPerSecond(), 1d);
    }

    @Test
    public void treatsAFutureInstantAsNow() throws Exception {
        provisioned(1, 2, null, 1, 1);
        when(measurements.query(any())).thenReturn(emptyResponse());
        final SnmpInterfaceMetricsRequestDTO request = request(ref(1, 2));
        request.setAt(System.currentTimeMillis() + 600_000L);

        final long before = System.currentTimeMillis();
        final SnmpInterfaceMetricsResponseDTO response = many(request);
        assertTrue(response.getAt() >= before && response.getAt() <= System.currentTimeMillis());
        // Now, not history: the status is still reported.
        assertEquals(1, response.getInterfaces().get(0).getIfOperStatus().intValue());
    }

    /**
     * Two weeks back the store has hourly rows. A 15-minute lookback ending at
     * 'at' returns only the hour that ends after it, which is not a sample at or
     * before the instant; the lookback widens to the store's step and the row
     * ending before 'at' answers.
     */
    @Test
    public void widensTheLookbackToTheStoreStepForConsolidatedHistory() throws Exception {
        final long hour = 3_600_000L;
        final long at = LAST_SAMPLE - 1_000L;
        final QueryResponse narrow = new QueryResponse();
        narrow.setLabels(new String[] { "i0in" });
        narrow.setColumns(List.of(new double[] { 5_000_000d }));
        narrow.setTimestamps(new long[] { LAST_SAMPLE });          // ends after 'at'
        narrow.setStep(hour);
        final QueryResponse wide = new QueryResponse();
        wide.setLabels(new String[] { "i0in" });
        wide.setColumns(List.of(new double[] { 1_250_000d, 5_000_000d }));
        wide.setTimestamps(new long[] { LAST_SAMPLE - hour, LAST_SAMPLE });
        wide.setStep(hour);
        when(measurements.query(any())).thenReturn(narrow, wide);
        final SnmpInterfaceMetricsRequestDTO request = request(ref(1, 2));
        request.setAt(at);

        final SnmpInterfaceMetricsResponseDTO response = many(request);
        assertEquals(3_600, response.getLookbackSeconds());
        assertEquals(3_600L, response.getStepSeconds().longValue());
        final SnmpInterfaceMetricsDTO dto = response.getInterfaces().get(0);
        assertEquals(LAST_SAMPLE - hour, dto.getSampledAt().longValue());
        assertEquals(10_000_000d, dto.getInBitsPerSecond(), 1d);

        final ArgumentCaptor<QueryRequest> captor = ArgumentCaptor.forClass(QueryRequest.class);
        verify(measurements, org.mockito.Mockito.times(2)).query(captor.capture());
        assertEquals(900_000L, captor.getAllValues().get(0).getEnd() - captor.getAllValues().get(0).getStart());
        assertEquals(hour, captor.getAllValues().get(1).getEnd() - captor.getAllValues().get(1).getStart());
    }

    /** The label is the collector's: punctuation becomes underscores before it reaches the id. */
    @Test
    public void addressesTheRrdByTheCollectorsLabel() throws Exception {
        when(measurements.query(any())).thenReturn(emptyResponse());
        rows.put("1:2", row(1, 2, "Gi0/2 [uplink]"));
        final SnmpInterfaceMetricsRequestDTO request = request(ref(1, 2));
        request.setMetrics(List.of("traffic"));

        many(request);

        final String resourceId = captureRequest().getSources().get(0).getResourceId();
        assertEquals("node[1].interfaceSnmp[Gi0_2__uplink_]", resourceId);
        assertEquals("Gi0_2__uplink_", org.opennms.netmgt.model.ResourceId.fromString(resourceId).name);
    }

    @Test
    public void leavesSampledAtAbsentWhenNothingWasSampled() throws Exception {
        final Map<String, double[]> columns = new LinkedHashMap<>();
        columns.put("i0in", new double[] { Double.NaN, Double.NaN });
        when(measurements.query(any())).thenReturn(response(columns, Map.of()));

        final SnmpInterfaceMetricsDTO dto = first(request(ref(1, 2)));
        assertNull(dto.getSampledAt());
        assertNull(dto.getInBitsPerSecond());
    }

    @Test
    public void skipsANullEntryInTheList() throws Exception {
        when(measurements.query(any())).thenReturn(emptyResponse());
        final SnmpInterfaceMetricsRequestDTO request = new SnmpInterfaceMetricsRequestDTO();
        request.setInterfaces(java.util.Arrays.asList(null, ref(1, 2)));

        assertEquals(1, many(request).getInterfaces().size());
    }

    @Test
    public void takesTheMostRecentSampleThatIsNotNaN() throws Exception {
        final Map<String, double[]> columns = new LinkedHashMap<>();
        columns.put("i0in", new double[] { 1_000_000d, 12_500_000d, Double.NaN, Double.NaN });
        columns.put("i0out", new double[] { Double.NaN, Double.NaN, Double.NaN, Double.NaN });
        when(measurements.query(any())).thenReturn(
                response(columns, Map.of("i0in.ifHighSpeed", "1000")));

        final SnmpInterfaceMetricsDTO dto = first(request(ref(1, 2)));
        assertEquals(100_000_000d, dto.getInBitsPerSecond(), 1d);
        assertEquals(LAST_SAMPLE - 2 * STEP, dto.getSampledAt().longValue());
        assertNull(dto.getOutBitsPerSecond());
        assertNull(dto.getOutUtilizationPercent());
    }

    @Test
    public void readsEachInterfaceOnceHoweverOftenItIsNamed() throws Exception {
        when(measurements.query(any())).thenReturn(emptyResponse());

        final SnmpInterfaceMetricsResponseDTO response =
                many(request(ref(1, 2), ref(1, 2), ref(1, 3)));
        assertEquals(2, response.getInterfaces().size());
        verify(snmpInterfaceDao).findMatching(any(org.opennms.core.criteria.Criteria.class));

        final List<String> resources = captureRequest().getSources().stream()
                .map(Source::getResourceId).distinct().collect(Collectors.toList());
        assertEquals(List.of("node[1].interfaceSnmp[if2]", "node[1].interfaceSnmp[if3]"), resources);
    }

    @Test
    public void addressesResourcesByIfIndexWithHcFallbacks() throws Exception {
        when(measurements.query(any())).thenReturn(emptyResponse());
        final SnmpInterfaceMetricsRequestDTO request = request(ref(7, 42));
        request.setMetrics(List.of("traffic"));

        many(request);

        final List<Source> sources = captureRequest().getSources();
        assertEquals(2, sources.size());
        for (final Source source : sources) {
            assertEquals("node[7].interfaceSnmp[if42]", source.getResourceId());
            assertTrue(source.getAttribute().startsWith("ifHC"));
            assertTrue(source.getFallbackAttribute().startsWith("if"));
        }
        // Relaxed, or one uncollected interface fails the whole batch.
        assertTrue(captureRequest().isRelaxed());
    }

    @Test
    public void answers503WhenTheStoreCannotAnswer() throws Exception {
        provisioned(3, 4, 10_000_000L, 1, 1);
        when(measurements.query(any())).thenThrow(new MeasurementException("rrdtool exited with status 1"));

        try {
            many(request(ref(1, 2), ref(3, 4)));
            fail("expected 503 rather than a summary with no rates");
        } catch (final WebApplicationException e) {
            assertEquals(503, e.getResponse().getStatus());
            assertTrue(String.valueOf(e.getResponse().getEntity()).contains("rrdtool exited with status 1"));
        }
    }

    @Test
    public void answersFromTheDatabaseAloneWithoutAMeasurementsService() {
        ReflectionTestUtils.setField(service, "m_measurementsService", null);
        provisioned(1, 2, 1_000_000_000L, 1, 1);

        final SnmpInterfaceMetricsDTO dto = first(request(ref(1, 2)));
        assertEquals(1_000_000_000L, dto.getSpeedBps().longValue());
        assertNull(dto.getInBitsPerSecond());
        assertNull(dto.getInUtilizationPercent());
    }

    @Test
    public void skipsIncompleteReferencesAndEmptyRequests() throws Exception {
        assertTrue(many(null).getInterfaces().isEmpty());
        assertTrue(many(new SnmpInterfaceMetricsRequestDTO()).getInterfaces().isEmpty());
        assertTrue(many(request(ref(null, 2), ref(1, null))).getInterfaces().isEmpty());
        verify(measurements, never()).query(any());
        verify(snmpInterfaceDao, never()).findMatching(any(org.opennms.core.criteria.Criteria.class));
    }

    @Test
    public void refusesMoreCountersThanItWillQuery() {
        final SnmpInterfaceRefDTO[] refs = new SnmpInterfaceRefDTO[1001];
        for (int i = 0; i < refs.length; i++) {
            refs[i] = ref(1, i);
        }
        try {
            many(request(refs));   // all four groups: 12012 counters
            fail("expected the cap to be enforced");
        } catch (final WebApplicationException e) {
            assertEquals(400, e.getResponse().getStatus());
        }
    }

    @Test
    public void acceptsThoseInterfacesWhenOnlyOneGroupIsAskedFor() throws Exception {
        when(measurements.query(any())).thenReturn(emptyResponse());
        final SnmpInterfaceRefDTO[] refs = new SnmpInterfaceRefDTO[501];
        for (int i = 0; i < refs.length; i++) {
            refs[i] = ref(1, i);
        }
        final SnmpInterfaceMetricsRequestDTO request = request(refs);
        request.setMetrics(List.of("traffic"));

        assertEquals(501, many(request).getInterfaces().size());
        final ArgumentCaptor<QueryRequest> captor = ArgumentCaptor.forClass(QueryRequest.class);
        verify(measurements, org.mockito.Mockito.times(2)).query(captor.capture());
        assertEquals(1_000, captor.getAllValues().get(0).getSources().size());
        assertEquals(2, captor.getAllValues().get(1).getSources().size());
    }

    /**
     * The store is asked in chunks of whole interfaces, never more than 1000
     * counters at once; each chunk's rows land on its own interfaces.
     */
    @Test
    public void chunksTheStoreQueryAndMergesTheAnswersInOrder() throws Exception {
        final SnmpInterfaceRefDTO[] refs = new SnmpInterfaceRefDTO[600];
        for (int i = 0; i < refs.length; i++) {
            refs[i] = ref(1, i);
        }
        final Map<String, double[]> first = new LinkedHashMap<>();
        first.put("i0in", new double[] { 1_250_000d });        // the first interface of chunk one
        final Map<String, double[]> second = new LinkedHashMap<>();
        second.put("i0in", new double[] { 2_500_000d });       // the first interface of chunk two, i.e. index 500
        when(measurements.query(any())).thenReturn(response(first, Map.of()), response(second, Map.of()));
        final SnmpInterfaceMetricsRequestDTO request = request(refs);
        request.setMetrics(List.of("traffic"));

        final List<SnmpInterfaceMetricsDTO> results = many(request).getInterfaces();
        assertEquals(600, results.size());
        assertEquals(10_000_000d, results.get(0).getInBitsPerSecond(), 1d);
        assertNull(results.get(1).getInBitsPerSecond());
        assertEquals(20_000_000d, results.get(500).getInBitsPerSecond(), 1d);
        assertEquals(500, results.get(500).getIfIndex().intValue());

        final ArgumentCaptor<QueryRequest> captor = ArgumentCaptor.forClass(QueryRequest.class);
        verify(measurements, org.mockito.Mockito.times(2)).query(captor.capture());
        for (final QueryRequest query : captor.getAllValues()) {
            assertTrue(query.getSources().size() <= 1_000);
        }
        assertTrue(captor.getAllValues().get(1).getSources().get(0).getResourceId().endsWith("[if500]"));
    }

    @Test
    public void readsErrorsAndDiscardsAsRatesAlongsideTraffic() throws Exception {
        final Map<String, double[]> columns = new LinkedHashMap<>();
        columns.put("i0in", new double[] { 12_500_000d });
        columns.put("i0out", new double[] { 1_250_000d });
        columns.put("i0pin", new double[] { 9_000d });
        columns.put("i0pmin", new double[] { 400d });
        columns.put("i0pbin", new double[] { 100d });
        columns.put("i0pout", new double[] { 1_100d });
        columns.put("i0ein", new double[] { 2.5d });
        columns.put("i0eout", new double[] { 0d });
        columns.put("i0din", new double[] { 0.75d });
        columns.put("i0dout", new double[] { 4d });
        when(measurements.query(any())).thenReturn(
                response(columns, Map.of("i0in.ifHighSpeed", "1000")));

        final SnmpInterfaceMetricsDTO dto = first(request(ref(1, 2)));
        assertEquals(9_000d, dto.getInUnicastPacketsPerSecond(), 0.001d);
        assertEquals(400d, dto.getInMulticastPacketsPerSecond(), 0.001d);
        assertEquals(100d, dto.getInBroadcastPacketsPerSecond(), 0.001d);
        // Only unicast collected outbound: the classes stand alone; no total to misread.
        assertEquals(1_100d, dto.getOutUnicastPacketsPerSecond(), 0.001d);
        assertNull(dto.getOutMulticastPacketsPerSecond());
        assertEquals(2.5d, dto.getInErrorsPerSecond(), 0.001d);
        assertEquals(0d, dto.getOutErrorsPerSecond(), 0.001d);
        assertEquals(0.75d, dto.getInDiscardsPerSecond(), 0.001d);
        assertEquals(4d, dto.getOutDiscardsPerSecond(), 0.001d);
    }

    @Test
    public void leavesAnUncollectedErrorCounterAbsentRatherThanZero() throws Exception {
        final Map<String, double[]> columns = new LinkedHashMap<>();
        columns.put("i0ein", new double[] { Double.NaN });
        when(measurements.query(any())).thenReturn(response(columns, Map.of()));

        final SnmpInterfaceMetricsDTO dto = first(request(ref(1, 2)));
        assertNull(dto.getInErrorsPerSecond());
        assertNull(dto.getOutDiscardsPerSecond());
    }

    @Test
    public void asksForEveryPacketClassUnderTheStockAliases() throws Exception {
        when(measurements.query(any())).thenReturn(emptyResponse());
        final SnmpInterfaceMetricsRequestDTO request = request(ref(1, 2));
        request.setMetrics(List.of("packets"));

        many(request);

        final Map<String, String> fallbacks = new LinkedHashMap<>();
        for (final Source source : captureRequest().getSources()) {
            fallbacks.put(source.getAttribute(), source.getFallbackAttribute());
        }
        assertEquals(List.of("ifHCInUcastPkts", "ifHCInMulticastPkts", "ifHCInBroadcastPkts",
                        "ifHCOutUcastPkts", "ifHCOutMulticastPkt", "ifHCOutBroadcastPkt"),
                new ArrayList<>(fallbacks.keySet()));
        // The 32-bit inbound alias really is spelled with a lower-case p in mib2.xml.
        assertEquals("ifInUcastpkts", fallbacks.get("ifHCInUcastPkts"));
        assertEquals("ifOutUcastPkts", fallbacks.get("ifHCOutUcastPkts"));
        assertNull(fallbacks.get("ifHCInMulticastPkts"));
    }

    @Test
    public void asksForErrorsAndDiscardsWithNoHighCapacityFallback() throws Exception {
        when(measurements.query(any())).thenReturn(emptyResponse());
        final SnmpInterfaceMetricsRequestDTO request = request(ref(1, 2));
        request.setMetrics(List.of("errors", "discards"));

        many(request);

        final List<Source> sources = captureRequest().getSources();
        assertEquals(4, sources.size());
        for (final Source source : sources) {
            assertTrue(source.getAttribute().matches("if(In|Out)(Errors|Discards)"));
            assertNull(source.getFallbackAttribute());
        }
    }

    @Test
    public void refusesAMetricGroupItDoesNotKnow() {
        final SnmpInterfaceMetricsRequestDTO request = request(ref(1, 2));
        request.setMetrics(List.of("traffic", "latency"));
        try {
            many(request);
            fail("expected an unknown group to be refused");
        } catch (final WebApplicationException e) {
            assertEquals(400, e.getResponse().getStatus());
        }
    }

    @Test
    public void clampsTheLookbackToSomethingTheDataSupports() throws Exception {
        when(measurements.query(any())).thenReturn(emptyResponse());
        final SnmpInterfaceMetricsRequestDTO request = request(ref(1, 2));
        request.setLookbackSeconds(1);

        assertEquals(300, many(request).getLookbackSeconds());
        final QueryRequest query = captureRequest();
        assertEquals(300_000L, query.getEnd() - query.getStart());
    }

    @Test
    public void answersOneInterfaceByPath() throws Exception {
        provisioned(5, 9, 1_000_000_000L, 1, 1);
        final Map<String, double[]> columns = new LinkedHashMap<>();
        columns.put("i0in", new double[] { 12_500_000d });
        when(measurements.query(any())).thenReturn(response(columns, Map.of()));

        requested = List.of(ref(5, 9));
        final SnmpInterfaceMetricsResponseDTO response = service.getOne(5, 9, null, null, List.of("traffic"));
        assertEquals(900, response.getLookbackSeconds());
        assertEquals(300L, response.getStepSeconds().longValue());
        assertEquals(1, response.getInterfaces().size());
        final SnmpInterfaceMetricsDTO dto = response.getInterfaces().get(0);
        assertEquals(5, dto.getNodeId().intValue());
        assertEquals(9, dto.getIfIndex().intValue());
        assertEquals(10d, dto.getInUtilizationPercent(), 0.001d);
        assertEquals(2, captureRequest().getSources().size());
    }
}
