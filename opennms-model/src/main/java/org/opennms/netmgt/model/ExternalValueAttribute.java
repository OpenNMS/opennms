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

/**
 * <p>ExternalValueAttribute class.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 */
public class ExternalValueAttribute implements OnmsAttribute {
    private String m_name;
    private String m_value;
    private OnmsResource m_resource;

    /**
     * <p>Constructor for ExternalValueAttribute.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param value a {@link java.lang.String} object.
     */
    public ExternalValueAttribute(String name, String value) {
        m_name = name;
        m_value = value;
    }

    /**
     * Get the name for this attribute.  This is the name for
     * this type of external value.
     *
     * @see org.opennms.netmgt.model.OnmsAttribute#getName()
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getName() {
        return m_name;
    }
    
    /**
     * <p>getValue</p>
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

}
