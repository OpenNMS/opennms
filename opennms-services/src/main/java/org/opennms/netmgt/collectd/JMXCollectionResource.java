/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd;

import javax.management.ObjectName;
import org.opennms.netmgt.config.collector.ServiceParameters;

/**
 * The Class JMXCollectionResource.
 */
public abstract class JMXCollectionResource extends AbstractCollectionResource {

    /**
     * Instantiates a new JMX collection resource.
     *
     * @param agent the agent
     */
    public JMXCollectionResource(CollectionAgent agent) {
        super(agent);
    }

    /**
     * Sets the attribute value.
     *
     * @param type the type
     * @param value the value
     */
    public void setAttributeValue(JMXCollectionAttributeType type, String value) {
        JMXCollectionAttribute attr = new JMXCollectionAttribute(this, type, type.getName(), value);
        addAttribute(attr);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.config.collector.CollectionResource#getResourceTypeName()
     */
    public abstract String getResourceTypeName();

    /* (non-Javadoc)
     * @see org.opennms.netmgt.config.collector.CollectionResource#getInstance()
     */
    public abstract String getInstance();

    public abstract ObjectName getObjectName();
}
