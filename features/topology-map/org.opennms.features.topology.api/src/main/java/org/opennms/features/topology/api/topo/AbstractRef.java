/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.api.topo;

public class AbstractRef implements Ref {
	
	private final String m_namespace;
	private final String m_id;
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
