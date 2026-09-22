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
import static org.mockito.ArgumentMatchers.anyInt;
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
    }

    private static SnmpInterfaceRefDTO ref(final Integer nodeId, final Integer ifIndex) {
        return new SnmpInterfaceRefDTO(nodeId, ifIndex);
    }

    private static SnmpInterfaceMetricsRequestDTO request(final SnmpInterfaceRefDTO... refs) {
        final SnmpInterfaceMetricsRequestDTO request = new SnmpInterfaceMetricsRequestDTO();
        request.setInterfaces(List.of(refs));
        return request;
    }

    private static QueryResponse response(final Map<String, double[]> columns,
                                         final Map<String, Object> constants) {
        final QueryResponse response = new QueryResponse();
        response.setLabels(columns.keySet().toArray(new String[0]));
        response.setColumns(new ArrayList<>(columns.values()));
        response.setConstants(constants);
        return response;
    }

    private static QueryResponse emptyResponse() {
        return response(new LinkedHashMap<>(), Map.of());
    }

    private void provisioned(final int nodeId, final int ifIndex, final Long ifSpeed,
                             final Integer adminStatus, final Integer operStatus) {
        final OnmsSnmpInterface snmpInterface = new OnmsSnmpInterface();
        snmpInterface.setIfIndex(ifIndex);
        snmpInterface.setIfSpeed(ifSpeed);
        snmpInterface.setIfAdminStatus(adminStatus);
        snmpInterface.setIfOperStatus(operStatus);
        when(snmpInterfaceDao.findByNodeIdAndIfIndex(nodeId, ifIndex)).thenReturn(snmpInterface);
    }

    private SnmpInterfaceMetricsDTO first(final SnmpInterfaceMetricsRequestDTO request) {
        return service.getMany(request).getInterfaces().get(0);
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
    public void treatsTheSaturated32BitIfSpeedAsUnknown() throws Exception {
        provisioned(1, 2, 4_294_967_295L, 1, 1);
        final Map<String, double[]> columns = new LinkedHashMap<>();
        columns.put("i0in", new double[] { 1_250_000_000d });
        when(measurements.query(any())).thenReturn(response(columns, Map.of()));

        final SnmpInterfaceMetricsDTO dto = first(request(ref(1, 2)));
        assertNull(dto.getSpeedBps());
        assertNull(dto.getInUtilizationPercent());
        assertEquals(10_000_000_000d, dto.getInBitsPerSecond(), 1d);
    }

    @Test
    public void prefersTheCollectedIfHighSpeedOverTheProvisionedSpeed() throws Exception {
        provisioned(1, 2, 100_000_000L, 1, 1);
        final Map<String, double[]> columns = new LinkedHashMap<>();
        columns.put("i0in", new double[] { 12_500_000d });
        when(measurements.query(any())).thenReturn(response(columns, Map.of("i0in.ifHighSpeed", "1000")));

        final SnmpInterfaceMetricsDTO dto = first(request(ref(1, 2)));
        assertEquals(1_000_000_000L, dto.getSpeedBps().longValue());
        assertEquals(10d, dto.getInUtilizationPercent(), 0.001d);
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
    public void leavesStatusAbsentForAnInterfaceTheDatabaseDoesNotKnow() throws Exception {
        when(measurements.query(any())).thenReturn(emptyResponse());

        final SnmpInterfaceMetricsDTO dto = first(request(ref(1, 2)));
        assertNull(dto.getIfAdminStatus());
        assertNull(dto.getIfOperStatus());
        assertNull(dto.getSpeedBps());
    }

    @Test
    public void statesTheWindowEveryRateWasAveragedOver() throws Exception {
        when(measurements.query(any())).thenReturn(emptyResponse());
        final SnmpInterfaceMetricsRequestDTO request = request(ref(1, 2));
        request.setWindowSeconds(3_600);

        final long before = System.currentTimeMillis();
        final SnmpInterfaceMetricsResponseDTO response = service.getMany(request);
        assertEquals(3_600, response.getWindowSeconds());
        assertTrue(response.getEnd() >= before && response.getEnd() <= System.currentTimeMillis());

        final QueryRequest query = captureRequest();
        assertEquals(response.getEnd(), query.getEnd());
        assertEquals(3_600_000L, query.getEnd() - query.getStart());
    }

    @Test
    public void takesTheMostRecentSampleThatIsNotNaN() throws Exception {
        final Map<String, double[]> columns = new LinkedHashMap<>();
        columns.put("i0in", new double[] { 1_000_000d, 12_500_000d, Double.NaN, Double.NaN });
        columns.put("i0out", new double[] { Double.NaN });
        when(measurements.query(any())).thenReturn(
                response(columns, Map.of("i0in.ifHighSpeed", "1000")));

        final SnmpInterfaceMetricsDTO dto = first(request(ref(1, 2)));
        assertEquals(100_000_000d, dto.getInBitsPerSecond(), 1d);
        assertNull(dto.getOutBitsPerSecond());
        assertNull(dto.getOutUtilizationPercent());
    }

    @Test
    public void readsEachInterfaceOnceHoweverOftenItIsNamed() throws Exception {
        when(measurements.query(any())).thenReturn(emptyResponse());

        final SnmpInterfaceMetricsResponseDTO response =
                service.getMany(request(ref(1, 2), ref(1, 2), ref(1, 3)));
        assertEquals(2, response.getInterfaces().size());
        verify(snmpInterfaceDao).findByNodeIdAndIfIndex(1, 2);
        verify(snmpInterfaceDao).findByNodeIdAndIfIndex(1, 3);

        final List<String> resources = captureRequest().getSources().stream()
                .map(Source::getResourceId).distinct().collect(Collectors.toList());
        assertEquals(List.of("node[1].interfaceSnmpByIfIndex[2]", "node[1].interfaceSnmpByIfIndex[3]"),
                resources);
    }

    @Test
    public void addressesResourcesByIfIndexWithHcFallbacks() throws Exception {
        when(measurements.query(any())).thenReturn(emptyResponse());
        final SnmpInterfaceMetricsRequestDTO request = request(ref(7, 42));
        request.setMetrics(List.of("traffic"));

        service.getMany(request);

        final List<Source> sources = captureRequest().getSources();
        assertEquals(2, sources.size());
        for (final Source source : sources) {
            assertEquals("node[7].interfaceSnmpByIfIndex[42]", source.getResourceId());
            assertTrue(source.getAttribute().startsWith("ifHC"));
            assertTrue(source.getFallbackAttribute().startsWith("if"));
        }
        // Relaxed, or one uncollected interface fails the whole batch.
        assertTrue(captureRequest().isRelaxed());
    }

    @Test
    public void keepsDatabaseStateWhenTheQueryFails() throws Exception {
        provisioned(3, 4, 10_000_000L, 1, 1);
        when(measurements.query(any())).thenThrow(new MeasurementException("boom"));

        final List<SnmpInterfaceMetricsDTO> results = service.getMany(request(ref(1, 2), ref(3, 4))).getInterfaces();
        assertEquals(2, results.size());
        assertNull(results.get(0).getInBitsPerSecond());
        assertEquals(3, results.get(1).getNodeId().intValue());
        assertEquals(10_000_000L, results.get(1).getSpeedBps().longValue());
        assertEquals(1, results.get(1).getIfOperStatus().intValue());
        assertNull(results.get(1).getInUtilizationPercent());
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
        assertTrue(service.getMany(null).getInterfaces().isEmpty());
        assertTrue(service.getMany(new SnmpInterfaceMetricsRequestDTO()).getInterfaces().isEmpty());
        assertTrue(service.getMany(request(ref(null, 2), ref(1, null))).getInterfaces().isEmpty());
        verify(measurements, never()).query(any());
        verify(snmpInterfaceDao, never()).findByNodeIdAndIfIndex(anyInt(), anyInt());
    }

    @Test
    public void refusesMoreCountersThanItWillQuery() {
        final SnmpInterfaceRefDTO[] refs = new SnmpInterfaceRefDTO[501];
        for (int i = 0; i < refs.length; i++) {
            refs[i] = ref(1, i);
        }
        try {
            service.getMany(request(refs));   // all four groups: 6012 counters
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

        assertEquals(501, service.getMany(request).getInterfaces().size());
        assertEquals(1002, captureRequest().getSources().size());
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
        assertEquals(9_500d, dto.getInPacketsPerSecond(), 0.001d);
        // Only unicast collected outbound: the total is what was collected, the rest absent.
        assertEquals(1_100d, dto.getOutUnicastPacketsPerSecond(), 0.001d);
        assertNull(dto.getOutMulticastPacketsPerSecond());
        assertEquals(1_100d, dto.getOutPacketsPerSecond(), 0.001d);
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

        service.getMany(request);

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
    public void leavesThePacketTotalAbsentWhenNoClassWasCollected() throws Exception {
        final Map<String, double[]> columns = new LinkedHashMap<>();
        columns.put("i0pmin", new double[] { Double.NaN });
        when(measurements.query(any())).thenReturn(response(columns, Map.of()));

        final SnmpInterfaceMetricsDTO dto = first(request(ref(1, 2)));
        assertNull(dto.getInPacketsPerSecond());
        assertNull(dto.getOutPacketsPerSecond());
    }

    @Test
    public void asksForErrorsAndDiscardsWithNoHighCapacityFallback() throws Exception {
        when(measurements.query(any())).thenReturn(emptyResponse());
        final SnmpInterfaceMetricsRequestDTO request = request(ref(1, 2));
        request.setMetrics(List.of("errors", "discards"));

        service.getMany(request);

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
            service.getMany(request);
            fail("expected an unknown group to be refused");
        } catch (final WebApplicationException e) {
            assertEquals(400, e.getResponse().getStatus());
        }
    }

    @Test
    public void clampsTheWindowToSomethingTheDataSupports() throws Exception {
        when(measurements.query(any())).thenReturn(emptyResponse());
        final SnmpInterfaceMetricsRequestDTO request = request(ref(1, 2));
        request.setWindowSeconds(1);

        assertEquals(300, service.getMany(request).getWindowSeconds());
        final QueryRequest query = captureRequest();
        assertEquals(300_000L, query.getEnd() - query.getStart());
    }

    @Test
    public void answersOneInterfaceByPath() throws Exception {
        provisioned(5, 9, 1_000_000_000L, 1, 1);
        final Map<String, double[]> columns = new LinkedHashMap<>();
        columns.put("i0in", new double[] { 12_500_000d });
        when(measurements.query(any())).thenReturn(response(columns, Map.of()));

        final SnmpInterfaceMetricsResponseDTO response = service.getOne(5, 9, null, List.of("traffic"));
        assertEquals(900, response.getWindowSeconds());
        assertEquals(1, response.getInterfaces().size());
        final SnmpInterfaceMetricsDTO dto = response.getInterfaces().get(0);
        assertEquals(5, dto.getNodeId().intValue());
        assertEquals(9, dto.getIfIndex().intValue());
        assertEquals(10d, dto.getInUtilizationPercent(), 0.001d);
        assertEquals(2, captureRequest().getSources().size());
    }
}
