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


import java.util.Objects;

/**
 * <p>StringPropertyAttribute class.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 */
public class StringPropertyAttribute implements OnmsAttribute {
    private String m_name;
    private String m_value;
    private OnmsResource m_resource;

    /**
     * <p>Constructor for StringPropertyAttribute.</p>
     *
     * @param name the name
     * @param value the value
     */
    public StringPropertyAttribute(String name, String value) {
        m_name = name;
        m_value = value;
    }

    /**
     * Get the name for this attribute.  This is the property name
     * from the properties file.
     *
     * @see org.opennms.netmgt.model.OnmsAttribute#getName()
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getName() {
        return m_name;
    }
    
    /**
     * Get the value for this attribute.  This is the property value
     * from the properties file.
     *
     * @return a {@link java.lang.String} object.
     */
    public String getValue() {
        return m_value;
    }

    /**
     * <p>getResource</p>
     *
     * @see org.opennms.netmgt.model.OnmsAttribute#getResource()
     * @return a {@link org.opennms.netmgt.model.OnmsResource} object.
     */
    @Override
    public OnmsResource getResource() {
        return m_resource;
    }

    /** {@inheritDoc} */
    @Override
    public void setResource(OnmsResource resource) {
        m_resource = resource;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StringPropertyAttribute that = (StringPropertyAttribute) o;
        return Objects.equals(this.m_name, that.m_name) &&
                Objects.equals(this.m_value, that.m_value) &&
                Objects.equals(this.m_resource, that.m_resource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_name, m_value, m_resource);
    }
}
