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

package org.opennms.netmgt.utils;

import java.sql.Connection;
import java.sql.SQLException;

import org.opennms.netmgt.model.OnmsNode.NodeLabelSource;

public interface NodeLabel {

	String getLabel();
	
	NodeLabelSource getSource();
	
	NodeLabel retrieveLabel(final int nodeID) throws SQLException;
	
	NodeLabel retrieveLabel(int nodeID, Connection dbConnection) throws SQLException;
	
	void assignLabel(final int nodeID, final NodeLabel nodeLabel) throws SQLException;
	
	void assignLabel(final int nodeID, NodeLabel nodeLabel, final Connection dbConnection) throws SQLException;
	
	NodeLabel computeLabel(final int nodeID) throws SQLException;
	
	NodeLabel computeLabel(final int nodeID, final Connection dbConnection) throws SQLException;
	
	String toString();
}
