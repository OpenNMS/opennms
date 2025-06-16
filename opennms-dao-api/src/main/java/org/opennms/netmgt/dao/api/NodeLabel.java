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
