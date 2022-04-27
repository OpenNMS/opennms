/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
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

package org.opennms.core.rpc.utils.mate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.features.scv.api.Credentials;
import org.opennms.features.scv.api.SecureCredentialsVault;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.model.OnmsGeolocation;
import org.opennms.netmgt.model.OnmsMetaData;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/applicationContext-rpc-utils.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class EntityScopeProviderIT {

    @Autowired
    private SessionUtils sessionUtils;

    @Autowired
    private DatabasePopulator populator;

    @Autowired
    private EntityScopeProvider provider;

    @Autowired
    private SecureCredentialsVault secureCredentialsVault;


    @Before
    public void setup() {
        this.populator.populateDatabase();
    }

    @Test
    public final void testNode() throws Exception {
        final Scope scope = this.provider.getScopeForNode(this.populator.getNode1().getId());

        assertThat(scope.get(new ContextKey("node", "label")), is(Optional.of(new Scope.ScopeValue(Scope.ScopeName.NODE, "node1"))));

        assertThat(scope.get(new ContextKey("node", "foreign-source")), is(Optional.of(new Scope.ScopeValue(Scope.ScopeName.NODE, "imported:"))));
        assertThat(scope.get(new ContextKey("node", "foreign-id")), is(Optional.of(new Scope.ScopeValue(Scope.ScopeName.NODE, "1"))));

        assertThat(scope.get(new ContextKey("node", "netbios-domain")), is(Optional.empty()));
        assertThat(scope.get(new ContextKey("node", "netbios-name")), is(Optional.empty()));

        assertThat(scope.get(new ContextKey("node", "os")), is(Optional.empty()));

        assertThat(scope.get(new ContextKey("node", "sys-name")), is(Optional.empty()));
        assertThat(scope.get(new ContextKey("node", "sys-location")), is(Optional.empty()));
        assertThat(scope.get(new ContextKey("node", "sys-contact")), is(Optional.empty()));
        assertThat(scope.get(new ContextKey("node", "sys-description")), is(Optional.empty()));

        assertThat(scope.get(new ContextKey("node", "location")), is(Optional.of(new Scope.ScopeValue(Scope.ScopeName.NODE, "Default"))));
        assertThat(scope.get(new ContextKey("node", "area")), is(Optional.of(new Scope.ScopeValue(Scope.ScopeName.NODE, "Default"))));
    }

    @Test
    public final void testNodeGeohash() throws Exception {
        final Scope scope = this.provider.getScopeForNode(this.populator.getNode1().getId());

        assertThat(scope.get(new ContextKey("node", "label")), is(Optional.of(new Scope.ScopeValue(Scope.ScopeName.NODE, "node1"))));
        // Geohash should be empty since the database populater does not set lat/lon by default
        assertThat(scope.get(new ContextKey("node", "geohash")), is(Optional.empty()));

        // Set a lat/lon
        sessionUtils.withTransaction(() -> {
            OnmsGeolocation geolocation = new OnmsGeolocation();
            geolocation.setLatitude(45.484107d);
            geolocation.setLongitude(-73.629706d);
            populator.getNode1().getAssetRecord().setGeolocation(geolocation);
            populator.getNodeDao().saveOrUpdate(populator.getNode1());
        });

        // Verify
        assertThat(scope.get(new ContextKey("node", "geohash")), is(Optional.of(new Scope.ScopeValue(Scope.ScopeName.NODE, "f25du80kpkpf"))));
    }


    @Test
    public final void testInterface() throws Exception {
        final Scope scope = this.provider.getScopeForInterface(this.populator.getNode1().getId(), "192.168.1.1");

        assertThat(scope.get(new ContextKey("interface", "hostname")), is(Optional.empty()));
        assertThat(scope.get(new ContextKey("interface", "address")), is(Optional.of(new Scope.ScopeValue(Scope.ScopeName.INTERFACE, "192.168.1.1"))));
        assertThat(scope.get(new ContextKey("interface", "netmask")), is(Optional.empty()));
        assertThat(scope.get(new ContextKey("interface", "if-index")), is(Optional.of(new Scope.ScopeValue(Scope.ScopeName.INTERFACE, "1"))));
        assertThat(scope.get(new ContextKey("interface", "if-alias")), is(Optional.of(new Scope.ScopeValue(Scope.ScopeName.INTERFACE, "Initial ifAlias value"))));
        assertThat(scope.get(new ContextKey("interface", "if-description")), is(Optional.of(new Scope.ScopeValue(Scope.ScopeName.INTERFACE, "ATM0"))));
        assertThat(scope.get(new ContextKey("interface", "phy-addr")), is(Optional.of(new Scope.ScopeValue(Scope.ScopeName.INTERFACE, "34E45604BB69"))));
    }

    @Test
    public final void testService() throws Exception {
        final Scope scope = this.provider.getScopeForService(this.populator.getNode1().getId(), InetAddressUtils.getInetAddress("192.168.1.1"), "ICMP");

        assertThat(scope.get(new ContextKey("service", "name")), is(Optional.of(new Scope.ScopeValue(Scope.ScopeName.SERVICE, "ICMP"))));
    }

    @Test
    @Transactional
    public void testScvInterpolation() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("username", "${requisition:dcb:username}");
        attributes.put("password", "${requisition:dcb:password}");

        OnmsNode node = this.populator.getNode1();
        OnmsMetaData metaData1 = new OnmsMetaData("requisition", "dcb:username", "${scv:alias:username}");
        OnmsMetaData metaData2 = new OnmsMetaData("requisition", "dcb:password", "${scv:alias:password}");
        node.getMetaData().add(metaData1);
        node.getMetaData().add(metaData2);
        this.populator.getNodeDao().saveOrUpdate(node);

        this.secureCredentialsVault.setCredentials("alias", new Credentials("horizon", "OpenNMS@30"));

        Map<String, Object> interpolatedAttributes = Interpolator.interpolateObjects(attributes, this.provider.getScopeForNode(this.populator.getNode1().getId()));

        Assert.assertEquals(interpolatedAttributes.get("username"), "horizon");
        Assert.assertEquals(interpolatedAttributes.get("password"), "OpenNMS@30");

    }
}
