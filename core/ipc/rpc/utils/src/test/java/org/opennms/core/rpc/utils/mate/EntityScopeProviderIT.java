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
import static org.hamcrest.Matchers.*;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.opennms.netmgt.dao.DatabasePopulator;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
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
    private DatabasePopulator populator;

    @Autowired
    private EntityScopeProvider provider;

    @Before
    public void setup() {
        this.populator.populateDatabase();
    }

    @Test
    public final void testNode() throws Exception {
        final Scope scope = this.provider.getScopeForNode(this.populator.getNode1().getId());

        assertThat(scope.get(new ContextKey("node", "label")), is(Optional.of("node1")));

        assertThat(scope.get(new ContextKey("node", "foreign-source")), is(Optional.of("imported:")));
        assertThat(scope.get(new ContextKey("node", "foreign-id")), is(Optional.of("1")));

        assertThat(scope.get(new ContextKey("node", "netbios-domain")), is(Optional.empty()));
        assertThat(scope.get(new ContextKey("node", "netbios-name")), is(Optional.empty()));

        assertThat(scope.get(new ContextKey("node", "os")), is(Optional.empty()));

        assertThat(scope.get(new ContextKey("node", "sys-name")), is(Optional.empty()));
        assertThat(scope.get(new ContextKey("node", "sys-location")), is(Optional.empty()));
        assertThat(scope.get(new ContextKey("node", "sys-contact")), is(Optional.empty()));
        assertThat(scope.get(new ContextKey("node", "sys-description")), is(Optional.empty()));

        assertThat(scope.get(new ContextKey("node", "location")), is(Optional.of("Default")));
        assertThat(scope.get(new ContextKey("node", "area")), is(Optional.of("Default")));
    }

    @Test
    public final void testInterface() throws Exception {
        final Scope scope = this.provider.getScopeForInterface(this.populator.getNode1().getId(), "192.168.1.1");

        assertThat(scope.get(new ContextKey("interface", "hostname")), is(Optional.empty()));
        assertThat(scope.get(new ContextKey("interface", "address")), is(Optional.of("192.168.1.1")));
        assertThat(scope.get(new ContextKey("interface", "netmask")), is(Optional.empty()));
        assertThat(scope.get(new ContextKey("interface", "if-index")), is(Optional.of("1")));
        assertThat(scope.get(new ContextKey("interface", "if-alias")), is(Optional.of("Initial ifAlias value")));
        assertThat(scope.get(new ContextKey("interface", "if-description")), is(Optional.of("ATM0")));
        assertThat(scope.get(new ContextKey("interface", "phy-addr")), is(Optional.of("34E45604BB69")));
    }

    @Test
    public final void testService() throws Exception {
        final Scope scope = this.provider.getScopeForService(this.populator.getNode1().getId(), InetAddressUtils.getInetAddress("192.168.1.1"), "ICMP");

        assertThat(scope.get(new ContextKey("service", "name")), is(Optional.of("ICMP")));
    }
}
