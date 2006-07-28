//
// Copyright (C) 2002 Sortova Consulting Group, Inc.  All rights reserved.
// Parts Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.sortova.com/
//

package org.opennms.web.element;


public class StpInterface
{
        int     m_nodeId;
        int     m_bridgeport;
		int     m_ifindex;
		int     m_stpportstate;
		int     m_stpportpathcost;
		int     m_stpportdesignatedcost;
		int     m_stpvlan;
		String  m_ipaddr;
		String  m_stpdesignatedroot;
        String  m_stpdesignatedbridge;
		String  m_stpdesignatedport;
        String  m_lastPollTime;
        char    m_status;
        int		m_stprootnodeid;
        int		m_stpbridgenodeid;
        /* package-protected so only the NetworkElementFactory can instantiate */
        StpInterface()
        {
        }

        /* package-protected so only the NetworkElementFactory can instantiate */
        StpInterface(        int     nodeId,
	int     bridgeport,
	int     ifindex,
	int     stpportstate,
	int     stpportpathcost,
	int     stpportdesignatedcost,
	int     stpvlan,
	String  stpdesignatedroot,
	String  stpdesignatedbridge,
	String  stpdesignatedport,
	String  lastPollTime,
	char    status,
	int		stprootnodeid,
	int 	stpbridgenodeid
)
        {
                m_nodeId = nodeId;
                m_bridgeport = bridgeport;
				m_ifindex = ifindex;
				m_stpportstate = stpportstate;
				m_stpportpathcost = stpportpathcost;
				m_stpportdesignatedcost = stpportdesignatedcost;
				m_stpvlan = stpvlan;
                m_stpdesignatedbridge = stpdesignatedbridge;
                m_stpdesignatedroot = stpdesignatedroot;
				m_stpdesignatedport = stpdesignatedport;
				m_lastPollTime = lastPollTime; 
                m_status = status;
                m_stprootnodeid = stprootnodeid;
                m_stpbridgenodeid = stpbridgenodeid;
        }

        public String toString()
        {
                StringBuffer str = new StringBuffer("Node Id = " + m_nodeId + "\n" );
                str.append("Bridge number of ports = " + m_bridgeport + "\n" );
                str.append("At Last Poll Time = " + m_lastPollTime + "\n" );
                str.append("Node At Status= " + m_status + "\n" );
                return str.toString();
        }

		/**
		 * @return
		 */
		public int get_bridgeport() {
			return m_bridgeport;
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
		public char get_status() {
			return m_status;
		}

		/**
		 * @return
		 */
		public String get_stpdesignatedbridge() {
			return m_stpdesignatedbridge;
		}

		/**
		 * @return
		 */
		public String get_stpdesignatedport() {
			return m_stpdesignatedport;
		}

		/**
		 * @return
		 */
		public String get_stpdesignatedroot() {
			return m_stpdesignatedroot;
		}

		/**
		 * @return
		 */
		public int get_stpportdesignatedcost() {
			return m_stpportdesignatedcost;
		}

		/**
		 * @return
		 */
		public int get_stpportpathcost() {
			return m_stpportpathcost;
		}

		/**
		 * @return
		 */
		public int get_stpportstate() {
			return m_stpportstate;
		}

		/**
		 * @return
		 */
		public int get_stpvlan() {
			return m_stpvlan;
		}

        /**
         * @return Returns the m_stpdesignatedbridgenodeid.
         */
        public int get_stpbridgenodeid() {
            return m_stpbridgenodeid;
        }
        /**
         * @return Returns the m_stpdesignatedrootnodeid.
         */
        public int get_stprootnodeid() {
            return m_stprootnodeid;
        }
		/**
		 * @return Returns the m_ipaddr.
		 */
		public String get_ipaddr() {
			return m_ipaddr;
		}
		/**
		 * @param m_ipaddr The m_ipaddr to set.
		 */
		public void set_ipaddr(String m_ipaddr) {
			this.m_ipaddr = m_ipaddr;
		}
}
