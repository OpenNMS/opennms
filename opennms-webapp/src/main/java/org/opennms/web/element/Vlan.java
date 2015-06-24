/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
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
import org.opennms.netmgt.model.OnmsVlan;
import org.opennms.netmgt.model.OnmsVlan.VlanStatus;
import org.opennms.netmgt.model.OnmsVlan.VlanType;
import org.opennms.web.api.Util;

/**
 * <p>Vlan class.</p>
 *
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class Vlan
{
    private final int m_nodeId;
    private final int m_vlanId;
    private final String m_vlanname;
    private final String m_vlantype;
    private final String m_vlanstatus;
    private final String m_lastPollTime;
    private final String m_status;

    /* package-protected so only the NetworkElementFactory can instantiate */
    Vlan(OnmsVlan vlan)
    {
        m_nodeId = vlan.getNode().getId();
        m_vlanId = vlan.getVlanId();
        m_vlanname = vlan.getVlanName();
        m_vlantype = VlanType.getVlanTypeString(vlan.getVlanType() == null ? VlanType.UNKNOWN.getIntCode() : vlan.getVlanType().getIntCode());
        m_vlanstatus = VlanStatus.getVlanStatusString(vlan.getVlanStatus() == null ? VlanStatus.UNKNOWN.getIntCode() : vlan.getVlanStatus().getIntCode());
        m_lastPollTime = Util.formatDateToUIString(vlan.getLastPollTime()); 
        m_status = StatusType.getStatusString(vlan.getStatus() == null ? StatusType.UNKNOWN.getCharCode() : vlan.getStatus().getCharCode());
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
     * <p>getStatus</p>
     *
     * @return a char.
     */
    public String getStatus() {
        return m_status;
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
    public String getVlanStatus() {
        return m_vlanstatus;
    }

    /**
     * <p>getVlanStatusString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getVlanStatusString() {
    	return m_vlanstatus;
    }

    /**
     * <p>getVlanType</p>
     *
     * @return a int.
     */
    public String getVlanType() {
        return m_vlantype;
    }

    /**
     * <p>getVlanTypeString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getVlanTypeString() {
        return m_vlantype;
    }

}
