/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: July 22, 2007
 *
 * Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
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
package org.opennms.netmgt.collectd;

import java.util.HashMap;
import java.util.Map;

import org.opennms.netmgt.model.OnmsSnmpInterface;

/**
 * <p>SnmpIfData class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public class SnmpIfData {

    private int m_nodeId;
    private boolean m_collectionEnabled;
    private int m_ifIndex;
    private int m_ifType;
    private String m_rrdLabel;
    private String m_ifAlias;
    
    private Map<String,String> m_attributes;

    /**
     * <p>Constructor for SnmpIfData.</p>
     *
     * @param snmpIface a {@link org.opennms.netmgt.model.OnmsSnmpInterface} object.
     */
    public SnmpIfData(OnmsSnmpInterface snmpIface) {
        m_nodeId = nullSafeUnbox(snmpIface.getNode().getId(), -1);
        m_collectionEnabled = snmpIface.isCollectionEnabled();
        m_ifIndex = nullSafeUnbox(snmpIface.getIfIndex(), -1);
        m_ifType = nullSafeUnbox(snmpIface.getIfType(), -1);
        m_rrdLabel = snmpIface.computeLabelForRRD();
        m_ifAlias = snmpIface.getIfAlias();
        m_attributes = new HashMap<String,String>();
        m_attributes.put("snmpphysaddr", snmpIface.getPhysAddr());
        m_attributes.put("snmpifindex", Integer.toString(m_ifIndex));
        m_attributes.put("snmpifdescr", snmpIface.getIfDescr());
        m_attributes.put("snmpiftype", Integer.toString(m_ifType));
        m_attributes.put("snmpifname", snmpIface.getIfName());
        m_attributes.put("snmpifadminstatus", Integer.toString(nullSafeUnbox(snmpIface.getIfAdminStatus(), -1)));
        m_attributes.put("snmpifoperstatus", Integer.toString(nullSafeUnbox(snmpIface.getIfOperStatus(), -1)));
        m_attributes.put("snmpifspeed", Long.toString(nullSafeUnbox(snmpIface.getIfSpeed(), -1)));
        m_attributes.put("snmpifalias", m_ifAlias);
    }
    
    int nullSafeUnbox(Integer num, int dflt) {
        return (num == null ? dflt : num.intValue());
    }

    long nullSafeUnbox(Long num, int dflt) {
        return (num == null ? dflt : num.intValue());
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
     * <p>isCollectionEnabled</p>
     *
     * @return a boolean.
     */
    public boolean isCollectionEnabled() {
        return m_collectionEnabled;
    }

    /**
     * <p>getIfIndex</p>
     *
     * @return a int.
     */
    public int getIfIndex() {
        return m_ifIndex;
    }

    /**
     * <p>getIfType</p>
     *
     * @return a int.
     */
    public int getIfType() {
        return m_ifType;
    }

    /**
     * <p>getLabelForRRD</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getLabelForRRD() {
        return m_rrdLabel;
    }

    /**
     * <p>getIfAlias</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getIfAlias() {
        return m_ifAlias;
    }
    
    /**
     * <p>getAttribtuesMap</p>
     *
     * @return a {@link java.util.Map} object.
     */
    public Map<String,String> getAttributesMap() {
        return m_attributes;
    }

}
