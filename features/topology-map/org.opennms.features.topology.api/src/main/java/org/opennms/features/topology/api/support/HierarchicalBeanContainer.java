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

import com.vaadin.v7.data.Container;
import com.vaadin.v7.data.util.BeanContainer;

public abstract class HierarchicalBeanContainer<K, T> extends BeanContainer<K,T> implements Container.Hierarchical {

	private static final long serialVersionUID = 194248426656888195L;

	public HierarchicalBeanContainer(Class<? super T> type) {
		super(type);
	}

	/**
	 * This is a naive implementation of this method that just checks the size of
	 * the collection returned by {@link #getChildren(Object)}.
	 */
	@Override
	public boolean hasChildren(Object key) {
		return getChildren(key).size() > 0;
	}

	/**
	 * This is a naive implementation of this method that just checks to see if
	 * {@link #getParent(Object)} returns null.
	 */
	@Override
	public boolean isRoot(Object key) {
		return (getParent(key) == null);
	}

	/**
	 * Expose {@link #fireItemSetChange()} as a public method.
	 */
        @Override
	public void fireItemSetChange() {
		super.fireItemSetChange();
	}
}
