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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.opennms.features.topology.api.Point;
import org.opennms.features.topology.api.topo.DefaultVertexRef;
import org.opennms.features.topology.api.topo.VertexRef;

/**
 */
public class VertexRefPointMapAdapter extends XmlAdapter<VertexRefPointMapAdapter.VertexRefPointMap, Map<VertexRef,Point>> {

	public static final class VertexRefPointMap {
		public List<VertexRefPointEntry> entry = new ArrayList<VertexRefPointEntry>(0);
	}

	public static final class VertexRefPointEntry {
		public VertexRefKey key;
		public PointValue value;
	}

	public static final class VertexRefKey {
		@XmlAttribute
		public String namespace;
		@XmlAttribute
		public String id;
		@XmlAttribute
		public String label;
	}

	public static final class PointValue {
		@XmlAttribute
		public int x;
		@XmlAttribute
		public int y;
	}

	@Override
	public VertexRefPointMapAdapter.VertexRefPointMap marshal(Map<VertexRef,Point> v) throws Exception {
		if (v == null) {
			return null;
		} else {
			VertexRefPointMap retval = new VertexRefPointMap();
			for (VertexRef key : v.keySet()) {
				VertexRefPointEntry entry = new VertexRefPointEntry();
				VertexRefKey newKey = new VertexRefKey();
				newKey.namespace = key.getNamespace();
				newKey.id = key.getId();
				newKey.label = key.getLabel();
				Point value = v.get(key);
				PointValue newValue = new PointValue();
				newValue.x = (int)value.getX(); // TODO cast to int for now
				newValue.y = (int)value.getY(); //TODO cast to int for now
				entry.key = newKey;
				entry.value = newValue;
				retval.entry.add(entry);
			}
			return retval;
		}
	}

	@Override
	public Map<VertexRef,Point> unmarshal(VertexRefPointMapAdapter.VertexRefPointMap v) throws Exception {
		if (v == null) {
			return null;
		} else {
			Map<VertexRef, Point> retval = new HashMap<VertexRef, Point>();
			for (VertexRefPointEntry entry : v.entry) {
				VertexRef ref = new DefaultVertexRef(entry.key.namespace, entry.key.id, entry.key.label);
				Point point = new Point(entry.value.x, entry.value.y);
				retval.put(ref, point);
			}
			return retval;
		}
	}
}