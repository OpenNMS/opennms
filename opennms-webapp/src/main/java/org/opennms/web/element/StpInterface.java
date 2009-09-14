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
 * Created: February 10, 2007
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


/**
 * 
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 */
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

        public static final String[] STP_PORT_STATUS = new String[] {
            "&nbsp;",     //0 (not supported)
            "Disabled",   //1
            "Blocking",   //2
            "Listening",  //3
            "Learning",   //4
            "Forwarding", //5
            "Broken",     //6
          };

        private static final Map<Character, String>     statusMap = new HashMap<Character, String>();
      	
        static {
            statusMap.put( new Character('A'), "Active" );
            statusMap.put( new Character(' '), "Unknown" );
            statusMap.put( new Character('D'), "Deleted" );
            statusMap.put( new Character('N'), "Not Active" );
        }


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

		public String getStpPortState() {
			return STP_PORT_STATUS[m_stpportstate];
			
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
		
	    public String getStatusString() {
	        return( (String)statusMap.get( new Character(m_status) ));
	    }

	    public String getVlanColorIdentifier() {
	        int red = 128;
	        int green = 128;
	        int blue = 128;
	        int redoffset = 47;
	        int greenoffset = 29;
	        int blueoffset = 23;
	        if (m_stpvlan == 0) return "";
	        if (m_stpvlan == 1) return "#FFFFFF";
	        red = (red + m_stpvlan * redoffset)%255;
	        green = (green + m_stpvlan * greenoffset)%255;
	        blue = (blue + m_stpvlan * blueoffset)%255;
	        if (red < 64) red = red+64;
	        if (green < 64) green = green+64;
	        if (blue < 64) blue = blue+64;
	        return "#"+Integer.toHexString(red)+Integer.toHexString(green)+Integer.toHexString(blue);
	    }

}
