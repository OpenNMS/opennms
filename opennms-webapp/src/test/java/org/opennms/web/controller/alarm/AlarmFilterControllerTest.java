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
package org.opennms.web.controller.alarm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.dao.hibernate.AlarmRepositoryHibernate;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.web.alarm.filter.AlarmTextFilter;
import org.opennms.web.alarm.filter.NodeNameLikeFilter;
import org.opennms.web.alarm.filter.SeverityOrFilter;
import org.opennms.web.alarm.filter.SituationFilter;
import org.opennms.web.filter.Filter;
import org.opennms.web.services.FilterFavoriteService;
import org.opennms.web.tags.filters.AlarmFilterCallback;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.servlet.ModelAndView;

/**
 * Tests for {@link AlarmFilterController} query string handling, focused on
 * verifying that {@link AlarmFilterController#parse(String)} correctly extracts filter
 * parameters and ignores non-filter parameters (limit, sortby, acktype, etc.).
 */
public class AlarmFilterControllerTest {

    private AlarmFilterController controller;
    private AlarmRepositoryHibernate alarmRepository;
    private FilterFavoriteService favoriteService;
    private AlarmFilterCallback filterCallback;

    @Before
    public void setUp() {
        alarmRepository = mock(AlarmRepositoryHibernate.class);
        favoriteService = mock(FilterFavoriteService.class);

        controller = new AlarmFilterController();
        controller.setServletContext(new MockServletContext("file:src/main/webapp"));
        controller.setWebAlarmRepository(alarmRepository);
        controller.setFavoriteService(favoriteService);
        controller.afterPropertiesSet();

        filterCallback = new AlarmFilterCallback(new MockServletContext("file:src/main/webapp"));

        when(alarmRepository.getMatchingAlarms(any())).thenReturn(new OnmsAlarm[0]);
        when(alarmRepository.countMatchingAlarms(any())).thenReturn(0);
        when(favoriteService.getFavorites(any(), any())).thenReturn(new ArrayList<>());
    }

    // -------------------------------------------------------------------------
    // Controller-level tests: verify list() handles query strings without error
    // -------------------------------------------------------------------------

    @Test
    public void testList_nullQueryString() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setQueryString(null);

        ModelAndView mv = controller.list(request, new MockHttpServletResponse());

        assertEquals("alarm/list", mv.getViewName());
    }

    @Test
    public void testList_emptyQueryString() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setQueryString("");

        ModelAndView mv = controller.list(request, new MockHttpServletResponse());

        assertEquals("alarm/list", mv.getViewName());
    }

    @Test
    public void testList_filterParamsOnly() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setQueryString("filter=alarmtext%3Dtest&filter=severity%3D3");

        ModelAndView mv = controller.list(request, new MockHttpServletResponse());

        assertEquals("alarm/list", mv.getViewName());
    }

    @Test
    public void testList_nonFilterParamsOnly() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setQueryString("limit=10&sortby=id&acktype=unacknowledged&display=short&multiple=0");

        ModelAndView mv = controller.list(request, new MockHttpServletResponse());

        assertEquals("alarm/list", mv.getViewName());
    }

    @Test
    public void testList_mixedFilterAndNonFilterParams() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setQueryString("limit=10&sortby=id&acktype=unacknowledged&filter=alarmtext%3Dtest&filter=severity%3D3");

        ModelAndView mv = controller.list(request, new MockHttpServletResponse());

        assertEquals("alarm/list", mv.getViewName());
    }

    @Test
    public void testList_ampEntityEncodedSeparator() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setQueryString("limit=10&amp;sortby=id&amp;filter=alarmtext%3Dtest&amp;filter=severity%3D3");

        ModelAndView mv = controller.list(request, new MockHttpServletResponse());

        assertEquals("alarm/list", mv.getViewName());
    }

    // -------------------------------------------------------------------------
    // Filter parsing tests: verify AlarmFilterCallback.parse
    // correctly extracts filters and ignores non-filter query parameters.
    // This is the exact expression used at AlarmFilterController.list(), line 98.
    // -------------------------------------------------------------------------

    private List<Filter> parseFiltersFromQueryString(String queryString) {
        return filterCallback.parse(queryString == null ? "" : queryString);
    }

    @Test
    public void testFilterParsing_nullQueryString_producesEmptyList() {
        List<Filter> filters = parseFiltersFromQueryString(null);
        assertTrue("Expected no filters for null query string", filters.isEmpty());
    }

    @Test
    public void testFilterParsing_emptyQueryString_producesEmptyList() {
        List<Filter> filters = parseFiltersFromQueryString("");
        assertTrue("Expected no filters for empty query string", filters.isEmpty());
    }

    @Test
    public void testFilterParsing_singleAlarmTextFilter() {
        List<Filter> filters = parseFiltersFromQueryString("filter=alarmtext%3Dtest");

        assertEquals(1, filters.size());
        assertTrue(filters.get(0) instanceof AlarmTextFilter);
    }

    @Test
    public void testFilterParsing_singleSeverityFilter() {
        List<Filter> filters = parseFiltersFromQueryString("filter=severity%3D3");

        assertEquals(1, filters.size());
        assertTrue(filters.get(0) instanceof SeverityOrFilter);
    }

    @Test
    public void testFilterParsing_multipleFilters() {
        List<Filter> filters = parseFiltersFromQueryString(
                "filter=alarmtext%3Dtest&filter=severity%3D3&filter=nodenamelike%3DMyNode");

        assertEquals(3, filters.size());
        assertEquals(1, filters.stream().filter(f -> f instanceof AlarmTextFilter).count());
        assertEquals(1, filters.stream().filter(f -> f instanceof SeverityOrFilter).count());
        assertEquals(1, filters.stream().filter(f -> f instanceof NodeNameLikeFilter).count());
    }

    @Test
    public void testFilterParsing_urlEncodedEqualsInValue() {
        // filter=alarmtext=my test (space encoded as +)
        List<Filter> filters = parseFiltersFromQueryString("filter=alarmtext%3Dmy+test");

        assertEquals(1, filters.size());
        assertTrue(filters.get(0) instanceof AlarmTextFilter);
    }

    @Test
    public void testFilterParsing_ampEntitySeparator_sameAsAmpersand() {
        List<Filter> filtersAmp = parseFiltersFromQueryString(
                "filter=alarmtext%3Dtest&filter=severity%3D3");
        List<Filter> filtersAmpEntity = parseFiltersFromQueryString(
                "filter=alarmtext%3Dtest&amp;filter=severity%3D3");

        assertEquals(filtersAmp.size(), filtersAmpEntity.size());
    }

    @Test
    public void testFilterParsing_duplicateFiltersAreDeduped() {
        List<Filter> filters = parseFiltersFromQueryString(
                "filter=alarmtext%3Dtest&filter=alarmtext%3Dtest");

        assertEquals("Duplicate filter params should be de-duplicated", 1, filters.size());
    }

    @Test
    public void testFilterParsing_commonNonFilterParams_areIgnored() {
        // limit, sortby, acktype, display, multiple, favoriteId have no matching filter TYPE
        // and must not produce any Filter objects.
        List<Filter> filters = parseFiltersFromQueryString(
                "limit=10&sortby=id&acktype=unacknowledged&display=short&multiple=0&favoriteId=123");

        assertTrue("Non-filter query params must not produce filters", filters.isEmpty());
    }

    @Test
    public void testFilterParsing_mixedParams_onlyFilterParamsProduceFilters() {
        // The full query string from the advanced search form includes many non-filter params.
        String queryString = "limit=10&sortby=id&acktype=unacknowledged&display=short&multiple=0"
                + "&filter=alarmtext%3Dtest&filter=severity%3D3&filter=nodenamelike%3DMyNode";

        List<Filter> filters = parseFiltersFromQueryString(queryString);

        assertEquals("Only filter= params should produce Filter objects", 3, filters.size());
    }

    @Test
    public void testFilterParsing_mixedParams_ampEntitySeparated_onlyFilterParamsProduceFilters() {
        // Same as above but using &amp; separators, which is what the alarm advanced search JSP sends.
        String queryString = "limit=10&amp;sortby=id&amp;acktype=unacknowledged"
                + "&amp;filter=alarmtext%3Dtest&amp;filter=severity%3D3&amp;filter=nodenamelike%3DMyNode";

        List<Filter> filters = parseFiltersFromQueryString(queryString);

        assertEquals("Only filter= params should produce Filter objects", 3, filters.size());
    }

    /**
     * Documents a known pre-existing behavior: the query param {@code situation=any} (sent by
     * the advanced search form when "Any" situation is selected) has the same key as
     * {@link SituationFilter#TYPE}, so it is parsed as {@code SituationFilter(false)}.
     *
     * <p>It is noted here so that any future fix to suppress spurious
     * situation/nodelocation filters can be verified by updating this test.</p>
     */
    @Test
    public void testFilterParsing_situationAnyParam_producesSpuriousSituationFilter_knownBehavior() {
        // "situation=any" is a non-filter control param meaning "no situation filter",
        // but its key collides with SituationFilter.TYPE = "situation".
        List<Filter> filters = parseFiltersFromQueryString("situation=any");

        assertEquals("situation=any currently creates a SituationFilter(false) due to key collision", 1, filters.size());
        assertTrue(filters.get(0) instanceof SituationFilter);
    }

    @Test
    public void testFilterParsing_alarmTextWithAmpersand_isNotTruncated() {
        // alarmtext = "ping & dns" → filter=alarmtext%3Dping+%26+dns
        List<Filter> filters = parseFiltersFromQueryString("filter=alarmtext%3Dping+%26+dns");

        assertEquals(1, filters.size());
        assertTrue(filters.get(0) instanceof AlarmTextFilter);
        assertEquals("ping & dns", ((AlarmTextFilter) filters.get(0)).getValue());
    }
}
