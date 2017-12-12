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

package org.opennms.netmgt.provision;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.features.geolocation.api.Coordinates;
import org.opennms.features.geolocation.api.GeolocationResolver;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsGeolocation;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class GeolocationProvisioningAdapterIT {

    @Autowired
    private NodeDao nodeDao;

    @Autowired
    private DatabasePopulator databasePopulator;

    @Before
    public void setUp() {
        databasePopulator.populateDatabase();
    }

    @After
    public void tearDown() {
        databasePopulator.resetDatabase();
    }

    // See NMS-9187
    @Test
    public void canHandleNullGeolocation() throws Exception {
        // Ensure that the geolocation is null
        final OnmsNode node = nodeDao.get(databasePopulator.getNode1().getId());
        Assert.assertNull(node.getAssetRecord().getGeolocation());

        // Mock the geolocation resolution
        final Coordinates coordinates = new Coordinates(-3.179090f, 51.481583f);
        final GeolocationResolver geolocationResolverMock = Mockito.mock(GeolocationResolver.class);
        Mockito.when(geolocationResolverMock.resolve(Mockito.anyString())).thenReturn(coordinates);

        // Manually invoke provisioning adapter
        final GeolocationProvisioningAdapter geolocationProvisioningAdapter = new GeolocationProvisioningAdapter();
        geolocationProvisioningAdapter.setNodeDao(nodeDao);
        geolocationProvisioningAdapter.afterPropertiesSet();
        geolocationProvisioningAdapter.updateGeolocation(geolocationResolverMock, node);

        // Node should not have been updated
        Assert.assertNull(node.getAssetRecord().getGeolocation());

        // Set a geolocation and resolve coordinates
        node.getAssetRecord().setGeolocation(new OnmsGeolocation());
        node.getAssetRecord().getGeolocation().setCity("Cardiff");
        nodeDao.saveOrUpdate(node);
        geolocationProvisioningAdapter.updateGeolocation(geolocationResolverMock, node);

        // Node should have been updated
        Assert.assertEquals(coordinates.getLongitude(), node.getAssetRecord().getGeolocation().getLongitude(), 0.001);
        Assert.assertEquals(coordinates.getLatitude(), node.getAssetRecord().getGeolocation().getLatitude(), 0.001);
    }
}