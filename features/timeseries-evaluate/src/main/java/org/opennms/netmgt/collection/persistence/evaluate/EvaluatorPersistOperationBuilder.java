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

package org.opennms.netmgt.collection.persistence.evaluate;

import org.opennms.netmgt.collection.api.CollectionAttributeType;
import org.opennms.netmgt.collection.api.PersistException;
import org.opennms.netmgt.collection.api.PersistOperationBuilder;

/**
 * The Class EvaluatorPersistOperationBuilder.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class EvaluatorPersistOperationBuilder implements PersistOperationBuilder {

    /**
     * Gets the name.
     *
     * @return the name
     */
    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    /**
     * Sets the attribute value.
     *
     * @param attributeType the attribute type
     * @param value the value
     */
    @Override
    public void setAttributeValue(CollectionAttributeType attributeType, Number value) {}

    /**
     * Sets the attribute metadata.
     *
     * @param metricIdentifier the metric identifier
     * @param name the name
     */
    @Override
    public void setAttributeMetadata(String metricIdentifier, String name) {}

    /**
     * Commit.
     *
     * @throws PersistException the persist exception
     */
    @Override
    public void commit() throws PersistException {}

}
