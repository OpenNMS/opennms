/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
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

package org.opennms.protocols.xml.collector;

import org.opennms.core.utils.DefaultTimeKeeper;
import org.opennms.core.utils.TimeKeeper;
import org.opennms.netmgt.collectd.AbstractCollectionResource;
import org.opennms.netmgt.collectd.CollectionAgent;
import org.opennms.netmgt.config.collector.ServiceParameters;

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

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.AbstractCollectionResource#shouldPersist(org.opennms.netmgt.config.collector.ServiceParameters)
     */
    @Override
    public boolean shouldPersist(ServiceParameters params) {
        return true;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.AbstractCollectionResource#rescanNeeded()
     */
    @Override
    public boolean rescanNeeded() {
        // A rescan is never needed for the XmlCollector, at least on resources
        return false;
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
     * @see org.opennms.netmgt.collectd.AbstractCollectionResource#getType()
     */
    @Override
    public int getType() {
        return -1; // Is this right?
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
     * @see org.opennms.netmgt.config.collector.CollectionResource#getParent()
     */
    @Override
    public String getParent() {
        return m_agent.getStorageDir().toString();
    }

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
