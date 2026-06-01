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
package org.opennms.netmgt.collectd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import com.vmware.vim25.DatastoreSummary;

/**
 * Unit tests for the {@link VmwareDatastoreCollector} extractor maps.
 *
 * These are the per-attribute extraction lambdas that convert a
 * {@link DatastoreSummary} into the numeric and string values the collector
 * persists. Edge cases worth pinning down:
 *
 * - {@code usedPct} when {@code capacity == 0} (avoid divide-by-zero).
 * - {@code uncommitted} when {@code summary.getUncommitted()} returns null
 *   (some datastores don't report it; we coerce to 0).
 * - {@code overcommittedBytes} when used + uncommitted does not exceed
 *   capacity (must floor at 0, never negative).
 * - {@code multipleHostAccess} when the boxed Boolean is null.
 * - String extractors when the underlying field is null (returned as null;
 *   the collect path is what coerces to ""). This test verifies the
 *   extractor itself doesn't fabricate a value.
 */
public class VmwareDatastoreCollectorExtractorTest {

    private static DatastoreSummary summaryOf(final long capacity,
                                              final long freeSpace,
                                              final Long uncommitted,
                                              final boolean accessible,
                                              final Boolean multipleHostAccess) {
        final DatastoreSummary s = mock(DatastoreSummary.class);
        when(s.getCapacity()).thenReturn(capacity);
        when(s.getFreeSpace()).thenReturn(freeSpace);
        when(s.getUncommitted()).thenReturn(uncommitted);
        when(s.isAccessible()).thenReturn(accessible);
        when(s.getMultipleHostAccess()).thenReturn(multipleHostAccess);
        return s;
    }

    @Test
    public void capacityAndFreeSpaceArePassedThrough() {
        final DatastoreSummary s = summaryOf(1000L, 250L, 0L, true, Boolean.FALSE);
        assertEquals(1000L, VmwareDatastoreCollector.NUMERIC_EXTRACTORS.get("capacity").applyAsLong(s));
        assertEquals(250L, VmwareDatastoreCollector.NUMERIC_EXTRACTORS.get("freeSpace").applyAsLong(s));
    }

    @Test
    public void usedIsCapacityMinusFreeSpace() {
        final DatastoreSummary s = summaryOf(1000L, 250L, 0L, true, Boolean.FALSE);
        assertEquals(750L, VmwareDatastoreCollector.NUMERIC_EXTRACTORS.get("used").applyAsLong(s));
    }

    @Test
    public void usedPctIsIntegerPercent() {
        final DatastoreSummary s = summaryOf(1000L, 250L, 0L, true, Boolean.FALSE);
        assertEquals(75L, VmwareDatastoreCollector.NUMERIC_EXTRACTORS.get("usedPct").applyAsLong(s));
    }

    @Test
    public void usedPctHandlesZeroCapacity() {
        // Empty/uninitialised datastore — avoid divide-by-zero.
        final DatastoreSummary s = summaryOf(0L, 0L, 0L, false, Boolean.FALSE);
        assertEquals(0L, VmwareDatastoreCollector.NUMERIC_EXTRACTORS.get("usedPct").applyAsLong(s));
    }

    @Test
    public void uncommittedHandlesNull() {
        // Some datastores (we saw labhostImages in our testbed run) report null.
        final DatastoreSummary s = summaryOf(1000L, 500L, null, true, Boolean.FALSE);
        assertEquals(0L, VmwareDatastoreCollector.NUMERIC_EXTRACTORS.get("uncommitted").applyAsLong(s));
    }

    @Test
    public void overcommittedBytesFloorsAtZero() {
        // used (500) + uncommitted (200) <= capacity (1000) -> not overcommitted.
        final DatastoreSummary s = summaryOf(1000L, 500L, 200L, true, Boolean.FALSE);
        assertEquals(0L, VmwareDatastoreCollector.NUMERIC_EXTRACTORS.get("overcommittedBytes").applyAsLong(s));
    }

    @Test
    public void overcommittedBytesReportsOverage() {
        // used (500) + uncommitted (700) = 1200, capacity 1000 -> 200 bytes overcommitted.
        final DatastoreSummary s = summaryOf(1000L, 500L, 700L, true, Boolean.FALSE);
        assertEquals(200L, VmwareDatastoreCollector.NUMERIC_EXTRACTORS.get("overcommittedBytes").applyAsLong(s));
    }

    @Test
    public void overcommittedBytesHandlesNullUncommitted() {
        final DatastoreSummary s = summaryOf(1000L, 500L, null, true, Boolean.FALSE);
        assertEquals(0L, VmwareDatastoreCollector.NUMERIC_EXTRACTORS.get("overcommittedBytes").applyAsLong(s));
    }

    @Test
    public void accessibleEncodesAsOneAndZero() {
        assertEquals(1L, VmwareDatastoreCollector.NUMERIC_EXTRACTORS.get("accessible").applyAsLong(
                summaryOf(1L, 0L, 0L, true, Boolean.FALSE)));
        assertEquals(0L, VmwareDatastoreCollector.NUMERIC_EXTRACTORS.get("accessible").applyAsLong(
                summaryOf(1L, 0L, 0L, false, Boolean.FALSE)));
    }

    @Test
    public void multipleHostAccessEncodesAsOneAndZero() {
        assertEquals(1L, VmwareDatastoreCollector.NUMERIC_EXTRACTORS.get("multipleHostAccess").applyAsLong(
                summaryOf(1L, 0L, 0L, true, Boolean.TRUE)));
        assertEquals(0L, VmwareDatastoreCollector.NUMERIC_EXTRACTORS.get("multipleHostAccess").applyAsLong(
                summaryOf(1L, 0L, 0L, true, Boolean.FALSE)));
    }

    @Test
    public void multipleHostAccessHandlesNull() {
        // Older / partially-populated DatastoreSummary instances may return null
        // from getMultipleHostAccess(); coerce to 0 rather than NPE.
        assertEquals(0L, VmwareDatastoreCollector.NUMERIC_EXTRACTORS.get("multipleHostAccess").applyAsLong(
                summaryOf(1L, 0L, 0L, true, null)));
    }

    @Test
    public void stringExtractorsPassNullThrough() {
        // The extractor itself returns null when the field is null.
        // (The collect() path is what coerces null to "".)
        final DatastoreSummary s = mock(DatastoreSummary.class);
        when(s.getName()).thenReturn(null);
        when(s.getType()).thenReturn(null);
        when(s.getUrl()).thenReturn(null);

        assertNull(VmwareDatastoreCollector.STRING_EXTRACTORS.get("name").apply(s));
        assertNull(VmwareDatastoreCollector.STRING_EXTRACTORS.get("type").apply(s));
        assertNull(VmwareDatastoreCollector.STRING_EXTRACTORS.get("url").apply(s));
    }

    @Test
    public void stringExtractorsReturnPopulatedValues() {
        final DatastoreSummary s = mock(DatastoreSummary.class);
        when(s.getName()).thenReturn("ArkhamHost1Datastore");
        when(s.getType()).thenReturn("VMFS");
        when(s.getUrl()).thenReturn("ds:///vmfs/volumes/abc/");

        assertEquals("ArkhamHost1Datastore", VmwareDatastoreCollector.STRING_EXTRACTORS.get("name").apply(s));
        assertEquals("VMFS", VmwareDatastoreCollector.STRING_EXTRACTORS.get("type").apply(s));
        assertEquals("ds:///vmfs/volumes/abc/", VmwareDatastoreCollector.STRING_EXTRACTORS.get("url").apply(s));
    }

    @Test
    public void unknownAttributeNamesAreAbsentFromBothMaps() {
        // Guards against accidental key drift between this map and the
        // configured <attrib name="..."> entries in vmware-datacollection-config.xml.
        assertTrue(VmwareDatastoreCollector.NUMERIC_EXTRACTORS.containsKey("capacity"));
        assertTrue(VmwareDatastoreCollector.NUMERIC_EXTRACTORS.containsKey("freeSpace"));
        assertTrue(VmwareDatastoreCollector.NUMERIC_EXTRACTORS.containsKey("used"));
        assertTrue(VmwareDatastoreCollector.NUMERIC_EXTRACTORS.containsKey("usedPct"));
        assertTrue(VmwareDatastoreCollector.NUMERIC_EXTRACTORS.containsKey("uncommitted"));
        assertTrue(VmwareDatastoreCollector.NUMERIC_EXTRACTORS.containsKey("overcommittedBytes"));
        assertTrue(VmwareDatastoreCollector.NUMERIC_EXTRACTORS.containsKey("accessible"));
        assertTrue(VmwareDatastoreCollector.NUMERIC_EXTRACTORS.containsKey("multipleHostAccess"));
        assertTrue(VmwareDatastoreCollector.STRING_EXTRACTORS.containsKey("name"));
        assertTrue(VmwareDatastoreCollector.STRING_EXTRACTORS.containsKey("type"));
        assertTrue(VmwareDatastoreCollector.STRING_EXTRACTORS.containsKey("url"));
    }
}
