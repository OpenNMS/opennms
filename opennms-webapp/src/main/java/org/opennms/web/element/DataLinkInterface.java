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


public class DataLinkInterface
{
        int     m_nodeId;
        int     m_nodeparentid;
		int     m_ifindex;
		int     m_parentifindex;
    	String  m_ipaddress;
		String  m_parentipaddress;
		String  m_lastPollTime;
        char    m_status;
        /* package-protected so only the NetworkElementFactory can instantiate */
        DataLinkInterface()
        {
        }

        /* package-protected so only the NetworkElementFactory can instantiate */
        DataLinkInterface(   int nodeId,
                int nodeparentid,
				int ifindex,
				int parentifindex,
				String ipaddress,
				String parentipaddress,
                String lastPollTime,
                char status)
        {
                m_nodeId = nodeId;
                m_nodeparentid = nodeparentid;
				m_ifindex = ifindex;
				m_parentifindex = parentifindex;
			    m_ipaddress = ipaddress; 
				m_parentipaddress = parentipaddress;
			    m_lastPollTime = lastPollTime; 
                m_status = status;
        }

        public String toString()
        {
                StringBuffer str = new StringBuffer("Node Id = " + m_nodeId + "\n" );
				str.append("IfIndex = " + m_ifindex + "\n" );
                str.append("Node Parent = " + m_nodeparentid + "\n" );
				str.append("Parent IfIndex = " + m_parentifindex + "\n" );
                str.append("At Last Poll Time = " + m_lastPollTime + "\n" );
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
	public int get_parentifindex() {
		return m_parentifindex;
	}

	/**
	 * @return
	 */
	public String get_ipaddr() {
		return m_ipaddress;
	}

	/**
	 * @return
	 */
	public String get_parentipaddr() {
		return m_parentipaddress;
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
		public int get_nodeparentid() {
			return m_nodeparentid;
		}

		/**
		 * @return
		 */
		public char get_status() {
			return m_status;
		}
		
		public void invertNodewithParent() {
			int nodeid = m_nodeId;
			String ipaddr = m_ipaddress;
			int ifindex = m_ifindex;
			
			int nodeparentid = m_nodeparentid;
			String parentipaddr = m_parentipaddress;
			int parentifindex = m_parentifindex;
			
			m_nodeId = nodeparentid;
			m_ipaddress = parentipaddr;
			m_ifindex = parentifindex;
			
			m_nodeparentid = nodeid;
			m_parentipaddress = ipaddr;
			m_parentifindex = ifindex;
			
		}

}
