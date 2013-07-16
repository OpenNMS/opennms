/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller;

import java.io.File;

import org.opennms.core.utils.TimeKeeper;
import org.opennms.netmgt.config.collector.CollectionResource;
import org.opennms.netmgt.config.collector.CollectionSetVisitor;
import org.opennms.netmgt.config.collector.ServiceParameters;
import org.opennms.netmgt.model.RrdRepository;

/**
 * <p>LatencyCollectionResource class.</p>
 *
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 * @version $Id: $
 */
public class LatencyCollectionResource implements CollectionResource {
    
    private String m_serviceName;
    private String m_ipAddress;

    /**
     * <p>Constructor for LatencyCollectionResource.</p>
     *
     * @param serviceName a {@link java.lang.String} object.
     * @param ipAddress a {@link java.lang.String} object.
     */
    public LatencyCollectionResource(String serviceName, String ipAddress) {
        super();
        m_serviceName = serviceName;
        m_ipAddress = ipAddress;
    }

    /**
     * <p>getInstance</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getInstance() {
        return m_ipAddress + "[" + m_serviceName + "]";
    }
    
    /**
     * <p>getServiceName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getServiceName() {
        return m_serviceName;
    }

    /**
     * <p>getIpAddress</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getIpAddress() {
        return m_ipAddress;
    }

    /**
     * <p>getLabel</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getLabel() {
        return m_serviceName;
    }

    /**
     * <p>getResourceTypeName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getResourceTypeName() {
        return "if";
    }

    /**
     * <p>getType</p>
     *
     * @return a int.
     */
    @Override
    public int getType() {
        return 0;
    }

    /**
     * <p>rescanNeeded</p>
     *
     * @return a boolean.
     */
    @Override
    public boolean rescanNeeded() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean shouldPersist(ServiceParameters params) {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public void visit(CollectionSetVisitor visitor) {
    }

    /**
     * <p>getOwnerName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getOwnerName() {
        return m_ipAddress;
    }

    /** {@inheritDoc} */
    @Override
    public File getResourceDir(RrdRepository repository) {
        return new File(repository.getRrdBaseDir(), m_ipAddress);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return m_serviceName + "@" + m_ipAddress;
    }

    @Override
    public String getParent() {
        return m_ipAddress;
    }

    @Override
    public TimeKeeper getTimeKeeper() {
        return null;
    }

}
