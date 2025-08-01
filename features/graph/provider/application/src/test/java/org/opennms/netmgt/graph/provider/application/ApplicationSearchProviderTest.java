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
package org.opennms.netmgt.graph.provider.application;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.opennms.netmgt.graph.provider.application.TestObjectCreator.createOnmsApplications;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.netmgt.dao.api.ApplicationDao;
import org.opennms.netmgt.graph.api.search.SearchContext;
import org.opennms.netmgt.graph.api.search.SearchSuggestion;
import org.opennms.netmgt.graph.api.service.GraphService;
import org.opennms.netmgt.model.OnmsApplication;

public class ApplicationSearchProviderTest {


    private String providerId = new ApplicationSearchProvider(Mockito.mock(ApplicationDao.class)).getProviderId();

    @Test
    public void shouldReturnEmptyListForEmptySearchResult() {
        assertSuggestions(new ArrayList<>(), "blah", new ArrayList<>());
        assertSuggestions(new ArrayList<>(), "", new ArrayList<>());
    }

    @Test
    public void shouldReturnSuggestionsForValidSearch() {
        List<OnmsApplication> applications = createOnmsApplications(3);
        List<SearchSuggestion> expectations = new ArrayList<>();
        for(OnmsApplication app : applications) {
            SearchSuggestion suggestion = new SearchSuggestion(
                    providerId,
                    OnmsApplication.class.getSimpleName(),
                    Integer.toString(app.getId()),
                    app.getName());
            expectations.add(suggestion);
        }
        assertSuggestions(applications, "blah", expectations);
    }

    private void assertSuggestions(List<OnmsApplication> applications, String input, List<SearchSuggestion> expectations) {
        ApplicationDao dao = Mockito.mock(ApplicationDao.class);
        when(dao.findMatching(any())).thenReturn(applications);
        ApplicationSearchProvider provider = new ApplicationSearchProvider(dao);
        SearchContext context = SearchContext.builder().graphService(Mockito.mock(GraphService.class)).build();
        List<SearchSuggestion> results = provider.getSuggestions(context,
                ApplicationGraph.NAMESPACE, input);
        assertEquals(expectations, results);
    }

}
