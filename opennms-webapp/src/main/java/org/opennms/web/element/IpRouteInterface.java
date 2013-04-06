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

package org.opennms.web.element;

import org.opennms.netmgt.model.OnmsArpInterface.StatusType;
import org.opennms.netmgt.model.OnmsIpRouteInterface;
import org.opennms.netmgt.model.OnmsIpRouteInterface.RouteType;
import org.opennms.web.api.Util;



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
	String  m_routetype;
	String  m_routeproto;
    String  m_routedest;
	String  m_routemask;
	String  m_routenexthop;
        String  m_lastPollTime;
        String  m_status;

        /* package-protected so only the NetworkElementFactory can instantiate */
        IpRouteInterface()
        {
        }

        /* package-protected so only the NetworkElementFactory can instantiate */
        IpRouteInterface(OnmsIpRouteInterface iproute)
        {
            m_nodeId = iproute.getNode().getId();
            m_routeifindex = iproute.getRouteIfIndex();
            m_routemetric1 = iproute.getRouteMetric1();
            m_routemetric2 = iproute.getRouteMetric2();
            m_routemetric3 = iproute.getRouteMetric3();
            m_routemetric4 = iproute.getRouteMetric4();
            m_routemetric5 = iproute.getRouteMetric5();
            m_routetype = RouteType.getRouteTypeString(iproute.getRouteType().getIntCode());
            m_routeproto= ElementUtil.getIpRouteProtocolString(iproute.getRouteProto());
            m_routenexthop = iproute.getRouteNextHop();
            m_routedest = iproute.getRouteDest();
            m_routemask = iproute.getRouteMask();
            m_lastPollTime = Util.formatDateToUIString(iproute.getLastPollTime()); 
            m_status = StatusType.getStatusString(iproute.getStatus().getCharCode());
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
		public String get_status() {
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
		public String get_routeproto() {
			return m_routeproto;
		}

		/**
		 * <p>get_routetype</p>
		 *
		 * @return a int.
		 */
		public String get_routetype() {
			return m_routetype;
		}

}
