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
package org.opennms.features.topology.plugins.topo.pathoutage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.topo.SearchQuery;
import org.opennms.features.topology.api.topo.simple.SimpleSearchProvider;
import org.opennms.features.topology.api.topo.VertexRef;

public class PathOutageSearchProvider extends SimpleSearchProvider {

	private final PathOutageProvider pathOutageProvider;

	public PathOutageSearchProvider(PathOutageProvider provider) {
		this.pathOutageProvider = provider;
	}

	@Override
	public String getSearchProviderNamespace() {
		return PathOutageProvider.NAMESPACE;
	}

	@Override
	public List<? extends VertexRef> queryVertices(SearchQuery searchQuery, GraphContainer container) {
		Objects.requireNonNull(searchQuery);
		Objects.requireNonNull(container);

		List<PathOutageVertex> results = new ArrayList<>();

		String queryString = searchQuery.getQueryString();
		pathOutageProvider.getCurrentGraph().getVertices().stream()
				.filter(v -> matches((PathOutageVertex) v, queryString))
				.forEach(v -> results.add((PathOutageVertex) v));
		return results;
	}

	private static boolean matches(PathOutageVertex vertex, String searchQuery) {
		return vertex.getLabel().contains(searchQuery);
	}
}
