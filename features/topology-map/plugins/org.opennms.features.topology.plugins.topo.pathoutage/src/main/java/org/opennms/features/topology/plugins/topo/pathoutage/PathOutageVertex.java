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

import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.features.topology.api.topo.LevelAware;
import org.opennms.netmgt.model.OnmsNode;

/**
 * Vertex class for the {@link PathOutageProvider} object
 */
class PathOutageVertex extends AbstractVertex implements LevelAware {

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