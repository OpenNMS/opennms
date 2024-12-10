/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.search.providers.node;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.mate.api.EntityScopeProvider;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsMetaData;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.search.api.SearchQuery;
import org.opennms.netmgt.search.api.SearchResult;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-rpc-client-mock.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(reuseDatabase = false)
public class NodeAssetSearchProviderIT {

    @Autowired
    private NodeDao nodeDao;

    @Autowired
    private EntityScopeProvider entityScopeProvider;

    private NodeAssetSearchProvider nodeAssetSearchProvider;


    @Before
    public void setUp() {
        nodeAssetSearchProvider = new NodeAssetSearchProvider(nodeDao, entityScopeProvider);
        nodeDao.save(buildTestNode());
    }

    @Test
    public void testShouldReturnEmptyAssetWhenQuerying() {
        final SearchQuery searchQuery = new SearchQuery("Not available");
        final SearchResult results = nodeAssetSearchProvider.query(searchQuery);
        assertTrue(results.isEmpty());
    }

    @Test
    public void testShouldReturnAssetWhenQuerying() {
        final SearchQuery searchQuery = new SearchQuery("Greek-120");
        final SearchResult results = nodeAssetSearchProvider.query(searchQuery);
        assertFalse(results.isEmpty());
        assertEquals(1, results.getResults().size());
    }

    @Test
    public void testShouldReturnAssetWhenQueryingWithHalfString() {
        final SearchQuery searchQuery = new SearchQuery("eek");
        final SearchResult results = nodeAssetSearchProvider.query(searchQuery);
        assertFalse( results.isEmpty());
        assertEquals(1, results.getResults().size());
    }

    private OnmsNode buildTestNode() {
        NetworkBuilder builder = new NetworkBuilder();
        builder.addNode("nodeA")
                .setForeignSource("nodeIdTest:")
                .setForeignId("A")
                .setType(OnmsNode.NodeType.ACTIVE);
        builder.addInterface("192.168.99.99")
                .setIsManaged("M")
                .setIsSnmpPrimary("P");

        OnmsNode node = builder.getCurrentNode();
        node.getMetaData().add(new OnmsMetaData("testContext", "testKey", "99099"));

        OnmsAssetRecord asset = new OnmsAssetRecord();
        asset.setSerialNumber("1001");
        asset.setMaintcontract("Greek-120");
        node.setAssetRecord(asset);

        return node;
    }
}