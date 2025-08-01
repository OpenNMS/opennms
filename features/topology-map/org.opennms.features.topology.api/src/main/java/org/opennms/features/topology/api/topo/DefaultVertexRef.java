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

/**
 * 
 * @author <a href="mailto:mvr@opennms.org>Markus von Ruden</a>
 * @author <a href="mailto:seth@opennms.org">Seth Leger</a>
 * @author <a href="mailto:thedesloge@opennms.org">Donald Desloge</a>
 *
 */
public class DefaultVertexRef extends AbstractRef implements VertexRef {

	public DefaultVertexRef(VertexRef ref) {
		super(ref);
	}

	/**
	 * Just a note here, it is important that the id parameter is set to the node id if the
	 * namespace is "nodes". (david@opennms.org)
	 * 
	 * @param namespace
	 * @param id
	 * @param label
	 */
	public DefaultVertexRef(String namespace, String id, String label) {
		super(namespace, id, label);
	}

	/**
	 * @deprecated Specify a useful label for the object
	 */
	public DefaultVertexRef(String namespace, String id) {
		super(namespace, id, namespace + ":" + id);
	}

	@Override
	public final boolean equals(Object obj) {
		if (obj instanceof VertexRef) {
			return super.equals(obj);
		}
		return false;
	}
	
	@Override
	public String toString() { return "VertexRef:"+getNamespace()+":"+getId(); } 

}
