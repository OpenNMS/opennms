/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.model;

/**
 * <p>EntityVisitor interface.</p>
 */
public interface EntityVisitor {
	
	/**
	 * <p>visitNode</p>
	 *
	 * @param node a {@link org.opennms.netmgt.model.OnmsNode} object.
	 */
	public void visitNode(OnmsNode node);
	/**
	 * <p>visitNodeComplete</p>
	 *
	 * @param node a {@link org.opennms.netmgt.model.OnmsNode} object.
	 */
	public void visitNodeComplete(OnmsNode node);
	/**
	 * <p>visitSnmpInterface</p>
	 *
	 * @param snmpIface a {@link org.opennms.netmgt.model.OnmsEntity} object.
	 */
	public void visitSnmpInterface(OnmsEntity snmpIface);
	/**
	 * <p>visitSnmpInterfaceComplete</p>
	 *
	 * @param snmpIface a {@link org.opennms.netmgt.model.OnmsEntity} object.
	 */
	public void visitSnmpInterfaceComplete(OnmsEntity snmpIface);
	/**
	 * <p>visitIpInterface</p>
	 *
	 * @param iface a {@link org.opennms.netmgt.model.OnmsIpInterface} object.
	 */
	public void visitIpInterface(OnmsIpInterface iface);
	/**
	 * <p>visitIpInterfaceComplete</p>
	 *
	 * @param iface a {@link org.opennms.netmgt.model.OnmsIpInterface} object.
	 */
	public void visitIpInterfaceComplete(OnmsIpInterface iface);
	/**
	 * <p>visitMonitoredService</p>
	 *
	 * @param monSvc a {@link org.opennms.netmgt.model.OnmsMonitoredService} object.
	 */
	public void visitMonitoredService(OnmsMonitoredService monSvc);
	/**
	 * <p>visitMonitoredServiceComplete</p>
	 *
	 * @param monSvc a {@link org.opennms.netmgt.model.OnmsMonitoredService} object.
	 */
	public void visitMonitoredServiceComplete(OnmsMonitoredService monSvc);

}
