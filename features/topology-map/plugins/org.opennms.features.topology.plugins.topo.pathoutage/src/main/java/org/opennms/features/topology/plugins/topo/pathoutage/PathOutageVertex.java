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

import org.opennms.features.topology.api.topo.AbstractLevelAwareVertex;
import org.opennms.features.topology.api.topo.LevelAware;
import org.opennms.netmgt.model.OnmsNode;

/**
 * Vertex class for the {@link PathOutageProvider} object
 */
class PathOutageVertex extends AbstractLevelAwareVertex implements LevelAware {

	private int level;

	public PathOutageVertex(OnmsNode node) {
		super(PathOutageProvider.NAMESPACE, String.valueOf(node.getId()), node.getLabel());
		setNodeID(node.getId());
		this.level = 0;
		this.setIconKey("pathoutage.default");
	}

	@Override
	public int getLevel() {
		return this.level;
	}

	public void setLevel(int level) {
		this.level = level;
	}
}