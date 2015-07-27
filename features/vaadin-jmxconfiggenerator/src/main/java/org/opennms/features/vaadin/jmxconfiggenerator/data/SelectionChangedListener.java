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
 * One part of the observer pattern. It indicates if the given model <code>T</code> has changed.
 * In future releases a ModelChangeEvent may be introduced.
 * 
 * @author Markus von Rüden
 */
public interface SelectionChangedListener<T> {

	class SelectionChangedEvent<T> {

		private final Item selectedItem;
		private T selectedBean;

		public SelectionChangedEvent(Item selectedItem, T selectedBean) {
			this.selectedItem = selectedItem;
			this.selectedBean = selectedBean;
		}

		public Item getSelectedItem()  {
			return selectedItem;
		}

		public T getSelectedBean() {
			return selectedBean;
		}
	}


	/**
	 * Is invoked after a selection changes.
	 * @param changeEvent the change Event
	 */
	void selectionChanged(SelectionChangedEvent<T> changeEvent);
}
