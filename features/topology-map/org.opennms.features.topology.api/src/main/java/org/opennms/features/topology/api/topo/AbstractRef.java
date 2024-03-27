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
package org.opennms.features.topology.api.topo;

// TODO rename class. It is not abstract it is instantiable
public class AbstractRef implements Ref {
	
	private final String m_namespace;
	protected String m_id;
	private String m_label;
	
	protected AbstractRef(String namespace, String id, String label) {
		m_namespace = namespace;
		m_id = id;
		m_label = label;
	}
	
	protected AbstractRef(Ref ref) {
		this(ref.getNamespace(), ref.getId(), ref.getLabel());
	}

	@Override
	public final String getId() {
		return m_id;
	}

	@Override
	public final String getNamespace() {
		return m_namespace;
	}

	@Override
	public final String getLabel() {
		return m_label;
	}

	public final void setLabel(String label) {
		m_label = label;
	}

	@Override
	public final int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
		result = prime * result
				+ ((getNamespace() == null) ? 0 : getNamespace().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		
		if (!(obj instanceof Ref)) return false;

		Ref ref = (Ref)obj;
		
		return getNamespace().equals(ref.getNamespace()) && getId().equals(ref.getId());

	}

	@Override
	public int compareTo(Ref o) {
		if (this.equals(o)) {
			return 0;
		} else {
			// Order by namespace, then ID
			if (this.getNamespace().equals(o.getNamespace())) {
				if (this.getId().equals(o.getId())) {
					// Shouldn't happen because equals() should return true
					throw new IllegalStateException("equals() was inaccurate in " + this.getClass().getName());
				} else {
					return this.getId().compareTo(o.getId());
				}
			} else {
				return this.getNamespace().compareTo(o.getNamespace());
			}
		}
	}
}
