/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
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

import java.util.HashMap;
import java.util.Map;

/**
 * <p>DataLinkInterface class.</p>
 *
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 */
public class LinkInterface
{
        private final Interface m_iface;
        private final Interface m_linkedInterface;
        private final String  m_lastPollTime;
        private final char    m_status;
        private final Integer m_linktypeid;
        
		private final Integer m_nodeId;
        private final Integer m_ifindex;
        private final Integer m_linkedNodeId;
        private final Integer m_linkedIfindex;

        private static final Map<Character, String> statusMap = new HashMap<Character, String>();

        private static final Map<Integer, String> linktypeMap = new HashMap<Integer, String>();
        
        static {
            statusMap.put( 'A', "Active" );
            statusMap.put( 'K', "Unknown" );
            statusMap.put( 'D', "Deleted" );
            statusMap.put( 'N', "Not Active" );
            statusMap.put( 'B', "Bad" );
            statusMap.put( 'G', "Good" );
        }
        

        static {
        	linktypeMap.put(9999, "Unknown");
        	linktypeMap.put(777, "DWO connection");
        	linktypeMap.put(1777, "Summary Link");        	
        }

        LinkInterface( Integer nodeid, Integer ifindex, Integer linkedNodeid, Integer linkedIfindex, Interface iface, Interface linkedIface, String lastPollTime,
                char status, Integer linktypeid)
        {
        		m_nodeId = nodeid;
        		m_ifindex = ifindex;
        		m_linkedNodeId = linkedNodeid;
        		m_linkedIfindex = linkedIfindex;
                m_iface = iface;
                m_linkedInterface = linkedIface;
			    m_lastPollTime = lastPollTime; 
                m_status = status;
                m_linktypeid = linktypeid;                
        }

		/**
         * <p>toString</p>
         *
         * @return a {@link java.lang.String} object.
         */
        public String toString()
        {
                StringBuffer str = new StringBuffer("Node Id = " + m_iface.getNodeId() + "\n" );
				str.append("IfIndex = " + m_iface.getIfIndex() + "\n" );
				str.append("Last Poll Time = " + m_lastPollTime + "\n" );
				str.append("Link Type Id = " + m_linktypeid + "\n" );
                str.append("Status= " + m_status + "\n" );
                return str.toString();
        }

        public Integer getNodeId() {
			return m_nodeId;
		}

        public Integer getIfindex() {
        	return m_ifindex;
        }
        
		public Integer getLinkedNodeId() {
			return m_linkedNodeId;
		}

		public Integer getLinkedIfindex() {
			return m_linkedIfindex;
		}

		/**
		 * <p>get_lastPollTime</p>
		 *
		 * @return a {@link java.lang.String} object.
		 */
		public String getLastPollTime() {
			return m_lastPollTime;
		}

		/**
		 * <p>get_status</p>m
		 *
		 * @return a char.
		 */
		public char getStatus() {
			return m_status;
		}
		
        /**
         */
        public String getStatusString() {
            if (statusMap.containsKey(m_status))
            	return statusMap.get( new Character(m_status) );
            return null;
        }
        
        public Integer getLinktypeId() {
        	return m_linktypeid;
        }
        
        public String getLinkTypeIdString() {
        	if (linktypeMap.containsKey(m_linktypeid))
        		return linktypeMap.get(m_linktypeid);
        	return null; 
        }
        
        public Interface getLinkedInterface() {
			return m_linkedInterface;
		}

		public Interface getInterface() {
			return m_iface;
		}

		public boolean hasInterface() {
			if ( m_iface == null) return false;
			return true;
		}

		public boolean hasLinkedInterface() {
			if ( m_linkedInterface == null) return false;
			return true;
		}

}
