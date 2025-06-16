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
package org.opennms.netmgt.eventd;

import java.math.BigInteger;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

import org.opennms.netmgt.eventd.processor.expandable.ExpandableParameterResolver;
import org.opennms.netmgt.xml.event.Event;

/**
 * EventUtil is used primarily for the event parm expansion - has methods used
 * by all the event components to send in the event and the element to expanded
 * and have the 'expanded' value sent back
 *
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Kumaraswamy </A>
 * @author <A HREF="mailto:weave@oculan.com">Brain Weaver </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public interface EventUtil {

	String getHardwareFieldValue(String parm, long nodeId);

	String expandParms(String string, Event event);
	
	String expandParms(String inp, Event event, Map<String, Map<String, String>> decode);

	String getNamedParmValue(String string, Event event);

	void expandMapValues(Map<String, String> parmMap, Event event);

	String getHostName(int nodeId, String hostip) throws SQLException;

	String getEventHost(Event event);

	/**
	 * Retrieve ifAlias from the snmpinterface table of the database given a particular
	 * nodeId and ipAddr.
	 *
	 * @param nodeId
	 *            Node identifier
	 * @param ipAddr
	 *            Interface IP address
	 *
	 * @return ifAlias Retreived ifAlias
	 *
	 * @throws SQLException
	 *             if database error encountered
	 */
	String getIfAlias(long nodeId, String ipAddr) throws SQLException;

	/**
	 * Helper method.
	 *
	 * @param parm
	 * @param nodeId
	 * @return The value of an asset field based on the nodeid of the event
	 */
	String getAssetFieldValue(String parm, long nodeId);

	/**
	 * Retrieve foreign id from the node table of the database given a particular nodeId.
	 *
	 * @param nodeId Node identifier
	 * @return foreignId Retrieved foreign id
	 * @throws SQLException if database error encountered
	 */
	String getForeignId(long nodeId) throws SQLException;

	/**
	 * Retrieve foreign source from the node table of the database given a particular
	 * nodeId.
	 *
	 * @param nodeId
	 *            Node identifier
	 *
	 * @return foreignSource Retrieved foreign source
	 *
	 * @throws SQLException
	 *             if database error encountered
	 */
	String getForeignSource(long nodeId) throws SQLException;

	/**
	 * Retrieve nodeLabel from the node table of the database given a particular
	 * nodeId.
	 *
	 * @param nodeId
	 *            Node identifier
	 *
	 * @return nodeLabel Retreived nodeLabel
	 *
	 * @throws SQLException
	 *             if database error encountered
	 */
	String getNodeLabel(long nodeId) throws SQLException;

	/**
	 * Retrieve nodeLocation from the node table of the database given a particular
	 * nodeId.
	 *
	 * @param nodeId
	 *            Node identifier
	 *
	 * @return nodeLocation Retrieved nodeLocation
	 *
	 * @throws SQLException
	 *             if database error encountered
	 */
	String getNodeLocation(long nodeId) throws SQLException;

	ExpandableParameterResolver getResolver(String token);
	
	Date decodeSnmpV2TcDateAndTime(BigInteger octetStringValue);

	String getPrimaryInterface(long nodeId) throws SQLException;
}

