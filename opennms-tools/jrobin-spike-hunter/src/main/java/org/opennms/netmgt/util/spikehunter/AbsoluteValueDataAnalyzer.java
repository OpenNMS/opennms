/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.util.spikehunter;

import java.lang.Math;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AbsoluteValueDataAnalyzer implements DataAnalyzer {
	private int m_value;
	private boolean m_verbose = false;
	
	public AbsoluteValueDataAnalyzer(List<Double> operands) {
		setParms(operands);
	}
	
	public List<Integer> findSamplesInViolation(double[] values) {
		List<Integer> violatorIndices = new ArrayList<>();
		
		for (int i = 0; i < values.length; i++) {
			if (Double.toString(values[i]).equals(Double.toString(Double.NaN))) {
				continue;
			}
			if (Math.abs(values[i]) > Math.abs(m_value)) {
				violatorIndices.add(i);
			}
		}		
		return violatorIndices;
	}
	
	public void setParms(List<Double> parms) {
		m_value = parms.get(0).intValue();
	}

	public String toString() {
		return "Absolute-value analyzer (N=" + m_value + ")";
	}
	
	public void setVerbose(boolean v) {
		m_verbose = v;
	}
}
