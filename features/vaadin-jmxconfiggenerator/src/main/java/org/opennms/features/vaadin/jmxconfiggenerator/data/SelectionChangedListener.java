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
package org.opennms.features.vaadin.jmxconfiggenerator.data;

import com.vaadin.v7.data.Item;

/**
 * Indicates if the selection in the MBeans tree has changed.
 * 
 * @author Markus von Rüden
 */
public interface SelectionChangedListener<T> {

	/**
	 * The event object used to provide information about the selection change.
	 * @param <T> The type of the selected bean.
	 */
	class SelectionChangedEvent<T> {

		private final Item selectedItem;
		private final String selectedItemId;
		private final T selectedBean;

		public SelectionChangedEvent(Item selectedItem, String selectedItemId, T selectedBean) {
			this.selectedItem = selectedItem;
			this.selectedItemId = selectedItemId;
			this.selectedBean = selectedBean;
		}

		public Item getSelectedItem()  {
			return selectedItem;
		}

		public T getSelectedBean() {
			return selectedBean;
		}

		public String getSelectedItemId() {
			return selectedItemId;
		}

	}


	/**
	 * Is invoked after the selection changed.
	 *
	 * @param changeEvent the change Event
	 */
	void selectionChanged(SelectionChangedEvent<T> changeEvent);
}
