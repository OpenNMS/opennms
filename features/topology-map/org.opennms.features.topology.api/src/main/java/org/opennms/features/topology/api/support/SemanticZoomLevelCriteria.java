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

import org.opennms.features.topology.api.topo.Criteria;

public class SemanticZoomLevelCriteria extends Criteria {
	private int m_szl;

	public SemanticZoomLevelCriteria(int szl) {
		m_szl = szl;
	}

	@Override
	public int hashCode() {
		return getNamespace().hashCode() * 31 + m_szl;
	}

	@Override
	public boolean equals(Object o) {
	    if (o == null) return false;
	    if (!(o instanceof SemanticZoomLevelCriteria)) return false;
		try {
			SemanticZoomLevelCriteria other = (SemanticZoomLevelCriteria)o;
			return getNamespace().equals(other.getNamespace()) && (getSemanticZoomLevel() == other.getSemanticZoomLevel());
		} catch (ClassCastException e) {
			return false;
		}
	}

	public int getSemanticZoomLevel() {
		return m_szl;
	}

	public void setSemanticZoomLevel(int szl) {
		m_szl = szl;
        setDirty(true);
	}

	@Override
	public ElementType getType() {
		return ElementType.VERTEX;
	}

	/**
	 * TODO This isn't really accurate...
	 */
	@Override
	public String getNamespace() {
		return "nodes";
	}
}
