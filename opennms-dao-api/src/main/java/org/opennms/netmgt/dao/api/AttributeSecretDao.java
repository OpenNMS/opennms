/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.api;

import java.util.Collection;

import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSecretAttribute;

/**
 * This is part of the 'secret' project from the 2005 Dev-Jam.  It will mostly
 * likely be replaced by or refactored into the new ResourceDao.
 */
public interface AttributeSecretDao {
	
	/**
	 * <p>getAttributesForNode</p>
	 *
	 * @param node a {@link org.opennms.netmgt.model.OnmsNode} object.
	 * @return a {@link java.util.Collection} object.
	 */
	Collection<OnmsSecretAttribute> getAttributesForNode(OnmsNode node);

	/**
	 * <p>getAttributesForInterface</p>
	 *
	 * @param iface a {@link org.opennms.netmgt.model.OnmsIpInterface} object.
	 * @return a {@link java.util.Collection} object.
	 */
	Collection<OnmsSecretAttribute> getAttributesForInterface(OnmsIpInterface iface);

	/**
	 * <p>getResponseTimeAttributeForService</p>
	 *
	 * @param svc a {@link org.opennms.netmgt.model.OnmsMonitoredService} object.
	 * @return a {@link org.opennms.netmgt.model.OnmsSecretAttribute} object.
	 */
	OnmsSecretAttribute getResponseTimeAttributeForService(OnmsMonitoredService svc);

	//OnmsSecretAttribute getAttribute(String id);
}
