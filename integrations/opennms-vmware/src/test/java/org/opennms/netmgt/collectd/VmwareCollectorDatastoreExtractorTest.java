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
 * Unit tests for {@link VmwareCollector}'s datastore extractor maps.
 *
 * These are the per-attribute extraction lambdas that convert a
 * {@link DatastoreSummary} into the numeric and string values the collector
 * persists when a {@code <vmware-group>} has
 * {@code resourceType="vmwareDatastoreCapacity"}. Edge cases worth pinning:
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
public class VmwareCollectorDatastoreExtractorTest {

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
        assertEquals(1000L, VmwareCollector.DATASTORE_NUMERIC_EXTRACTORS.get("capacity").applyAsLong(s));
        assertEquals(250L, VmwareCollector.DATASTORE_NUMERIC_EXTRACTORS.get("freeSpace").applyAsLong(s));
    }

    @Test
    public void usedIsCapacityMinusFreeSpace() {
        final DatastoreSummary s = summaryOf(1000L, 250L, 0L, true, Boolean.FALSE);
        assertEquals(750L, VmwareCollector.DATASTORE_NUMERIC_EXTRACTORS.get("used").applyAsLong(s));
    }

    @Test
    public void usedPctIsIntegerPercent() {
        final DatastoreSummary s = summaryOf(1000L, 250L, 0L, true, Boolean.FALSE);
        assertEquals(75L, VmwareCollector.DATASTORE_NUMERIC_EXTRACTORS.get("usedPct").applyAsLong(s));
    }

    @Test
    public void usedPctHandlesZeroCapacity() {
        // Empty/uninitialised datastore — avoid divide-by-zero.
        final DatastoreSummary s = summaryOf(0L, 0L, 0L, false, Boolean.FALSE);
        assertEquals(0L, VmwareCollector.DATASTORE_NUMERIC_EXTRACTORS.get("usedPct").applyAsLong(s));
    }

    @Test
    public void uncommittedNonNull() {
        final DatastoreSummary s = summaryOf(1000L, 250L, 100L, true, Boolean.FALSE);
        assertEquals(100L, VmwareCollector.DATASTORE_NUMERIC_EXTRACTORS.get("uncommitted").applyAsLong(s));
    }

    @Test
    public void uncommittedNullCoercesToZero() {
        final DatastoreSummary s = summaryOf(1000L, 250L, null, true, Boolean.FALSE);
        assertEquals(0L, VmwareCollector.DATASTORE_NUMERIC_EXTRACTORS.get("uncommitted").applyAsLong(s));
    }

    @Test
    public void overcommittedFloorsAtZeroWhenUsedPlusUncommittedFitsCapacity() {
        // capacity 1000, free 500 → used 500, uncommitted 200 → used+unc=700, < 1000.
        final DatastoreSummary s = summaryOf(1000L, 500L, 200L, true, Boolean.FALSE);
        assertEquals(0L, VmwareCollector.DATASTORE_NUMERIC_EXTRACTORS.get("overcommittedBytes").applyAsLong(s));
    }

    @Test
    public void overcommittedReportsExcessWhenUsedPlusUncommittedExceedsCapacity() {
        // capacity 1000, free 100 → used 900, uncommitted 300 → used+unc=1200, excess 200.
        final DatastoreSummary s = summaryOf(1000L, 100L, 300L, true, Boolean.FALSE);
        assertEquals(200L, VmwareCollector.DATASTORE_NUMERIC_EXTRACTORS.get("overcommittedBytes").applyAsLong(s));
    }

    @Test
    public void overcommittedNullUncommittedFloorsAtUsedMinusCapacity() {
        // uncommitted null → 0 → used 900, capacity 1000 → max(0, 900 - 1000) = 0
        final DatastoreSummary s = summaryOf(1000L, 100L, null, true, Boolean.FALSE);
        assertEquals(0L, VmwareCollector.DATASTORE_NUMERIC_EXTRACTORS.get("overcommittedBytes").applyAsLong(s));
    }

    @Test
    public void accessibleBooleanMapsToOneOrZero() {
        assertEquals(1L, VmwareCollector.DATASTORE_NUMERIC_EXTRACTORS.get("accessible")
                .applyAsLong(summaryOf(1L, 1L, 0L, true, Boolean.FALSE)));
        assertEquals(0L, VmwareCollector.DATASTORE_NUMERIC_EXTRACTORS.get("accessible")
                .applyAsLong(summaryOf(1L, 1L, 0L, false, Boolean.FALSE)));
    }

    @Test
    public void multipleHostAccessBooleanMapsToOneOrZero() {
        assertEquals(1L, VmwareCollector.DATASTORE_NUMERIC_EXTRACTORS.get("multipleHostAccess")
                .applyAsLong(summaryOf(1L, 1L, 0L, true, Boolean.TRUE)));
        assertEquals(0L, VmwareCollector.DATASTORE_NUMERIC_EXTRACTORS.get("multipleHostAccess")
                .applyAsLong(summaryOf(1L, 1L, 0L, true, Boolean.FALSE)));
    }

    @Test
    public void multipleHostAccessNullCoercesToZero() {
        assertEquals(0L, VmwareCollector.DATASTORE_NUMERIC_EXTRACTORS.get("multipleHostAccess")
                .applyAsLong(summaryOf(1L, 1L, 0L, true, null)));
    }

    @Test
    public void stringExtractorsPassThroughNonNull() {
        final DatastoreSummary s = mock(DatastoreSummary.class);
        when(s.getName()).thenReturn("ArkhamHost1Datastore");
        when(s.getType()).thenReturn("VMFS");
        when(s.getUrl()).thenReturn("ds:///vmfs/volumes/abc/");
        assertEquals("ArkhamHost1Datastore", VmwareCollector.DATASTORE_STRING_EXTRACTORS.get("name").apply(s));
        assertEquals("VMFS", VmwareCollector.DATASTORE_STRING_EXTRACTORS.get("type").apply(s));
        assertEquals("ds:///vmfs/volumes/abc/", VmwareCollector.DATASTORE_STRING_EXTRACTORS.get("url").apply(s));
    }

    @Test
    public void stringExtractorsReturnNullWithoutFabricating() {
        final DatastoreSummary s = mock(DatastoreSummary.class);
        // Mockito returns null by default for non-stubbed methods on object-returning calls.
        assertNull(VmwareCollector.DATASTORE_STRING_EXTRACTORS.get("name").apply(s));
        assertNull(VmwareCollector.DATASTORE_STRING_EXTRACTORS.get("type").apply(s));
        assertNull(VmwareCollector.DATASTORE_STRING_EXTRACTORS.get("url").apply(s));
    }

    @Test
    public void numericExtractorMapContainsAllExpectedKeys() {
        assertTrue(VmwareCollector.DATASTORE_NUMERIC_EXTRACTORS.containsKey("capacity"));
        assertTrue(VmwareCollector.DATASTORE_NUMERIC_EXTRACTORS.containsKey("freeSpace"));
        assertTrue(VmwareCollector.DATASTORE_NUMERIC_EXTRACTORS.containsKey("used"));
        assertTrue(VmwareCollector.DATASTORE_NUMERIC_EXTRACTORS.containsKey("usedPct"));
        assertTrue(VmwareCollector.DATASTORE_NUMERIC_EXTRACTORS.containsKey("uncommitted"));
        assertTrue(VmwareCollector.DATASTORE_NUMERIC_EXTRACTORS.containsKey("overcommittedBytes"));
        assertTrue(VmwareCollector.DATASTORE_NUMERIC_EXTRACTORS.containsKey("accessible"));
        assertTrue(VmwareCollector.DATASTORE_NUMERIC_EXTRACTORS.containsKey("multipleHostAccess"));
        assertEquals(8, VmwareCollector.DATASTORE_NUMERIC_EXTRACTORS.size());
    }

    @Test
    public void stringExtractorMapContainsAllExpectedKeys() {
        assertTrue(VmwareCollector.DATASTORE_STRING_EXTRACTORS.containsKey("name"));
        assertTrue(VmwareCollector.DATASTORE_STRING_EXTRACTORS.containsKey("type"));
        assertTrue(VmwareCollector.DATASTORE_STRING_EXTRACTORS.containsKey("url"));
        assertEquals(3, VmwareCollector.DATASTORE_STRING_EXTRACTORS.size());
    }
}
