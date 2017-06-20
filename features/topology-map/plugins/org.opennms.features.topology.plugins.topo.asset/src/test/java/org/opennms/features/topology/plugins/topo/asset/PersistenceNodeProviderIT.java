/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.topo.asset;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.features.topology.plugins.topo.asset.layers.Layer;
import org.opennms.features.topology.plugins.topo.asset.layers.LayerDefinition;
import org.opennms.features.topology.plugins.topo.asset.layers.LayerDefinitionRepository;
import org.opennms.features.topology.plugins.topo.asset.layers.PersistenceNodeProvider;
import org.opennms.features.topology.plugins.topo.asset.layers.Layers;
import org.opennms.features.topology.plugins.topo.asset.layers.Restriction;
import org.opennms.features.topology.plugins.topo.asset.layers.NodeParamLabels;
import org.opennms.features.topology.plugins.topo.asset.util.NodeBuilder;
import org.opennms.netmgt.dao.api.CategoryDao;
import org.opennms.netmgt.dao.api.GenericPersistenceAccessor;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(reuseDatabase = false)
public class PersistenceNodeProviderIT {

    @Autowired
    private NodeDao nodeDao;

    @Autowired
    private CategoryDao categoryDao;

    @Autowired
    private GenericPersistenceAccessor genericPersistenceAccessor;

    @Before
    public void setUp() {
        OnmsNode node1 = new NodeBuilder().withLabel("Node 1").getNode();
        nodeDao.save(node1);

        OnmsNode parentNode = new NodeBuilder().withLabel("Node Parent").withForeignId("parentForeignId").withForeignSource("parentForeignSource").getNode();
        nodeDao.save(parentNode);

        OnmsNode node2 = new NodeBuilder()
                .withLabel("Node 2")
                .withCategories("Server")
                .withForeignId("1234")
                .withForeignSource("dummmy")
                .withOperatingSystem("Windows")
                .withSysname("Digger")
                .withSyslocation("Moon")
                .withAssets()
                    .withAddress1("Address 1")
                    .withAddress2("Address 2")
                    .withBuilding("Building")
                    .withCategory("Category")
                    .withDepartment("Department")
                    .withDescription("Description")
                    .withCircuitId("CircuitId")
                    .withCity("City")
                    .withCountry("Country")
                    .withDisplayCategory("DisplayCategory")
                    .withDivision("Division")
                    .withFloor("Floor")
                    .withLatitude("10")
                    .withLongitude("10")
                    .withManagedObjectInstance("ManagedObjectInstance")
                    .withManagedObjectType("ManagedObjectType")
                    .withManufacturer("Manufacturer")
                    .withModelNumber("ModelNumber")
                    .withNotifyCategory("NotifyCategory")
                    .withOperatingSystem("OperatingSystem")
                    .withPollerCategory("PollerCategory")
                    .withPort("Port")
                    .withRack("Rack")
                    .withRegion("Region")
                    .withRoom("Room")
                    .withSlot("slot")
                    .withState("State")
                    .withThresholdCategory("ThresholdCategory")
                    .withVendor("Vendor")
                    .withZip("Zip")
                .done()
                .getNode();
        node2.getCategories().forEach(c -> categoryDao.save(c));
        node2.setParent(parentNode);
        nodeDao.save(node2);
    }

    /**
     * In order to build a valid hierarchy, each{@link Layer}
     * must ensure that the value for each node IS NOT null.
     * To let the database do the filtering, a {@link Restriction}
     * annotation must be present.
     * This tests ensures that each defined {@link Layer} actually provides a restriction and that the {@link Restriction} works as expected.
     */
    @Test
    public void verifyLayerRestrictions() throws Exception {
        List<LayerDefinition> mapping = new LayerDefinitionRepository().getDefinitions(NodeParamLabels.ALL_KEYS);
        Assert.assertEquals(NodeParamLabels.ALL_KEYS.size(), Layers.values().length);
        Assert.assertEquals(Layers.values().length, mapping.size());

        final List<OnmsNode> nodes = new PersistenceNodeProvider(genericPersistenceAccessor).getNodes(mapping);
        Assert.assertEquals(1, nodes.size());
        OnmsNode actualNode = nodes.get(0);
        OnmsNode expectedNode = nodeDao.findByLabel("Node 2").get(0);
        Assert.assertEquals(expectedNode.getId(), actualNode.getId());

    }
}
