/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.util.spikehunter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NextDataReplacer implements DataReplacer {

	public double[] replaceValues(double[] values, List<Integer> indices) {
		Set<Integer> indexSet = new HashSet<Integer>();
		for (int i : indices) {
			indexSet.add(i);
		}
		
		for (int i : indices) {
			int newIndex = walkForwards(i, indexSet, values.length - 1);
			if (newIndex >= 0) {
				values[i] = newIndex;
			} else {
				values[i] = Double.NaN;
			}
		}
		return null;
	}
	
	private int walkForwards(int badIndex, Set<Integer> invalidIndices, int maxIndex) {
		for (int i = badIndex + 1; i <= maxIndex; i++) {
			if (! invalidIndices.contains(i)) {
				return i;
			}
		}
		return -1;
	}

}
