/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.app.internal.support;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.features.topology.api.ConfigurableIconRepository;

import com.google.common.collect.Maps;

public class IconRepositoryManagerTest {

    private static class TestIconRepository implements ConfigurableIconRepository {

        private Map<String, String> m_iconMap = Maps.newHashMap();

        @Override
        public boolean contains(String iconKey) {
            return m_iconMap.containsKey(iconKey);
        }

        @Override
        public String getSVGIconId(String iconKey) {
            return m_iconMap.get(iconKey);
        }

        @Override
        public void addIconMapping(String iconKey, String iconId) {
            m_iconMap.put(iconKey, iconId);
        }

        @Override
        public void removeIconMapping(String iconKey) {
            m_iconMap.remove(iconKey);
        }

        @Override
        public void save() {

        }

        private TestIconRepository withIconConfig(String iconKey, String iconId) {
            addIconMapping(iconKey, iconId);
            return this;
        }
    }
    
    @Test
    public void testParseConfig() {
        TestIconRepository iconRepository = new TestIconRepository()
                .withIconConfig("key1", "mx9600_external")
                .withIconConfig("linkd.group", "cloud")
                .withIconConfig("linkd.system", "generic")
                .withIconConfig("linkd.system.snmp.1.3.6.1.4.1.9.1.283", "router")
                .withIconConfig("linkd.system.snmp.1.3.6.1.4.1.9.1.485", "server1")
                .withIconConfig("linkd.system.snmp.1.3.6", "server2");

        IconRepositoryManager iconManager = new IconRepositoryManager();
        iconManager.onBind(iconRepository);

        // Verify direct key
        Assert.assertEquals("mx9600_external", iconManager.getSVGIconId("key1"));
        Assert.assertEquals("cloud", iconManager.getSVGIconId("linkd.group"));
        Assert.assertEquals("generic", iconManager.getSVGIconId("linkd.system"));
        Assert.assertEquals("router", iconManager.getSVGIconId("linkd.system.snmp.1.3.6.1.4.1.9.1.283"));
        Assert.assertEquals("server1", iconManager.getSVGIconId("linkd.system.snmp.1.3.6.1.4.1.9.1.485"));

        // verify path
        Assert.assertEquals("server2", iconManager.getSVGIconId("linkd.system.snmp.1.3.6.X"));
        Assert.assertEquals("generic", iconManager.getSVGIconId("linkd.system.DOES_NOT_EXIST"));
        Assert.assertEquals("server2", iconManager.getSVGIconId("linkd.system.snmp.1.3.6.1.4.1.5813.1.13"));
        Assert.assertEquals("server2", iconManager.getSVGIconId("linkd.system.snmp.1.3.6.1.4.1.9.1"));
        Assert.assertEquals("server2", iconManager.getSVGIconId("linkd.system.snmp.1.3.6.1.7"));
    }

}
