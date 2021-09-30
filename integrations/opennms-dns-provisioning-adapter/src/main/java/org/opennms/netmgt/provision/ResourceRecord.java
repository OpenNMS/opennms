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

package org.opennms.netmgt.provision;

/**
 * <p>ResourceRecord class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class ResourceRecord {

    private String m_name;
    private String m_rClass;
    private Integer m_rdLength;
    private String m_rdata;
    
    private String m_ttl;
    
    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getName() {
        return m_name;
    }

    /**
     * <p>setName</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    public void setName(String name) {
        m_name = name;
    }

    /**
     * <p>getRClass</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getRClass() {
        return m_rClass;
    }

    /**
     * <p>setClass</p>
     *
     * @param class1 a {@link java.lang.String} object.
     */
    public void setClass(String class1) {
        m_rClass = class1;
    }

    /**
     * <p>getRdLength</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getRdLength() {
        return m_rdLength;
    }

    /**
     * <p>setRdLength</p>
     *
     * @param rdLength a {@link java.lang.Integer} object.
     */
    public void setRdLength(Integer rdLength) {
        m_rdLength = rdLength;
    }

    /**
     * <p>getRdata</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getRdata() {
        return m_rdata;
    }

    /**
     * <p>setRdata</p>
     *
     * @param rdata a {@link java.lang.String} object.
     */
    public void setRdata(String rdata) {
        m_rdata = rdata;
    }

    /**
     * <p>getTtl</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getTtl() {
        return m_ttl;
    }

    /**
     * <p>setTtl</p>
     *
     * @param ttl a {@link java.lang.String} object.
     */
    public void setTtl(String ttl) {
        m_ttl = ttl;
    }

}
