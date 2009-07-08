/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
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

package org.opennms.netmgt.provision;

public class ResourceRecord {

    private String m_name;
    private String m_rClass;
    private Integer m_rdLength;
    private String m_rdata;
    
    private String m_ttl;
    
    public String getName() {
        return m_name;
    }

    public void setName(String name) {
        m_name = name;
    }

    public String getRClass() {
        return m_rClass;
    }

    public void setClass(String class1) {
        m_rClass = class1;
    }

    public Integer getRdLength() {
        return m_rdLength;
    }

    public void setRdLength(Integer rdLength) {
        m_rdLength = rdLength;
    }

    public String getRdata() {
        return m_rdata;
    }

    public void setRdata(String rdata) {
        m_rdata = rdata;
    }

    public String getTtl() {
        return m_ttl;
    }

    public void setTtl(String ttl) {
        m_ttl = ttl;
    }

}
