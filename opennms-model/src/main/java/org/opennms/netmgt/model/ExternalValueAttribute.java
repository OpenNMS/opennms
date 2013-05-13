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
