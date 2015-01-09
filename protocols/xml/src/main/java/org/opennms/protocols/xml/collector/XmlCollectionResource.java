/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
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

package org.opennms.protocols.xml.collector;

import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.TimeKeeper;
import org.opennms.netmgt.collection.support.AbstractCollectionResource;
import org.opennms.netmgt.collection.support.DefaultTimeKeeper;

/**
 * The abstract Class XmlCollectionResource.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public abstract class XmlCollectionResource extends AbstractCollectionResource {

    /** The Time Keeper. */
    private TimeKeeper m_timeKeeper = new DefaultTimeKeeper();

    /**
     * Instantiates a new XML collection resource.
     *
     * @param agent the agent
     */
    public XmlCollectionResource(CollectionAgent agent) {
        super(agent);
    }

    /**
     * Sets the attribute value.
     *
     * @param type the type
     * @param value the value
     */
    public void setAttributeValue(XmlCollectionAttributeType type, String value) {
        XmlCollectionAttribute attr = new XmlCollectionAttribute(this, type, value);
        addAttribute(attr);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.config.collector.CollectionResource#getResourceTypeName()
     */
    @Override
    public abstract String getResourceTypeName();

    /* (non-Javadoc)
     * @see org.opennms.netmgt.config.collector.CollectionResource#getInstance()
     */
    @Override
    public abstract String getInstance();

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.AbstractCollectionResource#getTimeKeeper()
     */
    @Override
    public TimeKeeper getTimeKeeper() {
        return m_timeKeeper;
    }

    /**
     * Sets the time keeper.
     *
     * @param timeKeeper the new time keeper
     */
    public void setTimeKeeper(TimeKeeper timeKeeper) {
        m_timeKeeper = timeKeeper;
    }
}
