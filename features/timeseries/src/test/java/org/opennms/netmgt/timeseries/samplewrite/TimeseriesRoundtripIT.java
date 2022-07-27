/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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


package org.opennms.netmgt.timeseries.samplewrite;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.opennms.netmgt.timeseries.samplewrite.MetaTagConfiguration.CONFIG_KEY_FOR_CATEGORIES;
import static org.opennms.netmgt.timeseries.samplewrite.MetaTagConfiguration.CONFIG_PREFIX_FOR_TAGS;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.integration.api.v1.timeseries.Aggregation;
import org.opennms.integration.api.v1.timeseries.IntrinsicTagNames;
import org.opennms.integration.api.v1.timeseries.Metric;
import org.opennms.integration.api.v1.timeseries.Sample;
import org.opennms.integration.api.v1.timeseries.StorageException;
import org.opennms.integration.api.v1.timeseries.Tag;
import org.opennms.integration.api.v1.timeseries.TimeSeriesFetchRequest;
import org.opennms.integration.api.v1.timeseries.immutables.ImmutableTag;
import org.opennms.integration.api.v1.timeseries.immutables.ImmutableTagMatcher;
import org.opennms.integration.api.v1.timeseries.immutables.ImmutableTimeSeriesFetchRequest;
import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.Persister;
import org.opennms.netmgt.collection.api.ResourceType;
import org.opennms.netmgt.collection.api.ResourceTypeMapper;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.collection.dto.CollectionSetDTO;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;
import org.opennms.netmgt.collection.support.builder.DeferredGenericTypeResource;
import org.opennms.netmgt.collection.support.builder.GenericTypeResource;
import org.opennms.netmgt.collection.support.builder.InterfaceLevelResource;
import org.opennms.netmgt.collection.support.builder.NodeLevelResource;
import org.opennms.netmgt.dao.api.AssetRecordDao;
import org.opennms.netmgt.dao.api.CategoryDao;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.model.StringPropertyAttribute;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.netmgt.timeseries.TimeseriesStorageManager;
import org.opennms.netmgt.timeseries.resource.TimeseriesResourceStorageDao;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath:/META-INF/opennms/applicationContext-timeseries-test.xml",
        "classpath:/META-INF/opennms/applicationContext-jceks-scv.xml"
})
@JUnitConfigurationEnvironment(systemProperties={
        "org.opennms.timeseries.strategy=integration"
})
@JUnitTemporaryDatabase(dirtiesContext=true)
public class TimeseriesRoundtripIT {

    @Autowired
    private TimeseriesPersisterFactory persisterFactory;

    @Autowired
    private MonitoringLocationDao locationDao;

    @Autowired
    private NodeDao nodeDao;

    @Autowired
    private CategoryDao categoryDao;

    @Autowired
    private AssetRecordDao assetDao;

    @Autowired
    private SnmpInterfaceDao snmpInterfaceDao;

    @Autowired
    private IpInterfaceDao ipInterfaceDao;

    @Autowired
    private TimeseriesStorageManager timeseriesStorageManager;

    @Autowired
    private MetaTagDataLoader metaTagDataLoader;

    @Autowired
    private TimeseriesResourceStorageDao resourceStorageDao;

    @Before
    public void setUp() {
        Map<String, String> config = new HashMap<>();
        config.put(CONFIG_PREFIX_FOR_TAGS + "nodelabel", "${node:label}");
        config.put(CONFIG_PREFIX_FOR_TAGS + "sysObjectID", "${node:sys-object-id}");
        config.put(CONFIG_PREFIX_FOR_TAGS + "vendor", "${asset:vendor}");
        config.put(CONFIG_PREFIX_FOR_TAGS + "if-description", "${interface:if-description}");
        config.put(CONFIG_KEY_FOR_CATEGORIES, "true");
        metaTagDataLoader.setConfig(new MetaTagConfiguration(config));
    }

    @Test
    public void canPersist() throws InterruptedException, StorageException, UnknownHostException {

        createAndSaveNode();

        ServiceParameters params = new ServiceParameters(Collections.emptyMap());
        RrdRepository repo = new RrdRepository();
        // Only the last element of the path matters here
        repo.setRrdBaseDir(Paths.get("a","path","that","ends","with","snmp").toFile());
        Persister persister = persisterFactory.createPersister(params, repo);

        CollectionAgent agent = mock(CollectionAgent.class);
        int nodeId = 1;
        when(agent.getStorageResourcePath()).thenReturn(ResourcePath.get(Integer.toString(nodeId)));
        NodeLevelResource nodeLevelResource = new NodeLevelResource(nodeId);
        InterfaceLevelResource ifLevelResource = new InterfaceLevelResource(nodeLevelResource, "1");

        ResourceType rt = mock(ResourceType.class, RETURNS_DEEP_STUBS);
        when(rt.getName()).thenReturn("Charles");
        when(rt.getStorageStrategy().getClazz()).thenReturn(MockStorageStrategy.class.getCanonicalName());
        when(rt.getPersistenceSelectorStrategy().getClazz()).thenReturn(MockPersistenceSelectorStrategy.class.getCanonicalName());

        DeferredGenericTypeResource deferredGenericTypeResource = new DeferredGenericTypeResource(nodeLevelResource, "Charles", "id");

        GenericTypeResource genericTypeResource = new GenericTypeResource(nodeLevelResource, rt, "idx");
        genericTypeResource.setTimestamp(new Date(0));
        ResourceTypeMapper.getInstance().setResourceTypeMapper((name) -> rt);

        Date now = new Date();
        CollectionSetDTO collectionSet = new CollectionSetBuilder(agent)
                // Node level
                .withNumericAttribute(nodeLevelResource, "metrics", "m1", 900, AttributeType.GAUGE)
                .withIdentifiedNumericAttribute(nodeLevelResource, "metrics", "m2", 1000, AttributeType.COUNTER, "idx-m2")
                .withStringAttribute(nodeLevelResource, "metrics", "sysname", "host1")
                .withStringAttribute(nodeLevelResource, "metrics2", "stringAttributeFromAnotherGroup", "123")
                // Interface level
                .withNumericAttribute(ifLevelResource, "if-metrics", "m3", 44, AttributeType.GAUGE)
                .withIdentifiedNumericAttribute(ifLevelResource, "if-metrics", "m4", 55, AttributeType.COUNTER, "idx-m4")
                .withStringAttribute(ifLevelResource, "if-metrics", "ifname", "eth0")
                // Generic
                .withNumericAttribute(deferredGenericTypeResource, "gen-metrics", "m5", 66, AttributeType.GAUGE)
                .withIdentifiedNumericAttribute(deferredGenericTypeResource, "gen-metrics", "m6", 77, AttributeType.COUNTER, "idx-m6")
                .withIdentifiedStringAttribute(deferredGenericTypeResource, "gen-metrics", "genname", "bgp", "oops")
                .withTimestamp(now)
                .build();

        // Persist
        collectionSet.visit(persister);

        // Wait for the sample(s) to be flushed
        Thread.sleep(5 * 1000);

        // Tags contained in the Metric:
        testForNumericAttribute("snmp/1/metrics", "m1", 900d);
        testForNumericAttribute("snmp/1/metrics", "m2", 1000d);
        // String attributes:
        testForStringAttributeAtMetricLevel("snmp/1/metrics", "m2", "idx-m2", "m2"); // Identified
        testForStringAttributeAtResourceLevel("snmp/1", "sysname", "host1");
        testForStringAttributeAtResourceLevel("snmp/1","stringAttributeFromAnotherGroup", "123");

        // Tags contained in the Metric:
        testForNumericAttribute("snmp/1/1/if-metrics", "m3", 44d);
        testForNumericAttribute("snmp/1/1/if-metrics", "m4", 55d);
        // String attributes:
        testForStringAttributeAtMetricLevel("snmp/1/1/if-metrics", "m4", "idx-m4", "m4"); // Identified
        testForStringAttributeAtResourceLevel("snmp/1/1", "ifname", "eth0");

        // Tags contained in the Metric:
        testForNumericAttribute("snmp/1/gen-metrics/gen-metrics", "m5", 66d);
        testForNumericAttribute("snmp/1/gen-metrics/gen-metrics", "m6", 77d);
        // String attributes:
        testForStringAttributeAtMetricLevel("snmp/1/gen-metrics/gen-metrics", "m6", "idx-m6", "m6"); // Identified
        testForStringAttributeAtResourceLevel("snmp/1/gen-metrics", "genname", "bgp");

        // test for additional meta tags that are provided to the timeseries plugin for external use. They are stored as additional meta tags
        // in the Metrics object
        testForMetaTag("snmp/1/metrics", "m1", "sysObjectID", "abc");
        testForMetaTag("snmp/1/metrics", "m1", "nodelabel","myNodeLabel");
        testForMetaTag("snmp/1/metrics", "m1", "vendor","myVendor");
        testForMetaTag("snmp/1/1/if-metrics", "m3",  "if-description","myDescription");
        testForMetaTag("snmp/1/metrics", "m1",  "cat_myCategory","myCategory");
    }

    private void testForNumericAttribute(String resourceId, String name, Double expectedValue) throws StorageException {
        List<Sample> sample = retrieveSamples(resourceId, name);
        assertEquals(1, sample.size());
        assertEquals(expectedValue, sample.get(0).getValue());

    }

    private void testForMetaTag(String resourceId, String name, String tagKey, String tagValue) throws StorageException {
        List<Sample> sample = retrieveSamples(resourceId, name) ;
        assertEquals(1, sample.size());
        Tag tag = new ImmutableTag(tagKey, tagValue);
        assertEquals(tag, sample.get(0).getMetric().getFirstTagByKey(tagKey));
    }

    private List<Sample> retrieveSamples(final String resourceId, final String name) throws StorageException {
        List<Metric> metrics = timeseriesStorageManager.get().findMetrics(Arrays.asList(
                ImmutableTagMatcher.builder().key(IntrinsicTagNames.resourceId).value(resourceId).build(),
                ImmutableTagMatcher.builder().key(IntrinsicTagNames.name).value(name).build()));
        assertEquals(1, metrics.size());

        TimeSeriesFetchRequest request = ImmutableTimeSeriesFetchRequest.builder()
                .aggregation(Aggregation.NONE)
                .start(Instant.ofEpochMilli(0))
                .end(Instant.now())
                .step(Duration.ofSeconds(1))
                .metric(metrics.get(0)).build();

        return timeseriesStorageManager.get().getTimeseries(request);
    }

    private void testForStringAttributeAtResourceLevel(String resourcePath, String attributeName, String expectedValue) throws StorageException {
        Map<String, String> stringAttributes;
        ResourcePath path = ResourcePath.fromString(resourcePath);
        stringAttributes = resourceStorageDao.getAttributes(path)
                .stream()
                .filter(a -> a instanceof StringPropertyAttribute)
                .map(a -> (StringPropertyAttribute) a)
                .collect(Collectors.toMap(StringPropertyAttribute::getName, StringPropertyAttribute::getValue));
        assertEquals(expectedValue, stringAttributes.get(attributeName));
        assertEquals(expectedValue, resourceStorageDao.getStringAttribute(path, attributeName));
    }

    private void testForStringAttributeAtMetricLevel(String resourceId, String metricName, String attributeName, String expectedValue) throws StorageException {
        List<Metric> metrics = this.timeseriesStorageManager.get()
                .findMetrics(Arrays.asList(
                        ImmutableTagMatcher.builder().key(IntrinsicTagNames.resourceId).value(resourceId).build(),
                        ImmutableTagMatcher.builder().key(IntrinsicTagNames.name).value(metricName).build()));
        assertEquals(1, metrics.size());
        Tag tag = metrics.get(0).getFirstTagByKey(attributeName);
        assertNotNull(tag);
        assertEquals(expectedValue, tag.getValue());
    }

    private void createAndSaveNode() throws UnknownHostException {
        OnmsCategory category = new OnmsCategory("myCategory");
        categoryDao.save(category);
        OnmsNode node = new OnmsNode(locationDao.getDefaultLocation(), "myNodeLabel");
        node.setForeignSource("TestGroup");
        node.setForeignId("1");
        node.setSysObjectId("abc");
        node.addCategory(category);

        OnmsAssetRecord assets = new OnmsAssetRecord();
        assets.setVendor("myVendor");
        assetDao.save(assets);
        node.setAssetRecord(assets);

        int nodeId = nodeDao.save(node);
        nodeDao.flush();
        assertEquals(1, nodeId); // we expect 1, otherwise we need to change the hardcoded paths above

        OnmsSnmpInterface snmpInterface = new OnmsSnmpInterface(node, 1);
        // snmpInterface.setId(1);
        snmpInterface.setIfAlias("Connection to OpenNMS Wifi");
        snmpInterface.setIfDescr("myDescription");
        snmpInterface.setIfName("en1/0");
        snmpInterface.setPhysAddr("00:00:00:00:00:01");
        int snmpInterfaceId = snmpInterfaceDao.save(snmpInterface);
        assertEquals(2, snmpInterfaceId); // we expect 2, otherwise we need to change the hardcoded paths above

        InetAddress address = InetAddress.getByName("10.0.1.1");
        OnmsIpInterface onmsIf = new OnmsIpInterface(address, node);
        onmsIf.setSnmpInterface(snmpInterface);
        onmsIf.setId(1);
        onmsIf.setIfIndex(1);
        onmsIf.setIpHostName("myHost");
        onmsIf.setIsSnmpPrimary(PrimaryType.PRIMARY);
        ipInterfaceDao.save(onmsIf);
        Set<OnmsIpInterface> ipInterfaces = new LinkedHashSet<>();
        ipInterfaces.add(onmsIf);
        node.setIpInterfaces(ipInterfaces);
        nodeDao.update(node);
    }

}
