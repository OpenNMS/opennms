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
