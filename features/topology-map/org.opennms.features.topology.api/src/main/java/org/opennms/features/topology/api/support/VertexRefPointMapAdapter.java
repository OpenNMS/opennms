/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.api.support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.opennms.features.topology.api.Point;
import org.opennms.features.topology.api.topo.AbstractVertexRef;
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
				newValue.x = value.getX();
				newValue.y = value.getY();
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
				VertexRef ref = new AbstractVertexRef(entry.key.namespace, entry.key.id, entry.key.label);
				Point point = new Point(entry.value.x, entry.value.y);
				retval.put(ref, point);
			}
			return retval;
		}
	}
}