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
        "classpath:/META-INF/opennms/applicationContext-mockSnmpPeerFactory.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
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