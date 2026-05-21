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

// NOTE: copy of opennms-dao/src/test/java/org/opennms/netmgt/dao/support/DatacollectionConfigEquivalence.java.
// Duplicated rather than test-jar'd so this PR doesn't touch every consumer of opennms-dao test classes.
// Keep in sync with the canonical version; future cleanup: extract to a shared test-utils module.

import org.opennms.netmgt.collection.api.Parameter;
import org.opennms.netmgt.config.api.DataCollectionConfigDao;
import org.opennms.netmgt.config.datacollection.DatacollectionConfig;
import org.opennms.netmgt.config.datacollection.Group;
import org.opennms.netmgt.config.datacollection.IpList;
import org.opennms.netmgt.config.datacollection.MibObj;
import org.opennms.netmgt.config.datacollection.MibObject;
import org.opennms.netmgt.config.datacollection.MibObjProperty;
import org.opennms.netmgt.config.datacollection.PersistenceSelectorStrategy;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.config.datacollection.Rrd;
import org.opennms.netmgt.config.datacollection.SnmpCollection;
import org.opennms.netmgt.config.datacollection.StorageStrategy;
import org.opennms.netmgt.config.datacollection.SystemDef;

import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Asserts merged-state equivalence between two {@link DatacollectionConfig}
 * instances — typically the one produced by the legacy XML loader and the one
 * produced by the DB-backed loader after running the migration.
 *
 * <p>Equivalence is intentionally <i>merged-state</i>: we compare what each
 * {@code <snmp-collection>} actually exposes to collectd (rrd metadata,
 * storage flag, max-vars, plus the merged group / systemDef / resource-type
 * sets) rather than how the data was assembled. The DB path can route
 * exclude-filter and {@code systemDef=} content through a synthetic
 * {@code __inline_<collection>} source whose name doesn't appear in the XML
 * path's {@code source_names}; once merged into the collection's
 * {@code <groups>} / {@code <systems>} the runtime view is identical.
 *
 * <p>The synthetic {@code __resource_type_collection} created by both paths
 * is compared at the resource-type-set level only — XML doesn't set
 * {@code snmpStorageFlag} / RRD step on it, the DB path does, and that's
 * cosmetic.
 *
 * <p>On any mismatch the assertion throws an {@link AssertionError} listing
 * every difference found, so a CI failure points directly at the divergent
 * field instead of just saying "not equal".
 */
public final class DatacollectionConfigEquivalence {

    private static final String RESOURCE_TYPE_COLLECTION_NAME = "__resource_type_collection";

    private DatacollectionConfigEquivalence() {
    }

    /**
     * Assert parity at the public DAO API surface — the methods collectd
     * actually calls during a collection cycle. Catches divergences that the
     * tree-shape comparator would miss because the API methods compute
     * derived views (sysoid matching, ifType filtering, resource-type map
     * lookups).
     *
     * <p>For each user snmp-collection on either side, this checks:
     * <ul>
     *   <li>{@code getSnmpStorageFlag}, {@code getStep}, {@code getRRAList} —
     *       scalar collection-level fields collectd reads at startup.</li>
     *   <li>{@code getMibObjectList(name, sysoid, address, ifType)} for the
     *       cross product of every sysoid declared in either side's
     *       systemDefs and a representative set of ifTypes (-1
     *       NODE_ATTRIBUTES, -2 ALL_IF_ATTRIBUTES, 6 ethernetCsmacd, 24
     *       softwareLoopback). Compared as multisets keyed by alias+oid.</li>
     *   <li>{@code getMibObjProperties} for the same (sysoid, address)
     *       inputs.</li>
     * </ul>
     *
     * <p>{@code getConfiguredResourceTypes()} is compared at the keyset
     * level. The DB side may contain a strict superset (the migration
     * imports every source's resource types; the XML loader only registers
     * those reachable through {@code <include-collection>}). Surplus on the
     * DB side is logged as a tolerance, not a failure — collectd looks up
     * by name, so unused entries are inert.
     */
    public static void assertDaoApiEquivalent(final DataCollectionConfigDao xml,
                                              final DataCollectionConfigDao db) {
        if (xml == null || db == null) {
            throw new AssertionError("DAO API parity check requires both DAOs (xml=" + xml + ", db=" + db + ")");
        }
        final List<String> diffs = new ArrayList<>();
        final Set<String> collectionNames = new TreeSet<>();
        for (final SnmpCollection sc : xml.getRootDataCollection().getSnmpCollections()) {
            if (sc != null && sc.getName() != null
                    && !RESOURCE_TYPE_COLLECTION_NAME.equals(sc.getName())) {
                collectionNames.add(sc.getName());
            }
        }
        for (final SnmpCollection sc : db.getRootDataCollection().getSnmpCollections()) {
            if (sc != null && sc.getName() != null
                    && !RESOURCE_TYPE_COLLECTION_NAME.equals(sc.getName())) {
                collectionNames.add(sc.getName());
            }
        }

        for (final String name : collectionNames) {
            compareDaoApiForCollection(name, xml, db, diffs);
        }
        compareConfiguredResourceTypes(xml, db, diffs);

        if (!diffs.isEmpty()) {
            throw new AssertionError("DataCollectionConfigDao API parity mismatch:\n  - "
                    + String.join("\n  - ", diffs));
        }
    }

    private static final int[] IF_TYPES_TO_PROBE = {
            DataCollectionConfigDao.NODE_ATTRIBUTES,
            DataCollectionConfigDao.ALL_IF_ATTRIBUTES,
            6,   // ethernetCsmacd — the dominant interface type in production
            24,  // softwareLoopback — exercises a different ifType filter path
    };

    private static void compareDaoApiForCollection(final String name,
                                                   final DataCollectionConfigDao xml,
                                                   final DataCollectionConfigDao db,
                                                   final List<String> diffs) {
        final String at = "dao[" + name + "]";
        compareScalar(at + ".getSnmpStorageFlag",
                xml.getSnmpStorageFlag(name), db.getSnmpStorageFlag(name), diffs);
        compareScalar(at + ".getStep",
                xml.getStep(name), db.getStep(name), diffs);
        // getRRAList: order is meaningful — RRD archive order is positional.
        final List<String> xRras = xml.getRRAList(name);
        final List<String> dRras = db.getRRAList(name);
        if (!Objects.equals(xRras, dRras)) {
            diffs.add(at + ".getRRAList differ: xml=" + xRras + " db=" + dRras);
        }

        final Set<String> sysoids = collectSysoids(xml.getRootDataCollection(), name);
        sysoids.addAll(collectSysoids(db.getRootDataCollection(), name));
        // Probe with an arbitrary address — addresses are filtered against
        // each systemDef's ipList. Most production systemDefs have no ipList
        // (matched by sysoid alone); using a non-routable address gives the
        // representative "no ip-list match" answer for the ones that do.
        final String probeAddress = "127.0.0.1";

        for (final String sysoid : sysoids) {
            for (final int ifType : IF_TYPES_TO_PROBE) {
                final String ctx = at + ".getMibObjectList(sysoid=" + sysoid + ", ifType=" + ifType + ")";
                compareMibObjectMultiset(ctx,
                        xml.getMibObjectList(name, sysoid, probeAddress, ifType),
                        db.getMibObjectList(name, sysoid, probeAddress, ifType),
                        diffs);
            }
            final String ctx = at + ".getMibObjProperties(sysoid=" + sysoid + ")";
            comparePropertyMultiset(ctx,
                    xml.getMibObjProperties(name, sysoid, probeAddress),
                    db.getMibObjProperties(name, sysoid, probeAddress),
                    diffs);
        }
    }

    private static Set<String> collectSysoids(final DatacollectionConfig config, final String collectionName) {
        final Set<String> out = new TreeSet<>();
        if (config == null || config.getSnmpCollections() == null) return out;
        for (final SnmpCollection sc : config.getSnmpCollections()) {
            if (sc == null || !collectionName.equals(sc.getName())) continue;
            for (final SystemDef sd : mergedSystemDefs(sc)) {
                if (sd == null) continue;
                if (sd.getSysoid() != null && !sd.getSysoid().isBlank()) {
                    out.add(sd.getSysoid());
                } else if (sd.getSysoidMask() != null && !sd.getSysoidMask().isBlank()) {
                    // Synthesize a probe sysoid that matches the mask. The
                    // mask is a prefix; appending ".0" gives a value that
                    // satisfies systemDefMatches.
                    out.add(sd.getSysoidMask() + "0");
                }
            }
        }
        return out;
    }

    private static void compareMibObjectMultiset(final String at,
                                                 final List<MibObject> xml,
                                                 final List<MibObject> db,
                                                 final List<String> diffs) {
        // MibObject identity is (alias, oid, type, instance) — collectd
        // dedupes on alias+oid downstream, so we treat the lists as
        // multisets keyed on that.
        final Map<String, Integer> xCounts = new TreeMap<>();
        for (final MibObject mo : xml) {
            xCounts.merge(mo.getAlias() + "|" + mo.getOid(), 1, Integer::sum);
        }
        final Map<String, Integer> dCounts = new TreeMap<>();
        for (final MibObject mo : db) {
            dCounts.merge(mo.getAlias() + "|" + mo.getOid(), 1, Integer::sum);
        }
        if (!xCounts.equals(dCounts)) {
            final TreeSet<String> only = new TreeSet<>(xCounts.keySet());
            only.removeAll(dCounts.keySet());
            final TreeSet<String> dbOnly = new TreeSet<>(dCounts.keySet());
            dbOnly.removeAll(xCounts.keySet());
            diffs.add(at + " differ: xml-only=" + only + " db-only=" + dbOnly);
        }
    }

    private static void comparePropertyMultiset(final String at,
                                                final List<MibObjProperty> xml,
                                                final List<MibObjProperty> db,
                                                final List<String> diffs) {
        final Map<String, Integer> xCounts = new TreeMap<>();
        for (final MibObjProperty p : xml) {
            xCounts.merge(p.getInstance() + "|" + p.getAlias(), 1, Integer::sum);
        }
        final Map<String, Integer> dCounts = new TreeMap<>();
        for (final MibObjProperty p : db) {
            dCounts.merge(p.getInstance() + "|" + p.getAlias(), 1, Integer::sum);
        }
        if (!xCounts.equals(dCounts)) {
            diffs.add(at + " differ: xml=" + xCounts + " db=" + dCounts);
        }
    }

    private static void compareConfiguredResourceTypes(final DataCollectionConfigDao xml,
                                                       final DataCollectionConfigDao db,
                                                       final List<String> diffs) {
        final Set<String> xKeys = new TreeSet<>(xml.getConfiguredResourceTypes().keySet());
        final Set<String> dKeys = new TreeSet<>(db.getConfiguredResourceTypes().keySet());

        // The DB-side superset (resource types from sources not pulled in by
        // any profile) is a known asymmetry: the migration imports every
        // source, the XML loader only registers what <include-collection>
        // pulls in. Surplus is inert at runtime (collectd looks up by name),
        // but xml-only entries would mean a real loss of data.
        final TreeSet<String> xmlOnly = new TreeSet<>(xKeys);
        xmlOnly.removeAll(dKeys);
        if (!xmlOnly.isEmpty()) {
            diffs.add("dao.getConfiguredResourceTypes: present in XML but missing from DB: " + xmlOnly);
        }
    }

    public static void assertEquivalent(final DatacollectionConfig fromXml,
                                        final DatacollectionConfig fromDb) {
        if (fromXml == null || fromDb == null) {
            throw new AssertionError(
                    "DatacollectionConfig parity check requires both inputs (xml=" + fromXml + ", db=" + fromDb + ")");
        }
        final List<String> diffs = new ArrayList<>();
        compareConfig(fromXml, fromDb, diffs);
        if (!diffs.isEmpty()) {
            throw new AssertionError("DatacollectionConfig parity mismatch:\n  - "
                    + String.join("\n  - ", diffs));
        }
    }

    // ─── Top-level walk ─────────────────────────────────────────────────

    private static void compareConfig(final DatacollectionConfig xml,
                                      final DatacollectionConfig db,
                                      final List<String> diffs) {
        final Map<String, SnmpCollection> xmlByName = mapCollections(xml);
        final Map<String, SnmpCollection> dbByName = mapCollections(db);

        // Real <snmp-collection>s (not the synthetic resource-type holder).
        final TreeSet<String> userNames = new TreeSet<>();
        userNames.addAll(xmlByName.keySet());
        userNames.addAll(dbByName.keySet());
        userNames.remove(RESOURCE_TYPE_COLLECTION_NAME);

        for (final String name : userNames) {
            final SnmpCollection x = xmlByName.get(name);
            final SnmpCollection d = dbByName.get(name);
            if (x == null) { diffs.add("snmp-collection '" + name + "' is in DB output but not XML output"); continue; }
            if (d == null) { diffs.add("snmp-collection '" + name + "' is in XML output but not DB output"); continue; }
            compareSnmpCollection(name, x, d, diffs);
        }

        // Synthetic resource-type holder: compare the resource-type set only.
        compareResourceTypeCollection(xml, db,
                xmlByName.get(RESOURCE_TYPE_COLLECTION_NAME),
                dbByName.get(RESOURCE_TYPE_COLLECTION_NAME),
                diffs);
    }

    private static Map<String, SnmpCollection> mapCollections(final DatacollectionConfig c) {
        final Map<String, SnmpCollection> out = new LinkedHashMap<>();
        if (c.getSnmpCollections() != null) {
            for (final SnmpCollection sc : c.getSnmpCollections()) {
                if (sc != null && sc.getName() != null) {
                    out.put(sc.getName(), sc);
                }
            }
        }
        return out;
    }

    // ─── Per-collection ─────────────────────────────────────────────────

    private static void compareSnmpCollection(final String name,
                                              final SnmpCollection xml,
                                              final SnmpCollection db,
                                              final List<String> diffs) {
        final String at = "snmp-collection[" + name + "]";

        compareScalar(at + ".snmpStorageFlag", xml.getSnmpStorageFlag(), db.getSnmpStorageFlag(), diffs);
        compareScalar(at + ".maxVarsPerPdu", xml.getMaxVarsPerPdu(), db.getMaxVarsPerPdu(), diffs);
        compareRrd(at + ".rrd", xml.getRrd(), db.getRrd(), diffs);

        // Compare only the systemDef-reachable view: collectd consumes groups
        // and resource types via systemDef references at runtime, so groups
        // present on one side but not reachable from any systemDef are dead
        // weight. The XML loader prunes them at load time
        // (DataCollectionConfigParser.addDatacollectionGroup walks systemDef
        // first, then pulls in only the groups it references); the DB loader
        // keeps all groups from each source. We compare the reachable view to
        // assert runtime equivalence rather than load-time identity.
        compareSystemDefs(at, mergedSystemDefs(xml), mergedSystemDefs(db), diffs);
        compareGroups(at, reachableGroups(xml), reachableGroups(db), diffs);
        // Per-collection resource types are an XML-vs-DB load asymmetry: XML
        // empties them out (DefaultDataCollectionConfigDao.translateConfig
        // moves them all into the synthetic __resource_type_collection); DB
        // keeps a copy on the source collection. Resource-type identity is
        // checked once via __resource_type_collection.
    }

    private static void compareResourceTypeCollection(final DatacollectionConfig xmlConfig,
                                                      final DatacollectionConfig dbConfig,
                                                      final SnmpCollection xml,
                                                      final SnmpCollection db,
                                                      final List<String> diffs) {
        if (xml == null && db == null) return;
        if (xml == null) {
            diffs.add("__resource_type_collection: present in DB output but not XML output");
            return;
        }
        if (db == null) {
            diffs.add("__resource_type_collection: present in XML output but not DB output");
            return;
        }
        // Cosmetic asymmetries (DB sets snmpStorageFlag / rrd-step, XML does
        // not) are intentionally ignored — only the resource-type set is
        // runtime-relevant.
        //
        // Restrict the comparison to resource types actually reachable from
        // some user snmp-collection's reachable groups. Both load paths put
        // unreachable resource types into __resource_type_collection by
        // different rules (XML keeps them per-source; DB pulls every source's
        // resource types via the migration). Comparing only what a runtime
        // snmpStorageFlag="select" can actually select keeps the assertion
        // about what collectd sees, not how the loaders chose to stage data.
        final Set<String> xmlReachable = reachableResourceTypeNames(xmlConfig);
        final Set<String> dbReachable = reachableResourceTypeNames(dbConfig);
        final List<ResourceType> xmlFiltered = filterResourceTypes(mergedResourceTypes(xml), xmlReachable);
        final List<ResourceType> dbFiltered = filterResourceTypes(mergedResourceTypes(db), dbReachable);
        compareResourceTypes("__resource_type_collection", xmlFiltered, dbFiltered, diffs);
    }

    private static List<ResourceType> filterResourceTypes(final List<ResourceType> all, final Set<String> keep) {
        final List<ResourceType> out = new ArrayList<>();
        if (all == null) return out;
        for (final ResourceType rt : all) {
            if (rt != null && rt.getName() != null && keep.contains(rt.getName())) {
                out.add(rt);
            }
        }
        return out;
    }

    private static Set<String> reachableResourceTypeNames(final DatacollectionConfig config) {
        final Set<String> out = new HashSet<>();
        if (config == null || config.getSnmpCollections() == null) return out;
        for (final SnmpCollection sc : config.getSnmpCollections()) {
            if (sc == null) continue;
            if (RESOURCE_TYPE_COLLECTION_NAME.equals(sc.getName())) continue;
            for (final Group g : reachableGroups(sc)) {
                if (g == null || g.getMibObjs() == null) continue;
                for (final MibObj mo : g.getMibObjs()) {
                    final String inst = mo == null ? null : mo.getInstance();
                    if (inst != null && !inst.isBlank()) {
                        // 'instance' references a resource-type name when it
                        // isn't a numeric index or the literal "ifIndex".
                        // Numeric / ifIndex won't be in the resource-type
                        // map; harmless to add and have it filtered out.
                        out.add(inst);
                    }
                }
            }
        }
        return out;
    }

    // ─── RRD ────────────────────────────────────────────────────────────

    private static void compareRrd(final String at, final Rrd xml, final Rrd db, final List<String> diffs) {
        if (xml == null && db == null) return;
        if (xml == null || db == null) {
            diffs.add(at + ": one is null (xml=" + xml + ", db=" + db + ")");
            return;
        }
        compareScalar(at + ".step", xml.getStep(), db.getStep(), diffs);
        // RRA order is meaningful — RRD archives are positional.
        final List<String> xmlRras = xml.getRras() != null ? xml.getRras() : List.of();
        final List<String> dbRras = db.getRras() != null ? db.getRras() : List.of();
        if (!xmlRras.equals(dbRras)) {
            diffs.add(at + ".rras differ: xml=" + xmlRras + " db=" + dbRras);
        }
    }

    // ─── Groups ─────────────────────────────────────────────────────────

    private static List<Group> mergedGroups(final SnmpCollection sc) {
        if (sc.getGroups() == null || sc.getGroups().getGroups() == null) {
            return List.of();
        }
        return sc.getGroups().getGroups();
    }

    /**
     * Compute the runtime-relevant group set: a group is reachable if some
     * systemDef in this collection lists it in {@code <collect>/<include-groups>},
     * or if it is transitively included via another reachable group's
     * {@code <include-groups>}. Mirrors how collectd resolves what to
     * actually collect when a node matches a systemDef.
     */
    private static List<Group> reachableGroups(final SnmpCollection sc) {
        final List<Group> all = mergedGroups(sc);
        if (all.isEmpty()) return all;
        final Map<String, Group> byName = byName(all, Group::getName);
        final Set<String> reachable = new HashSet<>();
        final Deque<String> work = new ArrayDeque<>();
        for (final SystemDef sd : mergedSystemDefs(sc)) {
            if (sd == null || sd.getCollect() == null) continue;
            final List<String> seeds = sd.getCollect().getIncludeGroups();
            if (seeds != null) work.addAll(seeds);
        }
        while (!work.isEmpty()) {
            final String name = work.poll();
            if (name == null || !reachable.add(name)) continue;
            final Group g = byName.get(name);
            if (g == null || g.getIncludeGroups() == null) continue;
            for (final String child : g.getIncludeGroups()) {
                if (child != null && !reachable.contains(child)) {
                    work.push(child);
                }
            }
        }
        final List<Group> out = new ArrayList<>(reachable.size());
        for (final String n : reachable) {
            final Group g = byName.get(n);
            if (g != null) out.add(g);
        }
        return out;
    }

    private static void compareGroups(final String at,
                                      final List<Group> xml,
                                      final List<Group> db,
                                      final List<String> diffs) {
        final Map<String, Group> xmlByName = byName(xml, Group::getName);
        final Map<String, Group> dbByName = byName(db, Group::getName);
        final TreeSet<String> all = new TreeSet<>();
        all.addAll(xmlByName.keySet());
        all.addAll(dbByName.keySet());
        for (final String n : all) {
            final Group x = xmlByName.get(n);
            final Group d = dbByName.get(n);
            if (x == null) { diffs.add(at + ".groups[" + n + "]: in DB only"); continue; }
            if (d == null) { diffs.add(at + ".groups[" + n + "]: in XML only"); continue; }
            compareGroup(at + ".groups[" + n + "]", x, d, diffs);
        }
    }

    private static void compareGroup(final String at, final Group xml, final Group db, final List<String> diffs) {
        compareScalar(at + ".ifType", xml.getIfType(), db.getIfType(), diffs);
        compareMibObjs(at, xml.getMibObjs(), db.getMibObjs(), diffs);
        compareSorted(at + ".includeGroups", xml.getIncludeGroups(), db.getIncludeGroups(), diffs);
        compareProperties(at, xml.getProperties(), db.getProperties(), diffs);
    }

    private static void compareMibObjs(final String at,
                                       final List<MibObj> xml,
                                       final List<MibObj> db,
                                       final List<String> diffs) {
        // Set-by-(alias+oid) — order in the XML isn't load-bearing.
        final Map<String, MibObj> xmlByKey = byName(xml, m -> m.getAlias() + "|" + m.getOid());
        final Map<String, MibObj> dbByKey = byName(db, m -> m.getAlias() + "|" + m.getOid());
        final TreeSet<String> all = new TreeSet<>();
        all.addAll(xmlByKey.keySet());
        all.addAll(dbByKey.keySet());
        for (final String k : all) {
            final MibObj x = xmlByKey.get(k);
            final MibObj d = dbByKey.get(k);
            if (x == null) { diffs.add(at + ".mibObj[" + k + "]: in DB only"); continue; }
            if (d == null) { diffs.add(at + ".mibObj[" + k + "]: in XML only"); continue; }
            compareScalar(at + ".mibObj[" + k + "].type", x.getType(), d.getType(), diffs);
            compareScalar(at + ".mibObj[" + k + "].instance", x.getInstance(), d.getInstance(), diffs);
            compareScalar(at + ".mibObj[" + k + "].minval", x.getMinval(), d.getMinval(), diffs);
            compareScalar(at + ".mibObj[" + k + "].maxval", x.getMaxval(), d.getMaxval(), diffs);
        }
    }

    private static void compareProperties(final String at,
                                          final List<MibObjProperty> xml,
                                          final List<MibObjProperty> db,
                                          final List<String> diffs) {
        final Map<String, MibObjProperty> xmlByName = byName(xml, MibObjProperty::getInstance);
        final Map<String, MibObjProperty> dbByName = byName(db, MibObjProperty::getInstance);
        final TreeSet<String> all = new TreeSet<>();
        all.addAll(xmlByName.keySet());
        all.addAll(dbByName.keySet());
        for (final String n : all) {
            final MibObjProperty x = xmlByName.get(n);
            final MibObjProperty d = dbByName.get(n);
            if (x == null) { diffs.add(at + ".property[" + n + "]: in DB only"); continue; }
            if (d == null) { diffs.add(at + ".property[" + n + "]: in XML only"); continue; }
            compareScalar(at + ".property[" + n + "].alias", x.getAlias(), d.getAlias(), diffs);
            compareScalar(at + ".property[" + n + "].clazz", x.getClassName(), d.getClassName(), diffs);
        }
    }

    // ─── SystemDefs ─────────────────────────────────────────────────────

    private static List<SystemDef> mergedSystemDefs(final SnmpCollection sc) {
        if (sc.getSystems() == null || sc.getSystems().getSystemDefs() == null) {
            return List.of();
        }
        return sc.getSystems().getSystemDefs();
    }

    private static void compareSystemDefs(final String at,
                                          final List<SystemDef> xml,
                                          final List<SystemDef> db,
                                          final List<String> diffs) {
        final Map<String, SystemDef> xmlByName = byName(xml, SystemDef::getName);
        final Map<String, SystemDef> dbByName = byName(db, SystemDef::getName);
        final TreeSet<String> all = new TreeSet<>();
        all.addAll(xmlByName.keySet());
        all.addAll(dbByName.keySet());
        for (final String n : all) {
            final SystemDef x = xmlByName.get(n);
            final SystemDef d = dbByName.get(n);
            if (x == null) { diffs.add(at + ".systemDef[" + n + "]: in DB only"); continue; }
            if (d == null) { diffs.add(at + ".systemDef[" + n + "]: in XML only"); continue; }
            compareScalar(at + ".systemDef[" + n + "].sysoid", x.getSysoid(), d.getSysoid(), diffs);
            compareScalar(at + ".systemDef[" + n + "].sysoidMask", x.getSysoidMask(), d.getSysoidMask(), diffs);
            final List<String> xIncludes = Optional.ofNullable(x.getCollect()).map(c -> c.getIncludeGroups()).orElse(List.of());
            final List<String> dIncludes = Optional.ofNullable(d.getCollect()).map(c -> c.getIncludeGroups()).orElse(List.of());
            compareSorted(at + ".systemDef[" + n + "].collect.includeGroups", xIncludes, dIncludes, diffs);
            compareIpList(at + ".systemDef[" + n + "].ipList", x.getIpList(), d.getIpList(), diffs);
        }
    }

    private static void compareIpList(final String at, final IpList xml, final IpList db, final List<String> diffs) {
        if (xml == null && db == null) return;
        // Both null-safe: an empty IpList is functionally equivalent to a missing one.
        final List<String> xAddrs = xml != null ? xml.getIpAddresses() : List.of();
        final List<String> dAddrs = db != null ? db.getIpAddresses() : List.of();
        compareSorted(at + ".ipAddresses", xAddrs, dAddrs, diffs);
        final List<String> xMasks = xml != null ? xml.getIpAddressMasks() : List.of();
        final List<String> dMasks = db != null ? db.getIpAddressMasks() : List.of();
        compareSorted(at + ".ipAddressMasks", xMasks, dMasks, diffs);
    }

    // ─── Resource types ─────────────────────────────────────────────────

    private static List<ResourceType> mergedResourceTypes(final SnmpCollection sc) {
        return sc.getResourceTypes() != null ? sc.getResourceTypes() : List.of();
    }

    private static void compareResourceTypes(final String at,
                                             final List<ResourceType> xml,
                                             final List<ResourceType> db,
                                             final List<String> diffs) {
        final Map<String, ResourceType> xmlByName = byName(xml, ResourceType::getName);
        final Map<String, ResourceType> dbByName = byName(db, ResourceType::getName);
        final TreeSet<String> all = new TreeSet<>();
        all.addAll(xmlByName.keySet());
        all.addAll(dbByName.keySet());
        for (final String n : all) {
            final ResourceType x = xmlByName.get(n);
            final ResourceType d = dbByName.get(n);
            if (x == null) { diffs.add(at + ".resourceType[" + n + "]: in DB only"); continue; }
            if (d == null) { diffs.add(at + ".resourceType[" + n + "]: in XML only"); continue; }
            compareScalar(at + ".resourceType[" + n + "].label", x.getLabel(), d.getLabel(), diffs);
            compareScalar(at + ".resourceType[" + n + "].resourceLabel", x.getResourceLabel(), d.getResourceLabel(), diffs);
            compareStorageStrategy(at + ".resourceType[" + n + "]", x.getStorageStrategy(), d.getStorageStrategy(), diffs);
            comparePersistenceSelector(at + ".resourceType[" + n + "]", x.getPersistenceSelectorStrategy(), d.getPersistenceSelectorStrategy(), diffs);
        }
    }

    private static void compareStorageStrategy(final String at,
                                               final StorageStrategy xml,
                                               final StorageStrategy db,
                                               final List<String> diffs) {
        if (xml == null && db == null) return;
        if (xml == null || db == null) {
            diffs.add(at + ".storageStrategy: one is null (xml=" + xml + ", db=" + db + ")");
            return;
        }
        compareScalar(at + ".storageStrategy.clazz", xml.getClazz(), db.getClazz(), diffs);
        compareParameters(at + ".storageStrategy", xml.getParameters(), db.getParameters(), diffs);
    }

    private static void comparePersistenceSelector(final String at,
                                                   final PersistenceSelectorStrategy xml,
                                                   final PersistenceSelectorStrategy db,
                                                   final List<String> diffs) {
        if (xml == null && db == null) return;
        if (xml == null || db == null) {
            diffs.add(at + ".persistenceSelectorStrategy: one is null (xml=" + xml + ", db=" + db + ")");
            return;
        }
        compareScalar(at + ".persistenceSelectorStrategy.clazz", xml.getClazz(), db.getClazz(), diffs);
        compareParameters(at + ".persistenceSelectorStrategy", xml.getParameters(), db.getParameters(), diffs);
    }

    private static void compareParameters(final String at,
                                          final List<Parameter> xml,
                                          final List<Parameter> db,
                                          final List<String> diffs) {
        final Map<String, String> xmlMap = paramMap(xml);
        final Map<String, String> dbMap = paramMap(db);
        if (!xmlMap.equals(dbMap)) {
            diffs.add(at + ".parameters differ: xml=" + xmlMap + " db=" + dbMap);
        }
    }

    private static Map<String, String> paramMap(final List<Parameter> params) {
        final Map<String, String> out = new TreeMap<>();
        if (params != null) {
            for (final Parameter p : params) {
                out.put(p.getKey(), p.getValue());
            }
        }
        return out;
    }

    // ─── Helpers ────────────────────────────────────────────────────────

    private static <T> Map<String, T> byName(final List<T> items, final java.util.function.Function<T, String> keyFn) {
        final Map<String, T> out = new LinkedHashMap<>();
        if (items != null) {
            for (final T item : items) {
                final String key = keyFn.apply(item);
                if (key != null) {
                    // Last-wins; both load paths dedupe by name so this is symmetric.
                    out.put(key, item);
                }
            }
        }
        return out;
    }

    private static void compareScalar(final String at, final Object xml, final Object db, final List<String> diffs) {
        if (!Objects.equals(xml, db)) {
            diffs.add(at + " differ: xml=" + repr(xml) + " db=" + repr(db));
        }
    }

    /**
     * Compare two list values as multisets (order-insensitive). Sorting both
     * sides before comparison surfaces value-level differences while
     * tolerating ordering quirks between the two load paths.
     */
    private static void compareSorted(final String at,
                                      final List<String> xml,
                                      final List<String> db,
                                      final List<String> diffs) {
        final List<String> a = xml != null ? new ArrayList<>(xml) : new ArrayList<>();
        final List<String> b = db != null ? new ArrayList<>(db) : new ArrayList<>();
        a.sort(Comparator.naturalOrder());
        b.sort(Comparator.naturalOrder());
        if (!a.equals(b)) {
            diffs.add(at + " differ: xml=" + a + " db=" + b);
        }
    }

    private static String repr(final Object v) {
        if (v == null) return "null";
        if (v instanceof String) return "\"" + v + "\"";
        return String.valueOf(v);
    }
}
