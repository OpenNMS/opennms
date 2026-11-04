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
package org.opennms.netmgt.search.providers.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.opennms.netmgt.search.api.SearchQuery;
import org.opennms.netmgt.search.api.SearchResult;
import org.opennms.netmgt.search.api.SearchResultItem;
import org.opennms.web.api.MenuProvider;
import org.opennms.web.navigate.DefaultMenuEntry;
import org.opennms.web.navigate.DisplayStatus;
import org.opennms.web.navigate.MenuEntry;

public class MenuActionSearchProviderTest {

    private static SearchResult search(String input, MenuEntry... entries) {
        final MenuProvider menuProvider = context -> new ArrayList<>(Arrays.asList(entries));
        final SearchQuery query = new SearchQuery(input);
        query.setMaxResults(SearchQuery.DEFAULT_MAX_RESULT);
        query.setPrincipal(() -> "u1");
        return new MenuActionSearchProvider(menuProvider).query(query);
    }

    @Test
    public void externalEntryCarriesProperties() {
        final SearchResult result = search("chat",
                new DefaultMenuEntry("Chat", "https://chat.opennms.org", DisplayStatus.DISPLAY_LINK, true, "_blank"));
        assertEquals(1, result.getResults().size());
        final SearchResultItem item = result.getResults().get(0);
        assertEquals("Open Chat", item.getLabel());
        assertEquals("https://chat.opennms.org", item.getUrl());
        assertEquals("true", item.getProperties().get("isExternalLink"));
        assertEquals("_blank", item.getProperties().get("linkTarget"));
    }

    @Test
    public void internalEntryHasNoLinkProperties() {
        final SearchResult result = search("alarms",
                new DefaultMenuEntry("Alarms", "alarm/list.htm", DisplayStatus.DISPLAY_LINK));
        assertEquals(1, result.getResults().size());
        final SearchResultItem item = result.getResults().get(0);
        assertFalse(item.getProperties().containsKey("isExternalLink"));
        assertFalse(item.getProperties().containsKey("linkTarget"));
    }

    @Test
    public void flatListOfLeavesIsSearchable() {
        final List<MenuEntry> entries = Arrays.asList(
                new DefaultMenuEntry("Alarms", "alarm/list.htm", DisplayStatus.DISPLAY_LINK),
                new DefaultMenuEntry("Alarm Statistics", "alarm/stats.htm", DisplayStatus.DISPLAY_LINK),
                new DefaultMenuEntry("Nodes", "element/nodeList.htm", DisplayStatus.DISPLAY_LINK));
        final SearchResult result = search("alarm", entries.toArray(new MenuEntry[0]));
        assertEquals(2, result.getResults().size());
    }
}
