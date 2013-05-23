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

import java.util.Comparator;

import org.opennms.core.utils.InetAddressComparator;
import org.opennms.netmgt.model.OnmsNode;

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
					return -1;
				}
			} else {
				if (o2 == null) {
					return 1;
				} else {
					if (o1.getPrimaryInterface() == null) {
						if (o2.getPrimaryInterface() == null) {
							return 0;
						} else {
							return -1;
						}
					} else {
						if (o2.getPrimaryInterface() == null) {
							return 1;
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
					} else if (nodeContainer.additionalSorting.size() == 1) {
						Comparator<OnmsNode> comparator = nodeContainer.additionalSorting.get(0);
						if (comparator instanceof PrimaryInterfaceAddressComparator) {
							nodeContainer.additionalSorting.set(0, new ReverseComparator<OnmsNode>(comparator));
						} else {
							nodeContainer.additionalSorting.set(0, new PrimaryInterfaceAddressComparator());
						}
					} else {
						// Unexpected number of comparators in the list...
						nodeContainer.additionalSorting.clear();
						nodeContainer.additionalSorting.add(new PrimaryInterfaceAddressComparator());
					}
					refreshRowCache();
				} else {
					nodeContainer.additionalSorting.clear();
					// We need to refresh the rows here too because the sorting for the other columns seems to be applied
					// before this listener is fired.
					refreshRowCache();
				}
			}
		});
	}
}
