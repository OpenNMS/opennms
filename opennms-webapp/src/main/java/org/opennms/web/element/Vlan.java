/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
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
 * <p>Vlan class.</p>
 *
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class Vlan extends RowStatus
{
    private final int m_nodeId;
    private final int m_vlanId;
    private final String m_vlanname;
    private final int m_vlantype;
    private final int m_vlanstatus;
    private final String m_lastPollTime;

    /**
     * <p>String identifiers for the enumeration of values:</p>
     * <ul>
     * <li>{@link DbVlanEntry#VLAN_TYPE_UNKNOWN}</li>
     * <li>{@link DbVlanEntry#VLAN_TYPE_ETHERNET}</li>
     * <li>{@link DbVlanEntry#VLAN_TYPE_FDDI}</li>
     * <li>{@link DbVlanEntry#VLAN_TYPE_TOKEN_RING}</li>
     * <li>{@link DbVlanEntry#VLAN_TYPE_FDDINET}</li>
     * <li>{@link DbVlanEntry#VLAN_TYPE_TRNET}</li>
     * <li>{@link DbVlanEntry#VLAN_TYPE_DEPRECATED}</li>
     * </ul>
     */
    
    public static final int VLAN_TYPE_UNKNOWN = 0;
    public static final int VLAN_TYPE_ETHERNET = 1;
    public static final int VLAN_TYPE_FDDI = 2;
    public static final int VLAN_TYPE_TOKEN_RING = 3;
    public static final int VLAN_TYPE_FDDINET = 4;
    public static final int VLAN_TYPE_TRNET = 5;
    public static final int VLAN_TYPE_DEPRECATED = 6;

    public static final int VLAN_STATUS_UNKNOWN = 0;
    public static final int VLAN_STATUS_OPERATIONAL = 1;
    public static final int VLAN_STATUS_SUSPENDED = 2;
    public static final int VLAN_STATUS_MTU_TOO_BIG_FOR_DEVICE = 3;
    public static final int VLAN_STATUS_MTU_TOO_BIG_FOR_TRUNK = 4;
    
    private static final String[] VLAN_TYPE = new String[] {
        "Unknown", //0
        "Ethernet", //1 
        "FDDI", //2
        "TokenRing", //3
        "FDDINet", //5
        "TRNet", //5
        "Deprecated" //6
    };

    /**
     * <p>String identifiers for the enumeration of values:</p>
     * <ul>
     * <li>{@link DbVlanEntry#VLAN_STATUS_UNKNOWN}</li>
     * <li>{@link DbVlanEntry#VLAN_STATUS_OPERATIONAL}</li>
     * <li>{@link DbVlanEntry#VLAN_STATUS_SUSPENDED}</li>
     * <li>{@link DbVlanEntry#VLAN_STATUS_MTU_TOO_BIG_FOR_DEVICE}</li>
     * <li>{@link DbVlanEntry#VLAN_STATUS_MTU_TOO_BIG_FOR_TRUNK}</li>
     * </ul>
     */ 
    private static final String[] VLAN_STATUS = new String[] {
        "unknown", //0
        "operational", //1
        "suspended", //2 
        "mtuTooBigForDevice", //3
        "mtuTooBigForTrunk" //4
    };


    /* package-protected so only the NetworkElementFactory can instantiate */
    Vlan(Integer nodeId, Integer vlanid, String vlanname, Integer vlantype, Integer vlanstatus, String lastpolltime, char status)
    {
    	super(status);
        m_nodeId = nodeId;
        m_vlanId = vlanid;
        m_vlanname = vlanname;
        m_vlantype = vlantype;
        m_vlanstatus = vlanstatus;
        m_lastPollTime = lastpolltime; 
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString()
    {
        StringBuffer str = new StringBuffer("Node Id = " + m_nodeId + "\n" );
        str.append("Vlan id = " + m_vlanId + "\n" );
        str.append("At Last Poll Time = " + m_lastPollTime + "\n" );
        str.append("Node At Status= " + m_status + "\n" );
        return str.toString();
    }


    /**
     * <p>getLastPollTime</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getLastPollTime() {
        return m_lastPollTime;
    }

    /**
     * <p>getNodeId</p>
     *
     * @return a int.
     */
    public int getNodeId() {
        return m_nodeId;
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

    /**
     * <p>getVlanId</p>
     *
     * @return a int.
     */
    public int getVlanId() {
        return m_vlanId;
    }

    /**
     * <p>getVlanName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getVlanName() {
        return m_vlanname;
    }

    /**
     * <p>getVlanStatus</p>
     *
     * @return a int.
     */
    public int getVlanStatus() {
        return m_vlanstatus;
    }

    /**
     * <p>getVlanStatusString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getVlanStatusString() {
        try {
            return VLAN_STATUS[m_vlanstatus];
        } catch (ArrayIndexOutOfBoundsException e) {
            return "Unknown";
        }
    }

    /**
     * <p>getVlanType</p>
     *
     * @return a int.
     */
    public int getVlanType() {
        return m_vlantype;
    }

    /**
     * <p>getVlanTypeString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getVlanTypeString() {
        try {
            return VLAN_TYPE[m_vlantype];
        } catch (ArrayIndexOutOfBoundsException e) {
            return "Unknown";
        }
    }

}
