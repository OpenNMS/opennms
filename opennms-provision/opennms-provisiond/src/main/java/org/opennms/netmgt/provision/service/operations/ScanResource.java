/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.provision.service.operations;

import java.util.HashMap;
import java.util.Map;

import org.opennms.netmgt.model.OnmsNode;

public class ScanResource {
    private String m_type;
    private OnmsNode m_node = null;
    private final Map<String,String> m_attributes = new HashMap<String,String>();

    public ScanResource(String type) {
        m_type = type;
    }
    
    public String getType() {
        return m_type;
    }

    public void setNode(OnmsNode node) {
        m_node = node;
    }

    public OnmsNode getNode() {
        return m_node;
    }

    // TODO: change node comparison to use spring
    public void setAttribute(String key, String value) {
        m_attributes.put(key, value);
        if (m_node != null) {
            if (key.equals("sysContact")) {
                m_node.setSysContact(value);
            } else if (key.equals("sysDescription")) {
                m_node.setSysDescription(value);
            } else if (key.equals("sysLocation")) {
                m_node.setSysLocation(value);
            } else if (key.equals("sysObjectId")) {
                m_node.setSysObjectId(value);
            }
        }
    }

    public String getAttribute(String key) {
        return m_attributes.get(key);
    }
}
