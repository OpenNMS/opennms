/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.browsers;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.opennms.core.criteria.Order;
import org.opennms.core.utils.InetAddressComparator;
import org.opennms.netmgt.model.OnmsNode;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Table;

@SuppressWarnings("serial")
public class NodeTable extends SelectionAwareTable {

	private static class ReverseComparator<T> implements Comparator<T> {
		private final Comparator<T> m_comparator;

		public ReverseComparator(Comparator<T> comparator) {
			m_comparator = comparator;
		}

		@Override
		public int compare(T o1, T o2) {
			int comparison = m_comparator.compare(o1, o2);
			if (comparison == 0) {
				return 0;
			} else if (comparison < 0) {
				return 1;
			} else {
				return -1;
			}
		}

	}

	private static class PrimaryInterfaceAddressComparator implements Comparator<OnmsNode> {
		@Override
		public int compare(OnmsNode o1, OnmsNode o2) {
			if (o1 == null) {
				if (o2 == null) {
					return 0;
				} else {
					return 1;
				}
			} else {
				if (o2 == null) {
					return -1;
				} else {
					if (o1.getPrimaryInterface() == null) {
						if (o2.getPrimaryInterface() == null) {
							return 0;
						} else {
							return 1;
						}
					} else {
						if (o2.getPrimaryInterface() == null) {
							return -1;
						} else {
							return new InetAddressComparator().compare(o1.getPrimaryInterface().getIpAddress(), o2.getPrimaryInterface().getIpAddress());
						}
					}
				}
			}
		}
	}

	@SuppressWarnings("unchecked") // Because Aries Blueprint cannot handle generics
	public NodeTable(String caption, OnmsDaoContainer container) {
		super(caption, container);

		addListener(new HeaderClickListener() {
			public void headerClick(HeaderClickEvent event) {
				NodeDaoContainer nodeContainer = (NodeDaoContainer)getContainerDataSource();
				String column = (String)event.getPropertyId();
				if ("primaryInterface".equals(column)) {
					if (nodeContainer.additionalSorting.size() == 0) {
						nodeContainer.additionalSorting.add(new PrimaryInterfaceAddressComparator());
						setTableSortContainerPropertyId(NodeTable.this, "primaryInterface", true);
					} else if (nodeContainer.additionalSorting.size() == 1) {
						Comparator<OnmsNode> comparator = nodeContainer.additionalSorting.get(0);
						if (comparator instanceof PrimaryInterfaceAddressComparator) {
							nodeContainer.additionalSorting.set(0, new ReverseComparator<OnmsNode>(comparator));
							setTableSortContainerPropertyId(NodeTable.this, "primaryInterface", false);
						} else {
							nodeContainer.additionalSorting.set(0, new PrimaryInterfaceAddressComparator());
							setTableSortContainerPropertyId(NodeTable.this, "primaryInterface", true);
						}
					} else {
						// Unexpected number of comparators in the list...
						nodeContainer.additionalSorting.clear();
						nodeContainer.additionalSorting.add(new PrimaryInterfaceAddressComparator());
						setTableSortContainerPropertyId(NodeTable.this, "primaryInterface", true);
					}
				} else {
					nodeContainer.additionalSorting.clear();
				}

				// We need to refresh the rows even if we are clearing the additionalSorting because 
				// the sorting for the other columns seems to be applied before this listener is fired.
				refreshRowCache();
			}
		});
	}

	private static void setTableSortContainerPropertyId(Table table, String propertyId, boolean ascending) {
		// ARGH This method should probably be protected but it is private... so I have 
		// to invoke it via reflection. :/
		try {
			Method method = Table.class.getDeclaredMethod("setSortContainerPropertyId", Object.class, boolean.class);
			method.setAccessible(true);
			method.invoke(table, "primaryInterface", false);

			method = Table.class.getDeclaredMethod("setSortAscending", boolean.class, boolean.class);
			method.setAccessible(true);
			method.invoke(table, ascending, false);
		} catch (Throwable e) {
			LoggerFactory.getLogger(table.getClass()).error("Reflection call failed inside NodeTable.setTableSortContainerPropertyId()", e);
		}
	}

	@Override
	public void sort(Object[] propertyId, boolean[] ascending) {
		if (propertyId.length > ascending.length) {
			throw new IllegalArgumentException("Property list and ascending list are different sizes");
		}

		// Remove "primaryInterface" from the list of sortable properties and rely on the 
		// HeaderClickListener to perform the sorting.
		List<Object> newIds = new ArrayList<Object>();
		List<Boolean> newAsc = new ArrayList<Boolean>();
		for(int i = 0; i < propertyId.length; i++) {
			if (!"primaryInterface".equals(propertyId[i])) {
				newIds.add(propertyId[i]);
				newAsc.add(ascending[i]);
			}
		}
		super.sort(newIds.toArray(new Object[0]), ArrayUtils.toPrimitive(newAsc.toArray(new Boolean[0])));
	}
}
