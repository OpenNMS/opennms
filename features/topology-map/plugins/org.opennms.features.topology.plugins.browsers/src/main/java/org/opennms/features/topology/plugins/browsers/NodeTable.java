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

	@SuppressWarnings("unchecked") // Because Aries Blueprint cannot handle generics
	public NodeTable(String caption, OnmsDaoContainer container) {
		super(caption, container);

		addListener(new HeaderClickListener() {
			public void headerClick(HeaderClickEvent event) {
				String column = (String)event.getPropertyId();
				if ("primaryInterface".equals(column)) {
					((NodeDaoContainer)getContainerDataSource()).additionalSorting.add(new Comparator<OnmsNode>() {
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
					});
					refreshRowCache();
				} else {
					((NodeDaoContainer)getContainerDataSource()).additionalSorting.clear();
				}
			}
		});
	}
}
