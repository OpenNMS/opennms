/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: July 28, 2006
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.web.element;

import java.util.HashMap;
import java.util.Map;

import org.opennms.netmgt.linkd.DbIpRouteInterfaceEntry;



/**
 * <p>IpRouteInterface class.</p>
 *
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 */
public class IpRouteInterface
{
        int     m_nodeId;
		int     m_routeifindex;
		int     m_routemetric1;
		int     m_routemetric2;
		int     m_routemetric3;
		int     m_routemetric4;
		int     m_routemetric5;
		int     m_routetype;
		int     m_routeproto;
        String  m_routedest;
		String  m_routemask;
		String  m_routenexthop;
        String  m_lastPollTime;
        char    m_status;

        private static final Map<Character, String> statusMap = new HashMap<Character, String>();

        static {
            statusMap.put( DbIpRouteInterfaceEntry.STATUS_ACTIVE, "Active" );
            statusMap.put( DbIpRouteInterfaceEntry.STATUS_UNKNOWN, "Unknown" );
            statusMap.put( DbIpRouteInterfaceEntry.STATUS_DELETED, "Deleted" );
            statusMap.put( DbIpRouteInterfaceEntry.STATUS_NOT_POLLED, "Not Active" );
        }

        /* package-protected so only the NetworkElementFactory can instantiate */
        IpRouteInterface()
        {
        }

        /* package-protected so only the NetworkElementFactory can instantiate */
        IpRouteInterface(int     nodeId,
	int     routeifindex,
	int     routemetric1,
	int     routemetric2,
	int     routemetric3,
	int     routemetric4,
	int     routemetric5,
	int     routetype,
	int     routeproto,
	String  routedest,
	String  routemask,
	String  routenexthop,
	String  lastPollTime,
	char    status
        )
        {
            m_nodeId = nodeId;
            m_routeifindex = routeifindex;
			m_routemetric1 = routemetric1;
			m_routemetric2 = routemetric2;
			m_routemetric3 = routemetric3;
			m_routemetric4 = routemetric4;
			m_routemetric5 = routemetric5;
			m_routetype = routetype;
			m_routeproto= routeproto;
			m_routenexthop = routenexthop;
			m_routedest = routedest;
			m_routemask = routemask;
			m_lastPollTime = lastPollTime; 
            m_status = status;
        }

        /**
         * <p>toString</p>
         *
         * @return a {@link java.lang.String} object.
         */
        public String toString()
        {
                StringBuffer str = new StringBuffer("Node Id = " + m_nodeId + "\n" );
                str.append("At Last Poll Time = " + m_lastPollTime + "\n" );
                str.append("Node At Status= " + m_status + "\n" );
                return str.toString();
        }
		/**
		 * <p>get_ifindex</p>
		 *
		 * @return a int.
		 */
		public int get_ifindex() {
			return m_routeifindex;
		}

		/**
		 * <p>get_lastPollTime</p>
		 *
		 * @return a {@link java.lang.String} object.
		 */
		public String get_lastPollTime() {
			return m_lastPollTime;
		}

		/**
		 * <p>get_nodeId</p>
		 *
		 * @return a int.
		 */
		public int get_nodeId() {
			return m_nodeId;
		}

		/**
		 * <p>get_status</p>
		 *
		 * @return a char.
		 */
		public char get_status() {
			return m_status;
		}

		/**
		 * <p>get_routedest</p>
		 *
		 * @return a {@link java.lang.String} object.
		 */
		public String get_routedest() {
			return m_routedest;
		}

		/**
		 * <p>get_routemask</p>
		 *
		 * @return a {@link java.lang.String} object.
		 */
		public String get_routemask() {
			return m_routemask;
		}

		/**
		 * <p>get_routemetric1</p>
		 *
		 * @return a int.
		 */
		public int get_routemetric1() {
			return m_routemetric1;
		}

		/**
		 * <p>get_routemetric2</p>
		 *
		 * @return a int.
		 */
		public int get_routemetric2() {
			return m_routemetric2;
		}

		/**
		 * <p>get_routemetric3</p>
		 *
		 * @return a int.
		 */
		public int get_routemetric3() {
			return m_routemetric3;
		}

		/**
		 * <p>get_routemetric4</p>
		 *
		 * @return a int.
		 */
		public int get_routemetric4() {
			return m_routemetric4;
		}

		/**
		 * <p>get_routemetric5</p>
		 *
		 * @return a int.
		 */
		public int get_routemetric5() {
			return m_routemetric5;
		}

		/**
		 * <p>get_routenexthop</p>
		 *
		 * @return a {@link java.lang.String} object.
		 */
		public String get_routenexthop() {
			return m_routenexthop;
		}

		/**
		 * <p>get_routeproto</p>
		 *
		 * @return a int.
		 */
		public int get_routeproto() {
			return m_routeproto;
		}

		/**
		 * <p>get_routetype</p>
		 *
		 * @return a int.
		 */
		public int get_routetype() {
			return m_routetype;
		}

}
