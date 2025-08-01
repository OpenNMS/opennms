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
package org.opennms.features.topology.api.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.features.topology.api.BoundingBox;
import org.opennms.features.topology.api.Point;
import org.opennms.features.topology.api.topo.DefaultVertexRef;
import org.opennms.features.topology.api.topo.SearchResult;
import org.opennms.features.topology.api.topo.VertexRef;

public class SavedHistoryTest {
	private static final String searchQuery = "query";

	@Test
	public void testMarshall() {
		Map<String,String> settings = new HashMap<String,String>();
		settings.put("hello", "world");

		VertexRef vert1 = new DefaultVertexRef("nodes", "1");
		VertexRef vert2 = new DefaultVertexRef("nodes", "2", "HasALabel");

		Map<VertexRef,Point> locations = new HashMap<VertexRef,Point>();
		locations.put(vert1, new Point(0, 0));
		locations.put(vert2, new Point(0, 0));

		SavedHistory savedHistory = new SavedHistory(
				0, 
				new BoundingBox(0,0,100,100), 
				locations,
				Collections.singleton(vert2),
				Collections.emptySet(),
				settings,
				Collections.emptyList()
		);
		System.out.print(JaxbUtils.marshal(savedHistory));

		// Specify a focus node
		savedHistory = new SavedHistory(
				0, 
				new BoundingBox(0,0,100,100), 
				locations,
				Collections.singleton(vert2),
				Collections.singleton(vert1),
				settings,
				Collections.emptyList()
		);
		System.out.print(JaxbUtils.marshal(savedHistory));
	}

	/**
	 * This methods tests whether the CRC checksums for {@link SavedHistory} object with {@link SearchResult}s are generated correctly,
	 * namely that they now take into account the search results
	 */
	@Test
	public void verifyCRCIncludesSearchResult() {
		Map<String,String> settings = new HashMap<String,String>();
		settings.put("hello", "world");

		VertexRef vert1 = new DefaultVertexRef("nodes", "1");
		VertexRef vert2 = new DefaultVertexRef("nodes", "2", "HasALabel");

		Map<VertexRef,Point> locations = new HashMap<VertexRef,Point>();
		locations.put(vert1, new Point(0, 0));
		locations.put(vert2, new Point(0, 0));

		// Creating SavedHistory object with no search results
		SavedHistory history1 = new SavedHistory(
				1,
				new BoundingBox(0,0,100,100),
				locations,
				Collections.singleton(vert2),
				Collections.emptySet(),
				settings,
				Collections.emptyList()
		);

		List<SearchResult> searchResults = new ArrayList<>();
		searchResults.add(new SearchResult("alarm", "alarmID", "someLabel", searchQuery, SearchResult.COLLAPSIBLE, !SearchResult.COLLAPSED));

		// Creating SavedHistory object with a single search result with ID = "alarmID"
		SavedHistory history2 = new SavedHistory(
				1,
				new BoundingBox(0,0,100,100),
				locations,
				Collections.singleton(vert2),
				Collections.emptySet(),
				settings,
				searchResults
		);

		// Assertion that the SavedHistory with no search results is different from the one with one
		Assert.assertNotEquals(history1.getFragment(), history2.getFragment());

		// Creating another SavedHistory WITH search result, identical to the 2nd one
		SavedHistory history3 = new SavedHistory(
				1,
				new BoundingBox(0,0,100,100),
				locations,
				Collections.singleton(vert2),
				Collections.emptySet(),
				settings,
				searchResults
		);

		// Assertion that two SavedHistory objects with the same search results are identical
		Assert.assertEquals(history2.getFragment(), history3.getFragment());
	}
}
