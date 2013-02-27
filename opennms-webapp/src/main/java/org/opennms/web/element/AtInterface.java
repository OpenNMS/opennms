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

import org.opennms.netmgt.model.OnmsArpInterface;

/**
 * <p>AtInterface class.</p>
 *
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 */
public class AtInterface extends RowStatus
{
        private final int     m_nodeId;
        private final int     m_sourcenodeid;
        private final int     m_ifindex;
        private final String  m_ipaddr;
        private final String  m_physaddr;
        private final String  m_lastPollTime;

        /* package-protected so only the NetworkElementFactory can instantiate */
        AtInterface(OnmsArpInterface onmsat)
        {
            super(onmsat.getStatus().getCharCode());
            m_nodeId = onmsat.getNode().getId();
            m_sourcenodeid = onmsat.getSourceNode().getId();
			m_ifindex = onmsat.getIfIndex();
            m_ipaddr = onmsat.getIpAddress();
            m_physaddr = onmsat.getPhysAddr();
            m_lastPollTime = onmsat.getLastPoll().toString(); 
        }

        /**
         * <p>toString</p>
         *
         * @return a {@link java.lang.String} object.
         */
        public String toString()
        {
                StringBuffer str = new StringBuffer("Node Id = " + m_nodeId + "\n" );
                str.append("Node Source = " + m_sourcenodeid + "\n" );
                str.append("At Last Poll Time = " + m_lastPollTime + "\n" );
                str.append("Dp name = " + m_physaddr + "\n" );
                str.append("Node At Status= " + m_status + "\n" );
                return str.toString();
        }
		/**
		 * <p>get_ifindex</p>
		 *
		 * @return a int.
		 */
		public int get_ifindex() {
			return m_ifindex;
		}

		/**
		 * <p>get_ipaddr</p>
		 *
		 * @return a {@link java.lang.String} object.
		 */
		public String get_ipaddr() {
			return m_ipaddr;
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
		 * <p>get_physaddr</p>
		 *
		 * @return a {@link java.lang.String} object.
		 */
		public String get_physaddr() {
			return m_physaddr;
		}

		/**
		 * <p>get_sourcenodeid</p>
		 *
		 * @return a int.
		 */
		public int get_sourcenodeid() {
			return m_sourcenodeid;
		}

}
