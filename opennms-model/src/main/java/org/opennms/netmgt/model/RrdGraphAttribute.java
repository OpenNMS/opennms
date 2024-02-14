/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.model;

import java.io.File;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RrdGraphAttribute that = (RrdGraphAttribute) o;
        return Objects.equals(this.m_name, that.m_name) &&
                Objects.equals(this.m_relativePath, that.m_relativePath) &&
                Objects.equals(this.m_rrdFile, that.m_rrdFile) &&
                Objects.equals(this.m_resource, that.m_resource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_name, m_relativePath, m_rrdFile, m_resource);
    }
}
