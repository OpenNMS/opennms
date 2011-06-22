/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 2 of the License,
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

package org.opennms.netmgt.linkd;

/**
 * <p>Vlan class.</p>
 *
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @version $Id: $
 */
public class Vlan {

		int vlanIndex;
		
		String vlanName;
		
		int vlanStatus = -1;
		
		int vlanType = -1;
		
		Vlan(int index, String name, int status,int type) {
			vlanIndex = index;
			vlanName = name;
			vlanStatus = status;
			vlanType = type;
		}

		Vlan(int index, String name, int status) {
			vlanIndex = index;
			vlanName = name;
			vlanStatus = status;

		}

		/**
		 * <p>Getter for the field <code>vlanIndex</code>.</p>
		 *
		 * @return a int.
		 */
		public int getVlanIndex() {
			return vlanIndex;
		}

		/**
		 * <p>Getter for the field <code>vlanName</code>.</p>
		 *
		 * @return a {@link java.lang.String} object.
		 */
		public String getVlanName() {
			return vlanName;
		}

		/**
		 * <p>Getter for the field <code>vlanStatus</code>.</p>
		 *
		 * @return a int.
		 */
		public int getVlanStatus() {
			return vlanStatus;
		}

		/**
		 * <p>Getter for the field <code>vlanType</code>.</p>
		 *
		 * @return a int.
		 */
		public int getVlanType() {
			return vlanType;
		}
	
}
