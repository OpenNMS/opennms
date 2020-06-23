/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.api;

import java.sql.SQLException;

import org.opennms.netmgt.model.OnmsNode.NodeLabelSource;

public interface NodeLabel {

	/**
	 * The property string in the properties file which specifies the method to
	 * use for determining which interface is primary on a multi-interface box.
	 */
	public static final String PROP_PRIMARY_INTERFACE_SELECT_METHOD = "org.opennms.bluebird.dp.primaryInterfaceSelectMethod";

	/**
	 * Maximum length for node label
	 */
	public static final int MAX_NODE_LABEL_LENGTH = 256;

	/**
	 * Primary interface selection method MIN. Using this selection method the
	 * interface with the smallest numeric IP address is considered the primary
	 * interface.
	 */
	public static final String SELECT_METHOD_MIN = "min";

	/**
	 * Primary interface selection method MAX. Using this selection method the
	 * interface with the greatest numeric IP address is considered the primary
	 * interface.
	 */
	public static final String SELECT_METHOD_MAX = "max";

	/**
	 * Default primary interface select method.
	 */
	public static final String DEFAULT_SELECT_METHOD = SELECT_METHOD_MIN;

	String getLabel();

	NodeLabelSource getSource();

	NodeLabel retrieveLabel(final int nodeID) throws SQLException;

	void assignLabel(final int nodeID, final NodeLabel nodeLabel) throws SQLException;

	NodeLabel computeLabel(final int nodeID) throws SQLException;

	String toString();
}
