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
import org.opennms.netmgt.model.OnmsStpInterface.StpPortStatus;
import org.opennms.netmgt.model.OnmsStpInterface;
import org.opennms.web.api.Util;


/**
 * <p>StpInterface class.</p>
 *
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 */
public class StpInterface
{
        int     m_nodeId;
        int     m_bridgeport;
		int     m_ifindex;
		String  m_stpportstate="";
		String  m_stpportpathcost="";
		String  m_stpportdesignatedcost="";
		int     m_stpvlan;
		String  m_ipaddr="";
		String  m_stpdesignatedroot="";
        String  m_stpdesignatedbridge="";
		String  m_stpdesignatedport="";
        String  m_lastPollTime;
        String  m_status;
        int		m_stprootnodeid;
        int		m_stpbridgenodeid;

        /* package-protected so only the NetworkElementFactory can instantiate */
        StpInterface()
        {
        }

        /* package-protected so only the NetworkElementFactory can instantiate */
        StpInterface(OnmsStpInterface stpinterf)
        {
                m_nodeId = stpinterf.getNode().getId();
                m_bridgeport = stpinterf.getBridgePort();
				m_ifindex = stpinterf.getIfIndex();
				m_stpvlan = stpinterf.getVlan();
				m_lastPollTime = Util.formatDateToUIString(stpinterf.getLastPollTime()); 
                m_status = StatusType.getStatusString(stpinterf.getStatus().getCharCode());
				if (stpinterf.getStpPortState() != null)
					m_stpportstate = StpPortStatus.getStpPortStatusString(stpinterf.getStpPortState().getIntCode());
				if (stpinterf.getStpPortPathCost() != null )
					m_stpportpathcost = stpinterf.getStpPortPathCost().toString();
				if (stpinterf.getStpPortDesignatedCost() != null)
					m_stpportdesignatedcost = stpinterf.getStpPortDesignatedCost().toString();
				if (stpinterf.getStpPortDesignatedBridge() != null)
					m_stpdesignatedbridge = stpinterf.getStpPortDesignatedBridge();
                if (stpinterf.getStpPortDesignatedRoot() != null)
                	m_stpdesignatedroot = stpinterf.getStpPortDesignatedRoot();
                if (stpinterf.getStpPortDesignatedPort() != null)
                	m_stpdesignatedport = stpinterf.getStpPortDesignatedPort();
        }

        public void setStpRootNodeid(Integer stprootnodeid) {
        m_stprootnodeid = stprootnodeid;
        }
        
        public void setStpBridgeNodeid(Integer stpbridgenodeid) {
            m_stpbridgenodeid = stpbridgenodeid;
        }

        public void setIpAddress(String ipaddr) {
        	m_ipaddr = ipaddr;
        }

        /**
         * <p>toString</p>
         *
         * @return a {@link java.lang.String} object.
         */
        @Override
        public String toString()
        {
                StringBuffer str = new StringBuffer("Node Id = " + m_nodeId + "\n" );
                str.append("Bridge number of ports = " + m_bridgeport + "\n" );
                str.append("At Last Poll Time = " + m_lastPollTime + "\n" );
                str.append("Node At Status= " + m_status + "\n" );
                return str.toString();
        }

		/**
		 * <p>get_bridgeport</p>
		 *
		 * @return a int.
		 */
		public int get_bridgeport() {
			return m_bridgeport;
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
		 * <p>get_stpdesignatedbridge</p>
		 *
		 * @return a {@link java.lang.String} object.
		 */
		public String get_stpdesignatedbridge() {
			return m_stpdesignatedbridge;
		}

		/**
		 * <p>get_stpdesignatedport</p>
		 *
		 * @return a {@link java.lang.String} object.
		 */
		public String get_stpdesignatedport() {
			return m_stpdesignatedport;
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
		 * <p>get_stpportdesignatedcost</p>
		 *
		 * @return a int.
		 */
		public String get_stpportdesignatedcost() {
			return m_stpportdesignatedcost;
		}

		/**
		 * <p>get_stpportpathcost</p>
		 *
		 * @return a int.
		 */
		public String get_stpportpathcost() {
			return m_stpportpathcost;
		}

		/**
		 * <p>get_stpportstate</p>
		 *
		 * @return a int.
		 */
		public String get_stpportstate() {
			return m_stpportstate;
		}

		/**
		 * <p>getStpPortState</p>
		 *
		 * @return a {@link java.lang.String} object.
		 */
		public String getStpPortState() {
			return m_stpportstate;
		}
			
		/**
		 * <p>get_stpvlan</p>
		 *
		 * @return a int.
		 */
		public int get_stpvlan() {
			return m_stpvlan;
		}

        /**
         * <p>get_stpbridgenodeid</p>
         *
         * @return Returns the m_stpdesignatedbridgenodeid.
         */
        public int get_stpbridgenodeid() {
            return m_stpbridgenodeid;
        }
        /**
         * <p>get_stprootnodeid</p>
         *
         * @return Returns the m_stpdesignatedrootnodeid.
         */
        public int get_stprootnodeid() {
            return m_stprootnodeid;
        }
		/**
		 * <p>get_ipaddr</p>
		 *
		 * @return Returns the m_ipaddr.
		 */
		public String get_ipaddr() {
			return m_ipaddr;
		}
		
	    /**
	     * <p>getStatusString</p>
	     *
	     * @return a {@link java.lang.String} object.
	     */
	    public String getStatusString() {
	        return m_status;
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
