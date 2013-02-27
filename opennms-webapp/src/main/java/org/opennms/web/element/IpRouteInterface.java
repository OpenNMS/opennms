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

/**
 * <p>IpRouteInterface class.</p>
 *
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 */
public class IpRouteInterface extends RowStatus
{
	/** 
	 * Integer representing route type
	 */
	public static final int ROUTE_TYPE_OTHER = 1;

	public static final int ROUTE_TYPE_INVALID = 2;

	public static final int ROUTE_TYPE_DIRECT = 3;

	public static final int ROUTE_TYPE_INDIRECT = 4;
	
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

        /* package-protected so only the NetworkElementFactory can instantiate */
        IpRouteInterface()
        {
        	super('K');
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
        	super(status);
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
