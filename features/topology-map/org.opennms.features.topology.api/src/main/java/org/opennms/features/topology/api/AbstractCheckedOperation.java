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
package org.opennms.features.topology.api;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.opennms.features.topology.api.topo.VertexRef;

public abstract class AbstractCheckedOperation implements CheckedOperation {

	protected boolean enabled(GraphContainer container) {
		return true;
	}

	protected boolean isChecked(GraphContainer container) {
		return false;
	}

	/**
	 * By default, call {@link #enabled(GraphContainer)
	 */
	@Override
	public boolean enabled(List<VertexRef> vertices, OperationContext context) {
		return enabled(context.getGraphContainer());
	}

	/**
	 * By default, call {@link #isChecked(GraphContainer)}
	 */
	@Override
	public boolean isChecked(List<VertexRef> vertices, OperationContext context) {
		return isChecked(context.getGraphContainer());
	}

	/**
	 * By default, save the state based on the checked status of the operation,
	 * independent of any currently-selected vertices.
	 */
	@Override
	public Map<String, String> createHistory(GraphContainer container) {
		return Collections.singletonMap(this.getClass().getName(), Boolean.toString(isChecked(container)));
	}
}
