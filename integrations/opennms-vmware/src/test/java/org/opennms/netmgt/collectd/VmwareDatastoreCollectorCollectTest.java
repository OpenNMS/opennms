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

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.CollectionStatus;
import org.opennms.netmgt.collection.api.ResourceTypeMapper;
import org.opennms.netmgt.collection.support.AbstractCollectionSetVisitor;
import org.opennms.netmgt.collection.support.IndexStorageStrategy;
import org.opennms.netmgt.collection.support.PersistAllSelectorStrategy;
import org.opennms.netmgt.config.datacollection.PersistenceSelectorStrategy;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.config.datacollection.StorageStrategy;
import org.opennms.netmgt.config.vmware.VmwareServer;
import org.opennms.netmgt.config.vmware.vijava.Attrib;
import org.opennms.netmgt.config.vmware.vijava.VmwareCollection;
import org.opennms.netmgt.config.vmware.vijava.VmwareGroup;
import org.opennms.netmgt.provision.service.vmware.VmwareImporter;
import org.opennms.protocols.vmware.VmwareViJavaAccess;

import com.vmware.vim25.DatastoreSummary;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.mo.Datastore;
import com.vmware.vim25.mo.ManagedEntity;

/**
 * Exercises {@link VmwareDatastoreCollector#collect(CollectionAgent, Map)} end-to-end
 * against a mocked {@link VmwareViJavaAccess}. Datastore sizes mirror the test
 * vCenter (vcsa0.dagonships.com): one small NFS volume, one mid VMFS volume,
 * one large VMFS volume modeled on {@code ArkhamHost1Datastore} (4 TB capacity,
 * ~27.7% used).
 */
public class VmwareDatastoreCollectorCollectTest {

    private VmwareViJavaAccess mockAccess;
    private VmwareDatastoreCollector collector;
    private CollectionAgent agent;
    private VmwareServer server;
    private VmwareCollection collection;

    @Before
    public void setUp() {
        // DeferredGenericTypeResource resolves resource type definitions via this
        // static singleton during visit; in production it is wired by Spring.
        ResourceTypeMapper.getInstance().setResourceTypeMapper(VmwareDatastoreCollectorCollectTest::resourceTypeFor);

        mockAccess = mock(VmwareViJavaAccess.class);
        collector = new VmwareDatastoreCollector() {
            @Override
            protected VmwareViJavaAccess createVmwareViJavaAccess(final VmwareServer ignored) {
                return mockAccess;
            }
        };

        agent = mock(CollectionAgent.class);
        when(agent.getNodeId()).thenReturn(42);

        server = new VmwareServer();
        server.setHostname("vcsa0.example.com");
        server.setUsername("user");
        server.setPassword("pass");

        collection = buildShippedCollection();
    }

    private static ResourceType resourceTypeFor(final String name) {
        final ResourceType rt = new ResourceType();
        rt.setName(name);
        final StorageStrategy ss = new StorageStrategy();
        ss.setClazz(IndexStorageStrategy.class.getName());
        rt.setStorageStrategy(ss);
        final PersistenceSelectorStrategy pss = new PersistenceSelectorStrategy();
        pss.setClazz(PersistAllSelectorStrategy.class.getName());
        rt.setPersistenceSelectorStrategy(pss);
        return rt;
    }

    @Test
    public void emptyDatastoreArrayProducesSucceededEmptyCollectionSet() throws Exception {
        when(mockAccess.searchManagedEntities("Datastore")).thenReturn(new ManagedEntity[0]);

        final CollectionSet result = collector.collect(agent, buildParameters());

        assertEquals(CollectionStatus.SUCCEEDED, result.getStatus());
        assertEquals(0, collect(result).resources.size());
    }

    @Test
    public void searchManagedEntitiesThrowsProducesFailed() throws Exception {
        when(mockAccess.searchManagedEntities("Datastore")).thenThrow(new RemoteException("boom"));

        final CollectionSet result = collector.collect(agent, buildParameters());

        assertEquals(CollectionStatus.FAILED, result.getStatus());
    }

    @Test
    public void happyPathCollectsAllDatastoresWithExpectedAttributes() throws Exception {
        // Lab-inspired sizes.
        final Datastore small = mockDatastore("datastore-1003", "iso-library",
                250_000_000_000L, 50_000_000_000L, null, "NFS", "nfs://nas/iso/", true, Boolean.TRUE);
        final Datastore medium = mockDatastore("datastore-1002", "production-vol",
                2_198_000_000_000L, 1_100_000_000_000L, 200_000_000_000L, "VMFS",
                "ds:///vmfs/volumes/def/", true, Boolean.TRUE);
        // ArkhamHost1Datastore: 4TB / 2.891TB free → 1.109TB used → 27% (integer truncation).
        final Datastore large = mockDatastore("datastore-1001", "ArkhamHost1Datastore",
                4_000_000_000_000L, 2_891_000_000_000L, 0L, "VMFS",
                "ds:///vmfs/volumes/abc/", true, Boolean.FALSE);

        when(mockAccess.searchManagedEntities("Datastore"))
                .thenReturn(new ManagedEntity[]{small, medium, large});

        final CollectionSet result = collector.collect(agent, buildParameters());

        assertEquals(CollectionStatus.SUCCEEDED, result.getStatus());
        final Captured captured = collect(result);
        assertEquals(3, captured.resources.size());
        assertTrue(captured.resources.contains("datastore-1001"));
        assertTrue(captured.resources.contains("datastore-1002"));
        assertTrue(captured.resources.contains("datastore-1003"));

        // Spot-check the large datastore (ArkhamHost1Datastore-shaped).
        assertEquals(4_000_000_000_000L, captured.numericFor("datastore-1001", "DsCapacity"));
        assertEquals(2_891_000_000_000L, captured.numericFor("datastore-1001", "DsFreeSpace"));
        assertEquals(1_109_000_000_000L, captured.numericFor("datastore-1001", "DsUsed"));
        assertEquals(27L, captured.numericFor("datastore-1001", "DsUsedPct"));
        assertEquals(0L, captured.numericFor("datastore-1001", "DsUncommitted"));
        assertEquals(0L, captured.numericFor("datastore-1001", "DsOvercommitted"));
        assertEquals(1L, captured.numericFor("datastore-1001", "DsAccessible"));
        assertEquals(0L, captured.numericFor("datastore-1001", "DsMultiHost"));
        assertEquals("VMFS", captured.stringFor("datastore-1001", "DsType"));
        assertEquals("ds:///vmfs/volumes/abc/", captured.stringFor("datastore-1001", "DsUrl"));
        assertEquals("ArkhamHost1Datastore",
                captured.stringFor("datastore-1001", "vmwareDatastoreCapacityName"));

        // Medium datastore: uncommitted is non-null and over-commit applies.
        // used = 2.198T - 1.100T = 1.098T; used + unc = 1.098T + 0.200T = 1.298T; cap = 2.198T
        // overcommit = max(0, 1.298T - 2.198T) = 0 (well under capacity)
        assertEquals(200_000_000_000L, captured.numericFor("datastore-1002", "DsUncommitted"));
        assertEquals(0L, captured.numericFor("datastore-1002", "DsOvercommitted"));
        assertEquals(1L, captured.numericFor("datastore-1002", "DsMultiHost"));

        // Small datastore: null uncommitted handled, capacity 250G / free 50G → 80% used.
        assertEquals(0L, captured.numericFor("datastore-1003", "DsUncommitted"));
        assertEquals(80L, captured.numericFor("datastore-1003", "DsUsedPct"));
        assertEquals("NFS", captured.stringFor("datastore-1003", "DsType"));
    }

    @Test
    public void perDatastoreSummaryFailureIsIsolated() throws Exception {
        final Datastore good = mockDatastore("datastore-2001", "good",
                1_000_000L, 250_000L, 0L, "VMFS", "ds:///good/", true, Boolean.FALSE);
        final Datastore bad = mockDatastoreWithSummaryThrow("datastore-2002", "bad");
        final Datastore alsoGood = mockDatastore("datastore-2003", "also-good",
                500_000L, 100_000L, 0L, "VMFS", "ds:///also/", true, Boolean.FALSE);

        when(mockAccess.searchManagedEntities("Datastore"))
                .thenReturn(new ManagedEntity[]{good, bad, alsoGood});

        final CollectionSet result = collector.collect(agent, buildParameters());

        assertEquals(CollectionStatus.SUCCEEDED, result.getStatus());
        final Captured captured = collect(result);
        assertEquals(2, captured.resources.size());
        assertTrue(captured.resources.contains("datastore-2001"));
        assertTrue(captured.resources.contains("datastore-2003"));
        assertNull(captured.numerics.get("datastore-2002"));
    }

    @Test
    public void noUsableGroupsShortCircuitsSuccessfully() throws Exception {
        final VmwareCollection emptyCollection = new VmwareCollection();
        emptyCollection.setVmwareGroup(new VmwareGroup[0]);
        final Map<String, Object> params = buildParameters();
        params.put(VmwareImporter.VMWARE_COLLECTION_KEY, emptyCollection);

        final CollectionSet result = collector.collect(agent, params);

        assertEquals(CollectionStatus.SUCCEEDED, result.getStatus());
        assertEquals(0, collect(result).resources.size());
    }

    // -------- helpers --------

    private Map<String, Object> buildParameters() {
        final Map<String, Object> p = new HashMap<>();
        p.put(VmwareImporter.VMWARE_COLLECTION_KEY, collection);
        p.put(VmwareImporter.METADATA_MANAGEMENT_SERVER, "vcsa0.example.com");
        p.put(VmwareImporter.VMWARE_SERVER_KEY, server);
        return p;
    }

    private VmwareCollection buildShippedCollection() {
        final VmwareCollection c = new VmwareCollection();
        final VmwareGroup g = new VmwareGroup();
        g.setName("vmwareDatastoreCapacity");
        g.setResourceType("vmwareDatastoreCapacity");
        g.setAttrib(new Attrib[]{
                attrib("capacity", "DsCapacity", AttributeType.GAUGE),
                attrib("freeSpace", "DsFreeSpace", AttributeType.GAUGE),
                attrib("used", "DsUsed", AttributeType.GAUGE),
                attrib("usedPct", "DsUsedPct", AttributeType.GAUGE),
                attrib("uncommitted", "DsUncommitted", AttributeType.GAUGE),
                attrib("overcommittedBytes", "DsOvercommitted", AttributeType.GAUGE),
                attrib("accessible", "DsAccessible", AttributeType.GAUGE),
                attrib("multipleHostAccess", "DsMultiHost", AttributeType.GAUGE),
                attrib("type", "DsType", AttributeType.STRING),
                attrib("url", "DsUrl", AttributeType.STRING),
        });
        c.setVmwareGroup(new VmwareGroup[]{g});
        return c;
    }

    private static Attrib attrib(final String name, final String alias, final AttributeType type) {
        final Attrib a = new Attrib();
        a.setName(name);
        a.setAlias(alias);
        a.setType(type);
        return a;
    }

    private static Datastore mockDatastore(final String moid, final String name,
                                            final long capacity, final long freeSpace,
                                            final Long uncommitted, final String type,
                                            final String url, final boolean accessible,
                                            final Boolean multipleHostAccess) {
        final Datastore ds = mock(Datastore.class);
        final ManagedObjectReference mor = mock(ManagedObjectReference.class);
        when(mor.getVal()).thenReturn(moid);
        when(ds.getMOR()).thenReturn(mor);
        when(ds.getName()).thenReturn(name);

        final DatastoreSummary summary = mock(DatastoreSummary.class);
        when(summary.getCapacity()).thenReturn(capacity);
        when(summary.getFreeSpace()).thenReturn(freeSpace);
        when(summary.getUncommitted()).thenReturn(uncommitted);
        when(summary.getName()).thenReturn(name);
        when(summary.getType()).thenReturn(type);
        when(summary.getUrl()).thenReturn(url);
        when(summary.isAccessible()).thenReturn(accessible);
        when(summary.getMultipleHostAccess()).thenReturn(multipleHostAccess);
        when(ds.getSummary()).thenReturn(summary);
        return ds;
    }

    private static Datastore mockDatastoreWithSummaryThrow(final String moid, final String name) {
        final Datastore ds = mock(Datastore.class);
        final ManagedObjectReference mor = mock(ManagedObjectReference.class);
        when(mor.getVal()).thenReturn(moid);
        when(ds.getMOR()).thenReturn(mor);
        when(ds.getName()).thenReturn(name);
        when(ds.getSummary()).thenThrow(new RuntimeException("summary unavailable"));
        return ds;
    }

    private static Captured collect(final CollectionSet set) {
        final Captured c = new Captured();
        set.visit(new AbstractCollectionSetVisitor() {
            private String current;

            @Override
            public void visitResource(final CollectionResource resource) {
                current = resource.getInstance();
                if (current != null) {
                    c.resources.add(current);
                    c.numerics.computeIfAbsent(current, k -> new HashMap<>());
                    c.strings.computeIfAbsent(current, k -> new HashMap<>());
                }
            }

            @Override
            public void visitAttribute(final CollectionAttribute attribute) {
                if (current == null) {
                    return;
                }
                if (attribute.getType() != null && attribute.getType().isNumeric()) {
                    c.numerics.get(current).put(attribute.getName(),
                            attribute.getNumericValue() == null ? null : attribute.getNumericValue().longValue());
                } else {
                    c.strings.get(current).put(attribute.getName(), attribute.getStringValue());
                }
            }
        });
        return c;
    }

    private static class Captured {
        final Set<String> resources = new HashSet<>();
        final Map<String, Map<String, Long>> numerics = new HashMap<>();
        final Map<String, Map<String, String>> strings = new HashMap<>();

        long numericFor(final String resource, final String attr) {
            return numerics.get(resource).get(attr);
        }

        String stringFor(final String resource, final String attr) {
            return strings.get(resource).get(attr);
        }
    }
}
