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
