//
// Modifications:
//
// 2007 June 01: Fix the name of the getter for lasltPollTime. - dj@opennms.org
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

import java.util.HashMap;
import java.util.Map;


public class Vlan
{
        int     m_nodeId;
		int     m_vlanId;
		String  m_vlanname;
		int 	m_vlantype;
		int 	m_vlanstatus;
        String  m_lastPollTime;
        char    m_status;

	    private static final String[] VLAN_TYPE = new String[] {
		    "&nbsp;",           //0 (not supported)
		    "Ethernet",           //1 
		    "FDDI",          //2
		    "TokenRing",  //3
            "FDDINet", //5
            "TRNet", //5
            "Deprecated" //6
		    };

	    private static final String[] VLAN_STATUS = new String[] {
		    "&nbsp;",           //0 (not supported)
		    "operational", //1
	    	"suspendid", //2 
	    	"mtuTooBigForDevice", //3
	    	"mtuTooBigForTrunk"	//4
	    };


        private static final Map<Character, String>     statusMap = new HashMap<Character, String>();
      	
        static {
            statusMap.put( new Character('A'), "Active" );
            statusMap.put( new Character(' '), "Unknown" );
            statusMap.put( new Character('D'), "Deleted" );
            statusMap.put( new Character('N'), "Not Active" );
        }

        /* package-protected so only the NetworkElementFactory can instantiate */
        Vlan()
        {
        }

        /* package-protected so only the NetworkElementFactory can instantiate */
        Vlan(int nodeId, int vlanid, String vlanname, int vlantype, int vlanstatus, String lastpolltime, char status)

        {
                m_nodeId = nodeId;
                m_vlanId = vlanid;
				m_vlanname = vlanname;
				m_vlantype = vlantype;
				m_vlanstatus = vlanstatus;
                m_lastPollTime = lastpolltime; 
                m_status = status;
        }

        public String toString()
        {
                StringBuffer str = new StringBuffer("Node Id = " + m_nodeId + "\n" );
                str.append("Vlan id = " + m_vlanId + "\n" );
                str.append("At Last Poll Time = " + m_lastPollTime + "\n" );
                str.append("Node At Status= " + m_status + "\n" );
                return str.toString();
        }


		/**
		 * @return
		 */
		public String getLastPollTime() {
			return m_lastPollTime;
		}

		/**
		 * @return
		 */
		public int getNodeId() {
			return m_nodeId;
		}

		/**
		 * @return
		 */
		public char getStatus() {
			return m_status;
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
	        if (m_vlanId == 0) return "";
	        if (m_vlanId == 1) return "#FFFFFF";
	        red = (red + m_vlanId * redoffset)%255;
	        green = (green + m_vlanId * greenoffset)%255;
	        blue = (blue + m_vlanId * blueoffset)%255;
	        if (red < 64) red = red+64;
	        if (green < 64) green = green+64;
	        if (blue < 64) blue = blue+64;
	        return "#"+Integer.toHexString(red)+Integer.toHexString(green)+Integer.toHexString(blue);
	    }

		public int getVlanId() {
			return m_vlanId;
		}

		public String getVlanName() {
			return m_vlanname;
		}

		public int getVlanStatus() {
			return m_vlanstatus;
		}

		public String getVlanStatusString() {
			return VLAN_STATUS[m_vlanstatus];
		}
		
		public int getVlanType() {
			return m_vlantype;
		}
		
		public String getVlanTypeString() {
			return VLAN_TYPE[m_vlantype];
		}

}
