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

package org.opennms.mock.snmp.responder;

import java.text.DecimalFormat;

import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.Variable;

public class SineWave implements DynamicVariable {
        @Override
	public Variable getVariableForOID(String oidStr) {
		String[] oids = oidStr.split("\\.");
		Integer instance = Integer.parseInt(oids[oids.length-1]);
		
		// Convert the instance from degrees to radians and calculate the sin
		double x = Math.sin(instance.doubleValue() * Math.PI / 180);
		
		// Round the value to two decimal places and multiply it by 100
		// Leaving us with an integer
		DecimalFormat epsilum = new DecimalFormat("#.##");
		x = Double.valueOf(epsilum.format(x)) * 100;
	
		// Convert the result back to an integer
		return new Integer32((int)x);
	}
}
