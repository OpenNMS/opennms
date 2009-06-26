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

/**
 * 
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 */
public class AtInterface
{
        int     m_nodeId;
        int     m_sourcenodeid;
		int     m_ifindex;
        String  m_ipaddr;
        String  m_physaddr;
        String  m_lastPollTime;
        char    m_status;
        /* package-protected so only the NetworkElementFactory can instantiate */
        AtInterface()
        {
        }

        /* package-protected so only the NetworkElementFactory can instantiate */
        AtInterface(   int nodeId,
                int sourcenodeid,
				int ifindex,
                String ipaddr,
                String physaddr,
                String lastPollTime,
                char status)
        {
                m_nodeId = nodeId;
                m_sourcenodeid = sourcenodeid;
				m_ifindex = ifindex;
                m_ipaddr = ipaddr;
                m_physaddr = physaddr;
                m_lastPollTime = lastPollTime; 
                m_status = status;
        }

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
		 * @return
		 */
		public int get_ifindex() {
			return m_ifindex;
		}

		/**
		 * @return
		 */
		public String get_ipaddr() {
			return m_ipaddr;
		}

		/**
		 * @return
		 */
		public String get_lastPollTime() {
			return m_lastPollTime;
		}

		/**
		 * @return
		 */
		public int get_nodeId() {
			return m_nodeId;
		}

		/**
		 * @return
		 */
		public String get_physaddr() {
			return m_physaddr;
		}

		/**
		 * @return
		 */
		public int get_sourcenodeid() {
			return m_sourcenodeid;
		}

		/**
		 * @return
		 */
		public char get_status() {
			return m_status;
		}

}
