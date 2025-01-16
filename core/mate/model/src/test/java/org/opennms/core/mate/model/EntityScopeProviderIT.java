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
package org.opennms.core.mate.model;

import static org.hamcrest.MatcherAssert.assertThat;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.mate.api.ContextKey;
import org.opennms.core.mate.api.EntityScopeProvider;
import org.opennms.core.mate.api.Interpolator;
import org.opennms.core.mate.api.Scope;
import org.opennms.core.mate.api.ScopeProvider;
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

import com.google.common.collect.Lists;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/applicationContext-entity-scope-provider.xml",
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

        assertThat(scope.get(new ContextKey("node", "label")), Matchers.is(Optional.of(new Scope.ScopeValue(Scope.ScopeName.NODE, "node1"))));

        assertThat(scope.get(new ContextKey("node", "foreign-source")), Matchers.is(Optional.of(new Scope.ScopeValue(Scope.ScopeName.NODE, "imported:"))));
        assertThat(scope.get(new ContextKey("node", "foreign-id")), Matchers.is(Optional.of(new Scope.ScopeValue(Scope.ScopeName.NODE, "1"))));

        assertThat(scope.get(new ContextKey("node", "netbios-domain")), Matchers.is(Optional.empty()));
        assertThat(scope.get(new ContextKey("node", "netbios-name")), Matchers.is(Optional.empty()));

        assertThat(scope.get(new ContextKey("node", "os")), Matchers.is(Optional.empty()));

        assertThat(scope.get(new ContextKey("node", "sys-name")), Matchers.is(Optional.empty()));
        assertThat(scope.get(new ContextKey("node", "sys-location")), Matchers.is(Optional.empty()));
        assertThat(scope.get(new ContextKey("node", "sys-contact")), Matchers.is(Optional.empty()));
        assertThat(scope.get(new ContextKey("node", "sys-description")), Matchers.is(Optional.empty()));

        assertThat(scope.get(new ContextKey("node", "location")), Matchers.is(Optional.of(new Scope.ScopeValue(Scope.ScopeName.NODE, "Default"))));
        assertThat(scope.get(new ContextKey("node", "area")), Matchers.is(Optional.of(new Scope.ScopeValue(Scope.ScopeName.NODE, "Default"))));
    }

    @Test
    public final void testNodeGeohash() throws Exception {
        final Scope scope = this.provider.getScopeForNode(this.populator.getNode1().getId());

        assertThat(scope.get(new ContextKey("node", "label")), Matchers.is(Optional.of(new Scope.ScopeValue(Scope.ScopeName.NODE, "node1"))));
        // Geohash should be empty since the database populater does not set lat/lon by default
        assertThat(scope.get(new ContextKey("node", "geohash")), Matchers.is(Optional.empty()));

        // Set a lat/lon
        sessionUtils.withTransaction(() -> {
            OnmsGeolocation geolocation = new OnmsGeolocation();
            geolocation.setLatitude(45.484107d);
            geolocation.setLongitude(-73.629706d);
            populator.getNode1().getAssetRecord().setGeolocation(geolocation);
            populator.getNodeDao().saveOrUpdate(populator.getNode1());
        });

        // Verify
        assertThat(scope.get(new ContextKey("node", "geohash")), Matchers.is(Optional.of(new Scope.ScopeValue(Scope.ScopeName.NODE, "f25du80kpkpf"))));
    }


    @Test
    public final void testInterface() throws Exception {
        final Scope scope = this.provider.getScopeForInterface(this.populator.getNode1().getId(), "192.168.1.1");

        assertThat(scope.get(new ContextKey("interface", "hostname")), Matchers.is(Optional.empty()));
        assertThat(scope.get(new ContextKey("interface", "address")), Matchers.is(Optional.of(new Scope.ScopeValue(Scope.ScopeName.INTERFACE, "192.168.1.1"))));
        assertThat(scope.get(new ContextKey("interface", "netmask")), Matchers.is(Optional.empty()));
        assertThat(scope.get(new ContextKey("interface", "if-index")), Matchers.is(Optional.of(new Scope.ScopeValue(Scope.ScopeName.INTERFACE, "1"))));
        assertThat(scope.get(new ContextKey("interface", "if-alias")), Matchers.is(Optional.of(new Scope.ScopeValue(Scope.ScopeName.INTERFACE, "Initial ifAlias value"))));
        assertThat(scope.get(new ContextKey("interface", "if-description")), Matchers.is(Optional.of(new Scope.ScopeValue(Scope.ScopeName.INTERFACE, "ATM0"))));
        assertThat(scope.get(new ContextKey("interface", "if-name")), Matchers.is(Optional.of(new Scope.ScopeValue(Scope.ScopeName.INTERFACE, "atm0"))));
        assertThat(scope.get(new ContextKey("interface", "phy-addr")), Matchers.is(Optional.of(new Scope.ScopeValue(Scope.ScopeName.INTERFACE, "34E45604BB69"))));
    }

    @Test
    public final void testService() throws Exception {
        final Scope scope = this.provider.getScopeForService(this.populator.getNode1().getId(), InetAddressUtils.getInetAddress("192.168.1.1"), "ICMP");

        assertThat(scope.get(new ContextKey("service", "name")), Matchers.is(Optional.of(new Scope.ScopeValue(Scope.ScopeName.SERVICE, "ICMP"))));
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

    @Test
    public final void testScopeProviders() {
        // set meta-data of node
        final OnmsNode node = this.populator.getNode1();
        OnmsMetaData metaData = new OnmsMetaData("context", "key", "value1");
        node.getMetaData().add(metaData);
        this.populator.getNodeDao().saveOrUpdate(node);

        // get an scope provider
        final ScopeProvider scope = this.provider.getScopeProviderForNode(this.populator.getNode1().getId());

        // this will retrieve the meta-data set before
        assertThat(scope.getScope().get(new ContextKey("context", "key")), Matchers.is(Optional.of(new Scope.ScopeValue(Scope.ScopeName.NODE, "value1"))));

        // now update the meta-data
        node.getMetaData().removeAll(Lists.newArrayList(metaData));
        metaData = new OnmsMetaData("context", "key", "value2");
        node.getMetaData().add(metaData);
        this.populator.getNodeDao().saveOrUpdate(node);

        // the provider will again retrieve the current meta-data
        assertThat(scope.getScope().get(new ContextKey("context", "key")), Matchers.is(Optional.of(new Scope.ScopeValue(Scope.ScopeName.NODE, "value2"))));
    }
}
