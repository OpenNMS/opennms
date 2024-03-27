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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.opennms.features.topology.api.BoundingBox;
import org.opennms.features.topology.api.Layout;
import org.opennms.features.topology.api.Point;
import org.opennms.features.topology.api.topo.VertexRef;

public class DefaultLayout implements Layout {
	
	private final Map<VertexRef, Point> m_locations = new HashMap<>();

	@Override
	public Point getLocation(VertexRef v) {
		if (v == null) {
			throw new IllegalArgumentException("Cannot fetch location of null vertex");
		}
		Point p = m_locations.get(v);
		if (p == null) {
			return new Point(0, 0);
		}
		return p;
	}

	@Override
	public Map<VertexRef,Point> getLocations() {
		return Collections.unmodifiableMap(new HashMap<>(m_locations));
	}

	@Override
	public void setLocation(VertexRef v, Point location) {
		if (v == null) {
			throw new IllegalArgumentException("Cannot set location of null vertex");
		}
		m_locations.put(v, location);
	}

	@Override
	public Point getInitialLocation(VertexRef v) {
		if (v == null) {
			throw new IllegalArgumentException("Cannot get initial location of null vertex");
		}
		return getLocation(v);
	}

	@Override
	public void updateLocations(Collection<VertexRef> displayVertices) {
		final Map<VertexRef, Point> collect = m_locations.entrySet().stream()
				.filter(eachEntry -> displayVertices.contains(eachEntry.getKey()))
				.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
		m_locations.clear();
		m_locations.putAll(collect);
	}

	@Override
    public BoundingBox getBounds() {
        if(m_locations.size() > 0) {
            return computeBoundingBox(m_locations.keySet());
        } else {
            BoundingBox bBox = new BoundingBox();
            bBox.addPoint(new Point(0, 0));
            return bBox;
        }
    }
    
    private static BoundingBox computeBoundingBox(Layout layout, VertexRef vertRef) {
        return new BoundingBox(layout.getLocation(vertRef), 100, 100);
    }
    
    @Override
    public BoundingBox computeBoundingBox(Collection<VertexRef> vertRefs) {
        if(vertRefs != null && vertRefs.size() > 0) {
            BoundingBox boundingBox = new BoundingBox();
            for(VertexRef vertRef : vertRefs) {
                boundingBox.addBoundingbox( computeBoundingBox(this, vertRef) );
            }
            return boundingBox;
        } else {
            return getBounds();
        }
    }
}
