/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd.wmi;

import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionAttributeType;
import org.opennms.netmgt.collection.support.AbstractCollectionResource;

/**
 * <p>Abstract WmiCollectionResource class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public abstract class WmiCollectionResource extends AbstractCollectionResource {

    /**
     * <p>Constructor for WmiCollectionResource.</p>
     *
     * @param agent a {@link org.opennms.netmgt.collection.api.CollectionAgent} object.
     */
    public WmiCollectionResource(CollectionAgent agent) {
        super(agent);
    }

    /**
     * <p>setAttributeValue</p>
     *
     * @param type a {@link org.opennms.netmgt.collection.api.CollectionAttributeType} object.
     * @param value a {@link java.lang.String} object.
     */
    public void setAttributeValue(final CollectionAttributeType type, final String value) {
        final WmiCollectionAttribute attr = new WmiCollectionAttribute(this, type, value);
        addAttribute(attr);
    }

    /**
     * <p>getResourceTypeName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public abstract String getResourceTypeName();


    /**
     * <p>getInstance</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public abstract String getInstance();

}
