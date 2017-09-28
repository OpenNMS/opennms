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

package org.opennms.features.vaadin.jmxconfiggenerator.data;

import com.vaadin.data.Item;

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
