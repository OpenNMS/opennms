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
package org.opennms.features.topology.plugins.topo.asset.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class OrFilter<T> implements Filter<T> {
	private final List<Filter<T>> orFilters;

	public OrFilter(Filter<T>... filters) {
		this(filters == null ? new ArrayList<>() : Arrays.asList(filters));
	}

	public OrFilter(List<Filter<T>> orFilters){
		this.orFilters= Objects.requireNonNull(orFilters);
	}

	@Override
	public boolean apply(T value) {
		for(Filter<T> f : orFilters){
			if(f.apply(value)) {
				return true;
			}
		}
		return false;
	}
}
