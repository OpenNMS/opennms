/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.topo.pathoutage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.topo.SearchQuery;
import org.opennms.features.topology.api.topo.SimpleSearchProvider;
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
		this.pathOutageProvider.getVertices(null).stream()
				.filter(v -> matches((PathOutageVertex) v, queryString))
				.forEach(v -> results.add((PathOutageVertex) v));
		return results;
	}

	private static boolean matches(PathOutageVertex vertex, String searchQuery) {
		return vertex.getLabel().contains(searchQuery);
	}
}
