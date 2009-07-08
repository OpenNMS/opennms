/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2008-2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.collectd.wmi;

import org.opennms.netmgt.collectd.CollectionAgent;
import org.opennms.netmgt.collectd.ServiceParameters;
import org.opennms.netmgt.collectd.CollectionAttributeType;
import org.opennms.netmgt.collectd.AbstractCollectionResource;

public abstract class WmiCollectionResource extends AbstractCollectionResource {
    public WmiCollectionResource(CollectionAgent agent) {
        super(agent);
    }

    public int getType() {
        return -1; //Is this right?
    }

    //A rescan is never needed for the WmiCollector, at least on resources
    public boolean rescanNeeded() {
        return false;
    }

    public boolean shouldPersist(ServiceParameters params) {
        return true;
    }

    public void setAttributeValue(CollectionAttributeType type, String value) {
        WmiCollectionAttribute attr = new WmiCollectionAttribute(this, type, type.getName(), value);
        addAttribute(attr);
    }

    public abstract String getResourceTypeName();


    public abstract String getInstance();   
}
