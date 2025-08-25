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
package org.opennms.netmgt.config;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.opennms.test.DaoTestConfigBean;
import org.opennms.netmgt.config.menu.MainMenu;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MenuTemplateFactoryIT {
    private MenuTemplateFactory factory;

    @Before
    public void setUp() throws Exception {
        DaoTestConfigBean daoTestConfig = new DaoTestConfigBean();
        daoTestConfig.setRelativeHomeDirectory("src/test/resources");
        daoTestConfig.afterPropertiesSet();

        factory = new MenuTemplateFactory();
    }

    @Test
    public void testGetMainMenu() throws IOException {
        MainMenu mainMenu = factory.cloneMainMenu();

        assertNotNull(mainMenu);
        assertEquals("default", mainMenu.templateName);
        assertNotNull(mainMenu.userTileProviders);
        assertEquals(1, mainMenu.userTileProviders.size());

        final String[] expectedMenuNames = new String[] {
            "Dashboards", "Inventory", "Monitoring", "Maps", "Topologies", "Metrics (Resource Graphs)",
            "Distributed Monitoring", "Manage Inventory", "User Management", "Integrations",
            "Tools", "Administration", "Internal Logs", "User Profile", "API Documentation",
            "Help", "Support"
        };

        // Check top level menus and names
        // Note, two top level items are separators which have no name
        assertNotNull(mainMenu.menus);
        assertEquals(expectedMenuNames.length + 2, mainMenu.menus.size());

        List<String> menuNames = mainMenu.menus.stream().filter(m -> m.name != null).map(m -> m.name).toList();
        assertThat(menuNames, containsInAnyOrder(expectedMenuNames));

        long separatorSize = mainMenu.menus.stream().filter(m -> m.type != null && m.type.equals("separator")).count();
        assertEquals(2, separatorSize);
    }
}
