/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd;

import java.util.List;

import org.opennms.netmgt.collectd.SnmpCollectionResource;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.config.datacollection.MibObjProperty;

/**
 * The Interface SnmpPropertyExtender.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public interface SnmpPropertyExtender {

    /**
     * Gets the target attribute.
     *
     * @param sourceAttributes the source attributes
     * @param targetResource the target resource
     * @param property the MibObj property with the settings of the string attribute to add from the collection set.
     * @return the target attribute
     */
    SnmpAttribute getTargetAttribute(List<CollectionAttribute> sourceAttributes, SnmpCollectionResource targetResource, MibObjProperty property);
}
