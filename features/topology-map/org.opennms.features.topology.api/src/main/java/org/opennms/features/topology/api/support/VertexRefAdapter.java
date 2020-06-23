/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.api.support;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.opennms.features.topology.api.topo.DefaultVertexRef;
import org.opennms.features.topology.api.topo.VertexRef;

/**
 * This {@link XmlAdapter} allows to marshal the by default not marshable {@link VertexRef}.
 *
 * @author mvrueden
 */
public class VertexRefAdapter extends XmlAdapter<VertexRefAdapter.VertexRefJaxbEntry, VertexRef> {

	@XmlRootElement(name="vertex")
	public static final class VertexRefJaxbEntry {
		@XmlAttribute
		public String namespace;
		@XmlAttribute
		public String id;
		@XmlAttribute
		public String label;
	}

	@Override
	public VertexRefJaxbEntry marshal(VertexRef input) throws Exception {
		if(input == null) {
			return null;
		}
		final VertexRefJaxbEntry entry = new VertexRefJaxbEntry();
		entry.namespace = input.getNamespace();
		entry.id = input.getId();
		entry.label = input.getLabel();
		return entry;
	}

	@Override
	public VertexRef unmarshal(VertexRefJaxbEntry input) throws Exception {
		if (input == null) {
			return null;
		}
		final VertexRef ref = new DefaultVertexRef(input.namespace, input.id, input.label);
		return ref;
	}
}