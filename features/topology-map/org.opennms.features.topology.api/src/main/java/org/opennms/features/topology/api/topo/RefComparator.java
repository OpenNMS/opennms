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

package org.opennms.features.topology.api.topo;

import java.util.Comparator;

/**
 * This comparator only cares about the tuple of namespace and id.
 */
public class RefComparator implements Comparator<Ref> {

	@Override
	public int compare(Ref a, Ref b) {
		if (a == null) {
			if (b == null) {
				return 0;
			} else {
				return 1;
			}
		} else if (b == null) {
			return -1;
		} else {
			if (a.getNamespace() == null) {
				if (b.getNamespace() == null) {
					if (a.getId() == null) {
						if (b.getId() == null) {
							return 0;
						} else {
							return 1;
						}
					} else if (b.getId() == null) {
						return -1;
					} else {
						return a.getId().compareTo(b.getId());
					}
				} else {
					return 1;
				}
			} else if (b.getNamespace() == null) {
				return -1;
			} else {
				int comparison = a.getNamespace().compareTo(b.getNamespace());
				if (comparison == 0) {
					if (a.getId() == null) {
						if (b.getId() == null) {
							return 0;
						} else {
							return 1;
						}
					} else if (b.getId() == null) {
						return -1;
					} else {
						return a.getId().compareTo(b.getId());
					}
				} else {
					return comparison;
				}
			}
		}
	}

}