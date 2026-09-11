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
package org.opennms.netmgt.availability;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.opennms.netmgt.config.categories.Category;
import org.opennms.netmgt.config.categories.CategoryGroup;
import org.opennms.netmgt.config.categories.Catinfo;

public class CategoriesXmlDefinitionProviderTest {

    /**
     * The categories model rejects a category without a rule while parsing,
     * so only duplicate labels can reach the provider from a real file.
     */
    @Test
    public void duplicateLabelsAreSkipped() {
        final CategoryGroup web = new CategoryGroup("WebConsole");
        web.setCommonRule("IPADDR != '0.0.0.0'");
        web.addCategory(new Category("Web Servers", null, 99.99, 97.0, "isHTTP", "HTTP", "HTTPS"));

        final CategoryGroup other = new CategoryGroup("Other");
        other.addCategory(new Category("Web Servers", null, 50.0, 40.0, "isHTTPS"));
        other.addCategory(new Category("Mail", null, 99.0, 90.0, "isSMTP"));

        final Catinfo config = new Catinfo();
        config.setCategoryGroups(Arrays.asList(web, other));

        final List<CategoryDefinition> definitions = CategoriesXmlDefinitionProvider.toDefinitions(config);
        assertEquals(2, definitions.size());

        final CategoryDefinition first = definitions.get(0);
        assertEquals("Web Servers", first.getLabel());
        assertEquals("(IPADDR != '0.0.0.0') & (isHTTP)", first.getRule());
        assertEquals(Arrays.asList("HTTP", "HTTPS"), first.getServices());
        assertEquals(Double.valueOf(97.0), first.getWarningThreshold());

        final CategoryDefinition second = definitions.get(1);
        assertEquals("Mail", second.getLabel());
        assertEquals("isSMTP", second.getRule());
    }
}
