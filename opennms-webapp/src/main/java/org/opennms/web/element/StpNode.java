/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
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

import org.opennms.netmgt.linkd.DbStpNodeEntry;


/**
 * <p>StpNode class.</p>
 *
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 */
public class StpNode
{
        int     m_nodeId;
        int     m_basenumports;
		int     m_basetype;
		int     m_stpprotocolspecification;
		int     m_stppriority;
		int     m_stprootcost;
		int     m_stprootport;
		int     m_basevlan;
		String  m_basevlanname;
        String  m_basebridgeaddress;
		String  m_stpdesignatedroot;
        String  m_lastPollTime;
        char    m_status;
        int 	m_stprootnodeid;

        /**
         * <p>String identifiers for the enumeration of values:</p>
         * <ul>
         * <li>{@link DbStpNodeEntry#BASE_TYPE_UNKNOWN}</li>
         * <li>{@link DbStpNodeEntry#BASE_TYPE_TRANSPARENT_ONLY}</li>
         * <li>{@link DbStpNodeEntry#BASE_TYPE_SOURCEROUTE_ONLY}</li>
         * <li>{@link DbStpNodeEntry#BASE_TYPE_SRT}</li>
         * </ul>
         */ 
	    private static final String[] BRIDGE_BASE_TYPE = new String[] {
		    null,                //0 (not a valid index)
		    "Unknown",           //1
		    "Transparent-Only",  //2
		    "Source-Route-Only", //3
		    "SRT"                //4
		    };

        /**
         * <p>String identifiers for the enumeration of values:</p>
         * <ul>
         * <li>{@link DbStpNodeEntry#STP_UNKNOWN}</li>
         * <li>{@link DbStpNodeEntry#STP_DECLB100}</li>
         * <li>{@link DbStpNodeEntry#STP_IEEE8011D}</li>
         * </ul>
         */ 
		    private static final String[] STP_PROTO_TYPE = new String[] {
		    null,               //0 (not a valid index)
		    "Unknown",          //1
		    "DEC LAN Bridge",   //2
		    "IEEE 802.1D",      //3
		 };


        private static final Map<Character, String> statusMap = new HashMap<Character, String>();
      	
        static {
            statusMap.put( DbStpNodeEntry.STATUS_ACTIVE, "Active" );
            statusMap.put( DbStpNodeEntry.STATUS_UNKNOWN, "Unknown" );
            statusMap.put( DbStpNodeEntry.STATUS_DELETED, "Deleted" );
            statusMap.put( DbStpNodeEntry.STATUS_NOT_POLLED, "Not Active" );
        }

        /* package-protected so only the NetworkElementFactory can instantiate */
        StpNode()
        {
        }

        /* package-protected so only the NetworkElementFactory can instantiate */
        StpNode(        int     nodeId,
	int     basenumports,
	int     basetype,
	int     stpprotocolspecification,
	int     stppriority,
	int     stprootcost,
	int     stprootport,
	int     basevlan,
	String  basevlanname,
	String  basebridgeaddress,
	String  stpdesignatedroot,
	String  lastPollTime,
	char    status,
	int		stprootnodeid
)
        {
                m_nodeId = nodeId;
                m_basenumports = basenumports;
				m_basetype = basetype;
				m_stpprotocolspecification = stpprotocolspecification;
				m_stppriority = stppriority;
				m_stprootcost = stprootcost;
				m_stprootcost = stprootport;
				m_basevlan = basevlan;
				m_basevlanname = basevlanname;
                m_basebridgeaddress = basebridgeaddress;
                m_stpdesignatedroot = stpdesignatedroot;
                m_lastPollTime = lastPollTime; 
                m_status = status;
                m_stprootnodeid = stprootnodeid;
        }

        /**
         * <p>toString</p>
         *
         * @return a {@link java.lang.String} object.
         */
        public String toString()
        {
                StringBuffer str = new StringBuffer("Node Id = " + m_nodeId + "\n" );
                str.append("Bridge number of ports = " + m_basenumports + "\n" );
                str.append("At Last Poll Time = " + m_lastPollTime + "\n" );
                str.append("Node At Status= " + m_status + "\n" );
                return str.toString();
        }

		/**
		 * <p>get_basebridgeaddress</p>
		 *
		 * @return a {@link java.lang.String} object.
		 */
		public String get_basebridgeaddress() {
			return m_basebridgeaddress;
		}

		/**
		 * <p>get_basenumports</p>
		 *
		 * @return a int.
		 */
		public int get_basenumports() {
			return m_basenumports;
		}

		/**
		 * <p>get_basetype</p>
		 *
		 * @return a int.
		 */
		public int get_basetype() {
			return m_basetype;
		}

		/**
		 * <p>getBaseType</p>
		 *
		 * @return a {@link java.lang.String} object.
		 */
		public String getBaseType() {
		    try {
		        return BRIDGE_BASE_TYPE[m_basetype];
		    } catch (ArrayIndexOutOfBoundsException e) {
		        return BRIDGE_BASE_TYPE[DbStpNodeEntry.BASE_TYPE_UNKNOWN];
		    }
		}
		/**
		 * <p>get_basevlan</p>
		 *
		 * @return a int.
		 */
		public int get_basevlan() {
			return m_basevlan;
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
		 * <p>get_stpdesignatedroot</p>
		 *
		 * @return a {@link java.lang.String} object.
		 */
		public String get_stpdesignatedroot() {
			return m_stpdesignatedroot;
		}

		/**
		 * <p>get_stppriority</p>
		 *
		 * @return a int.
		 */
		public int get_stppriority() {
			return m_stppriority;
		}

		/**
		 * <p>get_stpprotocolspecification</p>
		 *
		 * @return a int.
		 */
		public int get_stpprotocolspecification() {
			return m_stpprotocolspecification;
		}

		/**
		 * <p>getStpProtocolSpecification</p>
		 *
		 * @return a {@link java.lang.String} object.
		 */
		public String getStpProtocolSpecification() {
            try {
                return STP_PROTO_TYPE[m_stpprotocolspecification];
            } catch (ArrayIndexOutOfBoundsException e) {
                return STP_PROTO_TYPE[DbStpNodeEntry.STP_UNKNOWN];
            }
		}

		/**
		 * <p>get_stprootcost</p>
		 *
		 * @return a int.
		 */
		public int get_stprootcost() {
			return m_stprootcost;
		}

		/**
		 * <p>get_stprootport</p>
		 *
		 * @return a int.
		 */
		public int get_stprootport() {
			return m_stprootport;
		}

        /**
         * <p>get_stprootnodeid</p>
         *
         * @return Returns the m_stprootnodeid.
         */
        public int get_stprootnodeid() {
            return m_stprootnodeid;
        }

		/**
		 * <p>getBaseVlanName</p>
		 *
		 * @return Returns the m_basevlanname.
		 */
		public String getBaseVlanName() {
			return m_basevlanname;
		}
		
	    /**
	     * <p>getStatusString</p>
	     *
	     * @return a {@link java.lang.String} object.
	     */
	    public String getStatusString() {
	        return statusMap.get( new Character(m_status) );
	    }

	    /**
	     * <p>getVlanColorIdentifier</p>
	     *
	     * @return a {@link java.lang.String} object.
	     */
	    public String getVlanColorIdentifier() {
	        int red = 128;
	        int green = 128;
	        int blue = 128;
	        int redoffset = 47;
	        int greenoffset = 29;
	        int blueoffset = 23;
	        if (m_basevlan == 0) return "";
	        if (m_basevlan == 1) return "#FFFFFF";
	        red = (red + m_basevlan * redoffset)%255;
	        green = (green + m_basevlan * greenoffset)%255;
	        blue = (blue + m_basevlan * blueoffset)%255;
	        if (red < 64) red = red+64;
	        if (green < 64) green = green+64;
	        if (blue < 64) blue = blue+64;
	        return "#"+Integer.toHexString(red)+Integer.toHexString(green)+Integer.toHexString(blue);
	    }

}
