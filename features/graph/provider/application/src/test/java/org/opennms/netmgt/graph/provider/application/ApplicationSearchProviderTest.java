/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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
