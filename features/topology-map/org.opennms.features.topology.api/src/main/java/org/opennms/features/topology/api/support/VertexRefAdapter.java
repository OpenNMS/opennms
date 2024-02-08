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