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

package org.opennms.netmgt.eventd;

import java.sql.SQLException;
import java.util.Map;

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

	String getValueOfParm(String tagUei, Event m_svcLostEvent);

	String getHostName(int nodeId, String hostip) throws SQLException;

	String getEventHost(Event event);

}
