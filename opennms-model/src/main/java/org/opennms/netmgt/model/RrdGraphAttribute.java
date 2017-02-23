/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.model;

import java.io.File;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * <p>RrdGraphAttribute class.</p>
 */
@XmlRootElement(name = "rrd-graph-attribute")
@XmlAccessorType(XmlAccessType.NONE)
public class RrdGraphAttribute implements OnmsAttribute {

    @XmlAttribute(name = "name")
    private String m_name;

    @XmlAttribute(name = "relativePath")
    private String m_relativePath;

    @XmlAttribute(name = "rrdFile")
    private String m_rrdFile;

    private OnmsResource m_resource;

    public RrdGraphAttribute() {
    }

    /**
     * <p>Constructor for RrdGraphAttribute.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param relativePath a {@link java.lang.String} object.
     * @param rrdFile a {@link java.lang.String} object.
     */
    public RrdGraphAttribute(String name, String relativePath, String rrdFile) {
        m_name = name;
        m_relativePath = relativePath;
        m_rrdFile = rrdFile;
    }

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getName() {
        return m_name;
    }

    public void setName(final String name) {
        this.m_name = name;
    }

    /**
     * Retrieve the resource for this attribute.
     *
     * @return a {@link org.opennms.netmgt.model.OnmsResource} object.
     */
    @Override
    public OnmsResource getResource() {
        return m_resource;
    }

    /**
     * {@inheritDoc}
     *
     * Set the resource for this attribute.  This is called
     * when the attribute is added to a resource.
     */
    @Override
    public void setResource(OnmsResource resource) {
        m_resource = resource;
    }

    /**
     * <p>getRrdRelativePath</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getRrdRelativePath() {
        return m_relativePath + File.separator + m_rrdFile;
    }

    public void setRrdRelativePath(final String relativePath) {
        this.m_relativePath = relativePath;
    }

    /**
     * <p>getRrdFile</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getRrdFile() {
        return m_rrdFile;
    }

    public void setRrdFile(final String rrdFile) {
        this.m_rrdFile = rrdFile;
    }

    /** {@inheritDoc} */
    @Override
	public String toString() {
    	return ""+m_resource + '.' + m_name;
	}

}
