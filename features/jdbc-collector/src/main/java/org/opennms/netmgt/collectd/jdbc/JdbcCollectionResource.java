/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd.jdbc;

import org.opennms.core.utils.TimeKeeper;
import org.opennms.netmgt.collectd.AbstractCollectionResource;
import org.opennms.netmgt.collectd.CollectionAgent;
import org.opennms.netmgt.config.collector.CollectionAttributeType;
import org.opennms.netmgt.config.collector.ServiceParameters;

public abstract class JdbcCollectionResource extends AbstractCollectionResource {
    
    public JdbcCollectionResource(CollectionAgent agent) {
        super(agent);
    }

    @Override
    public boolean shouldPersist(ServiceParameters params) {
        return true;
    }

    @Override
    public boolean rescanNeeded() {
        // A rescan is never needed for the JdbcCollector, at least on resources
        return false;
    }
    
    public void setAttributeValue(CollectionAttributeType type, String value) {
        JdbcCollectionAttribute attr = new JdbcCollectionAttribute(this, type, type.getName(), value);
        addAttribute(attr);
    }

    @Override
    public int getType() {
        return -1; //Is this right?
    }

    @Override
    public abstract String getResourceTypeName();

    @Override
    public abstract String getInstance();
    
    @Override
    public String getParent() {
        return m_agent.getStorageDir().toString();
    }

    @Override
    public TimeKeeper getTimeKeeper() {
        return null;
    }

}
