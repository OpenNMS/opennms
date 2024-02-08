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
package org.opennms.features.topology.app.internal;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlTransient;

import org.opennms.features.topology.api.topo.AbstractVertex;

public class TestVertex extends AbstractVertex {

	List<TestEdge> m_edges = new ArrayList<>();

	public TestVertex(String id) {
		this(id, id);
	}

	public TestVertex(String id, String label) {
		super("test", id, label);
	}

	public TestVertex(String id, int x, int y) {
		this(id, "no-label");
		setX(x);
		setY(y);
	}

	@XmlTransient
	public List<TestEdge> getEdges() {
		return m_edges;
	}

	void addEdge(TestEdge edge) {
		m_edges.add(edge);
	}

	void removeEdge(TestEdge edge) {
		m_edges.remove(edge);
	}
}
